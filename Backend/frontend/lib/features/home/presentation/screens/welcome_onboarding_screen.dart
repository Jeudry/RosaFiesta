import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/design_system.dart';

// ═══════════════════════════════════════════════════════════════════════════
// VF PALETTE — Art Deco Velvet / Grand Ledger
// Completely separate from the shared AppColors — this screen owns its
// own monochromatic gold-on-noir / cognac-on-parchment language.
// ═══════════════════════════════════════════════════════════════════════════

class _VfC {
  // Dark
  static const dkBg      = Color(0xFF0A0709);
  static const dkSurface = Color(0xFF160C13);
  static const dkCard    = Color(0xFF1F1219);
  static const dkGold    = Color(0xFFC9A356);
  static const dkGoldDim = Color(0xFF896E2A);
  static const dkIvory   = Color(0xFFF0E8DF);
  static const dkMuted   = Color(0xFFB8A89C);
  static const dkDim     = Color(0xFF7A6A60);
  static const dkBorder  = Color(0x28C9A356);

  // Light
  static const ltBg      = Color(0xFFF5EDE0);
  static const ltSurface = Color(0xFFEDE0CF);
  static const ltCard    = Color(0xFFFAF4EC);
  static const ltInk     = Color(0xFF1C0F12);
  static const ltMuted   = Color(0xFF5C4040);
  static const ltDim     = Color(0xFF9C7A70);
  static const ltCognac  = Color(0xFF7A5210);
  static const ltBorder  = Color(0x357A5210);
}

class _VfT {
  final Color bg, surface, card, text, textMuted, textDim, border, accent;
  final bool isDark;

  const _VfT({
    required this.bg,
    required this.surface,
    required this.card,
    required this.text,
    required this.textMuted,
    required this.textDim,
    required this.border,
    required this.accent,
    required this.isDark,
  });

  static const dark = _VfT(
    bg: _VfC.dkBg, surface: _VfC.dkSurface, card: _VfC.dkCard,
    text: _VfC.dkIvory, textMuted: _VfC.dkMuted, textDim: _VfC.dkDim,
    border: _VfC.dkBorder, accent: _VfC.dkGold, isDark: true,
  );

  static const light = _VfT(
    bg: _VfC.ltBg, surface: _VfC.ltSurface, card: _VfC.ltCard,
    text: _VfC.ltInk, textMuted: _VfC.ltMuted, textDim: _VfC.ltDim,
    border: _VfC.ltBorder, accent: _VfC.ltCognac, isDark: false,
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// SCREEN
// ═══════════════════════════════════════════════════════════════════════════

/// Rosa Fiesta Welcome & Onboarding — "Grand Ledger" (Art Deco Velvet)
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
  late final AnimationController _rotateController;
  late final AnimationController _shimmerController;

  int    _currentPage = 0;
  double _pageOffset  = 0.0;
  static const _totalPages = 4;

  @override
  void initState() {
    super.initState();
    _pageController = PageController()
      ..addListener(() {
        setState(() => _pageOffset = _pageController.page ?? 0.0);
      });
    _entranceController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 1400),
    )..forward();
    _rotateController = AnimationController(
      vsync: this, duration: const Duration(seconds: 40),
    )..repeat();
    _shimmerController = AnimationController(
      vsync: this, duration: const Duration(seconds: 3),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _pageController.dispose();
    _entranceController.dispose();
    _rotateController.dispose();
    _shimmerController.dispose();
    super.dispose();
  }

  void _goToPage(int page) => _pageController.animateToPage(
    page,
    duration: const Duration(milliseconds: 650),
    curve: Curves.easeInOutQuart,
  );

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? _VfT.dark : _VfT.light;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 500),
      color: t.bg,
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            // Layer 1 — Art Deco diamond tile background
            Positioned.fill(
              child: IgnorePointer(
                child: CustomPaint(
                  painter: _DiamondTilePainter(
                    color: t.isDark
                        ? _VfC.dkGold.withOpacity(0.03)
                        : _VfC.ltCognac.withOpacity(0.055),
                  ),
                ),
              ),
            ),
            // Layer 2 — Corner ornaments (static)
            Positioned.fill(
              child: IgnorePointer(
                child: CustomPaint(
                  painter: _CornerOrnamentPainter(color: t.accent),
                ),
              ),
            ),
            // Layer 3 — Content
            SafeArea(
              child: Column(
                children: [
                  _buildTopBar(t),
                  Expanded(
                    child: PageView(
                      controller: _pageController,
                      onPageChanged: (i) => setState(() => _currentPage = i),
                      children: [
                        _buildSlide(0, t),
                        _buildSlide(1, t),
                        _buildSlide(2, t),
                        _buildAuthPage(t),
                      ],
                    ),
                  ),
                  _buildBottomBar(t),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ─── TOP BAR ─────────────────────────────────────────────────────────────

  Widget _buildTopBar(_VfT t) {
    final l10n = AppLocalizations.of(context)!;
    return FadeTransition(
      opacity: CurvedAnimation(
        parent: _entranceController,
        curve: const Interval(0.0, 0.45),
      ),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(28, 18, 28, 0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Brand mark
            Text(
              'RF',
              style: GoogleFonts.cormorantGaramond(
                color: t.accent, fontSize: 20,
                fontWeight: FontWeight.w600, letterSpacing: 4,
              ),
            ),
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 10),
              width: 1, height: 14,
              color: t.accent.withOpacity(0.35),
            ),
            Text(
              'ROSA FIESTA',
              style: GoogleFonts.raleway(
                color: t.textDim, fontSize: 8,
                fontWeight: FontWeight.w700, letterSpacing: 3.5,
              ),
            ),
            const Spacer(),
            // Theme toggle — minimal circular icon button
            GestureDetector(
              onTap: () => context.read<ThemeProvider>().toggle(),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 400),
                width: 34, height: 34,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(color: t.accent.withOpacity(0.4), width: 1),
                  color: t.isDark
                      ? _VfC.dkGold.withOpacity(0.06)
                      : _VfC.ltCognac.withOpacity(0.05),
                ),
                child: Icon(
                  t.isDark ? Icons.light_mode_outlined : Icons.dark_mode_outlined,
                  size: 15, color: t.accent,
                ),
              ),
            ),
            if (_currentPage < _totalPages - 1) ...[
              const SizedBox(width: 14),
              GestureDetector(
                onTap: () => _goToPage(_totalPages - 1),
                child: Text(
                  l10n.skipButton.toUpperCase(),
                  style: GoogleFonts.raleway(
                    color: t.textDim, fontSize: 8,
                    fontWeight: FontWeight.w700, letterSpacing: 2.5,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  // ─── ONBOARDING SLIDE ────────────────────────────────────────────────────

  Widget _buildSlide(int index, _VfT t) {
    final l10n = AppLocalizations.of(context)!;

    const slides = [
      _SlideData(numeral: 'I',   motif: _MotifType.starburst),
      _SlideData(numeral: 'II',  motif: _MotifType.rosette),
      _SlideData(numeral: 'III', motif: _MotifType.crosshatch),
    ];
    final slide = slides[index];
    final titles = [l10n.onboardingTitle1, l10n.onboardingTitle2, l10n.onboardingTitle3];
    final descs  = [l10n.onboardingDesc1,  l10n.onboardingDesc2,  l10n.onboardingDesc3];

    // Parallax: elements shift at different rates while swiping
    final rawShift   = (_pageOffset - index) * 40.0;
    final titleShift = rawShift.clamp(-50.0, 50.0);
    final numeralShift = (rawShift * 0.6).clamp(-40.0, 40.0);
    final motifShift   = (rawShift * 0.25).clamp(-25.0, 25.0);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Spacer(flex: 1),

          // Large background numeral — decorative, very low opacity
          FadeTransition(
            opacity: CurvedAnimation(
              parent: _entranceController,
              curve: const Interval(0.05, 0.5),
            ),
            child: Transform.translate(
              offset: Offset(numeralShift, 0),
              child: Text(
                slide.numeral,
                style: GoogleFonts.cormorantGaramond(
                  color: t.accent.withOpacity(0.07),
                  fontSize: 148, fontWeight: FontWeight.w300, height: 1.0,
                ),
              ),
            ),
          ),

          // Geometric motif
          Center(
            child: Transform.translate(
              offset: Offset(motifShift, 0),
              child: AnimatedBuilder(
                animation: Listenable.merge([_rotateController, _shimmerController]),
                builder: (_, __) => SizedBox(
                  width: 190, height: 190,
                  child: CustomPaint(
                    painter: _ArtDecoMotifPainter(
                      motif: slide.motif,
                      color: t.accent,
                      rotate: _rotateController.value,
                      shimmer: _shimmerController.value,
                    ),
                  ),
                ),
              ),
            ),
          ),

          const SizedBox(height: 36),

          // Text — left-aligned editorial style
          FadeTransition(
            opacity: CurvedAnimation(
              parent: _entranceController,
              curve: const Interval(0.3, 0.75),
            ),
            child: Transform.translate(
              offset: Offset(titleShift, 0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Thin rule + Roman numeral label
                  Row(
                    children: [
                      Container(width: 24, height: 1, color: t.accent.withOpacity(0.55)),
                      const SizedBox(width: 10),
                      Text(
                        '${slide.numeral} / III',
                        style: GoogleFonts.raleway(
                          color: t.accent, fontSize: 9,
                          fontWeight: FontWeight.w700, letterSpacing: 3,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 18),
                  Text(
                    titles[index],
                    style: GoogleFonts.cormorantGaramond(
                      color: t.text, fontSize: 40,
                      fontWeight: FontWeight.w400, height: 1.15, letterSpacing: -0.2,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    descs[index],
                    style: GoogleFonts.raleway(
                      color: t.textMuted, fontSize: 13.5,
                      fontWeight: FontWeight.w400, height: 1.75, letterSpacing: 0.1,
                    ),
                  ),
                ],
              ),
            ),
          ),

          const Spacer(flex: 2),
        ],
      ),
    );
  }

  // ─── AUTH PAGE ────────────────────────────────────────────────────────────

  Widget _buildAuthPage(_VfT t) {
    final l10n = AppLocalizations.of(context)!;
    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: ConstrainedBox(
        constraints: BoxConstraints(
          minHeight: MediaQuery.of(context).size.height -
              MediaQuery.of(context).padding.top - 120,
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const SizedBox(height: 16),

            // Logo surrounded by mandala motif
            AnimatedBuilder(
              animation: Listenable.merge([_rotateController, _shimmerController]),
              builder: (_, __) => SizedBox(
                width: 200, height: 200,
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    CustomPaint(
                      size: const Size(200, 200),
                      painter: _ArtDecoMotifPainter(
                        motif: _MotifType.mandala,
                        color: t.accent,
                        rotate: _rotateController.value,
                        shimmer: _shimmerController.value,
                      ),
                    ),
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 400),
                      width: 84, height: 84,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: Colors.white,
                        border: Border.all(color: t.accent.withOpacity(0.5), width: 1.5),
                        boxShadow: [
                          BoxShadow(
                            color: t.accent.withOpacity(0.12),
                            blurRadius: 24, spreadRadius: 2,
                          ),
                        ],
                      ),
                      child: ClipOval(
                        child: Image.asset(
                          'assets/images/logo_rosafiesta.png',
                          width: 84, height: 84, fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Icon(
                            Icons.local_florist, size: 38, color: t.accent,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Brand name
            Text(
              'ROSA FIESTA',
              style: GoogleFonts.cormorantGaramond(
                color: t.accent, fontSize: 34,
                fontWeight: FontWeight.w500, letterSpacing: 7,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              l10n.welcomeSubheadline.toUpperCase(),
              style: GoogleFonts.raleway(
                color: t.textDim, fontSize: 8,
                fontWeight: FontWeight.w700, letterSpacing: 3.5,
              ),
            ),

            const SizedBox(height: 28),

            // Ornamental divider
            Row(children: [
              Expanded(child: Container(height: 0.5, color: t.accent.withOpacity(0.3))),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 14),
                child: Text('◆', style: TextStyle(color: t.accent, fontSize: 8)),
              ),
              Expanded(child: Container(height: 0.5, color: t.accent.withOpacity(0.3))),
            ]),

            const SizedBox(height: 28),

            // CTA buttons
            _VfButton(
              label: l10n.beginButton.toUpperCase(),
              onTap: () => Navigator.push(context,
                  MaterialPageRoute(builder: (_) => const RegisterScreen())),
              t: t, filled: true,
            ),
            const SizedBox(height: 12),
            _VfButton(
              label: l10n.loginButton.toUpperCase(),
              onTap: () => Navigator.push(context,
                  MaterialPageRoute(builder: (_) => const LoginScreen())),
              t: t, filled: false,
            ),
            const SizedBox(height: 20),
            Text(
              l10n.alreadyHaveAccount,
              style: GoogleFonts.raleway(
                color: t.textDim, fontSize: 11, letterSpacing: 0.4,
              ),
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  // ─── BOTTOM BAR ──────────────────────────────────────────────────────────

  Widget _buildBottomBar(_VfT t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(28, 0, 28, 32),
      child: Row(
        children: [
          // Thin-line progress indicator
          Row(
            mainAxisSize: MainAxisSize.min,
            children: List.generate(_totalPages, (i) {
              final active = i == _currentPage;
              final past   = i < _currentPage;
              return Padding(
                padding: const EdgeInsets.only(right: 7),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 400),
                  curve: Curves.easeOutCubic,
                  width: active ? 34 : 12,
                  height: 1.5,
                  color: (active || past)
                      ? t.accent
                      : t.isDark
                          ? Colors.white.withOpacity(0.1)
                          : Colors.black.withOpacity(0.1),
                ),
              );
            }),
          ),
          const Spacer(),
          // Next button — square, no fill
          if (_currentPage < _totalPages - 1)
            GestureDetector(
              onTap: () => _goToPage(_currentPage + 1),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 350),
                width: 46, height: 46,
                decoration: BoxDecoration(
                  border: Border.all(color: t.accent.withOpacity(0.6), width: 1),
                  color: t.isDark
                      ? _VfC.dkGold.withOpacity(0.07)
                      : _VfC.ltCognac.withOpacity(0.05),
                ),
                child: Icon(Icons.arrow_forward, color: t.accent, size: 17),
              ),
            ),
        ],
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// BUTTON
// ═══════════════════════════════════════════════════════════════════════════

class _VfButton extends StatefulWidget {
  final String label;
  final VoidCallback onTap;
  final _VfT t;
  final bool filled;

  const _VfButton({
    required this.label, required this.onTap,
    required this.t, required this.filled,
  });

  @override
  State<_VfButton> createState() => _VfButtonState();
}

class _VfButtonState extends State<_VfButton> {
  bool _pressed = false;

  @override
  Widget build(BuildContext context) {
    final t = widget.t;
    return GestureDetector(
      onTapDown: (_) => setState(() => _pressed = true),
      onTapUp:   (_) { setState(() => _pressed = false); widget.onTap(); },
      onTapCancel: () => setState(() => _pressed = false),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        width: double.infinity, height: 52,
        transform: Matrix4.identity()..scale(_pressed ? 0.98 : 1.0),
        transformAlignment: Alignment.center,
        decoration: BoxDecoration(
          color: widget.filled
              ? t.accent.withOpacity(_pressed ? 0.85 : 1.0)
              : Colors.transparent,
          border: Border.all(
            color: widget.filled
                ? t.accent.withOpacity(_pressed ? 0.7 : 1.0)
                : t.accent.withOpacity(0.55),
            width: 1,
          ),
        ),
        child: Center(
          child: Text(
            widget.label,
            style: GoogleFonts.raleway(
              color: widget.filled
                  ? (t.isDark ? _VfC.dkBg : Colors.white)
                  : t.accent,
              fontSize: 10, fontWeight: FontWeight.w800, letterSpacing: 3.5,
            ),
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// DIAMOND TILE BACKGROUND PAINTER
// ═══════════════════════════════════════════════════════════════════════════

class _DiamondTilePainter extends CustomPainter {
  final Color color;
  _DiamondTilePainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final p = Paint()
      ..color = color
      ..strokeWidth = 0.6
      ..style = PaintingStyle.stroke;

    const step = 58.0;
    const half = step / 2;

    for (double x = -half; x < size.width + half; x += step) {
      for (double y = -half; y < size.height + half; y += step) {
        final path = Path()
          ..moveTo(x,        y - half)
          ..lineTo(x + half, y)
          ..lineTo(x,        y + half)
          ..lineTo(x - half, y)
          ..close();
        canvas.drawPath(path, p);
      }
    }
  }

  @override
  bool shouldRepaint(covariant _DiamondTilePainter old) => old.color != color;
}

// ═══════════════════════════════════════════════════════════════════════════
// CORNER ORNAMENT PAINTER
// ═══════════════════════════════════════════════════════════════════════════

class _CornerOrnamentPainter extends CustomPainter {
  final Color color;
  _CornerOrnamentPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final linePaint = Paint()
      ..color = color.withOpacity(0.35)
      ..strokeWidth = 0.8
      ..style = PaintingStyle.stroke;
    final dotPaint = Paint()
      ..color = color.withOpacity(0.5)
      ..style = PaintingStyle.fill;

    const margin = 22.0;
    const len    = 44.0;

    // Draw corner bracket at (cx, cy) with x/y direction signs
    void corner(double cx, double cy, double dx, double dy) {
      // L-bracket
      canvas.drawLine(Offset(cx, cy), Offset(cx + dx * len, cy), linePaint);
      canvas.drawLine(Offset(cx, cy), Offset(cx, cy + dy * len), linePaint);
      // Short inner tick
      canvas.drawLine(
        Offset(cx + dx * len * 0.35, cy + dy * 4),
        Offset(cx + dx * len * 0.35, cy - dy * 4),
        linePaint,
      );
      canvas.drawLine(
        Offset(cx + dx * 4, cy + dy * len * 0.35),
        Offset(cx - dx * 4, cy + dy * len * 0.35),
        linePaint,
      );
      // Corner dot
      canvas.drawCircle(Offset(cx, cy), 2.5, dotPaint);
    }

    corner(margin, margin, 1, 1);                             // top-left
    corner(size.width - margin, margin, -1, 1);               // top-right
    corner(margin, size.height - margin, 1, -1);              // bottom-left
    corner(size.width - margin, size.height - margin, -1, -1); // bottom-right
  }

  @override
  bool shouldRepaint(covariant _CornerOrnamentPainter old) => old.color != color;
}

// ═══════════════════════════════════════════════════════════════════════════
// ART DECO MOTIF PAINTER
// ═══════════════════════════════════════════════════════════════════════════

enum _MotifType { starburst, rosette, crosshatch, mandala }

class _ArtDecoMotifPainter extends CustomPainter {
  final _MotifType motif;
  final Color color;
  final double rotate;  // 0..1
  final double shimmer; // 0..1

  _ArtDecoMotifPainter({
    required this.motif, required this.color,
    required this.rotate, required this.shimmer,
  });

  Paint _line(double opacity, {double w = 0.8}) => Paint()
    ..color = color.withOpacity(opacity)
    ..strokeWidth = w
    ..style = PaintingStyle.stroke
    ..strokeCap = StrokeCap.round;

  Paint _fill(double opacity) => Paint()
    ..color = color.withOpacity(opacity)
    ..style = PaintingStyle.fill;

  @override
  void paint(Canvas canvas, Size size) {
    final c = Offset(size.width / 2, size.height / 2);
    final r = size.width / 2;

    canvas.save();
    canvas.translate(c.dx, c.dy);

    switch (motif) {
      case _MotifType.starburst:  _starburst(canvas, r);  break;
      case _MotifType.rosette:    _rosette(canvas, r);    break;
      case _MotifType.crosshatch: _crosshatch(canvas, r); break;
      case _MotifType.mandala:    _mandala(canvas, r);    break;
    }

    canvas.restore();
  }

  void _starburst(Canvas canvas, double r) {
    // Slow forward rotation
    canvas.rotate(rotate * math.pi * 2 * 0.12);

    // Two concentric circles
    canvas.drawCircle(Offset.zero, r * 0.92, _line(0.18));
    canvas.drawCircle(Offset.zero, r * 0.65, _line(0.10));

    // 12 radiating spokes (alternating long/short)
    for (int i = 0; i < 12; i++) {
      final a = i * math.pi / 6;
      final inner = r * 0.22;
      final outer = (i % 2 == 0) ? r * 0.89 : r * 0.60;
      final op    = (i % 2 == 0) ? 0.22 + shimmer * 0.08 : 0.10;
      canvas.drawLine(
        Offset(math.cos(a) * inner, math.sin(a) * inner),
        Offset(math.cos(a) * outer, math.sin(a) * outer),
        _line(op),
      );
    }

    // 6 diamond accents on outer ring
    for (int i = 0; i < 6; i++) {
      final a  = i * math.pi / 3;
      final cx = math.cos(a) * r * 0.92;
      final cy = math.sin(a) * r * 0.92;
      const d  = 4.5;
      final dp = _line(0.4 + shimmer * 0.2, w: 1.1);
      canvas.drawLine(Offset(cx - d, cy), Offset(cx, cy - d), dp);
      canvas.drawLine(Offset(cx, cy - d), Offset(cx + d, cy), dp);
      canvas.drawLine(Offset(cx + d, cy), Offset(cx, cy + d), dp);
      canvas.drawLine(Offset(cx, cy + d), Offset(cx - d, cy), dp);
    }

    // Center dot
    canvas.drawCircle(Offset.zero, 3, _fill(0.4 + shimmer * 0.15));
  }

  void _rosette(Canvas canvas, double r) {
    canvas.rotate(rotate * math.pi * 2 * 0.07);

    // 8 overlapping petal-circles
    for (int i = 0; i < 8; i++) {
      final a  = i * math.pi / 4;
      final cx = math.cos(a) * r * 0.44;
      final cy = math.sin(a) * r * 0.44;
      canvas.drawCircle(Offset(cx, cy), r * 0.44, _line(0.08));
    }

    // Concentric circles
    canvas.drawCircle(Offset.zero, r * 0.87, _line(0.14));
    canvas.drawCircle(Offset.zero, r * 0.56, _line(0.09));
    canvas.drawCircle(Offset.zero, r * 0.22, _line(0.18));

    // 16 spokes
    for (int i = 0; i < 16; i++) {
      final a  = i * math.pi / 8;
      final op = (i % 2 == 0) ? 0.18 + shimmer * 0.07 : 0.07;
      canvas.drawLine(
        Offset(math.cos(a) * r * 0.22, math.sin(a) * r * 0.22),
        Offset(math.cos(a) * r * 0.84, math.sin(a) * r * 0.84),
        _line(op),
      );
    }

    // 8 outer dots
    for (int i = 0; i < 8; i++) {
      final a = i * math.pi / 4;
      canvas.drawCircle(
        Offset(math.cos(a) * r * 0.87, math.sin(a) * r * 0.87),
        2.0, _fill(0.3 + shimmer * 0.1),
      );
    }
  }

  void _crosshatch(Canvas canvas, double r) {
    canvas.rotate(-rotate * math.pi * 2 * 0.06);

    // Outer octagon
    final octPath = Path();
    for (int i = 0; i < 8; i++) {
      final a = i * math.pi / 4 - math.pi / 8;
      final p = Offset(math.cos(a) * r * 0.88, math.sin(a) * r * 0.88);
      i == 0 ? octPath.moveTo(p.dx, p.dy) : octPath.lineTo(p.dx, p.dy);
    }
    octPath.close();
    canvas.drawPath(octPath, _line(0.20, w: 1.0));

    // Rotated inner square
    canvas.save();
    canvas.rotate(math.pi / 4);
    const sq = 0.52;
    canvas.drawRect(
      Rect.fromCenter(center: Offset.zero, width: r * sq * 2, height: r * sq * 2),
      _line(0.12),
    );
    canvas.restore();

    // Cross lines
    canvas.drawLine(Offset(-r * 0.85, 0), Offset(r * 0.85, 0), _line(0.14));
    canvas.drawLine(Offset(0, -r * 0.85), Offset(0, r * 0.85), _line(0.14));
    canvas.drawLine(Offset(-r * 0.6, -r * 0.6), Offset(r * 0.6, r * 0.6), _line(0.07));
    canvas.drawLine(Offset(r * 0.6, -r * 0.6), Offset(-r * 0.6, r * 0.6), _line(0.07));

    // 4 corner diamonds
    for (int i = 0; i < 4; i++) {
      final a  = i * math.pi / 2 + math.pi / 4;
      final cx = math.cos(a) * r * 0.6;
      final cy = math.sin(a) * r * 0.6;
      const d  = 5.0;
      final dp = _fill(0.30 + shimmer * 0.15);
      // draw small diamond
      final dPath = Path()
        ..moveTo(cx, cy - d)
        ..lineTo(cx + d, cy)
        ..lineTo(cx, cy + d)
        ..lineTo(cx - d, cy)
        ..close();
      canvas.drawPath(dPath, dp);
    }

    // Center circle
    canvas.drawCircle(Offset.zero, r * 0.12, _line(0.22));
  }

  void _mandala(Canvas canvas, double r) {
    canvas.rotate(rotate * math.pi * 2 * 0.04);

    // 4 concentric rings with dots
    for (int ring = 0; ring < 4; ring++) {
      final ringR = r * (0.22 + ring * 0.20);
      canvas.drawCircle(Offset.zero, ringR, _line(0.08 + ring * 0.025));

      final dots   = 8 + ring * 4;
      final dotOp  = 0.12 + (ring == 0 ? shimmer * 0.12 : 0.0);
      final dotRad = 2.2 - ring * 0.3;

      for (int i = 0; i < dots; i++) {
        final a = i * math.pi * 2 / dots;
        canvas.drawCircle(
          Offset(math.cos(a) * ringR, math.sin(a) * ringR),
          dotRad.clamp(1.0, 2.2), _fill(dotOp),
        );
      }
    }

    // 12 spokes from inner ring to outer
    for (int i = 0; i < 12; i++) {
      final a = i * math.pi / 6;
      canvas.drawLine(
        Offset(math.cos(a) * r * 0.20, math.sin(a) * r * 0.20),
        Offset(math.cos(a) * r * 0.82, math.sin(a) * r * 0.82),
        _line(0.07 + (i % 3 == 0 ? 0.06 : 0.0)),
      );
    }

    // 6 outer petal arcs
    for (int i = 0; i < 6; i++) {
      final a = i * math.pi / 3;
      canvas.drawArc(
        Rect.fromCircle(
          center: Offset(math.cos(a) * r * 0.68, math.sin(a) * r * 0.68),
          radius: r * 0.22,
        ),
        a + math.pi * 0.8, math.pi * 0.4, false,
        _line(0.10 + shimmer * 0.05, w: 0.9),
      );
    }
  }

  @override
  bool shouldRepaint(covariant _ArtDecoMotifPainter old) =>
      old.rotate != rotate || old.shimmer != shimmer || old.color != color;
}

// ═══════════════════════════════════════════════════════════════════════════
// SLIDE DATA
// ═══════════════════════════════════════════════════════════════════════════

class _SlideData {
  final String numeral;
  final _MotifType motif;
  const _SlideData({required this.numeral, required this.motif});
}
