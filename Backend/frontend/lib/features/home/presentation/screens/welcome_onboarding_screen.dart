import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

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

  int _currentPage = 0;

  static const _totalPages = 4;

  final _slides = const [
    _SlideData(
      icon: Icons.auto_awesome_rounded,
      gradient1: AppColors.coral,
      gradient2: AppColors.hotPink,
    ),
    _SlideData(
      icon: Icons.storefront_rounded,
      gradient1: AppColors.teal,
      gradient2: AppColors.sky,
    ),
    _SlideData(
      icon: Icons.insights_rounded,
      gradient1: AppColors.violet,
      gradient2: AppColors.hotPink,
    ),
  ];

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
    _entranceController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 800),
    )..forward();
  }

  @override
  void dispose() {
    _pageController.dispose();
    _entranceController.dispose();
    super.dispose();
  }

  void _goToPage(int page) => _pageController.animateToPage(
    page,
    duration: const Duration(milliseconds: 500),
    curve: Curves.easeInOutCubic,
  );

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 400),
      color: t.base,
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            // Layer 1 — Subtle gradient background
            _buildGradientBackground(t),
            // Layer 2 — Main content
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

  Widget _buildGradientBackground(RfTheme t) {
    final pageColors = [
      [AppColors.coral, AppColors.hotPink],
      [AppColors.teal, AppColors.sky],
      [AppColors.violet, AppColors.hotPink],
      [AppColors.hotPink, AppColors.amber],
    ];
    final colors = _currentPage < pageColors.length
        ? pageColors[_currentPage]
        : pageColors.last;

    return Positioned.fill(
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 600),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              colors[0].withOpacity(t.isDark ? 0.15 : 0.08),
              colors[1].withOpacity(t.isDark ? 0.08 : 0.05),
            ],
          ),
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
          // Simple icon with gradient background
          Container(
            width: 160, height: 160,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [slide.gradient1, slide.gradient2],
              ),
              boxShadow: [
                BoxShadow(
                  color: slide.gradient1.withOpacity(0.3),
                  blurRadius: 30,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: Icon(slide.icon, size: 64, color: Colors.white),
          ),
          const SizedBox(height: 40),
          // Title card
          Container(
            padding: const EdgeInsets.fromLTRB(20, 24, 20, 24),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              color: t.isDark
                  ? Colors.black.withOpacity(0.18)
                  : Colors.white.withOpacity(0.93),
              border: Border.all(color: t.borderFaint),
            ),
            child: Column(
              children: [
                Text(
                  titles[index],
                  style: GoogleFonts.outfit(
                    color: t.textPrimary, fontSize: 28,
                    fontWeight: FontWeight.w800, height: 1.15, letterSpacing: -0.5,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                Container(
                  width: 48, height: 3,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(2),
                    gradient: LinearGradient(colors: [slide.gradient1, slide.gradient2]),
                  ),
                ),
                const SizedBox(height: 16),
                Text(
                  descs[index],
                  style: GoogleFonts.dmSans(
                    color: t.textMuted, fontSize: 15,
                    fontWeight: FontWeight.w400, height: 1.6, letterSpacing: 0.2,
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
          const Spacer(flex: 3),
        ],
      ),
    );
  }

  // ─── AUTH PAGE ───
  Widget _buildAuthPage(RfTheme t) {
    final l10n = AppLocalizations.of(context)!;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 28),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const SizedBox(height: 24),
          // Simple logo
          Container(
            width: 100, height: 100,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: AppColors.coral.withOpacity(0.2),
                  blurRadius: 30,
                  spreadRadius: 5,
                ),
              ],
            ),
            child: ClipOval(
              child: Image.asset(
                'assets/images/logo_rosafiesta.png',
                width: 100, height: 100,
                fit: BoxFit.cover,
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
          const SizedBox(height: 24),
          // Brand name
          ShaderMask(
            shaderCallback: (b) => const LinearGradient(
              colors: [AppColors.hotPink, AppColors.amber, AppColors.teal],
            ).createShader(b),
            child: Text(
              'Rosa Fiesta',
              style: GoogleFonts.outfit(
                color: Colors.white, fontSize: 36,
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
          const SizedBox(height: 32),
          // Auth buttons
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
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Dots
            ...List.generate(_totalPages, (i) {
              final isActive = i == _currentPage;
              return AnimatedContainer(
                duration: const Duration(milliseconds: 300),
                margin: const EdgeInsets.only(right: 8),
                width: isActive ? 24 : 8, height: 8,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(4),
                  color: isActive
                      ? activeColor
                      : (t.isDark
                          ? Colors.white.withOpacity(0.12)
                          : Colors.black.withOpacity(0.1)),
                ),
              );
            }),
            // Next button
            if (!isLast) ...[
              const SizedBox(width: 16),
              GestureDetector(
                onTap: () => _goToPage(_currentPage + 1),
                child: Container(
                  width: 44, height: 44,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: activeColor,
                    boxShadow: [
                      BoxShadow(
                        color: activeColor.withOpacity(0.3),
                        blurRadius: 12, offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.arrow_forward_rounded, color: Colors.white, size: 20,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _SlideData {
  final IconData icon;
  final Color gradient1;
  final Color gradient2;

  const _SlideData({
    required this.icon, required this.gradient1,
    required this.gradient2,
  });
}
