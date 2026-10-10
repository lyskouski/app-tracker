This is a Kotlin Multiplatform project targeting Android, Desktop (JVM), iOS, and Kotlin/Wasm, built with Gradle and Compose Multiplatform.

- [/androidApp](./androidApp) contains the Android application.
- [/desktopApp](./desktopApp) contains the desktop (JVM) application.
- [/iosApp](./iosApp) contains the iOS application.
- [/shared](./shared) holds shared Compose UI, business logic, SQLDelight queries, and platform-specific implementations. [src](./shared/src) is common code; sibling `src@<platform>` folders hold platform-specific code.
- [/webApp](./webApp) contains the web application, compiled to WebAssembly with Kotlin/Wasm.

The Gradle wrapper downloads the pinned Gradle version on first use. Build the project with `./gradlew build`. The `project.yaml` and `module.yaml` files are retained for the earlier Kotlin Toolchain setup; use Gradle for SQLDelight code generation.

### Running

Run individual applications from the command line:

- Android app: `./gradlew :androidApp:installDebug`
- Desktop app: `./gradlew :desktopApp:run`
- iOS framework (macOS): `./gradlew :iosApp:linkDebugFrameworkIosSimulatorArm64`
- Web app: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`

### Testing

Run the project's tests with `./gradlew test`, or target a module, for example `./gradlew :shared:allTests`.

SQLDelight generates the shared database API from
[`Database.sq`](../shared/src/commonMain/sqldelight/com/tercad/zwyka/infrastructures/Database.sq).
Android, desktop, and iOS use their platform drivers. The WASM app uses a SQL.js Web Worker and
persists its database in IndexedDB.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Kotlin Toolchain](https://kotlin-toolchain.org/dev/), [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/), [Kotlin/Wasm](https://kotl.in/wasm/).
