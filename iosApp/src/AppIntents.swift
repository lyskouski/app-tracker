import AppIntents
import KotlinModules

/*
 * Siri Shortcuts: today's habit states without opening the app.
 * The actions run in the app process (IosAutomation.kt); a shown app reloads its screen.
 */

struct HabitEntity: AppEntity {
    static let typeDisplayRepresentation: TypeDisplayRepresentation = "Habit"
    static let defaultQuery = HabitEntityQuery()

    let id: Int64
    let title: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(title)")
    }
}

/// Active habits; Siri finds them by the spoken title.
struct HabitEntityQuery: EntityStringQuery {
    @MainActor
    func entities(for identifiers: [Int64]) async throws -> [HabitEntity] {
        try await habits().filter { identifiers.contains($0.id) }
    }

    @MainActor
    func entities(matching string: String) async throws -> [HabitEntity] {
        try await habits().filter { $0.title.localizedCaseInsensitiveContains(string) }
    }

    @MainActor
    func suggestedEntities() async throws -> [HabitEntity] {
        try await habits()
    }

    @MainActor
    private func habits() async throws -> [HabitEntity] {
        try await IosAutomation.shared.habits().map { HabitEntity(id: $0.id, title: $0.title) }
    }
}

struct CompleteHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Complete Habit"
    static let description = IntentDescription(
        "Marks today's state of the habit as done, or stores the reached value of its numeric target."
    )

    @Parameter(title: "Habit")
    var habit: HabitEntity

    @Parameter(title: "Value")
    var value: Double?

    static var parameterSummary: some ParameterSummary {
        Summary("Complete \(\.$habit) with \(\.$value)")
    }

    @MainActor
    func perform() async throws -> some IntentResult & ProvidesDialog {
        let message = if let value {
            try await IosAutomation.shared.completeWithValue(habitId: habit.id, value: value)
        } else {
            try await IosAutomation.shared.complete(habitId: habit.id)
        }
        return .result(dialog: "\(message)")
    }
}

struct AddHabitValueIntent: AppIntent {
    static let title: LocalizedStringResource = "Add to Habit"
    static let description = IntentDescription(
        "Adds the value to today's reached value of the habit's numeric target, e.g. a glass of water."
    )

    @Parameter(title: "Habit")
    var habit: HabitEntity

    @Parameter(title: "Value", default: 1)
    var value: Double

    static var parameterSummary: some ParameterSummary {
        Summary("Add \(\.$value) to \(\.$habit)")
    }

    @MainActor
    func perform() async throws -> some IntentResult & ProvidesDialog {
        let message = try await IosAutomation.shared.add(habitId: habit.id, value: value)
        return .result(dialog: "\(message)")
    }
}

struct SkipHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Skip Habit"
    static let description = IntentDescription("Marks today's state of the habit as skipped.")

    @Parameter(title: "Habit")
    var habit: HabitEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Skip \(\.$habit)")
    }

    @MainActor
    func perform() async throws -> some IntentResult & ProvidesDialog {
        let message = try await IosAutomation.shared.skip(habitId: habit.id)
        return .result(dialog: "\(message)")
    }
}

struct LeftTodayIntent: AppIntent {
    static let title: LocalizedStringResource = "Habits Left Today"
    static let description = IntentDescription("Tells the habits that are neither done nor skipped today.")

    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> & ProvidesDialog {
        let message = try await IosAutomation.shared.leftToday()
        return .result(value: message, dialog: "\(message)")
    }
}

struct ZwykaShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: CompleteHabitIntent(),
            phrases: [
                "Complete \(\.$habit) in \(.applicationName)",
                "Mark \(\.$habit) as done in \(.applicationName)",
                "Complete a habit in \(.applicationName)",
            ],
            shortTitle: "Complete Habit",
            systemImageName: "checkmark.circle"
        )
        AppShortcut(
            intent: AddHabitValueIntent(),
            phrases: [
                "Add to \(\.$habit) in \(.applicationName)",
                "Add to a habit in \(.applicationName)",
            ],
            shortTitle: "Add to Habit",
            systemImageName: "plus.circle"
        )
        AppShortcut(
            intent: SkipHabitIntent(),
            phrases: [
                "Skip \(\.$habit) in \(.applicationName)",
                "Skip a habit in \(.applicationName)",
            ],
            shortTitle: "Skip Habit",
            systemImageName: "forward.circle"
        )
        AppShortcut(
            intent: LeftTodayIntent(),
            phrases: [
                "What is left today in \(.applicationName)",
                "Which habits are left in \(.applicationName)",
            ],
            shortTitle: "Habits Left Today",
            systemImageName: "list.bullet.circle"
        )
    }
}
