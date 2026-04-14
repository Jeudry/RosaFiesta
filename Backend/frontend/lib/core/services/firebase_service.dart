import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:frontend/features/events/presentation/screens/event_detail_screen.dart';
import 'package:frontend/features/products/presentation/screens/product_detail_screen.dart';
import 'package:frontend/features/active_event/presentation/screens/mi_evento_screen.dart';

class FirebaseService {
  static final FirebaseService _instance = FirebaseService._internal();
  factory FirebaseService() => _instance;
  FirebaseService._internal();

  final FlutterLocalNotificationsPlugin _notifications =
      FlutterLocalNotificationsPlugin();

  FirebaseMessaging get _fcm => FirebaseMessaging.instance;

  // Callback for when user taps a notification
  static void Function(String? screen, String? eventId)? onNotificationTap;

  /// Stores pending navigation from notification tap until app is ready.
  static final Map<String, String?> _pendingNavigation = {};

  Future<void> initialize() async {
    try {
      // Initialize local notifications
      await _initializeNotifications();

      // Request permissions for iOS
      NotificationSettings settings = await _fcm.requestPermission(
        alert: true,
        announcement: false,
        badge: true,
        carPlay: false,
        criticalAlert: false,
        provisional: false,
        sound: true,
      );

      if (kDebugMode) {
        print('User granted permission: ${settings.authorizationStatus}');
      }

      // Configure background messaging
      FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

      // Initial token fetch
      String? token = await getToken();
      if (kDebugMode) {
        print("FCM Token: $token");
      }
    } catch (e) {
      if (kDebugMode) {
        print("Error initializing Firebase Messaging: $e");
      }
    }
  }

  Future<void> _initializeNotifications() async {
    // Android settings
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');

    // iOS settings
    const iosSettings = DarwinInitializationSettings(
      requestAlertPermission: false,
      requestBadgePermission: false,
      requestSoundPermission: false,
    );

    const initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _notifications.initialize(
      settings: initSettings,
      onDidReceiveNotificationResponse: _onNotificationResponse,
    );
  }

  void _onNotificationResponse(NotificationResponse response) {
    // Handle notification tap
    final payload = response.payload;
    if (payload != null) {
      final parts = payload.split('|');
      final screen = parts.isNotEmpty ? parts[0] : null;
      final eventId = parts.length > 1 ? parts[1] : null;
      onNotificationTap?.call(screen, eventId);
    }
  }

  Future<String?> getToken() async {
    return await _fcm.getToken();
  }

  static Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
    await Firebase.initializeApp();
    if (kDebugMode) {
      print("Handling a background message: ${message.messageId}");
    }
  }

  void setupInteractions() {
    // Handle foreground messages - show local notification
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      _showLocalNotification(message);
    });

    // Handle messages that opened the app
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      _handleNotificationTap(message);
    });
  }

  Future<void> _showLocalNotification(RemoteMessage message) async {
    final title = message.notification?.title ?? message.data['title'] ?? 'Rosa Fiesta';
    final body = message.notification?.body ?? message.data['body'] ?? '';
    final screen = message.data['screen'];
    final eventId = message.data['event_id'];

    // Build payload for tap handling
    String payload = screen ?? 'home';
    if (eventId != null) {
      payload += '|$eventId';
    }

    // Android notification details
    final androidDetails = AndroidNotificationDetails(
      'rosa_fiesta_channel',
      'Rosa Fiesta Notifications',
      channelDescription: 'Notificaciones de Rosa Fiesta',
      importance: Importance.high,
      priority: Priority.high,
      styleInformation: BigTextStyleInformation(body),
    );

    // iOS notification details
    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _notifications.show(
      id: DateTime.now().millisecondsSinceEpoch ~/ 1000,
      title: title,
      body: body,
      notificationDetails: details,
      payload: payload,
    );
  }

  void _handleNotificationTap(RemoteMessage message) {
    final screen = message.data['screen'];
    final eventId = message.data['event_id'];
    onNotificationTap?.call(screen, eventId);
  }

  // Method to display a local notification (for testing/manual triggers)
  Future<void> showNotification({
    required String title,
    required String body,
    String? screen,
    String? eventId,
  }) async {
    String payload = screen ?? 'home';
    if (eventId != null) {
      payload += '|$eventId';
    }

    final androidDetails = AndroidNotificationDetails(
      'rosa_fiesta_channel',
      'Rosa Fiesta Notifications',
      channelDescription: 'Notificaciones de Rosa Fiesta',
      importance: Importance.high,
      priority: Priority.high,
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _notifications.show(
      id: DateTime.now().millisecondsSinceEpoch ~/ 1000,
      title: title,
      body: body,
      notificationDetails: details,
      payload: payload,
    );
  }

  /// Navigate to the appropriate screen based on notification payload.
  static void navigateFromNotification(String? screen, String? eventId) {
    if (screen == null || screen == 'home') return;
    _pendingNavigation['screen'] = screen;
    _pendingNavigation['eventId'] = eventId;
  }

  /// Returns the pending navigation map if any.
  static Map<String, String?> getPendingNavigation() => Map.from(_pendingNavigation);

  /// Clears pending navigation and navigates to the target screen.
  static void performNavigationFromPending(BuildContext context) {
    final screen = _pendingNavigation.remove('screen');
    final eventId = _pendingNavigation.remove('eventId');
    if (screen == null) return;

    // Use a post-frame callback to ensure Navigator is ready
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _navigateToScreen(context, screen, eventId);
    });
  }

  static void _navigateToScreen(BuildContext context, String screen, String? eventId) {
    if (!context.mounted) return;

    Widget screenToShow;
    switch (screen) {
      case 'event_detail':
        screenToShow = EventDetailScreen(eventId: eventId ?? '');
        break;
      case 'product_detail':
        screenToShow = ProductDetailScreen(productId: eventId ?? '');
        break;
      case 'mi_evento':
        screenToShow = const MiEventoScreen();
        break;
      default:
        return; // For 'home' or unknown, just return to main shell
    }

    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => screenToShow),
    );
    debugPrint('Notification navigation: screen=$screen, eventId=$eventId');
  }
}
