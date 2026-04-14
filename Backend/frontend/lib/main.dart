import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'core/app_theme.dart';
import 'core/theme_provider.dart';
import 'features/auth/presentation/auth_provider.dart';
import 'features/home/presentation/screens/welcome_onboarding_screen.dart';
import 'features/shell/main_shell.dart';
import 'features/auth/presentation/screens/confirmation_screen.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'core/api_client.dart';
import 'features/products/presentation/products_provider.dart';
import 'features/products/presentation/reviews_provider.dart';
import 'features/categories/presentation/categories_provider.dart';
import 'features/profile/presentation/profile_provider.dart';
import 'features/events/presentation/events_provider.dart';
import 'features/events/presentation/chat_provider.dart';
import 'features/events/presentation/debrief_provider.dart';
import 'features/guests/data/guests_repository.dart';
import 'features/guests/presentation/guests_provider.dart';
import 'features/tasks/data/tasks_repository.dart';
import 'features/tasks/presentation/tasks_provider.dart';
import 'features/events/presentation/timeline_provider.dart';
import 'features/stats/presentation/stats_provider.dart';
import 'package:frontend/features/events/data/timeline_repository.dart';
import 'core/services/notification_service.dart';
import 'core/services/voice_search_service.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/services/firebase_service.dart';
import 'core/services/hive_service.dart';
import 'core/services/sync_service.dart';
import 'features/active_event/presentation/active_event_provider.dart';
import 'features/ai_assistant/presentation/assistant_provider.dart';
import 'features/favorites/presentation/favorites_provider.dart';
import 'features/events/presentation/screens/event_detail_screen.dart';
import 'features/products/presentation/screens/product_detail_screen.dart';

import 'package:flutter/foundation.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await dotenv.load(fileName: ".env");
  } catch (e) {
    print("Warning: Failed to load .env file: $e");
  }

  try {
    // Only initialize Firebase if not on web, or if we have options (not implemented here)
    if (!kIsWeb) {
      await Firebase.initializeApp();
      await FirebaseService().initialize();
      FirebaseService().setupInteractions();
      // Wire up notification tap to store pending navigation
      FirebaseService.onNotificationTap = (screen, eventId) {
        FirebaseService.navigateFromNotification(screen, eventId);
      };
    } else {
      print("Warning: Firebase initialization skipped on Web");
    }
  } catch (e) {
    print("Warning: Firebase initialization failed: $e");
  }

  ApiClient.init();

  try {
    await HiveService.init();
    SyncService().init();
  } catch (e) {
    print("Warning: Hive initialization failed: $e");
  }

  try {
    await NotificationService().init();
  } catch (e) {
    print("Warning: NotificationService initialization failed: $e");
  }

  try {
    VoiceSearchService().initialize();
  } catch (e) {
    print("Warning: VoiceSearch initialization failed: $e");
  }

  final guestsRepository = GuestsRepository();
  final tasksRepository = EventTasksRepository();
  final timelineRepository = TimelineRepository();

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => ProductsProvider()),
        ChangeNotifierProvider(create: (_) => ActiveEventProvider()),
        ChangeNotifierProvider(create: (_) => FavoritesProvider()),
        ChangeNotifierProvider(create: (_) => CategoriesProvider()),
        ChangeNotifierProvider(create: (_) => ProfileProvider()),
        ChangeNotifierProvider(create: (_) => EventsProvider()),
        ChangeNotifierProvider(create: (_) => GuestsProvider(guestsRepository)),
        ChangeNotifierProvider(
          create: (_) => EventTasksProvider(tasksRepository),
        ),
        ChangeNotifierProvider(
          create: (_) => TimelineProvider(timelineRepository),
        ),
        ChangeNotifierProvider(create: (_) => StatsProvider()),
        ChangeNotifierProvider(create: (_) => ReviewsProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
        ChangeNotifierProvider(create: (_) => DebriefProvider()),
        ChangeNotifierProvider(create: (_) => AssistantProvider()),
      ],
      child: const RosaFiestaApp(),
    ),
  );
}

class RosaFiestaApp extends StatelessWidget {
  const RosaFiestaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Rosa Fiesta',
      theme: AppTheme.lightTheme,
      debugShowCheckedModeBanner: false,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [Locale('en'), Locale('es')],
      locale: const Locale('es', 'ES'),
      home: const _AuthGate(),
      onGenerateRoute: (settings) {
        final uri = Uri.parse(settings.name ?? '');
        final pathSegments = uri.pathSegments;

        // /confirm/:token — email confirmation
        if (pathSegments.length == 2 && pathSegments[0] == 'confirm') {
          return MaterialPageRoute(
            builder: (context) => ConfirmationScreen(token: pathSegments[1]),
          );
        }

        // /event/:id — event detail
        if (pathSegments.length == 2 && pathSegments[0] == 'event') {
          return MaterialPageRoute(
            builder: (context) => EventDetailScreen(eventId: pathSegments[1]),
          );
        }

        // /product/:id — product detail
        if (pathSegments.length == 2 && pathSegments[0] == 'product') {
          return MaterialPageRoute(
            builder: (context) => ProductDetailScreen(productId: pathSegments[1]),
          );
        }

        return null;
      },
    );
  }
}

/// Checks if user has a saved session — if so, goes straight to HomeScreen.
class _AuthGate extends StatefulWidget {
  const _AuthGate();

  @override
  State<_AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<_AuthGate> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AuthProvider>().tryRestoreSession();
    });
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();

    if (!auth.initialized) {
      // Still loading — show a simple splash
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (auth.isAuthenticated) {
      // Check if there's pending navigation from a notification tap
      _handlePendingNavigation(context);
      return const MainShell();
    }

    return const WelcomeOnboardingScreen();
  }

  void _handlePendingNavigation(BuildContext context) {
    final pending = FirebaseService.getPendingNavigation();
    if (pending['screen'] != null) {
      FirebaseService.performNavigationFromPending(context);
    }
  }
}
