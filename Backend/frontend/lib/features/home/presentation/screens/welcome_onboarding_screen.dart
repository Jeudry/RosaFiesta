import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';

// ─── Shared vivid accent spectrum ───
const _electricCoral = Color(0xFFFF6B6B);
const _tropicalTeal  = Color(0xFF00D4AA);
const _goldenAmber   = Color(0xFFFFB800);
const _vividViolet   = Color(0xFF8B5CF6);
const _hotPink       = Color(0xFFFF3CAC);
const _skyBlue       = Color(0xFF4FC3F7);

// ─── Theme token bag ───
class _T {
  final Color base;
  final Color surface;
  final Color card;
  final Color textPrimary;
  final Color textMuted;
  final Color textDim;
  final Color borderFaint;
  final bool isDark;

  const _T({
    required this.base,
    required this.surface,
    required this.card,
    required this.textPrimary,
    required this.textMuted,
    required this.textDim,
    required this.borderFaint,
    required this.isDark,
  });

  static const dark = _T(
    base:        Color(0xFF0A0A14),
    surface:     Color(0xFF12121E),
    card:        Color(0xFF1A1A2E),
    textPrimary: Color(0xFFF8F8FF),
    textMuted:   Color(0xFF9B9BC0),
    textDim:     Color(0xFF6B6B8D),
    borderFaint: Color(0x1AFFFFFF),
    isDark:      true,
  );

  static const light = _T(
    base:        Color(0xFFFAFAFC),
    surface:     Color(0xFFFFFFFF),
    card:        Color(0xFFF4F4F8),
    textPrimary: Color(0xFF0A0A1E),
    textMuted:   Color(0xFF5A5A80),
    textDim:     Color(0xFF9090B0),
    borderFaint: Color(0x1A000000),
    isDark:      false,
  );
}

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
  bool _isDark = true;

  static const _totalPages = 4;
  _T get _t => _isDark ? _T.dark : _T.light;

  final _slides = const [
    _SlideData(
      icon: Icons.auto_awesome_rounded,
      gradient1: _electricCoral,
      gradient2: _hotPink,
      accentGlow: _electricCoral,
    ),
    _SlideData(
      icon: Icons.storefront_rounded,
      gradient1: _tropicalTeal,
      gradient2: _skyBlue,
      accentGlow: _tropicalTeal,
    ),
    _SlideData(
      icon: Icons.insights_rounded,
      gradient1: _vividViolet,
      gradient2: _hotPink,
      accentGlow: _vividViolet,
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

  void _toggleTheme() => setState(() => _isDark = !_isDark);

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
    final t = _t;
    final glowColor1 = _lerpPageColor([
      _electricCoral, _tropicalTeal, _vividViolet, _hotPink,
    ]);
    final glowColor2 = _lerpPageColor([
      _hotPink, _skyBlue, _hotPink, _goldenAmber,
    ]);

    return AnimatedContainer(
      duration: const Duration(milliseconds: 400),
      color: t.base,
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            // Layer 1 — Gradient orbs
            _GradientOrbs(
              controller: _gradientController,
              color1: glowColor1,
              color2: glowColor2,
              isDark: t.isDark,
            ),
            // Layer 2 — Decorative event elements (petals, rings, diamonds)
            _DecoLayer(
              floatController: _floatController,
              decoController: _decoController,
              pulseController: _pulseController,
              isDark: t.isDark,
            ),
            // Layer 3 — Grid pattern
            Positioned.fill(
              child: IgnorePointer(
                child: CustomPaint(
                  painter: _GridPatternPainter(
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
  Widget _buildTopBar(_T t) {
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
              GestureDetector(
                onTap: _toggleTheme,
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 400),
                  width: 52, height: 28,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    color: t.isDark
                        ? Colors.white.withOpacity(0.08)
                        : _electricCoral.withOpacity(0.12),
                    border: Border.all(
                      color: t.isDark
                          ? Colors.white.withOpacity(0.12)
                          : _electricCoral.withOpacity(0.3),
                      width: 1,
                    ),
                  ),
                  child: Stack(
                    children: [
                      AnimatedPositioned(
                        duration: const Duration(milliseconds: 300),
                        curve: Curves.easeInOutCubic,
                        left: t.isDark ? 2 : 26,
                        top: 2,
                        child: Container(
                          width: 22, height: 22,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: LinearGradient(
                              colors: t.isDark
                                  ? [_vividViolet, _skyBlue]
                                  : [_goldenAmber, _electricCoral],
                            ),
                          ),
                          child: Icon(
                            t.isDark
                                ? Icons.nightlight_round
                                : Icons.wb_sunny_rounded,
                            size: 13, color: Colors.white,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
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
  Widget _buildOnboardingPage(int index, _SlideData slide, _T t) {
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
  Widget _buildAuthPage(_T t) {
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
                                  _electricCoral.withOpacity(t.isDark ? 0.4 : 0.6),
                                  _goldenAmber.withOpacity(t.isDark ? 0.3 : 0.5),
                                  _tropicalTeal.withOpacity(t.isDark ? 0.4 : 0.6),
                                  _vividViolet.withOpacity(t.isDark ? 0.3 : 0.5),
                                  _electricCoral.withOpacity(t.isDark ? 0.4 : 0.6),
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
                              color: _electricCoral.withOpacity(
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
                                  colors: [_electricCoral, _goldenAmber],
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
                      colors: [_hotPink, _goldenAmber, _tropicalTeal],
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
                      _LuxeButton(
                        label: l10n.beginButton,
                        onTap: () => Navigator.push(context,
                            MaterialPageRoute(builder: (_) => const RegisterScreen())),
                        filled: true, t: t,
                      ),
                      const SizedBox(height: 12),
                      _LuxeButton(
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
  Widget _buildPageIndicator(_T t) {
    final isLast = _currentPage == _totalPages - 1;
    final pageColors = [_electricCoral, _tropicalTeal, _vividViolet, _goldenAmber];
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
// DECORATIVE LAYER — petals, rings, diamonds, bubbles (event-themed)
// ═══════════════════════════════════════════════════════════════════════
class _DecoLayer extends StatelessWidget {
  final AnimationController floatController;
  final AnimationController decoController;
  final AnimationController pulseController;
  final bool isDark;

  const _DecoLayer({
    required this.floatController,
    required this.decoController,
    required this.pulseController,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    // Light mode: use darker stroke colors + higher multiplier so elements
    // are visible against the light background without being distracting.
    final base = isDark ? 1.0 : 1.8;

    return Positioned.fill(
      child: IgnorePointer(
        child: AnimatedBuilder(
          animation: Listenable.merge([floatController, decoController, pulseController]),
          builder: (context, _) {
            return CustomPaint(
              painter: _DecoPainter(
                floatT: floatController.value,
                decoT: decoController.value,
                pulseT: pulseController.value,
                size: size,
                isDark: isDark,
                baseOpacity: base,
              ),
            );
          },
        ),
      ),
    );
  }
}

class _DecoPainter extends CustomPainter {
  final double floatT;
  final double decoT;
  final double pulseT;
  final Size size;
  final bool isDark;
  final double baseOpacity;

  _DecoPainter({
    required this.floatT,
    required this.decoT,
    required this.pulseT,
    required this.size,
    required this.isDark,
    required this.baseOpacity,
  });

  // Seeded random positions (stable across repaints)
  static final _rng = math.Random(42);
  static final _positions = List.generate(
      40, (_) => [_rng.nextDouble(), _rng.nextDouble(), _rng.nextDouble()]);

  @override
  void paint(Canvas canvas, Size sz) {
    // Clip to the "safe zone": avoid the top bar (~18%) and page indicator (~13%)
    // so no deco element ever renders on top of UI controls.
    canvas.save();
    canvas.clipRect(Rect.fromLTWH(
      0,
      sz.height * 0.18,
      sz.width,
      sz.height * (1.0 - 0.18 - 0.13),
    ));

    // ── Outline circles (like balloons / table centerpieces) ──
    _drawOutlineCircles(canvas, sz);
    // ── Petal shapes (floral decoration) ──
    _drawPetals(canvas, sz);
    // ── Diamond outlines (wedding / event decoration) ──
    _drawDiamonds(canvas, sz);
    // ── Tiny filled sparkle dots ──
    _drawSparkles(canvas, sz);

    canvas.restore();
  }

  void _drawOutlineCircles(Canvas canvas, Size sz) {
    final colors = [
      _electricCoral, _tropicalTeal, _goldenAmber,
      _vividViolet, _hotPink, _skyBlue,
    ];
    const specs = [
      // [xFrac, yFrac, radius, colorIdx, speedMult, phase]
      [0.08, 0.12, 22.0, 0, 1.0, 0.0],
      [0.90, 0.18, 16.0, 1, 0.7, 1.2],
      [0.75, 0.08, 28.0, 2, 0.9, 2.5],
      [0.05, 0.45, 18.0, 3, 1.1, 0.8],
      [0.93, 0.55, 20.0, 4, 0.8, 3.1],
      [0.15, 0.78, 25.0, 5, 1.2, 1.8],
      [0.82, 0.80, 13.0, 0, 0.6, 4.2],
      [0.50, 0.92, 18.0, 1, 1.0, 2.0],
      [0.35, 0.06, 15.0, 2, 1.3, 5.1],
      [0.62, 0.72, 18.0, 3, 0.9, 0.3],
    ];

    for (final s in specs) {
      final xFrac = s[0] as double;
      final yFrac = s[1] as double;
      final radius = s[2] as double;
      final colorIdx = (s[3] as double).toInt();
      final speedMult = s[4] as double;
      final phase = s[5] as double;

      final t = floatT * math.pi * 2 + phase;
      final dx = math.sin(t) * 6;
      final dy = math.cos(t + 1.57) * 7;
      final opacity = (0.05 + math.sin(t + 1.0).abs() * 0.04) * baseOpacity;

      final paint = Paint()
        ..color = colors[colorIdx].withOpacity(opacity)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.5;

      canvas.drawCircle(
        Offset(xFrac * sz.width + dx, yFrac * sz.height + dy),
        radius,
        paint,
      );
    }
  }

  void _drawPetals(Canvas canvas, Size sz) {
    const specs = [
      // [xFrac, yFrac, size, colorHex, phase]
      [0.20, 0.25, 20.0, 0xFFFF6B6B, 0.0],
      [0.88, 0.35, 15.0, 0xFF00D4AA, 1.5],
      [0.04, 0.60, 18.0, 0xFFFF3CAC, 3.0],
      [0.78, 0.65, 22.0, 0xFFFFB800, 2.2],
      [0.42, 0.88, 16.0, 0xFF8B5CF6, 4.1],
      [0.55, 0.15, 18.0, 0xFFFF6B6B, 5.3],
      [0.28, 0.95, 15.0, 0xFF4FC3F7, 1.0],
      [0.96, 0.88, 20.0, 0xFF00D4AA, 6.0],
    ];

    for (final s in specs) {
      final xFrac = s[0] as double;
      final yFrac = s[1] as double;
      final petalSize = s[2] as double;
      final colorHex = (s[3] as double).toInt();
      final phase = s[4] as double;

      final t = floatT * math.pi * 2 + phase;
      final dx = math.sin(t) * 7;
      final dy = math.cos(t + 1.2) * 8;
      final rotation = decoT * math.pi * 2 + phase;
      final opacity = (0.04 + math.sin(t).abs() * 0.04) * baseOpacity;

      final cx = xFrac * sz.width + dx;
      final cy = yFrac * sz.height + dy;

      final paint = Paint()
        ..color = Color(colorHex | 0xFF000000).withOpacity(opacity)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.0;

      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rotation);

      // 5-petal flower outline
      for (int p = 0; p < 5; p++) {
        final petalAngle = p * 2 * math.pi / 5;
        final px = math.cos(petalAngle) * petalSize * 0.55;
        final py = math.sin(petalAngle) * petalSize * 0.55;
        final path = Path()
          ..moveTo(0, 0)
          ..addOval(Rect.fromCenter(
            center: Offset(px, py),
            width: petalSize * 0.55,
            height: petalSize * 0.75,
          ));
        canvas.drawPath(path, paint);
      }

      canvas.restore();
    }
  }

  void _drawDiamonds(Canvas canvas, Size sz) {
    const specs = [
      // [xFrac, yFrac, size, colorHex, phase, speedMult]
      [0.12, 0.35, 22.0, 0xFF8B5CF6, 0.5, 0.9],
      [0.85, 0.12, 18.0, 0xFFFFB800, 2.0, 1.1],
      [0.70, 0.50, 26.0, 0xFFFF3CAC, 3.5, 0.7],
      [0.25, 0.68, 20.0, 0xFF00D4AA, 1.2, 1.3],
      [0.48, 0.30, 16.0, 0xFFFF6B6B, 4.8, 1.0],
      [0.92, 0.72, 22.0, 0xFF4FC3F7, 2.7, 0.8],
      [0.38, 0.82, 18.0, 0xFF8B5CF6, 6.1, 1.2],
    ];

    for (final s in specs) {
      final xFrac = s[0] as double;
      final yFrac = s[1] as double;
      final dSize = s[2] as double;
      final colorHex = (s[3] as double).toInt();
      final phase = s[4] as double;
      final speedMult = s[5] as double;

      final t = floatT * math.pi * 2 + phase;
      final dx = math.sin(t) * 6;
      final dy = math.cos(t + 1.0) * 8;
      final rotation = decoT * math.pi * 2 + phase * 0.3;
      final opacity = (0.06 + math.cos(t).abs() * 0.05) * baseOpacity;

      final cx = xFrac * sz.width + dx;
      final cy = yFrac * sz.height + dy;

      final paint = Paint()
        ..color = Color(colorHex | 0xFF000000).withOpacity(opacity)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.8;

      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rotation);

      final path = Path()
        ..moveTo(0, -dSize)
        ..lineTo(dSize * 0.6, 0)
        ..lineTo(0, dSize)
        ..lineTo(-dSize * 0.6, 0)
        ..close();
      canvas.drawPath(path, paint);

      canvas.restore();
    }
  }

  void _drawSparkles(Canvas canvas, Size sz) {
    const specs = [
      // [xFrac, yFrac, radius, colorHex, phase]
      [0.32, 0.14, 6.0, 0xFFFFB800, 0.0],
      [0.67, 0.22, 5.0, 0xFFFF6B6B, 1.8],
      [0.10, 0.52, 6.0, 0xFF00D4AA, 3.3],
      [0.88, 0.45, 5.0, 0xFF8B5CF6, 2.1],
      [0.45, 0.75, 7.0, 0xFFFF3CAC, 4.5],
      [0.72, 0.90, 6.0, 0xFFFFB800, 1.0],
      [0.18, 0.88, 5.0, 0xFF4FC3F7, 5.7],
      [0.58, 0.55, 5.0, 0xFFFF6B6B, 3.9],
      [0.80, 0.30, 6.0, 0xFF00D4AA, 0.7],
      [0.05, 0.20, 5.0, 0xFF8B5CF6, 2.6],
      [0.95, 0.60, 7.0, 0xFFFFB800, 5.2],
      [0.40, 0.45, 5.0, 0xFFFF3CAC, 1.4],
    ];

    for (final s in specs) {
      final xFrac = s[0] as double;
      final yFrac = s[1] as double;
      final radius = s[2] as double;
      final colorHex = (s[3] as double).toInt();
      final phase = s[4] as double;

      final t = floatT * math.pi * 2 + phase;
      final dx = math.sin(t) * 5;
      final dy = math.cos(t + 0.8) * 6;
      final opacity = (0.08 + math.sin(t + 1.0).abs() * 0.10) * baseOpacity;

      final paint = Paint()
        ..color = Color(colorHex | 0xFF000000).withOpacity(opacity)
        ..style = PaintingStyle.fill;

      canvas.drawCircle(
        Offset(xFrac * sz.width + dx, yFrac * sz.height + dy),
        radius,
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _DecoPainter old) =>
      old.floatT != floatT || old.decoT != decoT || old.pulseT != pulseT;
}

// ═══════════════════════════════════════════════════════════════════════
// LUXE BUTTON
// ═══════════════════════════════════════════════════════════════════════
class _LuxeButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final bool filled;
  final _T t;

  const _LuxeButton({
    required this.label, required this.onTap,
    required this.filled, required this.t,
  });

  @override
  Widget build(BuildContext context) {
    if (filled) {
      return GestureDetector(
        onTap: onTap,
        child: Container(
          width: double.infinity, height: 56,
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              begin: Alignment.topLeft, end: Alignment.bottomRight,
              colors: [_electricCoral, _hotPink],
            ),
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: _electricCoral.withOpacity(t.isDark ? 0.35 : 0.25),
                blurRadius: 20, offset: const Offset(0, 8),
              ),
            ],
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: GoogleFonts.outfit(
              color: Colors.white, fontSize: 15,
              fontWeight: FontWeight.w700, letterSpacing: 2,
            ),
          ),
        ),
      );
    }

    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 400),
        width: double.infinity, height: 56,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          color: t.isDark
              ? Colors.white.withOpacity(0.04)
              : Colors.black.withOpacity(0.04),
          border: Border.all(color: t.borderFaint, width: 1.5),
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: GoogleFonts.outfit(
            color: t.textPrimary, fontSize: 15,
            fontWeight: FontWeight.w600, letterSpacing: 2,
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════
// GRADIENT ORBS BACKGROUND
// ═══════════════════════════════════════════════════════════════════════
class _GradientOrbs extends StatelessWidget {
  final AnimationController controller;
  final Color color1;
  final Color color2;
  final bool isDark;

  const _GradientOrbs({
    required this.controller, required this.color1,
    required this.color2, required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final phase = controller.value * math.pi * 2;
        final o1 = isDark ? 0.12 : 0.08;
        final o2 = isDark ? 0.10 : 0.06;
        return Stack(
          children: [
            Positioned(
              left: -80 + 60 * math.sin(phase),
              top: -100 + 40 * math.cos(phase),
              child: Container(
                width: 400, height: 400,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [color1.withOpacity(o1), color1.withOpacity(0)],
                  ),
                ),
              ),
            ),
            Positioned(
              right: -120 + 50 * math.cos(phase * 0.7),
              bottom: -80 + 60 * math.sin(phase * 0.7),
              child: Container(
                width: 350, height: 350,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [color2.withOpacity(o2), color2.withOpacity(0)],
                  ),
                ),
              ),
            ),
            Positioned(
              left: MediaQuery.of(context).size.width * 0.3 +
                  30 * math.sin(phase * 1.3),
              top: MediaQuery.of(context).size.height * 0.4 +
                  20 * math.cos(phase * 1.3),
              child: Container(
                width: 200, height: 200,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      _goldenAmber.withOpacity(isDark ? 0.04 : 0.05),
                      _goldenAmber.withOpacity(0),
                    ],
                  ),
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════
// HEX RING PAINTER
// ═══════════════════════════════════════════════════════════════════════
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
// ═══════════════════════════════════════════════════════════════════════
class _GridPatternPainter extends CustomPainter {
  final Color color;
  _GridPatternPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..color = color..strokeWidth = 0.5;
    const spacing = 60.0;
    for (double x = 0; x < size.width; x += spacing) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += spacing) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter old) => false;
}

// ═══════════════════════════════════════════════════════════════════════
// DATA MODEL
// ═══════════════════════════════════════════════════════════════════════
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
