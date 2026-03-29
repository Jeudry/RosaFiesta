import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';

// ─── Shared vivid accent spectrum ───
const _electricCoral = Color(0xFFFF6B6B);
const _tropicalTeal = Color(0xFF00D4AA);
const _goldenAmber = Color(0xFFFFB800);
const _vividViolet = Color(0xFF8B5CF6);
const _hotPink = Color(0xFFFF3CAC);
const _skyBlue = Color(0xFF4FC3F7);

// ─── Theme token bag ───
class _T {
  final Color base;
  final Color surface;
  final Color card;
  final Color textPrimary;
  final Color textMuted;
  final Color textDim;
  final Color borderFaint;
  final Color particleOpacityFactor; // unused — see factor below
  final double particleFactor; // 0=dark 1=light
  final bool isDark;

  const _T({
    required this.base,
    required this.surface,
    required this.card,
    required this.textPrimary,
    required this.textMuted,
    required this.textDim,
    required this.borderFaint,
    required this.particleFactor,
    required this.isDark,
  }) : particleOpacityFactor = const Color(0x00000000);

  static const dark = _T(
    base: Color(0xFF0A0A14),
    surface: Color(0xFF12121E),
    card: Color(0xFF1A1A2E),
    textPrimary: Color(0xFFF8F8FF),
    textMuted: Color(0xFF9B9BC0),
    textDim: Color(0xFF6B6B8D),
    borderFaint: Color(0x1AFFFFFF),
    particleFactor: 0.0,
    isDark: true,
  );

  static const light = _T(
    base: Color(0xFFFAFAFC),
    surface: Color(0xFFFFFFFF),
    card: Color(0xFFF4F4F8),
    textPrimary: Color(0xFF0A0A1E),
    textMuted: Color(0xFF5A5A80),
    textDim: Color(0xFF9090B0),
    borderFaint: Color(0x1A000000),
    particleFactor: 1.0,
    isDark: false,
  );
}

/// Rosa Fiesta Welcome & Onboarding — "Tropical Luxe"
/// Deep dark or crisp light base, vivid neon gradients, bold geometry.
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
  late final AnimationController _themeController;

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
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..forward();

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    )..repeat(reverse: true);

    _floatController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 8),
    )..repeat();

    _gradientController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 12),
    )..repeat();

    _themeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 400),
    );
  }

  @override
  void dispose() {
    _pageController.dispose();
    _entranceController.dispose();
    _pulseController.dispose();
    _floatController.dispose();
    _gradientController.dispose();
    _themeController.dispose();
    super.dispose();
  }

  void _toggleTheme() {
    setState(() => _isDark = !_isDark);
    if (_isDark) {
      _themeController.reverse();
    } else {
      _themeController.forward();
    }
  }

  void _goToPage(int page) {
    _pageController.animateToPage(
      page,
      duration: const Duration(milliseconds: 500),
      curve: Curves.easeInOutCubic,
    );
  }

  Color _lerpPageColor(List<Color> colors) {
    final page = _pageOffset.clamp(0.0, colors.length - 1.0);
    final index = page.floor();
    final t = page - index;
    if (index >= colors.length - 1) return colors.last;
    return Color.lerp(colors[index], colors[index + 1], t)!;
  }

  @override
  Widget build(BuildContext context) {
    final t = _t;
    final glowColor1 = _lerpPageColor([
      _electricCoral,
      _tropicalTeal,
      _vividViolet,
      _hotPink,
    ]);
    final glowColor2 = _lerpPageColor([
      _hotPink,
      _skyBlue,
      _hotPink,
      _goldenAmber,
    ]);

    return AnimatedContainer(
      duration: const Duration(milliseconds: 400),
      color: t.base,
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            // Layer 1 — Animated gradient orbs
            _GradientOrbs(
              controller: _gradientController,
              color1: glowColor1,
              color2: glowColor2,
              isDark: t.isDark,
            ),

            // Layer 2 — Geometric accent shapes
            ..._buildGeometricShapes(t),

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

            // Layer 4 — Floating particles
            ..._buildParticles(t),

            // Layer 5 — Main content
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
      position: Tween<Offset>(
        begin: const Offset(0, -1),
        end: Offset.zero,
      ).animate(CurvedAnimation(
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
              // Logo with neon glow ring
              AnimatedBuilder(
                animation: _pulseController,
                builder: (context, _) {
                  final pulse = _pulseController.value;
                  return Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: _electricCoral
                            .withOpacity(0.3 + pulse * 0.2),
                        width: 1.5,
                      ),
                      boxShadow: t.isDark
                          ? [
                              BoxShadow(
                                color: _electricCoral
                                    .withOpacity(0.15 + pulse * 0.1),
                                blurRadius: 12,
                              ),
                            ]
                          : null,
                    ),
                    child: ClipOval(
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 400),
                        color: t.surface,
                        padding: const EdgeInsets.all(3),
                        child: Image.asset(
                          'assets/images/logo_rosafiesta.png',
                          fit: BoxFit.contain,
                          errorBuilder: (_, __, ___) => Icon(
                            Icons.local_florist,
                            size: 22,
                            color: _electricCoral,
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),

              const SizedBox(width: 12),

              AnimatedDefaultTextStyle(
                duration: const Duration(milliseconds: 400),
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 4,
                ),
                child: const Text('ROSA FIESTA'),
              ),

              const Spacer(),

              // Theme toggle
              GestureDetector(
                onTap: _toggleTheme,
                child: AnimatedBuilder(
                  animation: _themeController,
                  builder: (context, _) {
                    return AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 52,
                      height: 28,
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
                              width: 22,
                              height: 22,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                gradient: LinearGradient(
                                  colors: t.isDark
                                      ? [
                                          _vividViolet,
                                          _skyBlue,
                                        ]
                                      : [
                                          _goldenAmber,
                                          _electricCoral,
                                        ],
                                ),
                              ),
                              child: Icon(
                                t.isDark
                                    ? Icons.nightlight_round
                                    : Icons.wb_sunny_rounded,
                                size: 13,
                                color: Colors.white,
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ),

              const SizedBox(width: 10),

              // Skip button
              if (_currentPage < _totalPages - 1)
                GestureDetector(
                  onTap: () => _goToPage(_totalPages - 1),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 400),
                    padding: const EdgeInsets.symmetric(
                        horizontal: 16, vertical: 8),
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
                        color: t.textMuted,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                        letterSpacing: 1.5,
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
                width: 240,
                height: 240,
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
                                t.isDark
                                    ? (0.08 + pulse * 0.04)
                                    : (0.12 + pulse * 0.06)),
                            slide.accentGlow.withOpacity(0),
                          ],
                        ),
                      ),
                    ),
                    // Rotating hex ring
                    AnimatedBuilder(
                      animation: _floatController,
                      builder: (context, _) {
                        return Transform.rotate(
                          angle: _floatController.value * math.pi * 2,
                          child: CustomPaint(
                            size: const Size(180, 180),
                            painter: _HexRingPainter(
                              color1: slide.gradient1,
                              color2: slide.gradient2,
                              progress: pulse,
                            ),
                          ),
                        );
                      },
                    ),
                    // Inner icon container
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 110,
                      height: 110,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: t.card,
                        border: Border.all(
                          color: slide.gradient1.withOpacity(0.3),
                          width: 1.5,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: slide.accentGlow.withOpacity(
                                t.isDark
                                    ? (0.2 + pulse * 0.1)
                                    : (0.15 + pulse * 0.08)),
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
                        child: Icon(
                          slide.icon,
                          size: 44,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ],
                ),
              );
            },
          ),

          const SizedBox(height: 48),

          // Title
          AnimatedDefaultTextStyle(
            duration: const Duration(milliseconds: 400),
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 34,
              fontWeight: FontWeight.w800,
              height: 1.15,
              letterSpacing: -0.5,
            ),
            child: Text(
              titles[index],
              textAlign: TextAlign.center,
            ),
          ),

          const SizedBox(height: 20),

          // Gradient accent line
          Container(
            width: 48,
            height: 3,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(2),
              gradient: LinearGradient(
                colors: [slide.gradient1, slide.gradient2],
              ),
            ),
          ),

          const SizedBox(height: 20),

          // Description
          AnimatedDefaultTextStyle(
            duration: const Duration(milliseconds: 400),
            style: GoogleFonts.dmSans(
              color: t.textMuted,
              fontSize: 16,
              fontWeight: FontWeight.w400,
              height: 1.6,
              letterSpacing: 0.2,
            ),
            child: Text(
              descs[index],
              textAlign: TextAlign.center,
            ),
          ),

          const Spacer(flex: 3),
        ],
      ),
    );
  }

  // ─── AUTH PAGE ───
  Widget _buildAuthPage(_T t) {
    final l10n = AppLocalizations.of(context)!;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 28),
      child: Column(
        children: [
          const Spacer(flex: 2),

          // Logo with rainbow ring
          AnimatedBuilder(
            animation: _pulseController,
            builder: (context, _) {
              final pulse = _pulseController.value;
              return SizedBox(
                width: 200,
                height: 200,
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    // Rotating rainbow ring
                    AnimatedBuilder(
                      animation: _floatController,
                      builder: (context, _) {
                        return Transform.rotate(
                          angle: _floatController.value * math.pi * 2 * 0.3,
                          child: Container(
                            width: 170,
                            height: 170,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              gradient: SweepGradient(
                                colors: [
                                  _electricCoral
                                      .withOpacity(t.isDark ? 0.4 : 0.6),
                                  _goldenAmber
                                      .withOpacity(t.isDark ? 0.3 : 0.5),
                                  _tropicalTeal
                                      .withOpacity(t.isDark ? 0.4 : 0.6),
                                  _vividViolet
                                      .withOpacity(t.isDark ? 0.3 : 0.5),
                                  _electricCoral
                                      .withOpacity(t.isDark ? 0.4 : 0.6),
                                ],
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                    // Cutout
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 158,
                      height: 158,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: t.base,
                      ),
                    ),
                    // Logo
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 120,
                      height: 120,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: t.surface,
                        boxShadow: [
                          BoxShadow(
                            color: _electricCoral
                                .withOpacity(t.isDark
                                    ? (0.15 + pulse * 0.1)
                                    : (0.2 + pulse * 0.1)),
                            blurRadius: 40,
                            spreadRadius: 5,
                          ),
                        ],
                      ),
                      child: ClipOval(
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Image.asset(
                            'assets/images/logo_rosafiesta.png',
                            fit: BoxFit.contain,
                            errorBuilder: (_, __, ___) => ShaderMask(
                              shaderCallback: (bounds) =>
                                  const LinearGradient(
                                colors: [_electricCoral, _goldenAmber],
                              ).createShader(bounds),
                              child: const Icon(
                                Icons.local_florist,
                                size: 50,
                                color: Colors.white,
                              ),
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

          // Gradient brand name
          ShaderMask(
            shaderCallback: (bounds) => const LinearGradient(
              colors: [_electricCoral, _goldenAmber, _tropicalTeal],
            ).createShader(bounds),
            child: Text(
              'Rosa Fiesta',
              style: GoogleFonts.outfit(
                color: Colors.white,
                fontSize: 44,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.5,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            l10n.welcomeSubheadline.toUpperCase(),
            style: GoogleFonts.dmSans(
              color: t.textDim,
              fontSize: 11,
              fontWeight: FontWeight.w600,
              letterSpacing: 4,
            ),
          ),

          const Spacer(flex: 2),

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
                      ? Colors.white.withOpacity(0.05)
                      : Colors.white.withOpacity(0.75),
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: t.borderFaint),
                  boxShadow: t.isDark
                      ? null
                      : [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.06),
                            blurRadius: 24,
                            offset: const Offset(0, 8),
                          ),
                        ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _LuxeButton(
                      label: l10n.beginButton,
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const RegisterScreen(),
                        ),
                      ),
                      filled: true,
                      t: t,
                    ),
                    const SizedBox(height: 12),
                    _LuxeButton(
                      label: l10n.loginButton,
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const LoginScreen(),
                        ),
                      ),
                      filled: false,
                      t: t,
                    ),
                    const SizedBox(height: 16),
                    Text(
                      l10n.alreadyHaveAccount,
                      style: GoogleFonts.dmSans(
                        color: t.textDim,
                        fontSize: 12,
                        letterSpacing: 0.3,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),

          const Spacer(),
        ],
      ),
    );
  }

  // ─── PAGE INDICATOR ───
  Widget _buildPageIndicator(_T t) {
    final isLast = _currentPage == _totalPages - 1;
    final pageColors = [
      _electricCoral,
      _tropicalTeal,
      _vividViolet,
      _goldenAmber,
    ];
    final activeColor = pageColors[_currentPage];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: List.generate(_totalPages, (i) {
              final isActive = i == _currentPage;
              return AnimatedContainer(
                duration: const Duration(milliseconds: 350),
                curve: Curves.easeOutCubic,
                margin: const EdgeInsets.only(right: 8),
                width: isActive ? 28 : 8,
                height: 8,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(4),
                  color: isActive
                      ? activeColor
                      : (t.isDark
                          ? Colors.white.withOpacity(0.12)
                          : Colors.black.withOpacity(0.1)),
                  boxShadow: isActive && t.isDark
                      ? [
                          BoxShadow(
                            color: activeColor.withOpacity(0.4),
                            blurRadius: 8,
                          ),
                        ]
                      : null,
                ),
              );
            }),
          ),
          if (!isLast)
            GestureDetector(
              onTap: () => _goToPage(_currentPage + 1),
              child: AnimatedBuilder(
                animation: _pulseController,
                builder: (context, _) {
                  final pulse = _pulseController.value;
                  return Container(
                    width: 54,
                    height: 54,
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
                              t.isDark
                                  ? (0.3 + pulse * 0.15)
                                  : (0.25 + pulse * 0.1)),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: const Icon(
                      Icons.arrow_forward_rounded,
                      color: Colors.white,
                      size: 22,
                    ),
                  );
                },
              ),
            )
          else
            const SizedBox(width: 54),
        ],
      ),
    );
  }

  // ─── GEOMETRIC ACCENT SHAPES ───
  List<Widget> _buildGeometricShapes(_T t) {
    final size = MediaQuery.of(context).size;
    final glowColor1 = _lerpPageColor([
      _electricCoral,
      _tropicalTeal,
      _vividViolet,
      _hotPink,
    ]);
    final shapeOpacity = t.isDark ? 1.0 : 0.6;

    return [
      // Top-right rounded square
      Positioned(
        right: -60,
        top: -60,
        child: AnimatedBuilder(
          animation: _floatController,
          builder: (context, _) {
            final drift =
                math.sin(_floatController.value * math.pi * 2) * 8;
            return Transform.translate(
              offset: Offset(drift, -drift * 0.5),
              child: Transform.rotate(
                angle: _floatController.value * 0.3,
                child: Container(
                  width: 180,
                  height: 180,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(30),
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        glowColor1.withOpacity(0.06 * shapeOpacity),
                        glowColor1.withOpacity(0.01),
                      ],
                    ),
                    border: Border.all(
                      color:
                          glowColor1.withOpacity(0.08 * shapeOpacity),
                      width: 1,
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
      // Bottom-left diamond
      Positioned(
        left: -40,
        bottom: size.height * 0.15,
        child: AnimatedBuilder(
          animation: _floatController,
          builder: (context, _) {
            final drift =
                math.cos(_floatController.value * math.pi * 2) * 10;
            return Transform.translate(
              offset: Offset(-drift * 0.5, drift),
              child: Transform.rotate(
                angle:
                    math.pi / 4 + _floatController.value * 0.2,
                child: Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: _tropicalTeal
                          .withOpacity(0.1 * shapeOpacity),
                      width: 1,
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
      // Small amber dot (mid-right)
      Positioned(
        right: 30,
        top: size.height * 0.4,
        child: AnimatedBuilder(
          animation: _pulseController,
          builder: (context, _) {
            final pulse = _pulseController.value;
            return Container(
              width: 12,
              height: 12,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: _goldenAmber
                    .withOpacity(0.3 + pulse * 0.2),
                boxShadow: t.isDark
                    ? [
                        BoxShadow(
                          color: _goldenAmber
                              .withOpacity(0.2 + pulse * 0.15),
                          blurRadius: 10,
                        ),
                      ]
                    : null,
              ),
            );
          },
        ),
      ),
      // Small coral dot (left)
      Positioned(
        left: 40,
        top: size.height * 0.55,
        child: AnimatedBuilder(
          animation: _pulseController,
          builder: (context, _) {
            final pulse = _pulseController.value;
            return Container(
              width: 8,
              height: 8,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: _electricCoral
                    .withOpacity(0.25 + pulse * 0.15),
              ),
            );
          },
        ),
      ),
    ];
  }

  // ─── FLOATING PARTICLES ───
  List<Widget> _buildParticles(_T t) {
    final size = MediaQuery.of(context).size;
    final random = math.Random(99);
    final colors = [
      _electricCoral,
      _tropicalTeal,
      _goldenAmber,
      _vividViolet,
      _skyBlue,
    ];
    final baseOpacity = t.isDark ? 1.0 : 0.7;

    return List.generate(12, (i) {
      final x = random.nextDouble();
      final y = random.nextDouble();
      final particleSize = 2.0 + random.nextDouble() * 3;
      final color = colors[i % colors.length];
      final phase = random.nextDouble() * math.pi * 2;

      return Positioned(
        left: x * size.width,
        top: y * size.height,
        child: AnimatedBuilder(
          animation: _floatController,
          builder: (context, _) {
            final tv = _floatController.value * math.pi * 2 + phase;
            final dx = math.sin(tv) * 5;
            final dy = math.cos(tv * 0.6) * 7;
            return Transform.translate(
              offset: Offset(dx, dy),
              child: Container(
                width: particleSize,
                height: particleSize,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: color.withOpacity(
                      (0.15 + math.sin(tv).abs() * 0.2) * baseOpacity),
                  boxShadow: t.isDark
                      ? [
                          BoxShadow(
                            color: color.withOpacity(0.1),
                            blurRadius: 6,
                          ),
                        ]
                      : null,
                ),
              ),
            );
          },
        ),
      );
    });
  }
}

// ─── LUXE BUTTON ───
class _LuxeButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final bool filled;
  final _T t;

  const _LuxeButton({
    required this.label,
    required this.onTap,
    required this.filled,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    if (filled) {
      return GestureDetector(
        onTap: onTap,
        child: Container(
          width: double.infinity,
          height: 56,
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [_electricCoral, _hotPink],
            ),
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: _electricCoral.withOpacity(t.isDark ? 0.35 : 0.25),
                blurRadius: 20,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: GoogleFonts.outfit(
              color: Colors.white,
              fontSize: 15,
              fontWeight: FontWeight.w700,
              letterSpacing: 2,
            ),
          ),
        ),
      );
    }

    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 400),
        width: double.infinity,
        height: 56,
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
            color: t.textPrimary,
            fontSize: 15,
            fontWeight: FontWeight.w600,
            letterSpacing: 2,
          ),
        ),
      ),
    );
  }
}

// ─── GRADIENT ORBS BACKGROUND ───
class _GradientOrbs extends StatelessWidget {
  final AnimationController controller;
  final Color color1;
  final Color color2;
  final bool isDark;

  const _GradientOrbs({
    required this.controller,
    required this.color1,
    required this.color2,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final phase = controller.value * math.pi * 2;
        final orbOpacity = isDark ? 0.12 : 0.08;
        final orb2Opacity = isDark ? 0.10 : 0.06;

        return Stack(
          children: [
            // Primary orb (top)
            Positioned(
              left: -80 + 60 * math.sin(phase),
              top: -100 + 40 * math.cos(phase),
              child: Container(
                width: 400,
                height: 400,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      color1.withOpacity(orbOpacity),
                      color1.withOpacity(0),
                    ],
                  ),
                ),
              ),
            ),
            // Secondary orb (bottom-right)
            Positioned(
              right: -120 + 50 * math.cos(phase * 0.7),
              bottom: -80 + 60 * math.sin(phase * 0.7),
              child: Container(
                width: 350,
                height: 350,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      color2.withOpacity(orb2Opacity),
                      color2.withOpacity(0),
                    ],
                  ),
                ),
              ),
            ),
            // Tertiary orb (center)
            Positioned(
              left: MediaQuery.of(context).size.width * 0.3 +
                  30 * math.sin(phase * 1.3),
              top: MediaQuery.of(context).size.height * 0.4 +
                  20 * math.cos(phase * 1.3),
              child: Container(
                width: 200,
                height: 200,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      _goldenAmber.withOpacity(isDark ? 0.04 : 0.06),
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

// ─── HEXAGONAL RING PAINTER ───
class _HexRingPainter extends CustomPainter {
  final Color color1;
  final Color color2;
  final double progress;

  _HexRingPainter({
    required this.color1,
    required this.color2,
    required this.progress,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    for (int i = 0; i < 6; i++) {
      final startAngle = i * math.pi / 3 + 0.1;
      final sweepAngle = math.pi / 3 - 0.2;
      final tv = i / 6.0;
      paint.color = Color.lerp(
        color1.withOpacity(0.25 + progress * 0.15),
        color2.withOpacity(0.25 + progress * 0.15),
        tv,
      )!;
      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        startAngle,
        sweepAngle,
        false,
        paint,
      );
    }

    final dotPaint = Paint()..style = PaintingStyle.fill;
    for (int i = 0; i < 6; i++) {
      final angle = i * math.pi / 3;
      dotPaint.color = Color.lerp(color1, color2, i / 6.0)!
          .withOpacity(0.4 + progress * 0.2);
      canvas.drawCircle(
        Offset(
          center.dx + radius * math.cos(angle),
          center.dy + radius * math.sin(angle),
        ),
        2.5,
        dotPaint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _HexRingPainter old) =>
      old.progress != progress;
}

// ─── GRID PATTERN PAINTER ───
class _GridPatternPainter extends CustomPainter {
  final Color color;
  _GridPatternPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 0.5;
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

// ─── DATA MODEL ───
class _SlideData {
  final IconData icon;
  final Color gradient1;
  final Color gradient2;
  final Color accentGlow;

  const _SlideData({
    required this.icon,
    required this.gradient1,
    required this.gradient2,
    required this.accentGlow,
  });
}
