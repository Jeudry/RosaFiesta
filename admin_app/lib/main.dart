import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'core/design_system.dart';
import 'core/router/app_router.dart';
import 'features/auth/presentation/providers/auth_provider.dart';
import 'features/dashboard/presentation/providers/dashboard_provider.dart';
import 'features/events/presentation/providers/events_provider.dart';
import 'features/quotes/presentation/providers/quotes_provider.dart';
import 'features/clients/presentation/providers/clients_provider.dart';
import 'features/products/presentation/providers/products_provider.dart';

void main() {
  runApp(const RosaFiestaAdminApp());
}

class RosaFiestaAdminApp extends StatelessWidget {
  const RosaFiestaAdminApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => DashboardProvider()),
        ChangeNotifierProvider(create: (_) => EventsProvider()),
        ChangeNotifierProvider(create: (_) => QuotesProvider()),
        ChangeNotifierProvider(create: (_) => ClientsProvider()),
        ChangeNotifierProvider(create: (_) => ProductsProvider()),
      ],
      child: MaterialApp(
        title: 'RosaFiesta Admin',
        debugShowCheckedModeBanner: false,
        theme: AdminTheme.light,
        darkTheme: AdminTheme.dark,
        themeMode: ThemeMode.dark, // Admin app defaults to dark
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: const [
          Locale('es', ''),
          Locale('en', ''),
        ],
        initialRoute: '/',
        onGenerateRoute: AppRouter.generateRoute,
      ),
    );
  }
}
