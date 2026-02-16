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
import 'features/shop/presentation/cart_provider.dart';
import 'features/categories/presentation/categories_provider.dart';

Future<void> main() async {
  await dotenv.load(fileName: ".env");
  ApiClient.init();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => ProductsProvider()),
        ChangeNotifierProvider(create: (_) => CartProvider()),
        ChangeNotifierProvider(create: (_) => CategoriesProvider()),
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

