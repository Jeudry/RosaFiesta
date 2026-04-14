import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';

import '../../../products/presentation/screens/products_list_screen.dart';
import '../../../home/presentation/screens/welcome_onboarding_screen.dart';
import '../../../favorites/presentation/screens/favorites_screen.dart';

/// Public shell shown to unauthenticated users.
///
/// Exposes the Catalog and Favorites tabs; "Iniciar sesión" prompts onboarding.
class AuthGateScreen extends StatefulWidget {
  const AuthGateScreen({super.key});

  @override
  State<AuthGateScreen> createState() => _AuthGateScreenState();
}

class _AuthGateScreenState extends State<AuthGateScreen>
    with TickerProviderStateMixin {
  late final AnimationController _floatController;
  late final AnimationController _decoController;
  late final AnimationController _pulseController;
  late final AnimationController _gradientController;

  int _index = 0;

  @override
  void initState() {
    super.initState();
    _floatController = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 5500))
      ..repeat();
    _decoController = AnimationController(
        vsync: this, duration: const Duration(seconds: 20))
      ..repeat();
    _pulseController = AnimationController(
        vsync: this, duration: const Duration(seconds: 3))
      ..repeat(reverse: true);
    _gradientController = AnimationController(
        vsync: this, duration: const Duration(seconds: 12))
      ..repeat();
  }

  @override
  void dispose() {
    _floatController.dispose();
    _decoController.dispose();
    _pulseController.dispose();
    _gradientController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          // Shared animated background
          RfGradientOrbs(
            controller: _gradientController,
            color1: AppColors.hotPink,
            color2: AppColors.violet,
            isDark: isDark,
          ),
          RfDecoLayer(
            floatController: _floatController,
            decoController: _decoController,
            pulseController: _pulseController,
            baseOpacity: isDark ? 1.0 : 1.8,
          ),
          Positioned.fill(
            child: IgnorePointer(
              child: CustomPaint(
                painter: RfGridPainter(
                    color: (isDark ? Colors.white : Colors.black)
                        .withValues(alpha: 0.006)),
              ),
            ),
          ),
          // Catalog and Favorites tabs via IndexedStack
          IndexedStack(
            index: _index,
            children: const [
              ProductsListScreen(),
              FavoritesScreen(),
            ],
          ),
          // Bottom bar overlay
          Positioned(
            left: 0, right: 0, bottom: 0,
            child: _buildBottomBar(t),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomBar(RfTheme t) {
    final bottomPad = MediaQuery.of(context).padding.bottom;

    return Padding(
      padding: EdgeInsets.fromLTRB(16, 0, 16, bottomPad + 20),
      child: Container(
        height: 70,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(35),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.08),
              blurRadius: 24,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 6),
          child: Row(
            children: [
              _navItem(
                Icons.storefront_outlined,
                'Catálogo',
                t,
                iconSize: 30,
                tabIndex: 0,
              ),
              _navItem(
                Icons.favorite_border_rounded,
                'Favoritos',
                t,
                iconSize: 28,
                tabIndex: 1,
              ),
              Expanded(
                flex: 1,
                child: GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (_) => const WelcomeOnboardingScreen()),
                    );
                  },
                  behavior: HitTestBehavior.opaque,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.login_rounded,
                          color: AppColors.hotPink, size: 28),
                      const SizedBox(height: 2),
                      Text(
                        'Iniciar sesión',
                        style: GoogleFonts.dmSans(
                          fontSize: 10,
                          fontWeight: FontWeight.w600,
                          color: AppColors.hotPink,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _navItem(IconData icon, String label, RfTheme t,
      {int? tabIndex, double iconSize = 28}) {
    final isActive = tabIndex != null && _index == tabIndex;
    return Expanded(
      flex: isActive ? 3 : 1,
      child: GestureDetector(
        onTap: () {
          if (tabIndex != null) {
            setState(() => _index = tabIndex);
          }
        },
        behavior: HitTestBehavior.opaque,
        child: isActive
            ? Container(
                margin: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppColors.violet, AppColors.hotPink],
                  ),
                  borderRadius: BorderRadius.circular(28),
                ),
                alignment: Alignment.center,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(icon, color: Colors.white, size: iconSize),
                    const SizedBox(width: 8),
                    Text(label,
                        style: GoogleFonts.dmSans(
                          fontSize: 17,
                          fontWeight: FontWeight.w700,
                          color: Colors.white,
                        )),
                  ],
                ),
              )
            : Icon(icon, color: t.textDim, size: iconSize),
      ),
    );
  }
}
