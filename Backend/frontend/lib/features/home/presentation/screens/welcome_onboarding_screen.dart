import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_theme.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';

// Petal Noir palette — file-level so extracted widgets can use them
const _noir = Color(0xFF0D0B14);
const _deepPlum = Color(0xFF1A0F2E);
const _warmRose = Color(0xFFE8446D);
const _softGold = Color(0xFFD4A853);
const _petalPink = Color(0xFFF2A4B8);
const _ivoryWhite = Color(0xFFFAF5F0);

/// Rosa Fiesta Welcome & Onboarding — "Petal Noir" aesthetic
/// Dark luxury with organic floral accents, cinematic typography,
/// and staggered reveal animations.
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
  late final AnimationController _ambientController;
  late final AnimationController _glowController;
  int _currentPage = 0;

  static const _totalPages = 4;

  final _onboardingData = const [
    _OnboardingSlide(
      icon: Icons.auto_awesome_rounded,
      accentColor: _softGold,
      glowColor: Color(0x40D4A853),
    ),
    _OnboardingSlide(
      icon: Icons.storefront_rounded,
      accentColor: _warmRose,
      glowColor: Color(0x40E8446D),
    ),
    _OnboardingSlide(
      icon: Icons.insights_rounded,
      accentColor: _petalPink,
      glowColor: Color(0x40F2A4B8),
    ),
  ];

  @override
  void initState() {
    super.initState();
    _pageController = PageController();

    _entranceController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1600),
    )..forward();

    _ambientController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 8),
    )..repeat();

    _glowController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _pageController.dispose();
    _entranceController.dispose();
    _ambientController.dispose();
    _glowController.dispose();
    super.dispose();
  }

  void _goToPage(int page) {
    _pageController.animateToPage(
      page,
      duration: const Duration(milliseconds: 600),
      curve: Curves.easeInOutCubic,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _noir,
      body: Stack(
        children: [
          // Layer 1 — Ambient gradient that breathes
          _AmbientBackground(controller: _ambientController),

          // Layer 2 — Floating petal shapes
          ..._buildPetalShapes(),

          // Layer 3 — Grain overlay for texture
          Positioned.fill(
            child: IgnorePointer(
              child: CustomPaint(painter: _GrainPainter()),
            ),
          ),

          // Layer 4 — Main content
          SafeArea(
            child: Column(
              children: [
                _buildTopBar(),
                Expanded(
                  child: PageView(
                    controller: _pageController,
                    onPageChanged: (i) => setState(() => _currentPage = i),
                    children: [
                      ..._onboardingData.asMap().entries.map((entry) =>
                          _buildOnboardingPage(entry.key, entry.value)),
                      _buildAuthPage(),
                    ],
                  ),
                ),
                _buildPageIndicator(),
                const SizedBox(height: 32),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ---------- TOP BAR ----------
  Widget _buildTopBar() {
    final l10n = AppLocalizations.of(context)!;

    return SlideTransition(
      position: Tween<Offset>(
        begin: const Offset(0, -1),
        end: Offset.zero,
      ).animate(CurvedAnimation(
        parent: _entranceController,
        curve: const Interval(0.0, 0.4, curve: Curves.easeOutCubic),
      )),
      child: FadeTransition(
        opacity: CurvedAnimation(
          parent: _entranceController,
          curve: const Interval(0.0, 0.3),
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
          child: Row(
            children: [
              // Logo mark
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: _softGold.withOpacity(0.4),
                    width: 1.5,
                  ),
                ),
                child: ClipOval(
                  child: Image.asset(
                    'assets/images/logo_rosafiesta.png',
                    errorBuilder: (_, __, ___) => Container(
                      color: _deepPlum,
                      child: const Icon(
                        Icons.local_florist,
                        size: 18,
                        color: _softGold,
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Text(
                'ROSA FIESTA',
                style: GoogleFonts.cormorantGaramond(
                  color: _ivoryWhite.withOpacity(0.9),
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  letterSpacing: 3,
                ),
              ),
              const Spacer(),
              if (_currentPage < _totalPages - 1)
                GestureDetector(
                  onTap: () => _goToPage(_totalPages - 1),
                  child: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: _ivoryWhite.withOpacity(0.15),
                      ),
                    ),
                    child: Text(
                      l10n.skipButton,
                      style: GoogleFonts.plusJakartaSans(
                        color: _ivoryWhite.withOpacity(0.5),
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

  // ---------- ONBOARDING PAGES ----------
  Widget _buildOnboardingPage(int index, _OnboardingSlide slide) {
    final l10n = AppLocalizations.of(context)!;
    final titles = [l10n.onboardingTitle1, l10n.onboardingTitle2, l10n.onboardingTitle3];
    final descs = [l10n.onboardingDesc1, l10n.onboardingDesc2, l10n.onboardingDesc3];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 28),
      child: Column(
        children: [
          const Spacer(flex: 3),

          // Glowing icon orb
          AnimatedBuilder(
            animation: _glowController,
            builder: (context, child) {
              final glowSize = 160 + _glowController.value * 20;
              return Container(
                width: glowSize,
                height: glowSize,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: slide.glowColor,
                      blurRadius: 60 + _glowController.value * 30,
                      spreadRadius: 10,
                    ),
                  ],
                ),
                child: Center(
                  child: Container(
                    width: 120,
                    height: 120,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [
                          slide.accentColor,
                          slide.accentColor.withOpacity(0.6),
                        ],
                      ),
                      border: Border.all(
                        color: _ivoryWhite.withOpacity(0.2),
                        width: 1,
                      ),
                    ),
                    child: Icon(
                      slide.icon,
                      size: 48,
                      color: _noir.withOpacity(0.85),
                    ),
                  ),
                ),
              );
            },
          ),

          const SizedBox(height: 56),

          // Title — large editorial typography
          Text(
            titles[index],
            textAlign: TextAlign.center,
            style: GoogleFonts.cormorantGaramond(
              color: _ivoryWhite,
              fontSize: 38,
              fontWeight: FontWeight.w700,
              height: 1.15,
              letterSpacing: -0.5,
            ),
          ),

          const SizedBox(height: 20),

          // Decorative divider
          Container(
            width: 40,
            height: 1.5,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.transparent,
                  slide.accentColor,
                  Colors.transparent,
                ],
              ),
            ),
          ),

          const SizedBox(height: 20),

          // Description
          Text(
            descs[index],
            textAlign: TextAlign.center,
            style: GoogleFonts.plusJakartaSans(
              color: _ivoryWhite.withOpacity(0.55),
              fontSize: 15,
              fontWeight: FontWeight.w400,
              height: 1.6,
              letterSpacing: 0.2,
            ),
          ),

          const Spacer(flex: 4),
        ],
      ),
    );
  }

  // ---------- AUTH PAGE ----------
  Widget _buildAuthPage() {
    final l10n = AppLocalizations.of(context)!;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        children: [
          const Spacer(flex: 2),

          // Logo with radial glow
          AnimatedBuilder(
            animation: _glowController,
            builder: (context, _) {
              return Container(
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: _softGold.withOpacity(0.15 + _glowController.value * 0.1),
                      blurRadius: 80,
                      spreadRadius: 20,
                    ),
                  ],
                ),
                child: Container(
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: _ivoryWhite,
                    border: Border.all(
                      color: _softGold.withOpacity(0.3),
                      width: 2,
                    ),
                  ),
                  child: Image.asset(
                    'assets/images/logo_rosafiesta.png',
                    height: 64,
                    errorBuilder: (_, __, ___) => const Icon(
                      Icons.local_florist,
                      size: 64,
                      color: AppColors.primary,
                    ),
                  ),
                ),
              );
            },
          ),

          const SizedBox(height: 32),

          // Brand name — cinematic
          Text(
            'Rosa Fiesta',
            style: GoogleFonts.cormorantGaramond(
              color: _ivoryWhite,
              fontSize: 46,
              fontWeight: FontWeight.w700,
              letterSpacing: 1,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            l10n.welcomeSubheadline.toUpperCase(),
            style: GoogleFonts.plusJakartaSans(
              color: _softGold.withOpacity(0.7),
              fontSize: 11,
              fontWeight: FontWeight.w500,
              letterSpacing: 3,
            ),
          ),

          const Spacer(flex: 2),

          // Auth card — frosted glass with gold border accent
          ClipRRect(
            borderRadius: BorderRadius.circular(28),
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
              child: Container(
                padding: const EdgeInsets.all(28),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.06),
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(
                    color: _softGold.withOpacity(0.15),
                    width: 1,
                  ),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Primary CTA — Get Started
                    _LuxuryButton(
                      label: l10n.beginButton,
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const RegisterScreen(),
                        ),
                      ),
                      filled: true,
                    ),
                    const SizedBox(height: 14),
                    // Secondary CTA — Login
                    _LuxuryButton(
                      label: l10n.loginButton,
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const LoginScreen(),
                        ),
                      ),
                      filled: false,
                    ),
                    const SizedBox(height: 18),
                    Text(
                      l10n.alreadyHaveAccount,
                      style: GoogleFonts.plusJakartaSans(
                        color: _ivoryWhite.withOpacity(0.35),
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

  // ---------- PAGE INDICATOR ----------
  Widget _buildPageIndicator() {
    final isLast = _currentPage == _totalPages - 1;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 28),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Petal-shaped dots
          Row(
            children: List.generate(_totalPages, (i) {
              final isActive = i == _currentPage;
              return AnimatedContainer(
                duration: const Duration(milliseconds: 400),
                curve: Curves.easeOutCubic,
                margin: const EdgeInsets.only(right: 6),
                width: isActive ? 32 : 8,
                height: 8,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(4),
                  color: isActive
                      ? _softGold
                      : _ivoryWhite.withOpacity(0.15),
                ),
              );
            }),
          ),

          // Next arrow
          if (!isLast)
            GestureDetector(
              onTap: () => _goToPage(_currentPage + 1),
              child: AnimatedBuilder(
                animation: _glowController,
                builder: (context, _) {
                  return Container(
                    width: 52,
                    height: 52,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [
                          _softGold,
                          _warmRose.withOpacity(0.8),
                        ],
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: _softGold.withOpacity(
                              0.2 + _glowController.value * 0.15),
                          blurRadius: 20,
                          offset: const Offset(0, 6),
                        ),
                      ],
                    ),
                    child: const Icon(
                      Icons.arrow_forward_rounded,
                      color: _noir,
                      size: 22,
                    ),
                  );
                },
              ),
            )
          else
            const SizedBox(width: 52),
        ],
      ),
    );
  }

  // ---------- FLOATING PETAL SHAPES ----------
  List<Widget> _buildPetalShapes() {
    final size = MediaQuery.of(context).size;
    final petals = <_PetalData>[
      _PetalData(0.85, 0.12, 180, _warmRose.withOpacity(0.04), 0),
      _PetalData(0.1, 0.25, 140, _softGold.withOpacity(0.03), 45),
      _PetalData(0.7, 0.75, 120, _petalPink.withOpacity(0.04), 120),
      _PetalData(0.05, 0.8, 100, _warmRose.withOpacity(0.03), 200),
      _PetalData(0.9, 0.55, 90, _softGold.withOpacity(0.025), 70),
    ];

    return petals.map((p) {
      return Positioned(
        left: p.x * size.width - p.size / 2,
        top: p.y * size.height - p.size / 2,
        child: AnimatedBuilder(
          animation: _ambientController,
          builder: (context, _) {
            final drift = math.sin(
                    _ambientController.value * math.pi * 2 +
                        p.rotationOffset * math.pi / 180) *
                10;
            return Transform.translate(
              offset: Offset(drift * 0.6, drift),
              child: Transform.rotate(
                angle: p.rotationOffset * math.pi / 180 +
                    _ambientController.value * 0.3,
                child: Container(
                  width: p.size,
                  height: p.size * 1.4,
                  decoration: BoxDecoration(
                    color: p.color,
                    borderRadius: BorderRadius.only(
                      topLeft: Radius.circular(p.size * 0.7),
                      topRight: Radius.circular(p.size * 0.3),
                      bottomLeft: Radius.circular(p.size * 0.3),
                      bottomRight: Radius.circular(p.size * 0.7),
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      );
    }).toList();
  }
}

// ---------- LUXURY BUTTON ----------
class _LuxuryButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final bool filled;

  const _LuxuryButton({
    required this.label,
    required this.onTap,
    required this.filled,
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
              colors: [
                _softGold,
                Color(0xFFBF923A),
              ],
            ),
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: _softGold.withOpacity(0.25),
                blurRadius: 20,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: GoogleFonts.plusJakartaSans(
              color: _noir,
              fontSize: 14,
              fontWeight: FontWeight.w700,
              letterSpacing: 2,
            ),
          ),
        ),
      );
    }

    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: double.infinity,
        height: 56,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: _ivoryWhite.withOpacity(0.2),
            width: 1.5,
          ),
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: GoogleFonts.plusJakartaSans(
            color: _ivoryWhite.withOpacity(0.8),
            fontSize: 14,
            fontWeight: FontWeight.w600,
            letterSpacing: 2,
          ),
        ),
      ),
    );
  }
}

// ---------- AMBIENT BACKGROUND ----------
class _AmbientBackground extends StatelessWidget {
  final AnimationController controller;
  const _AmbientBackground({required this.controller});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final phase = controller.value * math.pi * 2;
        return Container(
          decoration: BoxDecoration(
            gradient: RadialGradient(
              center: Alignment(
                -0.3 + 0.6 * math.sin(phase),
                -0.5 + 0.4 * math.cos(phase),
              ),
              radius: 1.8,
              colors: [
                _deepPlum,
                _noir,
              ],
            ),
          ),
        );
      },
    );
  }
}

// ---------- GRAIN TEXTURE ----------
class _GrainPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final random = math.Random(0);
    final paint = Paint()
      ..color = Colors.white.withOpacity(0.012)
      ..style = PaintingStyle.fill;

    for (int i = 0; i < 800; i++) {
      final x = random.nextDouble() * size.width;
      final y = random.nextDouble() * size.height;
      canvas.drawCircle(Offset(x, y), 0.5 + random.nextDouble() * 0.5, paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

// ---------- DATA MODELS ----------
class _OnboardingSlide {
  final IconData icon;
  final Color accentColor;
  final Color glowColor;

  const _OnboardingSlide({
    required this.icon,
    required this.accentColor,
    required this.glowColor,
  });
}

class _PetalData {
  final double x;
  final double y;
  final double size;
  final Color color;
  final double rotationOffset;

  const _PetalData(this.x, this.y, this.size, this.color, this.rotationOffset);
}
