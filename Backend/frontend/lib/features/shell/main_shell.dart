import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../events/presentation/screens/events_list_screen.dart';
import '../events/presentation/screens/event_calendar_screen.dart';
import '../favorites/presentation/screens/favorites_screen.dart';
import '../home/presentation/screens/home_screen.dart';
import '../products/presentation/screens/products_list_screen.dart';
import '../profile/presentation/screens/profile_screen.dart';
import '../ai_assistant/presentation/screens/assistant_screen.dart';

/// Persistent shell that wraps the 4 main tabs with a shared bottom bar,
/// AI assistant FAB, animated background and welcome tooltip.
///
/// Each tab renders its own content-only screen (no bottom bar, no FAB).
class MainShell extends StatefulWidget {
  final int initialIndex;
  const MainShell({super.key, this.initialIndex = 0});

  static _MainShellState? of(BuildContext context) =>
      context.findAncestorStateOfType<_MainShellState>();

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell>
    with TickerProviderStateMixin {
  int _index = 0;

  late final AnimationController _floatController;
  late final AnimationController _decoController;
  late final AnimationController _pulseController;
  late final AnimationController _gradientController;
  late final AnimationController _aiFabGlowController;
  late final AnimationController _aiTooltipController;
  bool _tooltipShown = false;
  Timer? _aiTooltipDismiss;

  @override
  void initState() {
    super.initState();
    _index = widget.initialIndex;

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
    _aiFabGlowController = AnimationController(
        vsync: this, duration: const Duration(seconds: 2))
      ..repeat(reverse: true);
    _aiTooltipController = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 400));

    WidgetsBinding.instance.addPostFrameCallback((_) {
      Future.delayed(const Duration(milliseconds: 1500), () {
        if (mounted && !_tooltipShown) {
          _tooltipShown = true;
          _aiTooltipController.forward();
          _aiTooltipDismiss = Timer(const Duration(seconds: 8), () {
            if (mounted) _aiTooltipController.reverse();
          });
        }
      });
    });
  }

  @override
  void dispose() {
    _floatController.dispose();
    _decoController.dispose();
    _pulseController.dispose();
    _gradientController.dispose();
    _aiFabGlowController.dispose();
    _aiTooltipController.dispose();
    _aiTooltipDismiss?.cancel();
    super.dispose();
  }

  /// Programmatically switch tab (used by links in child content like
  /// the home's "Ver todo" button).
  void goToTab(int index) {
    if (index == _index) return;
    setState(() => _index = index);
  }

  AnimationController get floatController => _floatController;
  AnimationController get decoController => _decoController;
  AnimationController get pulseController => _pulseController;
  AnimationController get gradientController => _gradientController;

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          // Shared animated background (lives across tabs)
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
          // Tab content — IndexedStack keeps state alive across switches
          IndexedStack(
            index: _index,
            children: const [
              HomeScreen(),
              ProductsListScreen(),
              EventsListScreen(),
              FavoritesScreen(),
            ],
          ),
          // AI Assistant FAB
          Positioned(
            right: 16,
            bottom: 120,
            child: _buildAssistantFab(t),
          ),
          // Bottom nav
          Positioned(
            left: 0, right: 0, bottom: 0,
            child: _buildBottomNav(t),
          ),
        ],
      ),
    );
  }

  // ── AI Assistant FAB ────────────────────────────────────────────────────

  Widget _buildAssistantFab(RfTheme t) {
    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.bottomRight,
      children: [
        // Tooltip bubble above the button
        Positioned(
          bottom: 68,
          right: 0,
          child: FadeTransition(
            opacity: _aiTooltipController,
            child: SlideTransition(
              position: Tween<Offset>(
                begin: const Offset(0, 0.3),
                end: Offset.zero,
              ).animate(CurvedAnimation(
                parent: _aiTooltipController,
                curve: Curves.easeOutCubic,
              )),
              child: CustomPaint(
                painter: _ChatBubblePainter(color: t.card),
                child: Container(
                  width: 240,
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 34),
                  child: Text(
                    'Soy tu asistente con inteligencia artificial, \u00a1puedo ayudarte a planificar el evento completo! \u{1F389}',
                    style: GoogleFonts.dmSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: t.textPrimary,
                      height: 1.4,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
        // Glowing circle button
        Semantics(
          button: true,
          label: 'Rosa IA',
          child: GestureDetector(
            onTap: () {
              _aiTooltipDismiss?.cancel();
              _aiTooltipController.reverse();
              _aiFabGlowController.stop();
              AssistantScreen.open(context);
            },
          child: AnimatedBuilder(
            animation: _aiFabGlowController,
            builder: (context, _) => Container(
              width: 76, height: 76,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.violet, AppColors.hotPink],
                ),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.hotPink.withValues(
                        alpha: 0.1 + _aiFabGlowController.value * 0.2),
                    blurRadius: 20,
                    spreadRadius: _aiFabGlowController.value * 3,
                  ),
                ],
              ),
              child: Stack(
                clipBehavior: Clip.none,
                alignment: Alignment.center,
                children: [
                  const Icon(Icons.support_agent_rounded,
                      color: Colors.white, size: 36),
                  Positioned(
                    bottom: 12,
                    left: -6,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 6, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(10),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.12),
                            blurRadius: 6,
                            offset: const Offset(0, 1),
                          ),
                        ],
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          _dot(),
                          const SizedBox(width: 3),
                          _dot(),
                          const SizedBox(width: 3),
                          _dot(),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        ),
      ],
    );
  }

  Widget _dot() {
    return Container(
      width: 4, height: 4,
      decoration: const BoxDecoration(
        color: AppColors.violet,
        shape: BoxShape.circle,
      ),
    );
  }

  // ── Bottom Nav ──────────────────────────────────────────────────────────

  Widget _buildBottomNav(RfTheme t) {
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
              _navItem(Icons.home_rounded, 'Inicio', t,
                  iconSize: 32, tabIndex: 0),
              _navItem(Icons.storefront_outlined, 'Catálogo', t,
                  iconSize: 30, tabIndex: 1),
              _navItem(Icons.event_outlined, 'Eventos', t,
                  iconSize: 28, tabIndex: 2),
              _navItem(Icons.favorite_border_rounded, 'Favoritos', t,
                  iconSize: 28, tabIndex: 3),
              _navItem(Icons.more_horiz_rounded, 'Más', t,
                  iconSize: 30,
                  onTap: () => _showMoreMenu(t)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _navItem(IconData icon, String label, RfTheme t,
      {int? tabIndex, VoidCallback? onTap, double iconSize = 28}) {
    final isActive = tabIndex != null && _index == tabIndex;
    return Expanded(
      flex: isActive ? 3 : 1,
      child: GestureDetector(
        onTap: () {
          if (tabIndex != null) {
            goToTab(tabIndex);
          } else {
            onTap?.call();
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

  // ── More Menu ───────────────────────────────────────────────────────────

  void _showMoreMenu(RfTheme t) {
    showModalBottomSheet(
      context: context,
      backgroundColor: t.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40, height: 4,
              decoration: BoxDecoration(
                color: t.textDim.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 20),
            _moreMenuItem(Icons.person_outline_rounded, 'Mi perfil', t,
                () => Navigator.push(context, MaterialPageRoute(
                    builder: (_) => const ProfileScreen()))),
            _moreMenuItem(Icons.calendar_month_outlined, 'Calendario', t,
                () => Navigator.push(context, MaterialPageRoute(
                    builder: (_) => const EventCalendarScreen()))),
            _moreMenuItem(Icons.settings_outlined, 'Configuración', t, () {}),
            _moreMenuItem(Icons.help_outline_rounded, 'Ayuda', t, () {}),
          ],
        ),
      ),
    );
  }

  Widget _moreMenuItem(IconData icon, String label, RfTheme t,
      VoidCallback onTap) {
    return ListTile(
      leading: Container(
        width: 42, height: 42,
        decoration: BoxDecoration(
          color: AppColors.hotPink.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(icon, color: AppColors.hotPink, size: 22),
      ),
      title: Text(label, style: GoogleFonts.dmSans(
        fontSize: 15, fontWeight: FontWeight.w600, color: t.textPrimary)),
      trailing: Icon(Icons.chevron_right_rounded, color: t.textDim, size: 22),
      onTap: () { Navigator.pop(context); onTap(); },
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    );
  }
}

// ── Chat Bubble Painter (shared with shell for the AI tooltip) ───────────────

class _ChatBubblePainter extends CustomPainter {
  final Color color;
  _ChatBubblePainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    const r = 18.0;
    final w = size.width;
    final h = size.height;
    const tail = 20.0;
    final bodyH = h - tail;

    final path = Path()
      ..moveTo(r, 0)
      ..lineTo(w - r, 0)
      ..arcToPoint(Offset(w, r), radius: const Radius.circular(r))
      ..lineTo(w, bodyH - r)
      ..arcToPoint(Offset(w - r, bodyH), radius: const Radius.circular(r))
      ..lineTo(w - 12, bodyH)
      ..lineTo(w - 8, h)
      ..lineTo(w - 28, bodyH)
      ..lineTo(r, bodyH)
      ..arcToPoint(Offset(0, bodyH - r), radius: const Radius.circular(r))
      ..lineTo(0, r)
      ..arcToPoint(Offset(r, 0), radius: const Radius.circular(r))
      ..close();

    canvas.drawPath(path, Paint()..color = color);
    canvas.drawPath(path, Paint()
      ..color = const Color(0x26000000)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.8);
  }

  @override
  bool shouldRepaint(_ChatBubblePainter old) => color != old.color;
}
