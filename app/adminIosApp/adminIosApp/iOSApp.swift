import SwiftUI
import ComposeApp

/// Punto de entrada independiente de iOS para RosaFiesta Admin (App Administrativa).
@main
struct AdminiOSApp: App {
    var body: some Scene {
        WindowGroup {
            AdminComposeView().ignoresSafeArea()
        }
    }
}

struct AdminComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.AdminMainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
