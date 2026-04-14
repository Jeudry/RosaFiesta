import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../events_provider.dart';
import '../../data/event_model.dart';
import 'event_detail_screen.dart';

class QuoteHistoryScreen extends StatefulWidget {
  const QuoteHistoryScreen({super.key});

  static Future<void> open(BuildContext context) {
    return Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const QuoteHistoryScreen()),
    );
  }

  @override
  State<QuoteHistoryScreen> createState() => _QuoteHistoryScreenState();
}

class _QuoteHistoryScreenState extends State<QuoteHistoryScreen>
    with TickerProviderStateMixin {
  late final AnimationController _gradientCtrl;

  @override
  void initState() {
    super.initState();
    _gradientCtrl = AnimationController(
        vsync: this, duration: const Duration(seconds: 12))
      ..repeat();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<EventsProvider>().fetchEvents();
    });
  }

  @override
  void dispose() {
    _gradientCtrl.dispose();
    super.dispose();
  }

  static final _statuses = [
    _StatusInfo('requested', 'Cotización\nsolicitada', 'solicitada', Icons.send_rounded, AppColors.sky),
    _StatusInfo('adjusted',  'Cotización\nrevisada',    'revisada',    Icons.tune_rounded, AppColors.amber),
    _StatusInfo('confirmed', 'Confirmada',              'confirmada',  Icons.check_circle_rounded, AppColors.teal),
    _StatusInfo('paid',      'Pagada',                  'pagada',      Icons.payment_rounded, AppColors.violet),
    _StatusInfo('completed',  'Completada',              'completada',  Icons.celebration_rounded, AppColors.hotPink),
  ];

  int _statusIndex(String status) {
    final i = _statuses.indexWhere((s) => s.key == status);
    return i < 0 ? 0 : i;
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_rounded, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text('Historial de Cotizaciones',
            style: GoogleFonts.outfit(
                fontSize: 20, fontWeight: FontWeight.w800, color: t.textPrimary)),
        centerTitle: true,
      ),
      body: Stack(
        children: [
          RfGradientOrbs(
            controller: _gradientCtrl,
            color1: AppColors.hotPink,
            color2: AppColors.violet,
            isDark: t.isDark,
          ),
          Positioned.fill(
            child: IgnorePointer(
              child: CustomPaint(
                painter: RfGridPainter(
                    color: (t.isDark ? Colors.white : Colors.black)
                        .withValues(alpha: 0.006)),
              ),
            ),
          ),
          SafeArea(
            child: Consumer<EventsProvider>(
              builder: (context, provider, _) {
                if (provider.isLoading) {
                  return const Center(
                      child: CircularProgressIndicator(color: AppColors.hotPink));
                }
                // Show only non-draft events (real quote requests)
                final historyEvents = provider.events
                    .where((e) => e.status != 'draft' && e.status != 'cancelled')
                    .toList();
                if (historyEvents.isEmpty) {
                  return _buildEmpty(t);
                }
                return ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  itemCount: historyEvents.length,
                  itemBuilder: (_, i) =>
                      _eventCard(historyEvents[i], t),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmpty(RfTheme t) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.history_rounded,
              color: t.textDim.withValues(alpha: 0.4), size: 64),
          const SizedBox(height: 16),
          Text('Sin cotizaciones aún',
              style: GoogleFonts.outfit(
                  fontSize: 18, fontWeight: FontWeight.w700, color: t.textMuted)),
          const SizedBox(height: 8),
          Text('Los eventos que cotices aparecerán aquí',
              style: GoogleFonts.dmSans(fontSize: 13, color: t.textDim)),
        ],
      ),
    );
  }

  Widget _eventCard(Event event, RfTheme t) {
    final idx = _statusIndex(event.status);
    final isCancelled = event.status == 'cancelled';

    return GestureDetector(
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(
            builder: (_) => EventDetailScreen(eventId: event.id)),
      ),
      child: Container(
        margin: const EdgeInsets.only(bottom: 16),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withValues(alpha: 0.06),
              blurRadius: 20,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header row
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(event.name.isNotEmpty ? event.name : 'Evento sin nombre',
                          style: GoogleFonts.outfit(
                              fontSize: 17, fontWeight: FontWeight.w800,
                              color: t.textPrimary)),
                      const SizedBox(height: 4),
                      if (event.date != null)
                        Text(
                          '${event.date!.day}/${event.date!.month}/${event.date!.year}',
                          style: GoogleFonts.dmSans(
                              fontSize: 13, color: t.textMuted),
                        ),
                    ],
                  ),
                ),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: (isCancelled
                            ? AppColors.coral
                            : _statuses[idx].color)
                        .withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: (isCancelled
                              ? AppColors.coral
                              : _statuses[idx].color)
                          .withValues(alpha: 0.3),
                    ),
                  ),
                  child: Text(
                    isCancelled
                        ? 'Cancelada'
                        : _statuses[idx].label.replaceAll('\n', ' '),
                    style: GoogleFonts.dmSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: isCancelled
                          ? AppColors.coral
                          : _statuses[idx].color,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 18),
            // Progress bar
            if (!isCancelled) _buildProgressBar(idx, t),
            const SizedBox(height: 14),
            // Stats row
            Row(
              children: [
                _statChip(Icons.people_outline_rounded,
                    '${event.guestCount} invitados', t),
                const SizedBox(width: 10),
                _statChip(
                  Icons.attach_money_rounded,
                  'RD\$${event.budget.toStringAsFixed(0)}',
                  t,
                ),
              ],
            ),
            const SizedBox(height: 12),
            // Location
            if (event.location.isNotEmpty)
              Row(
                children: [
                  Icon(Icons.location_on_outlined,
                      size: 14, color: t.textDim),
                  const SizedBox(width: 4),
                  Expanded(
                    child: Text(event.location,
                        style: GoogleFonts.dmSans(
                            fontSize: 12, color: t.textDim),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildProgressBar(int currentIdx, RfTheme t) {
    return Row(
      children: [
        for (var i = 0; i < _statuses.length; i++) ...[
          _buildDot(_statuses[i], i <= currentIdx, t),
          if (i < _statuses.length - 1)
            Expanded(
              child: Container(
                height: 2,
                margin: const EdgeInsets.only(bottom: 20),
                decoration: BoxDecoration(
                  gradient: i < currentIdx
                      ? const LinearGradient(
                          colors: [AppColors.teal, AppColors.hotPink])
                      : null,
                  color: i >= currentIdx
                      ? t.borderFaint.withValues(alpha: 0.4)
                      : null,
                  borderRadius: BorderRadius.circular(1),
                ),
              ),
            ),
        ],
      ],
    );
  }

  Widget _buildDot(_StatusInfo status, bool active, RfTheme t) {
    return Column(
      children: [
        Container(
          width: 28, height: 28,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: active ? status.color : Colors.transparent,
            border: Border.all(
              color: active ? status.color : t.borderFaint,
              width: 2,
            ),
          ),
          child: active
              ? Icon(status.icon, size: 14, color: Colors.white)
              : null,
        ),
        const SizedBox(height: 4),
        Text(
          status.shortLabel,
          style: GoogleFonts.dmSans(
            fontSize: 9,
            fontWeight: FontWeight.w600,
            color: active ? status.color : t.textDim,
          ),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }

  Widget _statChip(IconData icon, String text, RfTheme t) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withValues(alpha: 0.04)
            : const Color(0xFFF3F4F6),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: t.textMuted),
          const SizedBox(width: 5),
          Text(text,
              style: GoogleFonts.dmSans(
                  fontSize: 12, fontWeight: FontWeight.w600,
                  color: t.textMuted)),
        ],
      ),
    );
  }
}

class _StatusInfo {
  final String key;
  final String label;
  final String shortLabel;
  final IconData icon;
  final Color color;

  const _StatusInfo(this.key, this.label, this.shortLabel, this.icon, this.color);
}
