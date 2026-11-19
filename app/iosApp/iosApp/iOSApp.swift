import SwiftUI
import UIKit
import UserNotifications
import ComposeApp

/// Delegate de UIKit para APNs push tokens y retorno de deep links (`rosafiesta://`).
final class RosaFiestaAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let tokenHex = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        MainViewControllerKt.handleApnsDeviceToken(tokenHex: tokenHex)
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        MainViewControllerKt.handleApnsRegistrationFailure()
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        return MainViewControllerKt.handleIncomingDeepLinkUrl(urlString: url.absoluteString)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if let targetUrl = response.notification.request.content.userInfo["url"] as? String {
            _ = MainViewControllerKt.handleIncomingDeepLinkUrl(urlString: targetUrl)
        }
        completionHandler()
    }
}

/// Punto de entrada de iOS para RosaFiesta (Compose Multiplatform).
@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(RosaFiestaAppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                .onOpenURL { url in
                    _ = MainViewControllerKt.handleIncomingDeepLinkUrl(urlString: url.absoluteString)
                }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
