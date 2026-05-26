import 'package:flutter/material.dart';
import 'dart:ui';
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
        return MaterialPageRoute(settings: settings, builder: (_) => const LoginScreen());
      case '/login':
        return MaterialPageRoute(settings: settings, builder: (_) => const LoginScreen());
      case '/dashboard':
        return MaterialPageRoute(settings: settings, builder: (_) => const DashboardScreen());
      case '/events':
        return MaterialPageRoute(settings: settings, builder: (_) => const EventsScreen());
      case '/events/create':
        return MaterialPageRoute(settings: settings, builder: (_) => const CreateEventScreen());
      case '/events/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(settings: settings, builder: (_) => EventDetailScreen(eventId: id));
      case '/quotes':
        return MaterialPageRoute(settings: settings, builder: (_) => const QuotesScreen());
      case '/quotes/create':
        return MaterialPageRoute(settings: settings, builder: (_) => const CreateQuoteScreen());
      case '/clients':
        return MaterialPageRoute(settings: settings, builder: (_) => const ClientsScreen());
      case '/clients/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(settings: settings, builder: (_) => ClientDetailScreen(clientId: id));
      case '/products':
        return MaterialPageRoute(settings: settings, builder: (_) => const ProductsScreen());
      case '/products/:id':
        final id = settings.arguments as String;
        return MaterialPageRoute(settings: settings, builder: (_) => ProductEditScreen(productId: id));
      case '/products/new':
        return MaterialPageRoute(settings: settings, builder: (_) => const ProductEditScreen());
      case '/products/categories':
        return MaterialPageRoute(settings: settings, builder: (_) => const CategoriesScreen());
      case '/products/bundles':
        return MaterialPageRoute(settings: settings, builder: (_) => const BundlesScreen());
      case '/ai-config':
        return MaterialPageRoute(settings: settings, builder: (_) => const AIConfigScreen());
      case '/ai-config/history':
        return MaterialPageRoute(settings: settings, builder: (_) => const AIHistoryScreen());
      case '/notifications':
        return MaterialPageRoute(settings: settings, builder: (_) => const NotificationsScreen());
      case '/notifications/email':
        return MaterialPageRoute(settings: settings, builder: (_) => const EmailTemplatesScreen());
      case '/notifications/whatsapp':
        return MaterialPageRoute(settings: settings, builder: (_) => const WhatsAppTemplatesScreen());
      case '/analytics':
        return MaterialPageRoute(settings: settings, builder: (_) => const AnalyticsScreen());
      case '/config':
        return MaterialPageRoute(settings: settings, builder: (_) => const ConfigScreen());
      case '/activity-log':
        return MaterialPageRoute(settings: settings, builder: (_) => const ActivityLogScreen());
      default:
        return MaterialPageRoute(settings: settings, builder: (_) => const Scaffold(body: Center(child: Text('Not Found'))));
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

class AdminScaffold extends StatefulWidget {
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
  State<AdminScaffold> createState() => _AdminScaffoldState();
}

class _AdminScaffoldState extends State<AdminScaffold> with TickerProviderStateMixin {
  late AnimationController _floatController;
  late AnimationController _decoController;
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _floatController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 25),
    )..repeat();
    _decoController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 45),
    )..repeat();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 6),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _floatController.dispose();
    _decoController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final currentRoute = ModalRoute.of(context)?.settings.name;
    
    // Purplish-magenta Candy Pop theme variations
    const candyMagenta = Color(0xFFD42A8F);
    const candyViolet = Color(0xFF7C3AED);

    return Scaffold(
      extendBodyBehindAppBar: true, // Let the background gradient flow up behind the app bar!
      extendBody: true, // Let the body flow beautifully behind the bottom navigation bar!
      appBar: AppBar(
        automaticallyImplyLeading: false, // We use custom leading logo and actions
        titleSpacing: 16,
        leading: widget.showBack
            ? IconButton(
                icon: Icon(
                  Icons.arrow_back_ios_new_rounded, 
                  size: 18, 
                  color: isDark ? Colors.white : const Color(0xFF2C1A4D)
                ),
                onPressed: () => Navigator.pop(context),
              )
            : Padding(
                padding: const EdgeInsets.only(left: 12),
                child: Center(
                  child: Container(
                    width: 32,
                    height: 32,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: AppColors.hotPink.withOpacity(0.4),
                        width: 1.5,
                      ),
                      image: const DecorationImage(
                        image: AssetImage('assets/images/logo_rosafiesta.png'),
                        fit: BoxFit.cover,
                      ),
                    ),
                  ),
                ),
              ),
        title: widget.showBack
            ? Text(
                widget.title,
                style: GoogleFonts.outfit(
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                  color: isDark ? Colors.white : const Color(0xFF2C1A4D),
                ),
              )
            : Row(
                children: [
                  ShaderMask(
                    shaderCallback: (bounds) => const LinearGradient(
                      colors: [AppColors.hotPink, AppColors.violet],
                    ).createShader(bounds),
                    child: Text(
                      'RosaFiesta',
                      style: GoogleFonts.outfit(
                        fontSize: 22,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                        letterSpacing: -0.2,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(
                        color: AppColors.primary.withOpacity(0.18),
                        width: 0.8,
                      ),
                    ),
                    child: Text(
                      'Portal',
                      style: GoogleFonts.outfit(
                        fontSize: 9,
                        fontWeight: FontWeight.w800,
                        color: AppColors.primary,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
                ],
              ),
        actions: [
          // Notifications
          IconButton(
            icon: const Icon(Icons.notifications_outlined, color: AppColors.primary, size: 22),
            onPressed: () => Navigator.pushNamed(context, '/notifications'),
          ),
          const SizedBox(width: 8),
        ],
        backgroundColor: Colors.transparent, // Translucent glass appbar!
        elevation: 0,
        flexibleSpace: ClipRect(
          child: BackdropFilter(
            filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
            child: Container(
              color: isDark ? Colors.black.withOpacity(0.25) : Colors.white.withOpacity(0.25),
            ),
          ),
        ),
      ),
      endDrawer: const AdminDrawer(), // Opens on the right!
      body: Stack(
        children: [
          // 1. Base background (Vibrant Pastel Gradient for Light Mode!)
          Container(
            decoration: BoxDecoration(
              gradient: isDark
                  ? const LinearGradient(
                      colors: [Color(0xFF07070F), Color(0xFF140D26)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    )
                  : const LinearGradient(
                      colors: [Color(0xFFFFF0F5), Color(0xFFF3E8FF)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
            ),
          ),

          // 2. Animated Candy Orbs (Purplish Accent Variation)
          RfGradientOrbs(
            controller: _floatController,
            color1: candyMagenta,
            color2: candyViolet,
            isDark: isDark,
          ),

          // High blur filter to blend gradient orbs
          Positioned.fill(
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 75, sigmaY: 75),
              child: Container(color: Colors.transparent),
            ),
          ),

          // 3. Sublte layout grid
          Positioned.fill(
            child: CustomPaint(
              painter: _RfGridPainter(
                color: isDark ? Colors.white.withOpacity(0.015) : Colors.black.withOpacity(0.015),
              ),
            ),
          ),

          // 4. Subtle background decorations layer
          RfDecoLayer(
            floatController: _floatController,
            decoController: _decoController,
            pulseController: _pulseController,
            baseOpacity: isDark ? 0.28 : 0.48,
          ),

          // 5. Main content
          Positioned.fill(
            child: SafeArea(
              child: widget.body,
            ),
          ),
        ],
      ),
      floatingActionButton: widget.floatingActionButton,
      bottomNavigationBar: Builder(
        builder: (ctx) => _buildBottomNav(ctx, isDark, currentRoute),
      ),
    );
  }

  Widget _buildBottomNav(BuildContext context, bool isDark, String? currentRoute) {
    final bottomPad = MediaQuery.of(context).padding.bottom;
    
    // Detect active tab index
    int activeIndex = 0;
    if (currentRoute == '/dashboard') {
      activeIndex = 0;
    } else if (currentRoute != null && currentRoute.startsWith('/events')) {
      activeIndex = 1;
    } else if (currentRoute != null && currentRoute.startsWith('/quotes')) {
      activeIndex = 2;
    } else if (currentRoute != null && currentRoute.startsWith('/clients')) {
      activeIndex = 3;
    } else {
      activeIndex = 0; // Default or fallback
    }

    final cardBg = isDark ? AppColors.cardDark : Colors.white;
    final textDim = isDark ? const Color(0xFF8B8BAA) : const Color(0xFF6B7280);

    return Padding(
      padding: EdgeInsets.fromLTRB(16, 0, 16, bottomPad + 20), // 100% mirrored margin!
      child: Container(
        height: 70, // 100% mirrored height!
        decoration: BoxDecoration(
          color: cardBg.withOpacity(0.92),
          borderRadius: BorderRadius.circular(35), // 100% mirrored radius!
          border: Border.all(
            color: isDark ? Colors.white.withOpacity(0.08) : AppColors.primary.withOpacity(0.12),
            width: 1.2,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(isDark ? 0.25 : 0.08),
              blurRadius: 20,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 6),
          child: Row(
            children: [
              _navItem(context, Icons.dashboard_rounded, 'Inicio', activeIndex == 0, textDim, () {
                if (currentRoute != '/dashboard') {
                  Navigator.pushReplacementNamed(context, '/dashboard');
                }
              }),
              _navItem(context, Icons.event_rounded, 'Eventos', activeIndex == 1, textDim, () {
                if (currentRoute != '/events') {
                  Navigator.pushReplacementNamed(context, '/events');
                }
              }),
              _navItem(context, Icons.request_quote_rounded, 'Cotizaciones', activeIndex == 2, textDim, () {
                if (currentRoute != '/quotes') {
                  Navigator.pushReplacementNamed(context, '/quotes');
                }
              }),
              _navItem(context, Icons.people_rounded, 'Clientes', activeIndex == 3, textDim, () {
                if (currentRoute != '/clients') {
                  Navigator.pushReplacementNamed(context, '/clients');
                }
              }),
              _navItem(context, Icons.more_horiz_rounded, 'Más', false, textDim, () {
                Scaffold.of(context).openEndDrawer();
              }),
            ],
          ),
        ),
      ),
    );
  }

  Widget _navItem(
    BuildContext context,
    IconData icon,
    String label,
    bool isActive,
    Color textDim,
    VoidCallback onTap,
  ) {
    return Expanded(
      flex: isActive ? 3 : 1,
      child: GestureDetector(
        onTap: onTap,
        behavior: HitTestBehavior.opaque,
        child: isActive
            ? Container(
                margin: const EdgeInsets.symmetric(vertical: 7, horizontal: 4),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppColors.violet, AppColors.hotPink], // 100% mirrored gradient colors!
                  ),
                  borderRadius: BorderRadius.circular(28),
                ),
                alignment: Alignment.center,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(icon, color: Colors.white, size: 20),
                    const SizedBox(width: 6),
                    Text(
                      label,
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
              )
            : Center(
                child: Icon(icon, color: textDim, size: 24),
              ),
      ),
    );
  }
}

// Subtle grid painter for the background
class _RfGridPainter extends CustomPainter {
  final Color color;
  const _RfGridPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 0.5;
    const step = 48.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(_RfGridPainter old) => old.color != color;
}
