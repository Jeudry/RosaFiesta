import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

/// A stroke drawn by the user on the sketch canvas.
class _Stroke {
  final List<Offset> points;
  final Color color;
  final double width;

  _Stroke({required this.color, required this.width}) : points = [];
}

/// Full-screen free-hand sketch canvas for event layout distribution.
///
/// The user draws a 2D top-down (cenital) view of their event space.
/// The sketch is captured and (in Phase 3) sent to the AI to generate
/// 3D distribution examples.
class SketchCanvasScreen extends StatefulWidget {
  const SketchCanvasScreen({super.key});

  @override
  State<SketchCanvasScreen> createState() => _SketchCanvasScreenState();
}

class _SketchCanvasScreenState extends State<SketchCanvasScreen> {
  final List<_Stroke> _strokes = [];
  final List<_Stroke> _redoStack = [];
  _Stroke? _currentStroke;

  double _strokeWidth = 3.0;
  Color _strokeColor = AppColors.hotPink;
  bool _isEraser = false;

  static const _palette = [
    AppColors.hotPink,
    AppColors.violet,
    AppColors.teal,
    AppColors.amber,
    AppColors.sky,
    AppColors.coral,
    Colors.white,
    Color(0xFF1A1A2E),
  ];

  static const _widths = [2.0, 3.0, 5.0, 8.0];

  void _undo() {
    if (_strokes.isEmpty) return;
    setState(() {
      _redoStack.add(_strokes.removeLast());
    });
  }

  void _redo() {
    if (_redoStack.isEmpty) return;
    setState(() {
      _strokes.add(_redoStack.removeLast());
    });
  }

  void _clear() {
    if (_strokes.isEmpty) return;
    setState(() {
      _redoStack.addAll(_strokes);
      _strokes.clear();
    });
  }

  void _toggleEraser() {
    setState(() {
      _isEraser = !_isEraser;
    });
  }

  /// Save the sketch and pop back. In Phase 3 this will encode the canvas
  /// as a PNG and pass it to the AI pipeline.
  void _saveAndReturn() {
    // For now we just pop — the sketch data lives in memory.
    // Phase 3: encode via PictureRecorder → Image → PNG bytes.
    Navigator.of(context).pop(true); // true = sketch was saved
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(t),
            _buildInfoBanner(t),
            Expanded(child: _buildCanvas(t)),
            _buildToolbar(t),
          ],
        ),
      ),
    );
  }

  // ── Header ───────────────────────────────────────────────────────────────

  Widget _buildHeader(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => Navigator.of(context).pop(),
            child: Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: t.isDark ? t.card : Colors.white,
                shape: BoxShape.circle,
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.close_rounded,
                  color: t.textPrimary, size: 22),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Boceto de distribución',
                  style: GoogleFonts.outfit(
                    fontSize: 18,
                    fontWeight: FontWeight.w800,
                    color: t.textPrimary,
                  ),
                ),
                Text(
                  'Vista cenital (desde arriba)',
                  style: GoogleFonts.dmSans(
                    fontSize: 12,
                    color: t.textMuted,
                  ),
                ),
              ],
            ),
          ),
          // Undo / Redo
          _headerAction(
            Icons.undo_rounded,
            _strokes.isNotEmpty ? _undo : null,
            t,
          ),
          const SizedBox(width: 6),
          _headerAction(
            Icons.redo_rounded,
            _redoStack.isNotEmpty ? _redo : null,
            t,
          ),
          const SizedBox(width: 6),
          _headerAction(Icons.delete_outline_rounded, _clear, t),
        ],
      ),
    );
  }

  Widget _headerAction(IconData icon, VoidCallback? onTap, RfTheme t) {
    final enabled = onTap != null;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          shape: BoxShape.circle,
          border: Border.all(color: t.borderFaint),
        ),
        child: Icon(icon,
            color: enabled
                ? t.textPrimary
                : t.textDim.withValues(alpha: 0.3),
            size: 20),
      ),
    );
  }

  // ── Info Banner ──────────────────────────────────────────────────────────

  Widget _buildInfoBanner(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [
              AppColors.violet.withValues(alpha: 0.08),
              AppColors.hotPink.withValues(alpha: 0.06),
            ],
          ),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.violet.withValues(alpha: 0.15)),
        ),
        child: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                    colors: [AppColors.violet, AppColors.hotPink]),
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.auto_awesome_rounded,
                  color: Colors.white, size: 18),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                'Dibuja la distribución de tu espacio. La IA generará ejemplos en 3D basados en tu boceto.',
                style: GoogleFonts.dmSans(
                  fontSize: 12,
                  color: t.textMuted,
                  height: 1.35,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Canvas ───────────────────────────────────────────────────────────────

  Widget _buildCanvas(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Container(
        decoration: BoxDecoration(
          color: t.isDark ? const Color(0xFF1A1A2E) : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: t.borderFaint, width: 1.5),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.06),
              blurRadius: 20,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(19),
          child: GestureDetector(
            onPanStart: (details) {
              setState(() {
                _redoStack.clear();
                final color = _isEraser
                    ? (t.isDark ? const Color(0xFF1A1A2E) : Colors.white)
                    : _strokeColor;
                _currentStroke = _Stroke(
                  color: color,
                  width: _isEraser ? _strokeWidth * 3 : _strokeWidth,
                );
                _currentStroke!.points
                    .add(details.localPosition);
                _strokes.add(_currentStroke!);
              });
            },
            onPanUpdate: (details) {
              if (_currentStroke == null) return;
              setState(() {
                _currentStroke!.points
                    .add(details.localPosition);
              });
            },
            onPanEnd: (_) {
              _currentStroke = null;
            },
            child: CustomPaint(
              painter: _SketchPainter(
                strokes: _strokes,
                gridColor: t.isDark
                    ? Colors.white.withValues(alpha: 0.04)
                    : Colors.black.withValues(alpha: 0.04),
              ),
              size: Size.infinite,
            ),
          ),
        ),
      ),
    );
  }

  // ── Bottom Toolbar ───────────────────────────────────────────────────────

  Widget _buildToolbar(RfTheme t) {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Color + width pickers row
          Row(
            children: [
              // Eraser toggle
              GestureDetector(
                onTap: _toggleEraser,
                child: Container(
                  width: 42,
                  height: 42,
                  decoration: BoxDecoration(
                    color: _isEraser
                        ? AppColors.violet.withValues(alpha: 0.15)
                        : (t.isDark ? t.card : Colors.white),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: _isEraser
                          ? AppColors.violet
                          : t.borderFaint,
                      width: _isEraser ? 2 : 1,
                    ),
                  ),
                  child: Icon(
                    Icons.auto_fix_high_rounded,
                    color: _isEraser ? AppColors.violet : t.textMuted,
                    size: 20,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              // Color circles
              Expanded(
                child: SizedBox(
                  height: 42,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: _palette.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 8),
                    itemBuilder: (context, i) {
                      final color = _palette[i];
                      final isSelected =
                          !_isEraser && _strokeColor == color;
                      return GestureDetector(
                        onTap: () {
                          setState(() {
                            _strokeColor = color;
                            _isEraser = false;
                          });
                        },
                        child: Container(
                          width: 32,
                          height: 32,
                          decoration: BoxDecoration(
                            color: color,
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: isSelected
                                  ? t.textPrimary
                                  : t.borderFaint,
                              width: isSelected ? 2.5 : 1,
                            ),
                            boxShadow: isSelected
                                ? [
                                    BoxShadow(
                                      color: color.withValues(alpha: 0.4),
                                      blurRadius: 8,
                                      spreadRadius: 1,
                                    )
                                  ]
                                : null,
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          // Stroke width + Save button row
          Row(
            children: [
              // Width chips
              ..._widths.map((w) {
                final isSelected = _strokeWidth == w;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: GestureDetector(
                    onTap: () => setState(() => _strokeWidth = w),
                    child: Container(
                      width: 36,
                      height: 36,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: isSelected
                            ? AppColors.violet.withValues(alpha: 0.12)
                            : (t.isDark ? t.card : Colors.white),
                        borderRadius: BorderRadius.circular(10),
                        border: Border.all(
                          color: isSelected
                              ? AppColors.violet
                              : t.borderFaint,
                        ),
                      ),
                      child: Container(
                        width: w * 1.5 + 2,
                        height: w * 1.5 + 2,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color:
                              isSelected ? AppColors.violet : t.textMuted,
                        ),
                      ),
                    ),
                  ),
                );
              }),
              const Spacer(),
              // Save button
              GestureDetector(
                onTap: _strokes.isNotEmpty ? _saveAndReturn : null,
                child: Container(
                  height: 48,
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  decoration: BoxDecoration(
                    gradient: _strokes.isNotEmpty
                        ? const LinearGradient(
                            colors: [AppColors.violet, AppColors.hotPink])
                        : null,
                    color: _strokes.isEmpty
                        ? t.textDim.withValues(alpha: 0.15)
                        : null,
                    borderRadius: BorderRadius.circular(24),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.auto_awesome_rounded,
                          color: _strokes.isNotEmpty
                              ? Colors.white
                              : t.textDim,
                          size: 18),
                      const SizedBox(width: 8),
                      Text(
                        'Generar ejemplos',
                        style: GoogleFonts.dmSans(
                          fontSize: 15,
                          fontWeight: FontWeight.w700,
                          color: _strokes.isNotEmpty
                              ? Colors.white
                              : t.textDim,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Custom painter that draws a subtle grid and all user strokes.
class _SketchPainter extends CustomPainter {
  final List<_Stroke> strokes;
  final Color gridColor;

  _SketchPainter({required this.strokes, required this.gridColor});

  @override
  void paint(Canvas canvas, Size size) {
    // Grid
    final gridPaint = Paint()
      ..color = gridColor
      ..strokeWidth = 0.5;

    const spacing = 30.0;
    for (double x = 0; x <= size.width; x += spacing) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), gridPaint);
    }
    for (double y = 0; y <= size.height; y += spacing) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), gridPaint);
    }

    // Strokes
    for (final stroke in strokes) {
      if (stroke.points.length < 2) continue;
      final paint = Paint()
        ..color = stroke.color
        ..strokeWidth = stroke.width
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round
        ..style = PaintingStyle.stroke;

      final path = Path()..moveTo(stroke.points[0].dx, stroke.points[0].dy);
      for (int i = 1; i < stroke.points.length; i++) {
        // Smooth with quadratic bezier between midpoints
        final p0 = stroke.points[i - 1];
        final p1 = stroke.points[i];
        final mid = Offset((p0.dx + p1.dx) / 2, (p0.dy + p1.dy) / 2);
        path.quadraticBezierTo(p0.dx, p0.dy, mid.dx, mid.dy);
      }
      canvas.drawPath(path, paint);
    }
  }

  @override
  bool shouldRepaint(covariant _SketchPainter oldDelegate) => true;
}
