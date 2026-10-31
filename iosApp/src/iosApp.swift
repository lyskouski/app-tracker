import SwiftUI
import KotlinModules

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        ViewControllerKt.ViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea()
    }
}

@main
struct iosApp: App {
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        // Habit titles spoken in the Siri phrases ("Complete Water in Zwyka"), after the habits are edited.
        .onChange(of: scenePhase) { _, phase in
            if phase != .active {
                ZwykaShortcuts.updateAppShortcutParameters()
            }
        }
    }
}
