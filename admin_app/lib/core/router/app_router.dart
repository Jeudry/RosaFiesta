import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../features/auth/presentation/providers/auth_provider.dart';
import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/events/presentation/screens/events_screen.dart';
import '../../features/events/presentation/screens/event_detail_screen.dart';
import '../../features/events/presentation/screens/create_event_screen.dart';
import '../../features/quotes/presentation/screens/quotes_screen.dart';
import '../../features/quotes/presentation/screens/create_quote_screen.dart';
import '../../features/clients/presentation/screens/clients_screen.dart';
import '../../features/clients/presentation/screens/client_detail_screen.dart';
import '../../features/products/presentation/screens/products_screen.dart';
import '../../features/products/presentation/screens/product_edit_screen.dart';
import '../../features/products/presentation/screens/categories_screen.dart';
import '../../features/products/presentation/screens/bundles_screen.dart';
import '../../features/ai_config/presentation/screens/ai_config_screen.dart';
import '../../features/ai_config/presentation/screens/ai_history_screen.dart';
import '../../features/notifications/presentation/screens/notifications_screen.dart';
import '../../features/notifications/presentation/screens/email_templates_screen.dart';
import '../../features/notifications/presentation/screens/whatsapp_templates_screen.dart';
import '../../features/analytics/presentation/screens/analytics_screen.dart';
import '../../features/config/presentation/screens/config_screen.dart';
import '../../features/activity_log/presentation/screens/activity_log_screen.dart';
import '../../core/design_system.dart';

class AppRouter {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case '/':
        return MaterialPageRoute(builder: (_) => const LoginScreen());
      case '/login':
        return MaterialPageRoute(builder: (_) => const LoginScreen());
      case '/dashboard':
        return MaterialPageRoute(builder: (_) => const DashboardScreen());
      case '/events':
        return MaterialPageRoute(builder: (_) => const EventsScreen());
      case '/events/create':
        return MaterialPageRoute(builder: (_) => const CreateEventScreen());
      case '/events/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(builder: (_) => EventDetailScreen(eventId: id));
      case '/quotes':
        return MaterialPageRoute(builder: (_) => const QuotesScreen());
      case '/quotes/create':
        return MaterialPageRoute(builder: (_) => const CreateQuoteScreen());
      case '/clients':
        return MaterialPageRoute(builder: (_) => const ClientsScreen());
      case '/clients/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(builder: (_) => ClientDetailScreen(clientId: id));
      case '/products':
        return MaterialPageRoute(builder: (_) => const ProductsScreen());
      case '/products/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(builder: (_) => ProductEditScreen(productId: id));
      case '/products/new':
        return MaterialPageRoute(builder: (_) => const ProductEditScreen());
      case '/products/categories':
        return MaterialPageRoute(builder: (_) => const CategoriesScreen());
      case '/products/bundles':
        return MaterialPageRoute(builder: (_) => const BundlesScreen());
      case '/ai-config':
        return MaterialPageRoute(builder: (_) => const AIConfigScreen());
      case '/ai-config/history':
        return MaterialPageRoute(builder: (_) => const AIHistoryScreen());
      case '/notifications':
        return MaterialPageRoute(builder: (_) => const NotificationsScreen());
      case '/notifications/email':
        return MaterialPageRoute(builder: (_) => const EmailTemplatesScreen());
      case '/notifications/whatsapp':
        return MaterialPageRoute(builder: (_) => const WhatsAppTemplatesScreen());
      case '/analytics':
        return MaterialPageRoute(builder: (_) => const AnalyticsScreen());
      case '/config':
        return MaterialPageRoute(builder: (_) => const ConfigScreen());
      case '/activity-log':
        return MaterialPageRoute(builder: (_) => const ActivityLogScreen());
      default:
        return MaterialPageRoute(builder: (_) => const Scaffold(body: Center(child: Text('Not Found'))));
    }
  }
}

class AdminDrawer extends StatelessWidget {
  const AdminDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = isDark ? AppColors.surfaceDark : AppColors.white;

    return Drawer(
      backgroundColor: bgColor,
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.fromLTRB(20, 60, 20, 20),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [AppColors.primary, AppColors.primary.withValues(alpha: 0.8)],
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(Icons.admin_panel_settings, color: Colors.white, size: 48),
                const SizedBox(height: 12),
                Text('RosaFiesta', style: GoogleFonts.outfit(fontSize: 22, fontWeight: FontWeight.w700, color: Colors.white)),
                Text('Admin Panel', style: GoogleFonts.dmSans(fontSize: 13, color: Colors.white70)),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              children: [
                _DrawerItem(icon: Icons.dashboard_outlined, label: 'Dashboard', route: '/dashboard'),
                _DrawerItem(icon: Icons.event_outlined, label: 'Eventos', route: '/events'),
                _DrawerItem(icon: Icons.request_quote_outlined, label: 'Cotizaciones', route: '/quotes'),
                _DrawerItem(icon: Icons.people_outline, label: 'Clientes', route: '/clients'),
                const Divider(),
                _DrawerItem(icon: Icons.inventory_2_outlined, label: 'Productos', route: '/products'),
                _DrawerItem(icon: Icons.category_outlined, label: 'Categorías', route: '/products/categories'),
                _DrawerItem(icon: Icons.card_giftcard_outlined, label: 'Bundles', route: '/products/bundles'),
                const Divider(),
                _DrawerItem(icon: Icons.smart_toy_outlined, label: 'IA Rosa', route: '/ai-config'),
                _DrawerItem(icon: Icons.smart_toy_outlined, label: 'Historial IA', route: '/ai-config/history'),
                const Divider(),
                _DrawerItem(icon: Icons.notifications_outlined, label: 'Notificaciones', route: '/notifications'),
                _DrawerItem(icon: Icons.analytics_outlined, label: 'Analytics', route: '/analytics'),
                _DrawerItem(icon: Icons.settings_outlined, label: 'Configuración', route: '/config'),
                _DrawerItem(icon: Icons.history_outlined, label: 'Log de Actividad', route: '/activity-log'),
                const SizedBox(height: 16),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: OutlinedButton.icon(
                    onPressed: () async {
                      await context.read<AuthProvider>().logout();
                      if (context.mounted) Navigator.of(context).pushReplacementNamed('/login');
                    },
                    icon: const Icon(Icons.logout, size: 18),
                    label: const Text('Cerrar Sesión'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.error,
                      side: const BorderSide(color: AppColors.error),
                    ),
                  ),
                ),
                const SizedBox(height: 16),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _DrawerItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String route;

  const _DrawerItem({required this.icon, required this.label, required this.route});

  @override
  Widget build(BuildContext context) {
    final currentRoute = ModalRoute.of(context)?.settings.name;
    final isSelected = currentRoute == route;

    return ListTile(
      leading: Icon(icon, size: 22),
      title: Text(label, style: GoogleFonts.dmSans(fontSize: 14)),
      selected: isSelected,
      selectedTileColor: AppColors.primary.withValues(alpha: 0.1),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      onTap: () {
        Navigator.pop(context);
        if (currentRoute != route) {
          Navigator.pushReplacementNamed(context, route);
        }
      },
    );
  }
}

class AdminScaffold extends StatelessWidget {
  final String title;
  final Widget body;
  final List<Widget>? actions;
  final bool showBack;
  final Widget? floatingActionButton;

  const AdminScaffold({
    super.key,
    required this.title,
    required this.body,
    this.actions,
    this.showBack = false,
    this.floatingActionButton,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
        leading: showBack
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => Navigator.pop(context),
              )
            : Builder(
                builder: (ctx) => IconButton(
                  icon: const Icon(Icons.menu),
                  onPressed: () => Scaffold.of(ctx).openDrawer(),
                ),
              ),
        actions: actions,
      ),
      drawer: const AdminDrawer(),
      body: body,
      floatingActionButton: floatingActionButton,
    );
  }
}
