import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'core/app_theme.dart';
import 'features/auth/presentation/auth_provider.dart';
import 'features/home/presentation/screens/welcome_onboarding_screen.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'core/api_client.dart';
import 'features/products/presentation/products_provider.dart';
import 'features/products/presentation/reviews_provider.dart';
import 'features/shop/presentation/cart_provider.dart';
import 'features/categories/presentation/categories_provider.dart';
import 'features/profile/presentation/profile_provider.dart';
import 'features/events/presentation/events_provider.dart';
import 'features/guests/data/guests_repository.dart';
import 'features/guests/presentation/guests_provider.dart';
import 'features/tasks/data/tasks_repository.dart';
import 'features/tasks/presentation/tasks_provider.dart';
import 'features/suppliers/presentation/suppliers_provider.dart';
import 'features/events/data/timeline_repository.dart';
import 'features/events/presentation/timeline_provider.dart';
import 'features/stats/presentation/stats_provider.dart';
import 'core/services/notification_service.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/services/firebase_service.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  await dotenv.load(fileName: ".env");
  ApiClient.init();
  await NotificationService().init();
  await FirebaseService().initialize();
  
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
      locale: const Locale('es'),
      home: const WelcomeOnboardingScreen(),
    );
  }
}

