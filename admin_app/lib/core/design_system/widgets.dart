import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'colors.dart';

/// Gradient button for admin app
class AdminButton extends StatelessWidget {
  final String label;
  final VoidCallback? onTap;
  final bool loading;
  final bool outlined;
  final IconData? icon;

  const AdminButton({
    super.key,
    required this.label,
    this.onTap,
    this.loading = false,
    this.outlined = false,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final child = loading
        ? const SizedBox(
            width: 20,
            height: 20,
            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
          )
        : Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (icon != null) ...[Icon(icon, size: 18), const SizedBox(width: 8)],
              Text(label),
            ],
          );

    if (outlined) {
      return OutlinedButton(onPressed: onTap, child: child);
    }

    return ElevatedButton(
      onPressed: loading ? null : onTap,
      child: child,
    );
  }
}

/// Stat card for dashboard
class StatCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;
  final Color? color;
  final VoidCallback? onTap;

  const StatCard({
    super.key,
    required this.title,
    required this.value,
    required this.icon,
    this.color,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final c = color ?? AppColors.primary;
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: c.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(icon, color: c, size: 18),
                  ),
                  const Spacer(),
                  if (onTap != null)
                    Icon(Icons.arrow_forward_ios, size: 12, color: AppColors.textMuted),
                ],
              ),
              const SizedBox(height: 6),
              FittedBox(
                fit: BoxFit.scaleDown,
                alignment: Alignment.centerLeft,
                child: Text(
                  value,
                  style: GoogleFonts.outfit(fontSize: 24, fontWeight: FontWeight.w700),
                ),
              ),
              const SizedBox(height: 2),
              Expanded(
                child: Align(
                  alignment: Alignment.bottomLeft,
                  child: Text(
                    title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.dmSans(color: AppColors.textMuted, fontSize: 12),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Alert card for critical items
class AlertCard extends StatelessWidget {
  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final VoidCallback? onTap;

  const AlertCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.icon,
    this.color = AppColors.error,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: color.withValues(alpha: 0.1),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: color, size: 18),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                    Text(subtitle, style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: AppColors.textMuted),
            ],
          ),
        ),
      ),
    );
  }
}

/// Section header
class SectionHeader extends StatelessWidget {
  final String title;
  final VoidCallback? onViewAll;

  const SectionHeader({super.key, required this.title, this.onViewAll});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title, style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
          if (onViewAll != null)
            TextButton(onPressed: onViewAll, child: const Text('Ver todo')),
        ],
      ),
    );
  }
}

/// Status badge
class StatusBadge extends StatelessWidget {
  final String label;
  final Color color;

  const StatusBadge({super.key, required this.label, required this.color});

  factory StatusBadge.pending() => const StatusBadge(label: 'Pendiente', color: AppColors.warning);
  factory StatusBadge.confirmed() => const StatusBadge(label: 'Confirmado', color: AppColors.success);
  factory StatusBadge.paid() => const StatusBadge(label: 'Pagado', color: AppColors.primary);
  factory StatusBadge.completed() => const StatusBadge(label: 'Completado', color: AppColors.teal);
  factory StatusBadge.rejected() => const StatusBadge(label: 'Rechazado', color: AppColors.error);
  factory StatusBadge.draft() => const StatusBadge(label: 'Borrador', color: AppColors.textMuted);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Text(
        label,
        style: GoogleFonts.dmSans(fontSize: 11, fontWeight: FontWeight.w600, color: color),
      ),
    );
  }
}

/// Data table row
class DataTableRow extends StatelessWidget {
  final List<String> cells;
  final List<double>? widths;
  final VoidCallback? onTap;

  const DataTableRow({super.key, required this.cells, this.widths, this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: const BoxDecoration(
          border: Border(bottom: BorderSide(color: AppColors.borderDark, width: 0.5)),
        ),
        child: Row(
          children: List.generate(cells.length, (i) {
            final w = widths != null && i < widths!.length ? widths![i] : null;
            return w != null
                ? SizedBox(width: w, child: Text(cells[i], style: GoogleFonts.dmSans(fontSize: 13)))
                : Expanded(child: Text(cells[i], style: GoogleFonts.dmSans(fontSize: 13)));
          }),
        ),
      ),
    );
  }
}

/// Search field
class AdminSearchField extends StatelessWidget {
  final TextEditingController? controller;
  final String hint;
  final ValueChanged<String>? onChanged;

  const AdminSearchField({super.key, this.controller, this.hint = 'Buscar...', this.onChanged});

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      onChanged: onChanged,
      decoration: InputDecoration(
        hintText: hint,
        prefixIcon: const Icon(Icons.search, size: 20),
        suffixIcon: controller != null && controller!.text.isNotEmpty
            ? IconButton(
                icon: const Icon(Icons.clear, size: 18),
                onPressed: () {
                  controller?.clear();
                  onChanged?.call('');
                },
              )
            : null,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      ),
    );
  }
}

/// Loading overlay
class LoadingOverlay extends StatelessWidget {
  final bool loading;
  final Widget child;

  const LoadingOverlay({super.key, required this.loading, required this.child});

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        child,
        if (loading)
          Container(
            color: Colors.black38,
            child: const Center(child: CircularProgressIndicator()),
          ),
      ],
    );
  }
}

/// Empty state
class EmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? action;

  const EmptyState({
    super.key,
    required this.icon,
    required this.title,
    this.subtitle,
    this.action,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 64, color: AppColors.textMuted),
            const SizedBox(height: 16),
            Text(title, style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600)),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(subtitle!, textAlign: TextAlign.center, style: GoogleFonts.dmSans(color: AppColors.textMuted)),
            ],
            if (action != null) ...[const SizedBox(height: 24), action!],
          ],
        ),
      ),
    );
  }
}

/// Section header for forms
class FormSection extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const FormSection({super.key, required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
        const SizedBox(height: 12),
        ...children,
      ],
    );
  }
}

/// Admin text field
class AdminTextField extends StatelessWidget {
  final String label;
  final TextEditingController? controller;
  final String? hint;
  final bool obscure;
  final int lines;
  final TextInputType? keyboardType;
  final String? Function(String?)? validator;
  final ValueChanged<String>? onChanged;
  final Widget? suffix;

  const AdminTextField({
    super.key,
    required this.label,
    this.controller,
    this.hint,
    this.obscure = false,
    this.lines = 1,
    this.keyboardType,
    this.validator,
    this.onChanged,
    this.suffix,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: GoogleFonts.dmSans(fontSize: 13, fontWeight: FontWeight.w500)),
        const SizedBox(height: 6),
        TextFormField(
          controller: controller,
          obscureText: obscure,
          maxLines: lines,
          keyboardType: keyboardType,
          validator: validator,
          onChanged: onChanged,
          decoration: InputDecoration(hintText: hint, suffixIcon: suffix),
        ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// AMBIENTES ANIMADOS Y PARTÍCULAS CANDY POP
// ═══════════════════════════════════════════════════════════════════════════

/// Orbes de gradiente animado ambiental para fondos de pantallas.
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
    this.isDark = false,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (_, __) {
        final phase = controller.value * math.pi * 2;
        final o1 = isDark ? 0.22 : 0.28;
        final o2 = isDark ? 0.20 : 0.24;
        final o3 = isDark ? 0.08 : 0.15;
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

/// Capa sin estado que anima a RfDecoPainter para agregar vida.
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

/// Dibujador de pétalos, anillos, diamantes y destellos flotantes.
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

