/// Rosa Fiesta Design System
///
/// Import this single file in any screen that uses the RF visual language:
///   import 'package:frontend/core/design_system.dart';
///
/// Provides:
///   RfTheme         — dark/light token bag (colors, typography values)
///   RfThemeToggle   — pill toggle that reads/writes ThemeProvider
///   RfLuxeButton    — full-width gradient CTA button
///   RfGradientOrbs  — animated background color blobs
///   RfDecoLayer     — animated decorative particles layer
///   RfDecoPainter   — CustomPainter that draws petals/diamonds/sparkles
///   RfGridPainter   — subtle background grid
///   RfFormField     — styled TextFormField matching RF design

library design_system;

import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'app_colors.dart';
import 'theme_provider.dart';
export 'theme_provider.dart';

// ═══════════════════════════════════════════════════════════════════════════
// THEME TOKENS
// ═══════════════════════════════════════════════════════════════════════════

/// The authoritative dark/light token bag.
/// Use [RfTheme.of(context)] to get the current theme from the provider,
/// or [RfTheme.dark] / [RfTheme.light] directly when you have the bool.
class RfTheme {
  final Color base;
  final Color surface;
  final Color card;
  final Color textPrimary;
  final Color textMuted;
  final Color textDim;
  final Color borderFaint;
  final bool isDark;

  const RfTheme({
    required this.base,
    required this.surface,
    required this.card,
    required this.textPrimary,
    required this.textMuted,
    required this.textDim,
    required this.borderFaint,
    required this.isDark,
  });

  /// Resolve from [ThemeProvider] in the widget tree.
  static RfTheme of(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    return isDark ? RfTheme.dark : RfTheme.light;
  }

  static const dark = RfTheme(
    base:        AppColors.darkBase,
    surface:     AppColors.darkSurface,
    card:        AppColors.darkCard,
    textPrimary: AppColors.darkTextPrimary,
    textMuted:   AppColors.darkTextMuted,
    textDim:     AppColors.darkTextDim,
    borderFaint: AppColors.darkBorder,
    isDark:      true,
  );

  static const light = RfTheme(
    base:        AppColors.lightBase,
    surface:     AppColors.lightSurface,
    card:        AppColors.lightCard,
    textPrimary: AppColors.lightTextPrimary,
    textMuted:   AppColors.lightTextMuted,
    textDim:     AppColors.lightTextDim,
    borderFaint: AppColors.lightBorder,
    isDark:      false,
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// THEME TOGGLE — pill with animated icon + label
// ═══════════════════════════════════════════════════════════════════════════

/// Standard theme toggle pill. Reads current state from [ThemeProvider]
/// and writes to it on tap.
class RfThemeToggle extends StatelessWidget {
  final RfTheme t;
  const RfThemeToggle({super.key, required this.t});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.read<ThemeProvider>().toggle(),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeInOutCubic,
        padding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          color: t.isDark
              ? Colors.white.withOpacity(0.06)
              : Colors.black.withOpacity(0.04),
          border: Border.all(
            color: t.isDark
                ? Colors.white.withOpacity(0.12)
                : Colors.black.withOpacity(0.10),
          ),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 280),
            transitionBuilder: (child, anim) => ScaleTransition(
              scale: anim,
              child: RotationTransition(
                turns:
                    Tween(begin: 0.25, end: 0.0).animate(anim),
                child: child,
              ),
            ),
            child: t.isDark
                ? Icon(
                    Icons.dark_mode_rounded,
                    key: const ValueKey(true),
                    color: const Color(0xFF7C8BF5),
                    size: 15,
                  )
                : ShaderMask(
                    shaderCallback: (b) => const LinearGradient(
                      colors: [Color(0xFFFFB800), Color(0xFFFF8C00)],
                    ).createShader(b),
                    child: const Icon(
                      Icons.wb_sunny_rounded,
                      key: ValueKey(false),
                      color: Colors.white,
                      size: 15,
                    ),
                  ),
          ),
          const SizedBox(width: 6),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 220),
            transitionBuilder: (child, anim) => FadeTransition(
              opacity: anim,
              child: SlideTransition(
                position: Tween<Offset>(
                  begin: const Offset(0, 0.3),
                  end: Offset.zero,
                ).animate(anim),
                child: child,
              ),
            ),
            child: Text(
              t.isDark ? 'Oscuro' : 'Claro',
              key: ValueKey(t.isDark),
              style: GoogleFonts.dmSans(
                color: t.isDark
                    ? Colors.white54
                    : const Color(0xFF5A5A80),
                fontSize: 12,
                fontWeight: FontWeight.w500,
                letterSpacing: 1.2,
              ),
            ),
          ),
        ]),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// LUXE BUTTON — full-width gradient CTA
// ═══════════════════════════════════════════════════════════════════════════

/// Primary action button. [filled]=true (default) renders the gradient CTA;
/// [filled]=false renders an outlined ghost button (requires [t]).
class RfLuxeButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final bool loading;
  final bool filled;
  final RfTheme? t;

  const RfLuxeButton({
    super.key,
    required this.label,
    required this.onTap,
    this.loading = false,
    this.filled = true,
    this.t,
  });

  @override
  Widget build(BuildContext context) {
    if (filled) {
      return GestureDetector(
        onTap: onTap,
        child: Container(
          width: double.infinity,
          height: 54,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            gradient: AppColors.buttonGradient,
            boxShadow: [
              BoxShadow(
                color: AppColors.hotPink.withOpacity(0.35),
                blurRadius: 20,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          child: Center(
            child: loading
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(
                        color: Colors.white, strokeWidth: 2.5),
                  )
                : Text(
                    label,
                    style: GoogleFonts.outfit(
                      color: Colors.white,
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 0.5,
                    ),
                  ),
          ),
        ),
      );
    }

    // Outlined / ghost variant
    final theme = t ?? RfTheme.dark;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 400),
        width: double.infinity,
        height: 54,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          color: theme.isDark
              ? Colors.white.withOpacity(0.04)
              : Colors.black.withOpacity(0.04),
          border: Border.all(color: theme.borderFaint, width: 1.5),
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: GoogleFonts.outfit(
            color: theme.textPrimary,
            fontSize: 15,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.5,
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// FORM FIELD — styled input matching RF design
// ═══════════════════════════════════════════════════════════════════════════

/// Styled [TextFormField] for use inside RF auth/form screens.
class RfFormField extends StatelessWidget {
  final String label;
  final IconData icon;
  final TextEditingController controller;
  final RfTheme t;
  final bool obscure;
  final String? Function(String?)? validator;

  const RfFormField({
    super.key,
    required this.label,
    required this.icon,
    required this.controller,
    required this.t,
    this.obscure = false,
    this.validator,
  });

  @override
  Widget build(BuildContext context) {
    final enabledColor = t.isDark
        ? Colors.white.withOpacity(0.10)
        : const Color(0xFFE8E8F0);
    final fillColor = t.isDark
        ? Colors.white.withOpacity(0.04)
        : const Color(0xFFF8F8FC);
    return TextFormField(
      controller: controller,
      obscureText: obscure,
      style: GoogleFonts.dmSans(color: t.textPrimary, fontSize: 15),
      cursorColor: AppColors.hotPink,
      decoration: InputDecoration(
        labelText: label,
        labelStyle:
            GoogleFonts.dmSans(color: t.textDim, fontSize: 14),
        prefixIcon: Icon(icon, color: t.textDim, size: 20),
        filled: true,
        fillColor: fillColor,
        contentPadding: const EdgeInsets.symmetric(
            horizontal: 20, vertical: 18),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: enabledColor),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: enabledColor),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide:
              const BorderSide(color: AppColors.hotPink, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(
              color: AppColors.coral.withOpacity(0.6)),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide:
              const BorderSide(color: AppColors.coral, width: 1.5),
        ),
      ),
      validator: validator,
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENT ORBS — animated background blobs
// ═══════════════════════════════════════════════════════════════════════════

/// Animated ambient color orbs for screen backgrounds.
/// Pass [color1] and [color2] to control orb hues; [isDark] controls opacity.
class RfGradientOrbs extends StatelessWidget {
  final AnimationController controller;
  final Color color1;
  final Color color2;
  final bool isDark;

  const RfGradientOrbs({
    super.key,
    required this.controller,
    required this.color1,
    required this.color2,
    this.isDark = true,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (_, __) {
        final phase = controller.value * math.pi * 2;
        final o1 = isDark ? 0.12 : 0.08;
        final o2 = isDark ? 0.10 : 0.06;
        final o3 = isDark ? 0.04 : 0.05;
        return Stack(children: [
          Positioned(
            left: -80 + 60 * math.sin(phase),
            top:  -100 + 40 * math.cos(phase),
            child: _orb(color1, 400, o1),
          ),
          Positioned(
            right:  -120 + 50 * math.cos(phase * 0.7),
            bottom: -80  + 60 * math.sin(phase * 0.7),
            child: _orb(color2, 350, o2),
          ),
          Positioned(
            left: 100 + 30 * math.cos(phase * 1.3),
            top:  300 + 40 * math.sin(phase * 1.3),
            child: _orb(color1, 250, o3),
          ),
        ]);
      },
    );
  }

  Widget _orb(Color c, double size, double opacity) => Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: RadialGradient(
            colors: [
              c.withOpacity(opacity),
              c.withOpacity(0),
            ],
          ),
        ),
      );
}

// ═══════════════════════════════════════════════════════════════════════════
// DECO LAYER — floating event-themed particles
// ═══════════════════════════════════════════════════════════════════════════

/// Stateless wrapper that animates [RfDecoPainter].
/// [baseOpacity] multiplier: 1.0 for dark, 1.8 for light.
class RfDecoLayer extends StatelessWidget {
  final AnimationController floatController;
  final AnimationController decoController;
  final AnimationController pulseController;
  final double baseOpacity;

  const RfDecoLayer({
    super.key,
    required this.floatController,
    required this.decoController,
    required this.pulseController,
    this.baseOpacity = 1.0,
  });

  @override
  Widget build(BuildContext context) {
    return Positioned.fill(
      child: IgnorePointer(
        child: AnimatedBuilder(
          animation: Listenable.merge(
              [floatController, decoController, pulseController]),
          builder: (_, __) => CustomPaint(
            painter: RfDecoPainter(
              floatT:      floatController.value,
              decoT:       decoController.value,
              pulseT:      pulseController.value,
              baseOpacity: baseOpacity,
            ),
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// DECO PAINTER — petals, rings, diamonds, sparkles
// ═══════════════════════════════════════════════════════════════════════════

class RfDecoPainter extends CustomPainter {
  final double floatT, decoT, pulseT, baseOpacity;

  const RfDecoPainter({
    required this.floatT,
    required this.decoT,
    required this.pulseT,
    this.baseOpacity = 1.0,
  });

  @override
  void paint(Canvas canvas, Size size) {
    canvas.clipRect(Rect.fromLTWH(0, 0, size.width, size.height));
    _drawCircles(canvas, size);
    _drawPetals(canvas, size);
    _drawDiamonds(canvas, size);
    _drawSparkles(canvas, size);
  }

  void _drawCircles(Canvas canvas, Size size) {
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0;
    final configs = [
      [size.width * 0.12, size.height * 0.18, 36.0, AppColors.hotPink, 0.0],
      [size.width * 0.88, size.height * 0.25, 28.0, AppColors.amber,   1.2],
      [size.width * 0.08, size.height * 0.72, 22.0, AppColors.teal,    2.4],
      [size.width * 0.92, size.height * 0.78, 32.0, AppColors.violet,  0.8],
      [size.width * 0.50, size.height * 0.08, 18.0, AppColors.coral,   1.8],
    ];
    for (final c in configs) {
      final t   = floatT * math.pi * 2 + (c[4] as double);
      final dy  = math.cos(t + 1.57) * 7;
      final osc = 0.5 + 0.5 * math.sin(floatT * math.pi * 2 + (c[4] as double));
      final op  = ((0.10 + osc * 0.12) * baseOpacity).clamp(0.0, 1.0);
      paint.color = (c[3] as Color).withOpacity(op);
      canvas.drawCircle(
          Offset(c[0] as double, (c[1] as double) + dy),
          c[2] as double,
          paint);
    }
  }

  void _drawPetals(Canvas canvas, Size size) {
    final paint = Paint()..style = PaintingStyle.fill;
    final configs = [
      [size.width * 0.18, size.height * 0.35, AppColors.hotPink, 0.3],
      [size.width * 0.82, size.height * 0.55, AppColors.violet,  1.5],
      [size.width * 0.06, size.height * 0.55, AppColors.teal,    2.7],
      [size.width * 0.94, size.height * 0.40, AppColors.amber,   0.9],
      [size.width * 0.45, size.height * 0.92, AppColors.coral,   2.1],
    ];
    for (final c in configs) {
      final t   = floatT * math.pi * 2 + (c[3] as double);
      final dy  = math.sin(t) * 9;
      final rot = decoT * math.pi * 2 + (c[3] as double);
      final osc = 0.5 + 0.5 * math.sin(t);
      final op  = ((0.07 + osc * 0.09) * baseOpacity).clamp(0.0, 1.0);
      paint.color = (c[2] as Color).withOpacity(op);
      final cx = c[0] as double;
      final cy = (c[1] as double) + dy;
      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rot);
      for (int i = 0; i < 5; i++) {
        canvas.save();
        canvas.rotate(i * math.pi * 2 / 5);
        final path = Path()
          ..moveTo(0, 0)
          ..cubicTo(-6, -10, -4, -22, 0, -26)
          ..cubicTo(4, -22, 6, -10, 0, 0);
        canvas.drawPath(path, paint);
        canvas.restore();
      }
      canvas.restore();
    }
  }

  void _drawDiamonds(Canvas canvas, Size size) {
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.8;
    final configs = [
      [size.width * 0.25, size.height * 0.12, 18.0, AppColors.amber,   0.5],
      [size.width * 0.75, size.height * 0.88, 22.0, AppColors.hotPink, 1.7],
      [size.width * 0.88, size.height * 0.12, 14.0, AppColors.teal,    2.9],
      [size.width * 0.15, size.height * 0.88, 20.0, AppColors.violet,  0.2],
    ];
    for (final c in configs) {
      final t   = floatT * math.pi * 2 + (c[4] as double);
      final dy  = math.cos(t + 1.57) * 7;
      final rot = decoT * math.pi * 2 * 0.5 + (c[4] as double);
      final osc = 0.5 + 0.5 * math.sin(t);
      final op  = ((0.12 + osc * 0.14) * baseOpacity).clamp(0.0, 1.0);
      paint.color = (c[3] as Color).withOpacity(op);
      final cx = c[0] as double;
      final cy = (c[1] as double) + dy;
      final r  = c[2] as double;
      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rot);
      final path = Path()
        ..moveTo(0, -r)
        ..lineTo(r * 0.6, 0)
        ..lineTo(0, r)
        ..lineTo(-r * 0.6, 0)
        ..close();
      canvas.drawPath(path, paint);
      canvas.restore();
    }
  }

  void _drawSparkles(Canvas canvas, Size size) {
    final paint = Paint()
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 1.5;
    final configs = [
      [size.width * 0.35, size.height * 0.20, 6.0, AppColors.amber,   0.6],
      [size.width * 0.65, size.height * 0.15, 5.0, AppColors.hotPink, 2.0],
      [size.width * 0.80, size.height * 0.45, 7.0, AppColors.teal,    3.4],
      [size.width * 0.20, size.height * 0.60, 5.0, AppColors.violet,  1.1],
      [size.width * 0.55, size.height * 0.85, 6.0, AppColors.coral,   2.7],
    ];
    for (final c in configs) {
      final t   = floatT * math.pi * 2 + (c[4] as double);
      final dy  = math.sin(t) * 6;
      final rot = decoT * math.pi * 2 + (c[4] as double);
      final osc = 0.5 + 0.5 * math.sin(t);
      final op  = ((0.15 + osc * 0.20) * baseOpacity).clamp(0.0, 1.0);
      paint.color = (c[3] as Color).withOpacity(op);
      final cx = c[0] as double;
      final cy = (c[1] as double) + dy;
      final r  = c[2] as double;
      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rot);
      for (int i = 0; i < 4; i++) {
        final angle = i * math.pi / 2;
        canvas.drawLine(
          Offset(math.cos(angle) * r * 0.3, math.sin(angle) * r * 0.3),
          Offset(math.cos(angle) * r,       math.sin(angle) * r),
          paint,
        );
      }
      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(RfDecoPainter old) =>
      old.floatT != floatT ||
      old.decoT != decoT ||
      old.pulseT != pulseT ||
      old.baseOpacity != baseOpacity;
}

// ═══════════════════════════════════════════════════════════════════════════
// GRID PAINTER — subtle dot grid background
// ═══════════════════════════════════════════════════════════════════════════

class RfGridPainter extends CustomPainter {
  final Color color;
  const RfGridPainter({required this.color});

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
  bool shouldRepaint(RfGridPainter old) => old.color != color;
}
