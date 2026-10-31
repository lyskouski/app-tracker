// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.stress

import java.io.File
import java.sql.DriverManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.AfterClass
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass

/*
 * Stress test of the Android app on a device or an emulator, driven by adb from the host
 * (a debug build: the database is reached by run-as):
 *
 *     ./gradlew :androidApp:installDebug
 *     ./gradlew :shared:androidStressTest [-PSTRESS_HABITS=60 -PSTRESS_YEARS=3 ...]
 *
 * The app gets two seeded databases with the same habits: "light" (two weeks of day states) and "heavy"
 * (years of them, with schedules and goals). Timings are compared between both, measured in turns,
 * rather than with fixed limits: an emulator without a GPU renders by the CPU, and its speed depends on the host.
 *
 * The original database of the app is restored at the end, unless -PSTRESS_KEEP_DATA=true.
 * Skipped without a connected device.
 */
class AndroidStressTest {
    @Test
    fun coldStartDoesNotDependOnHistory() {
        log("Cold start")
        log("device load: ${waitForIdle()}")
        val times = mapOf(light to mutableListOf<Int>(), heavy to mutableListOf())
        repeat(intSetting("STRESS_STARTS", 5)) {
            for ((database, values) in times) {
                pushDatabase(database)
                coldStart()
                coldStart()?.let(values::add)
            }
        }
        val light = median(times.getValue(light))
        val heavy = median(times.getValue(heavy))
        log("light: median $light ms ${times.getValue(Companion.light).sorted()}")
        log("heavy: median $heavy ms ${times.getValue(Companion.heavy).sorted()}, x${ratio(heavy, light)}")
        assertTrue(heavy <= light * doubleSetting("STRESS_MAX_START_RATIO", 1.5), "Cold start grows with the history")
    }

    @Test
    fun memoryStaysBounded() {
        log("Memory")
        pushDatabase(heavy)
        coldStart()
        Thread.sleep(3_000)
        val output = shell("dumpsys meminfo $PACKAGE")
        val kilobytes = (Regex("""TOTAL PSS:\s+(\d+)""").find(output) ?: Regex("""TOTAL\s+(\d+)""").find(output))
            ?.groupValues?.get(1)?.toInt() ?: fail("PSS is not reported")
        log("PSS with the heavy data: ${kilobytes / 1024} MB")
        assertTrue(kilobytes / 1024 < intSetting("STRESS_MAX_PSS_MB", 400), "Memory of the app")
    }

    @Test
    fun scrollFramesDoNotDependOnHistory() {
        log("Scroll")
        log("device load: ${waitForIdle()}")
        val frames = mapOf(light to mutableListOf<Frames>(), heavy to mutableListOf())
        repeat(intSetting("STRESS_SCROLL_ROUNDS", 3)) {
            for ((database, values) in frames) {
                pushDatabase(database)
                scrollFrames(intSetting("STRESS_SWIPES", 40))?.let(values::add)
            }
        }
        val light = Frames.median(frames.getValue(light)) ?: fail("Frames are not reported")
        val heavy = Frames.median(frames.getValue(heavy)) ?: fail("Frames are not reported")
        log("light: $light")
        log("heavy: $heavy, 50th percentile x${ratio(heavy.p50, light.p50)}")
        assertTrue(
            heavy.p50 <= light.p50 * doubleSetting("STRESS_MAX_FRAME_RATIO", 1.5),
            "Frame time grows with the history",
        )
    }

    @Test
    fun concurrentIntentsAreAllCounted() {
        log("Concurrent intents")
        pushDatabase(heavy)
        coldStart()
        val intents = intSetting("STRESS_INTENTS", 300)
        val parallel = intSetting("STRESS_PARALLEL", 16)
        val before = counterValue() ?: fail("No habit '$COUNTER'")
        val started = System.nanoTime()
        // [parallel] senders at once: the receiver gets their broadcasts in parallel. Every `am` is a process
        // calling the system server, hundreds of them at once exhaust its binder threads and restart Android.
        shell(
            "seq 1 $intents | xargs -P $parallel -n 1 sh -c " +
                "\"am broadcast -n $RECEIVER -a $PACKAGE.action.ADD --es habit '$COUNTER' --ei value 1 >/dev/null\"",
            timeoutSeconds = 1_800,
        )
        val seconds = (System.nanoTime() - started) / 1e9
        Thread.sleep(2_000)
        val counted = ((counterValue() ?: before) - before).toInt()
        log("counted: $counted of $intents in ${"%.1f".format(seconds)} s")
        assertEquals(intents, counted, "Lost ADD intents")
        assertTrue(shell("pidof $PACKAGE", check = false).isNotBlank(), "The app has died")
    }

    @Test
    fun randomInputDoesNotCrash() {
        log("Monkey")
        pushDatabase(heavy)
        val events = intSetting("STRESS_MONKEY_EVENTS", 5_000)
        val seed = intSetting("STRESS_MONKEY_SEED", 52)
        shell("logcat -c -b crash", check = false)
        // Not responding apps (the system UI as well, on a slow emulator) do not stop the run, they are counted.
        // A native crash of the app is reported as its crash; the native crashes of system daemons are not ours.
        val output = shell(
            "monkey -p $PACKAGE -s $seed --throttle 30 --pct-syskeys 0 --pct-appswitch 2 --ignore-timeouts -v $events",
            check = false,
            timeoutSeconds = 3_600,
        )
        val logFile = File(output(), "monkey.log").apply { writeText(output + "\n\n" + shell("logcat -d -b crash", check = false)) }
        val injected = Regex("""Events injected: (\d+)""").find(output)?.groupValues?.get(1)?.toInt() ?: 0
        val crash = Regex("""// Long Msg: (.*)""").find(output)?.groupValues?.get(1)
            ?: "System appears to have crashed".takeIf { it in output }
        val anrs = Regex("""// NOT RESPONDING: (\S+)""").findAll(output).map { it.groupValues[1] }.toList()
        log("events: $injected of $events (seed $seed), log: $logFile")
        log("not responding: ${anrs.groupingBy { it }.eachCount().ifEmpty { "none" }}")
        assertEquals(null, crash, "Crash, see $logFile")
        // An emulator rendering by the CPU may need -PSTRESS_MAX_ANRS above 0.
        val appAnrs = anrs.count { it == PACKAGE }
        assertTrue(appAnrs <= intSetting("STRESS_MAX_ANRS", 0), "$appAnrs ANRs of the app, see $logFile")
        // An aborted run proves nothing about the events it has not sent.
        assertEquals(events, injected, "Monkey aborted, see $logFile")
    }

    /** Frame times of a scroll, ms. */
    private data class Frames(val p50: Int, val p90: Int, val count: Int) {
        override fun toString() = "frame 50th percentile $p50 ms, 90th $p90 ms, $count frames"

        companion object {
            fun median(values: List<Frames>): Frames? = values.takeIf { it.isNotEmpty() }?.let {
                Frames(median(it.map(Frames::p50)), median(it.map(Frames::p90)), median(it.map(Frames::count)))
            }
        }
    }

    companion object {
        private const val PACKAGE = "com.tercad.zwyka"
        private const val ACTIVITY = "$PACKAGE/.MainActivity"
        private const val RECEIVER = "$PACKAGE/.AutomationReceiver"
        private const val DATABASE = "databases/zwyka.db"
        private const val COUNTER = "Stress Counter"

        private lateinit var original: File
        private lateinit var light: File
        private lateinit var heavy: File

        @JvmStatic
        @BeforeClass
        fun seedDatabases() {
            assumeTrue("No Android device", adbOrNull("get-state")?.trim() == "device")
            assumeTrue("$PACKAGE is not installed", PACKAGE in shell("pm list packages $PACKAGE"))
            // The app creates its database on the first start.
            shell("am start -W -n $ACTIVITY")
            log("Output: ${output()}")
            original = File(output(), "original.db").also(::pullDatabase)
            val habits = intSetting("STRESS_HABITS", 60)
            light = File(output(), "light.db").also { original.copyTo(it, overwrite = true) }
            heavy = File(output(), "heavy.db").also { original.copyTo(it, overwrite = true) }
            seed(light, habits, days = 14)
            val states = seed(heavy, habits, days = 365 * intSetting("STRESS_YEARS", 3))
            log("heavy data: $habits habits, $states day states, ${habits * 3} scheduled times, ${habits * 5} goals")
        }

        @JvmStatic
        @AfterClass
        fun restoreDatabase() {
            if (::original.isInitialized && System.getProperty("STRESS_KEEP_DATA") != "true") pushDatabase(original)
        }

        /*
         * Habits (every 4th has a numeric target, long and right-to-left titles) with [days] of day states,
         * schedules and goals; and the counter of the intents. Returns the count of day states.
         */
        private fun seed(file: File, habits: Int, days: Int): Int {
            val random = Random(52)
            val today = LocalDate.now()
            val first = today.minusDays(days.toLong())
            val created = first.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val icons = listOf("FitnessCenter", "MenuBook", "LocalDrink", "SelfImprovement", "DirectionsWalk", null)
            val marks = listOf("·", "\\", "|", "/", "X", "▣", "—")
            var states = 0
            DriverManager.getConnection("jdbc:sqlite:${file.path}").use { connection ->
                connection.autoCommit = false
                connection.createStatement().use { statement ->
                    listOf("DayState", "Goal", "GoalGroup", "HabitTime", "Intent", "Habit")
                        .forEach { statement.executeUpdate("DELETE FROM $it") }
                }
                val habit = connection.prepareStatement(
                    "INSERT INTO Habit(title, icon, color, activeDays, isArchived, targetValue, targetType, createdAt, updatedAt)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS,
                )
                val dayState = connection.prepareStatement("INSERT INTO DayState(habitId, date, mark) VALUES (?, ?, ?)")
                val habitTime = connection.prepareStatement(
                    "INSERT INTO HabitTime(habitId, timeStart, timeEnd, note, activeDays, notify) VALUES (?, ?, ?, ?, 127, 0)",
                )
                val group = connection.prepareStatement(
                    "INSERT INTO GoalGroup(habitId, title) VALUES (?, 'Goals')",
                    java.sql.Statement.RETURN_GENERATED_KEYS,
                )
                val goal = connection.prepareStatement("INSERT INTO Goal(groupId, title, isFinished, createdAt) VALUES (?, ?, ?, ?)")

                fun insert(title: String, icon: String?, activeDays: Int, archived: Boolean, target: Double?, unit: String?): Long {
                    habit.setString(1, title)
                    habit.setString(2, icon)
                    habit.setString(3, "#%06X".format(random.nextInt(0x1000000)))
                    habit.setInt(4, activeDays)
                    habit.setInt(5, if (archived) 1 else 0)
                    if (target == null) habit.setNull(6, java.sql.Types.REAL) else habit.setDouble(6, target)
                    habit.setString(7, unit)
                    habit.setLong(8, created)
                    habit.setLong(9, created)
                    habit.executeUpdate()
                    return habit.generatedKeys.use { it.next(); it.getLong(1) }
                }

                for (index in 0 until habits) {
                    val numeric = index % 4 == 0
                    val title = listOf(
                        "Habit %03d".format(index),
                        "Привычка номер $index с очень длинным названием для проверки",
                        "習慣 $index",
                        "عادة $index",
                    )[index % 4]
                    val id = insert(
                        title, icons[index % icons.size], listOf(127, 127, 31, 85).random(random),
                        archived = index % 17 == 16, target = 8.0.takeIf { numeric }, unit = "glasses".takeIf { numeric },
                    )
                    var day = first
                    while (day < today) {
                        if (random.nextDouble() < 0.8) {
                            dayState.setLong(1, id)
                            dayState.setString(2, day.toString())
                            dayState.setString(3, if (numeric) random.nextInt(12).toString() else marks.random(random))
                            dayState.addBatch()
                            states++
                        }
                        day = day.plusDays(1)
                    }
                    repeat(3) { slot ->
                        habitTime.setLong(1, id)
                        habitTime.setInt(2, 360 + slot * 300 + index % 60)
                        habitTime.setInt(3, 420 + slot * 300 + index % 60)
                        habitTime.setString(4, "Note $index/$slot")
                        habitTime.executeUpdate()
                    }
                    group.setLong(1, id)
                    group.executeUpdate()
                    val groupId = group.generatedKeys.use { it.next(); it.getLong(1) }
                    repeat(5) {
                        goal.setLong(1, groupId)
                        goal.setString(2, "Goal $it")
                        goal.setInt(3, it % 2)
                        goal.setLong(4, created)
                        goal.executeUpdate()
                    }
                }
                dayState.executeBatch()
                // Counter of the intents: a numeric target without marks.
                insert(COUNTER, null, 127, archived = false, target = 100_000.0, unit = "times")
                connection.commit()
            }
            return states
        }

        private fun pullDatabase(file: File) {
            shell("am force-stop $PACKAGE")
            file.writeBytes(adbBytes("exec-out", "run-as", PACKAGE, "cat", DATABASE))
        }

        private fun pushDatabase(file: File) {
            shell("am force-stop $PACKAGE")
            // The journal of the replaced database would be applied to the new one.
            shell("run-as $PACKAGE rm -f $DATABASE-wal $DATABASE-shm $DATABASE-journal", check = false)
            adbBytes("exec-in", "run-as", PACKAGE, "sh", "-c", "cat > $DATABASE", input = file.readBytes())
        }

        /** Time to the first frame of a cold start, ms. */
        private fun coldStart(): Int? {
            shell("am force-stop $PACKAGE")
            val time = Regex("""TotalTime: (\d+)""").find(shell("am start -W -n $ACTIVITY"))?.groupValues?.get(1)?.toInt()
            Thread.sleep(2_000)
            return time
        }

        /** Frame times of [swipes] over the "Overall" table, in all four directions. */
        private fun scrollFrames(swipes: Int): Frames? {
            val (width, height) = Regex("""(\d+)x(\d+)""").find(shell("wm size"))!!.destructured.toList().map(String::toInt)
            coldStart()
            Thread.sleep(1_000)
            // The "Overall" tab is the last of the three on top.
            shell("input tap ${width * 5 / 6} ${height * 7 / 100}")
            Thread.sleep(2_000)
            shell("dumpsys gfxinfo $PACKAGE reset")
            repeat(swipes) { index ->
                val (x0, y0, x1, y1) = when (index % 4) {
                    0 -> listOf(width / 2, height * 3 / 4, width / 2, height / 4) // later days
                    1 -> listOf(width / 2, height / 4, width / 2, height * 3 / 4) // earlier days
                    2 -> listOf(width * 4 / 5, height / 2, width / 5, height / 2) // next habits
                    else -> listOf(width / 5, height / 2, width * 4 / 5, height / 2) // previous habits
                }
                shell("input swipe $x0 $y0 $x1 $y1 150")
            }
            val output = shell("dumpsys gfxinfo $PACKAGE")
            val values = listOf("""\n50th percentile: (\d+)ms""", """\n90th percentile: (\d+)ms""", """Total frames rendered: (\d+)""")
                .map { Regex(it).find(output)?.groupValues?.get(1)?.toInt() ?: return null }
            return Frames(values[0], values[1], values[2])
        }

        /** Today's value of the counter, by an ADD of 0 (the answer is "Stress Counter: N / 100000 times"). */
        private fun counterValue(): Double? =
            Regex("""data="$COUNTER: ([\d.]+)""")
                .find(shell("am broadcast -n $RECEIVER -a $PACKAGE.action.ADD --es habit '$COUNTER' --es value 0"))
                ?.groupValues?.get(1)?.toDouble()

        /** Waits until the device is idle: timings are meaningless while it finishes its boot or a dexopt. */
        private fun waitForIdle(limit: Double = 1.5, timeoutSeconds: Int = 600): Double {
            val deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L
            var load: Double
            do {
                load = shell("cat /proc/loadavg").trim().split(" ").first().toDouble()
                if (load < limit) break
                Thread.sleep(10_000)
            } while (System.nanoTime() < deadline)
            return load
        }

        /** Output of [command] on the device; its errors are included: monkey reports crashes and ANRs there. */
        private fun shell(command: String, check: Boolean = true, timeoutSeconds: Long = 600): String =
            String(adbBytes("shell", command, check = check, timeoutSeconds = timeoutSeconds, withErrors = true))

        private fun adbOrNull(vararg args: String): String? =
            runCatching { String(adbBytes(*args, timeoutSeconds = 30)) }.getOrNull()

        private fun adbBytes(
            vararg args: String,
            input: ByteArray? = null,
            check: Boolean = true,
            timeoutSeconds: Long = 600,
            withErrors: Boolean = false,
        ): ByteArray {
            // Without errors for the binary output, e.g. the pulled database.
            val process = ProcessBuilder(adb(), *args)
                .apply { if (withErrors) redirectErrorStream(true) else redirectError(ProcessBuilder.Redirect.DISCARD) }
                .start()
            process.outputStream.use { stream -> input?.let(stream::write) }
            val output = process.inputStream.readBytes()
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                error("adb ${args.joinToString(" ")} timed out")
            }
            check(!check || process.exitValue() == 0) { "adb ${args.joinToString(" ")} failed: ${String(output)}" }
            return output
        }

        /** adb of -PADB, of ANDROID_HOME or ANDROID_SDK_ROOT, or of the PATH. */
        private fun adb(): String =
            System.getProperty("ADB")
                ?: listOf("ANDROID_HOME", "ANDROID_SDK_ROOT")
                    .firstNotNullOfOrNull { System.getenv(it) }
                    ?.let { File(it, "platform-tools/adb") }?.takeIf(File::exists)?.path
                ?: "adb"

        private fun output(): File =
            File(System.getProperty("STRESS_OUTPUT") ?: "build/stress").apply { mkdirs() }

        private fun intSetting(name: String, default: Int) = System.getProperty(name)?.toInt() ?: default

        private fun doubleSetting(name: String, default: Double) = System.getProperty(name)?.toDouble() ?: default

        private fun median(values: List<Int>): Int = values.sorted().let { it.getOrNull(it.size / 2) } ?: fail("No values")

        private fun ratio(value: Int, base: Int) = "%.2f".format(value.toDouble() / base.coerceAtLeast(1))

        private fun log(message: String) = println("  $message")
    }
}
