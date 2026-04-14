import 'dart:async';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';

class EventCountdownWidget extends StatefulWidget {
  final DateTime eventDate;
  final String eventName;

  const EventCountdownWidget({
    super.key,
    required this.eventDate,
    required this.eventName,
  });

  @override
  State<EventCountdownWidget> createState() => _EventCountdownWidgetState();
}

class _EventCountdownWidgetState extends State<EventCountdownWidget>
    with SingleTickerProviderStateMixin {
  Timer? _timer;
  Duration _remaining = Duration.zero;
  late final AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _calculateRemaining();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) => _calculateRemaining());
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );
    // Pulse animation if event is within 7 days
    if (_remaining.inDays < 7 && _remaining.inDays >= 0) {
      _pulseController.repeat(reverse: true);
    }
  }

  @override
  void dispose() {
    _timer?.cancel();
    _pulseController.dispose();
    super.dispose();
  }

  void _calculateRemaining() {
    if (!mounted) return;
    final now = DateTime.now();
    setState(() {
      if (widget.eventDate.isAfter(now)) {
        _remaining = widget.eventDate.difference(now);
      } else {
        _remaining = Duration.zero;
      }
    });
  }

  bool get _isPast => _remaining == Duration.zero;
  bool get _isWithin7Days => _remaining.inDays < 7 && !_isPast;

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    if (_isPast) {
      return _buildFinishedCard(t);
    }

    final days = _remaining.inDays;
    final hours = _remaining.inHours % 24;
    final minutes = _remaining.inMinutes % 60;
    final seconds = _remaining.inSeconds % 60;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: t.isDark
              ? [const Color(0xFF1a1a2e), const Color(0xFF16213e)]
              : [Colors.white, const Color(0xFFF8F9FA)],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.hotPink.withValues(alpha: 0.1),
            blurRadius: 20,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        children: [
          Text(
            'para tu evento',
            style: GoogleFonts.dmSans(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: t.textMuted,
              letterSpacing: 0.5,
            ),
          ),
          const SizedBox(height: 16),
          _buildCountdownRow(days, hours, minutes, seconds, t),
        ],
      ),
    );
  }

  Widget _buildCountdownRow(int days, int hours, int minutes, int seconds, RfTheme t) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _buildUnit('días', days, isLarge: true),
        _buildSeparator(),
        _buildUnit('horas', hours, isLarge: true),
        _buildSeparator(),
        _buildUnit('min', minutes, isLarge: false),
        _buildSeparator(),
        _buildUnit('seg', seconds, isLarge: false),
      ],
    );
  }

  Widget _buildUnit(String label, int value, {required bool isLarge}) {
    final scale = isLarge ? 1.0 : 0.75;
    final displayValue = value.toString().padLeft(2, '0');

    Widget content = Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            displayValue,
            style: GoogleFonts.outfit(
              fontSize: 48 * scale,
              fontWeight: FontWeight.w800,
              color: Colors.white,
              height: 1,
            ),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: GoogleFonts.dmSans(
            fontSize: 12 * scale,
            fontWeight: FontWeight.w600,
            color: Colors.white70,
          ),
        ),
      ],
    );

    if (isLarge && _isWithin7Days) {
      content = ScaleTransition(
        scale: Tween<double>(begin: 1.0, end: 1.08).animate(
          CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
        ),
        child: content,
      );
    }

    return content;
  }

  Widget _buildSeparator() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ShaderMask(
            shaderCallback: (b) => AppColors.titleGradient.createShader(b),
            child: Text(
              ':',
              style: GoogleFonts.outfit(
                fontSize: 36,
                fontWeight: FontWeight.w800,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Widget _buildFinishedCard(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppColors.teal, AppColors.sky],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.teal.withValues(alpha: 0.3),
            blurRadius: 20,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.celebration_rounded, color: Colors.white, size: 28),
          const SizedBox(width: 12),
          Text(
            '¡El evento está en curso!',
            style: GoogleFonts.outfit(
              fontSize: 18,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ],
      ),
    );
  }
}