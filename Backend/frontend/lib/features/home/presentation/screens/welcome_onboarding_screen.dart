import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

/// Rosa Fiesta Welcome & Onboarding — "Tropical Luxe"
class WelcomeOnboardingScreen extends StatefulWidget {
  const WelcomeOnboardingScreen({super.key});

  @override
  State<WelcomeOnboardingScreen> createState() =>
      _WelcomeOnboardingScreenState();
}

class _WelcomeOnboardingScreenState extends State<WelcomeOnboardingScreen>
    with TickerProviderStateMixin {
  late final PageController _pageController;
  late final AnimationController _entranceController;
  late final AnimationController _pulseController;
  late final AnimationController _floatController;
  late final AnimationController _gradientController;
  late final AnimationController _decoController;

  int _currentPage = 0;
  double _pageOffset = 0.0;

  static const _totalPages = 4;

  final _slides = const [
    _SlideData(
      icon: Icons.auto_awesome_rounded,
      gradient1: AppColors.coral,
      gradient2: AppColors.hotPink,
      accentGlow: AppColors.coral,
    ),
    _SlideData(
      icon: Icons.storefront_rounded,
      gradient1: AppColors.teal,
      gradient2: AppColors.sky,
      accentGlow: AppColors.teal,
    ),
    _SlideData(
      icon: Icons.insights_rounded,
      gradient1: AppColors.violet,
      gradient2: AppColors.hotPink,
      accentGlow: AppColors.violet,
    ),
  ];

  @override
  void initState() {
    super.initState();
    _pageController = PageController()
      ..addListener(() {
        setState(() => _pageOffset = _pageController.page ?? 0.0);
      });
    _entranceController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 1200),
    )..forward();
    _pulseController = AnimationController(
      vsync: this, duration: const Duration(seconds: 3),
    )..repeat(reverse: true);
    _floatController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 5500),
    )..repeat();
    _gradientController = AnimationController(
      vsync: this, duration: const Duration(seconds: 12),
    )..repeat();
    _decoController = AnimationController(
      vsync: this, duration: const Duration(seconds: 20),
    )..repeat();
  }

  @override
  void dispose() {
    _pageController.dispose();
    _entranceController.dispose();
    _pulseController.dispose();
    _floatController.dispose();
    _gradientController.dispose();
    _decoController.dispose();
    super.dispose();
  }

  void _toggleTheme() => context.read<ThemeProvider>().toggle();

  void _goToPage(int page) => _pageController.animateToPage(
    page,
    duration: const Duration(milliseconds: 500),
    curve: Curves.easeInOutCubic,
  );

  Color _lerpPageColor(List<Color> colors) {
    final page = _pageOffset.clamp(0.0, colors.length - 1.0);
    final index = page.floor();
    final tv = page - index;
    if (index >= colors.length - 1) return colors.last;
    return Color.lerp(colors[index], colors[index + 1], tv)!;
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;
    final glowColor1 = _lerpPageColor([
      AppColors.coral, AppColors.teal, AppColors.violet, AppColors.hotPink,
    ]);
    final glowColor2 = _lerpPageColor([
      AppColors.hotPink, AppColors.sky, AppColors.hotPink, AppColors.amber,
    ]);

    return AnimatedContainer(
      duration: const Duration(milliseconds: 400),
      color: t.base,
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            // Layer 1 — Gradient orbs
            RfGradientOrbs(
              controller: _gradientController,
              color1: glowColor1,
              color2: glowColor2,
              isDark: t.isDark,
            ),
            // Layer 2 — Decorative event elements (petals, rings, diamonds)
            RfDecoLayer(
          floatController: _floatController,
          decoController:  _decoController,
          pulseController: _pulseController,
          baseOpacity: t.isDark ? 1.0 : 1.8,
        ),
            // Layer 3 — Grid pattern
            Positioned.fill(
              child: IgnorePointer(
                child: CustomPaint(
                  painter: RfGridPainter(
                    color: (t.isDark ? Colors.white : Colors.black)
                        .withOpacity(0.015),
                  ),
                ),
              ),
            ),
            // Layer 4 — Main content
            SafeArea(
              child: Column(
                children: [
                  _buildTopBar(t),
                  Expanded(
                    child: PageView(
                      controller: _pageController,
                      onPageChanged: (i) =>
                          setState(() => _currentPage = i),
                      children: [
                        ..._slides.asMap().entries.map(
                            (e) => _buildOnboardingPage(e.key, e.value, t)),
                        _buildAuthPage(t),
                      ],
                    ),
                  ),
                  _buildPageIndicator(t),
                  const SizedBox(height: 36),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ─── TOP BAR ───
  Widget _buildTopBar(RfTheme t) {
    final l10n = AppLocalizations.of(context)!;
    return SlideTransition(
      position: Tween<Offset>(begin: const Offset(0, -1), end: Offset.zero)
          .animate(CurvedAnimation(
        parent: _entranceController,
        curve: const Interval(0.0, 0.5, curve: Curves.easeOutCubic),
      )),
      child: FadeTransition(
        opacity: CurvedAnimation(
          parent: _entranceController,
          curve: const Interval(0.0, 0.4),
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
          child: Row(
            children: [
              const Spacer(),
              // Theme toggle
              RfThemeToggle(t: t),
              const SizedBox(width: 10),
              if (_currentPage < _totalPages - 1)
                GestureDetector(
                  onTap: () => _goToPage(_totalPages - 1),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 400),
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      color: t.isDark
                          ? Colors.white.withOpacity(0.06)
                          : Colors.black.withOpacity(0.04),
                      border: Border.all(color: t.borderFaint),
                    ),
                    child: Text(
                      l10n.skipButton,
                      style: GoogleFonts.dmSans(
                        color: t.textMuted, fontSize: 12,
                        fontWeight: FontWeight.w500, letterSpacing: 1.5,
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  // ─── ONBOARDING PAGES ───
  Widget _buildOnboardingPage(int index, _SlideData slide, RfTheme t) {
    final l10n = AppLocalizations.of(context)!;
    final titles = [
      l10n.onboardingTitle1,
      l10n.onboardingTitle2,
      l10n.onboardingTitle3,
    ];
    final descs = [
      l10n.onboardingDesc1,
      l10n.onboardingDesc2,
      l10n.onboardingDesc3,
    ];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: Column(
        children: [
          const Spacer(flex: 2),
          // Icon with animated ring
          AnimatedBuilder(
            animation: _pulseController,
            builder: (context, _) {
              final pulse = _pulseController.value;
              return SizedBox(
                width: 240, height: 240,
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    // Outer glow
                    Container(
                      width: 200 + pulse * 10,
                      height: 200 + pulse * 10,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: RadialGradient(
                          colors: [
                            slide.accentGlow.withOpacity(
                                t.isDark ? (0.08 + pulse * 0.04) : (0.12 + pulse * 0.06)),
                            slide.accentGlow.withOpacity(0),
                          ],
                        ),
                      ),
                    ),
                    // Rotating hex ring
                    AnimatedBuilder(
                      animation: _floatController,
                      builder: (context, _) => Transform.rotate(
                        angle: _floatController.value * math.pi * 2,
                        child: CustomPaint(
                          size: const Size(180, 180),
                          painter: _HexRingPainter(
                            color1: slide.gradient1,
                            color2: slide.gradient2,
                            progress: pulse,
                          ),
                        ),
                      ),
                    ),
                    // Inner icon
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 110, height: 110,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: t.isDark ? t.card : Colors.white,
                        border: Border.all(
                          color: slide.gradient1.withOpacity(0.3), width: 1.5,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: slide.accentGlow.withOpacity(
                                t.isDark ? (0.2 + pulse * 0.1) : (0.15 + pulse * 0.08)),
                            blurRadius: t.isDark ? 30 : 20,
                            spreadRadius: 2,
                          ),
                        ],
                      ),
                      child: ShaderMask(
                        shaderCallback: (bounds) => LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [slide.gradient1, slide.gradient2],
                        ).createShader(bounds),
                        child: Icon(slide.icon, size: 44, color: Colors.white),
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
          const SizedBox(height: 48),
          // Subtle safe-zone behind text so deco elements don't intrude
          AnimatedContainer(
            duration: const Duration(milliseconds: 400),
            padding: const EdgeInsets.fromLTRB(20, 24, 20, 24),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              color: t.isDark
                  ? Colors.black.withOpacity(0.18)
                  : Colors.white.withOpacity(0.93),
              border: Border.all(color: t.borderFaint),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(t.isDark ? 0.0 : 0.06),
                  blurRadius: 24, offset: const Offset(0, 8),
                ),
              ],
            ),
            child: Column(
              children: [
                AnimatedDefaultTextStyle(
                  duration: const Duration(milliseconds: 400),
                  style: GoogleFonts.outfit(
                    color: t.textPrimary, fontSize: 34,
                    fontWeight: FontWeight.w800, height: 1.15, letterSpacing: -0.5,
                  ),
                  child: Text(titles[index], textAlign: TextAlign.center),
                ),
                const SizedBox(height: 18),
                Container(
                  width: 48, height: 3,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(2),
                    gradient: LinearGradient(colors: [slide.gradient1, slide.gradient2]),
                  ),
                ),
                const SizedBox(height: 18),
                AnimatedDefaultTextStyle(
                  duration: const Duration(milliseconds: 400),
                  style: GoogleFonts.dmSans(
                    color: t.textMuted, fontSize: 16,
                    fontWeight: FontWeight.w400, height: 1.6, letterSpacing: 0.2,
                  ),
                  child: Text(descs[index], textAlign: TextAlign.center),
                ),
              ],
            ),
          ),
          const Spacer(flex: 3),
        ],
      ),
    );
  }

  // ─── AUTH PAGE ─── (scrollable so content never clips on small screens)
  Widget _buildAuthPage(RfTheme t) {
    final l10n = AppLocalizations.of(context)!;
    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 28),
      child: ConstrainedBox(
        constraints: BoxConstraints(
          minHeight: MediaQuery.of(context).size.height -
              MediaQuery.of(context).padding.top - 120,
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const SizedBox(height: 24),
            // Logo with rainbow ring
            AnimatedBuilder(
              animation: _pulseController,
              builder: (context, _) {
                final pulse = _pulseController.value;
                return SizedBox(
                  width: 200, height: 200,
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      AnimatedBuilder(
                        animation: _floatController,
                        builder: (context, _) => Transform.rotate(
                          angle: _floatController.value * math.pi * 2 * 0.3,
                          child: Container(
                            width: 170, height: 170,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              gradient: SweepGradient(
                                colors: [
                                  AppColors.coral.withOpacity(t.isDark ? 0.4 : 0.6),
                                  AppColors.amber.withOpacity(t.isDark ? 0.3 : 0.5),
                                  AppColors.teal.withOpacity(t.isDark ? 0.4 : 0.6),
                                  AppColors.violet.withOpacity(t.isDark ? 0.3 : 0.5),
                                  AppColors.coral.withOpacity(t.isDark ? 0.4 : 0.6),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                      AnimatedContainer(
                        duration: const Duration(milliseconds: 400),
                        width: 158, height: 158,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle, color: t.base,
                        ),
                      ),
                      AnimatedContainer(
                        duration: const Duration(milliseconds: 400),
                        width: 120, height: 120,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: Colors.white, // logo PNG has white bg — always white
                          boxShadow: [
                            BoxShadow(
                              color: AppColors.coral.withOpacity(
                                  t.isDark ? (0.15 + pulse * 0.1) : (0.2 + pulse * 0.1)),
                              blurRadius: 40, spreadRadius: 5,
                            ),
                          ],
                        ),
                        child: ClipOval(
                          child: Image.asset(
                              'assets/images/logo_rosafiesta.png',
                              width: 120, height: 120,
                              fit: BoxFit.cover, // fills circle, clips PNG's own square bg
                              errorBuilder: (_, __, ___) => ShaderMask(
                                shaderCallback: (b) => const LinearGradient(
                                  colors: [AppColors.coral, AppColors.amber],
                                ).createShader(b),
                                child: const Icon(
                                  Icons.local_florist, size: 50, color: Colors.white,
                                ),
                              ),
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 28),
            // Brand name — opaque container blocks deco elements from bleeding through
            AnimatedContainer(
              duration: const Duration(milliseconds: 400),
              padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 18),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                color: t.isDark
                    ? Colors.black.withOpacity(0.2)
                    : Colors.white.withOpacity(0.93),
                border: Border.all(color: t.borderFaint),
              ),
              child: Column(
                children: [
                  ShaderMask(
                    shaderCallback: (b) => const LinearGradient(
                      colors: [AppColors.hotPink, AppColors.amber, AppColors.teal],
                    ).createShader(b),
                    child: Text(
                      'Rosa Fiesta',
                      style: GoogleFonts.outfit(
                        color: Colors.white, fontSize: 44,
                        fontWeight: FontWeight.w800, letterSpacing: -0.5,
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    l10n.welcomeSubheadline.toUpperCase(),
                    style: GoogleFonts.dmSans(
                      color: t.textDim, fontSize: 11,
                      fontWeight: FontWeight.w600, letterSpacing: 4,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 32),
            // Auth card
            ClipRRect(
              borderRadius: BorderRadius.circular(24),
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 16, sigmaY: 16),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 400),
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: t.isDark
                        ? Colors.white.withOpacity(0.06)
                        : Colors.white, // fully opaque in light — deco stays behind
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(color: t.borderFaint),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(t.isDark ? 0.0 : 0.07),
                        blurRadius: 28, offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      RfLuxeButton(
                        label: l10n.beginButton,
                        onTap: () => Navigator.push(context,
                            MaterialPageRoute(builder: (_) => const RegisterScreen())),
                        filled: true, t: t,
                      ),
                      const SizedBox(height: 12),
                      RfLuxeButton(
                        label: l10n.loginButton,
                        onTap: () => Navigator.push(context,
                            MaterialPageRoute(builder: (_) => const LoginScreen())),
                        filled: false, t: t,
                      ),
                      const SizedBox(height: 16),
                      Text(
                        l10n.alreadyHaveAccount,
                        style: GoogleFonts.dmSans(
                          color: t.textDim, fontSize: 12, letterSpacing: 0.3,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  // ─── PAGE INDICATOR ───
  Widget _buildPageIndicator(RfTheme t) {
    final isLast = _currentPage == _totalPages - 1;
    final pageColors = [AppColors.coral, AppColors.teal, AppColors.violet, AppColors.amber];
    final activeColor = pageColors[_currentPage];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: SizedBox(
        height: 54,
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Dots — centered
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(_totalPages, (i) {
                final isActive = i == _currentPage;
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 350),
                  curve: Curves.easeOutCubic,
                  margin: const EdgeInsets.only(right: 8),
                  width: isActive ? 28 : 8, height: 8,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(4),
                    color: isActive
                        ? activeColor
                        : (t.isDark
                            ? Colors.white.withOpacity(0.12)
                            : Colors.black.withOpacity(0.1)),
                    boxShadow: isActive && t.isDark
                        ? [BoxShadow(color: activeColor.withOpacity(0.4), blurRadius: 8)]
                        : null,
                  ),
                );
              }),
            ),
            // Arrow — right side
            if (!isLast)
              Align(
                alignment: Alignment.centerRight,
                child: GestureDetector(
                  onTap: () => _goToPage(_currentPage + 1),
                  child: AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, _) {
                      final pulse = _pulseController.value;
                      return Container(
                        width: 54, height: 54,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: LinearGradient(
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                            colors: [
                              activeColor,
                              pageColors[(_currentPage + 1) % _totalPages],
                            ],
                          ),
                          boxShadow: [
                            BoxShadow(
                              color: activeColor.withOpacity(
                                  t.isDark ? (0.3 + pulse * 0.15) : (0.25 + pulse * 0.1)),
                              blurRadius: 16, offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: const Icon(
                          Icons.arrow_forward_rounded, color: Colors.white, size: 22,
                        ),
                      );
                    },
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════
// SHARED THEME TOGGLE — pill with animated icon swap
class _HexRingPainter extends CustomPainter {
  final Color color1;
  final Color color2;
  final double progress;

  _HexRingPainter({required this.color1, required this.color2, required this.progress});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    for (int i = 0; i < 6; i++) {
      paint.color = Color.lerp(
        color1.withOpacity(0.25 + progress * 0.15),
        color2.withOpacity(0.25 + progress * 0.15),
        i / 6.0,
      )!;
      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        i * math.pi / 3 + 0.1, math.pi / 3 - 0.2, false, paint,
      );
    }

    final dotPaint = Paint()..style = PaintingStyle.fill;
    for (int i = 0; i < 6; i++) {
      final angle = i * math.pi / 3;
      dotPaint.color =
          Color.lerp(color1, color2, i / 6.0)!.withOpacity(0.4 + progress * 0.2);
      canvas.drawCircle(
        Offset(center.dx + radius * math.cos(angle),
               center.dy + radius * math.sin(angle)),
        2.5, dotPaint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _HexRingPainter old) => old.progress != progress;
}

// ═══════════════════════════════════════════════════════════════════════
// GRID PATTERN PAINTER
class _SlideData {
  final IconData icon;
  final Color gradient1;
  final Color gradient2;
  final Color accentGlow;

  const _SlideData({
    required this.icon, required this.gradient1,
    required this.gradient2, required this.accentGlow,
  });
}
