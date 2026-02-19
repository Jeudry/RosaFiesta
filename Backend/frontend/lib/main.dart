import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'core/app_theme.dart';
import 'features/auth/presentation/auth_provider.dart';
import 'features/home/presentation/screens/welcome_onboarding_screen.dart';
import 'features/auth/presentation/screens/confirmation_screen.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'core/api_client.dart';
import 'features/products/presentation/products_provider.dart';
import 'features/products/presentation/reviews_provider.dart';
import 'features/shop/presentation/cart_provider.dart';
import 'features/categories/presentation/categories_provider.dart';
import 'features/profile/presentation/profile_provider.dart';
import 'features/events/presentation/events_provider.dart';
import 'features/events/presentation/chat_provider.dart';
import 'features/guests/data/guests_repository.dart';
import 'features/guests/presentation/guests_provider.dart';
import 'features/tasks/data/tasks_repository.dart';
import 'features/tasks/presentation/tasks_provider.dart';
import 'features/events/presentation/timeline_provider.dart';
import 'features/stats/presentation/stats_provider.dart';
import 'package:frontend/features/suppliers/presentation/suppliers_provider.dart';
import 'package:frontend/features/events/data/timeline_repository.dart';
import 'package:frontend/features/suppliers/data/suppliers_repository.dart';
import 'core/services/notification_service.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/services/firebase_service.dart';

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
    } else {
      print("Warning: Firebase initialization skipped on Web");
    }
  } catch (e) {
    print("Warning: Firebase initialization failed: $e");
  }

  ApiClient.init();
  
  try {
    await NotificationService().init();
  } catch (e) {
    print("Warning: NotificationService initialization failed: $e");
  }
  
  final guestsRepository = GuestsRepository();
  final tasksRepository = EventTasksRepository();
  final suppliersRepository = SuppliersRepository();
  final timelineRepository = TimelineRepository();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => ProductsProvider()),
        ChangeNotifierProvider(create: (_) => CartProvider()),
        ChangeNotifierProvider(create: (_) => CategoriesProvider()),
        ChangeNotifierProvider(create: (_) => ProfileProvider()),
        ChangeNotifierProvider(create: (_) => EventsProvider()),
        ChangeNotifierProvider(create: (_) => GuestsProvider(guestsRepository)),
        ChangeNotifierProvider(create: (_) => EventTasksProvider(tasksRepository)),
        ChangeNotifierProvider(create: (_) => SuppliersProvider(suppliersRepository)),
        ChangeNotifierProvider(create: (_) => TimelineProvider(timelineRepository)),
        ChangeNotifierProvider(create: (_) => StatsProvider()),
        ChangeNotifierProvider(create: (_) => ReviewsProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
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
      supportedLocales: const [
        Locale('en'),
        Locale('es'),
      ],
      locale: const Locale('es', 'ES'),
      home: const WelcomeOnboardingScreen(),
      onGenerateRoute: (settings) {
        if (settings.name != null && settings.name!.startsWith('/confirm/')) {
          final uri = Uri.parse(settings.name!);
          if (uri.pathSegments.length == 2 && uri.pathSegments[0] == 'confirm') {
            final token = uri.pathSegments[1];
            return MaterialPageRoute(
              builder: (context) => ConfirmationScreen(token: token),
            );
          }
        }
        return null;
      },
    );
  }
}

