import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/features/events/presentation/events_provider.dart';
import 'package:frontend/features/shell/main_shell.dart';
import 'package:frontend/core/services/share_service.dart';

class OrderConfirmationScreen extends StatefulWidget {
  final String eventId;

  const OrderConfirmationScreen({super.key, required this.eventId});

  @override
  State<OrderConfirmationScreen> createState() => _OrderConfirmationScreenState();
}

class _OrderConfirmationScreenState extends State<OrderConfirmationScreen>
    with TickerProviderStateMixin {
  late final AnimationController _floatCtrl;
  late final AnimationController _decoCtrl;
  late final AnimationController _pulseCtrl;
  late final AnimationController _gradCtrl;
  late final AnimationController _checkCtrl;

  final ShareService _shareService = ShareService();

  @override
  void initState() {
    super.initState();
    _floatCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 5500))..repeat();
    _decoCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 20))..repeat();
    _pulseCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 3))..repeat(reverse: true);
    _gradCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 12))..repeat();
    _checkCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
  }

  @override
  void dispose() {
    _floatCtrl.dispose();
    _decoCtrl.dispose();
    _pulseCtrl.dispose();
    _gradCtrl.dispose();
    _checkCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(children: [
        RfGradientOrbs(
          controller: _gradCtrl,
          color1: AppColors.teal,
          color2: AppColors.hotPink,
          isDark: t.isDark,
        ),
        RfDecoLayer(
          floatController: _floatCtrl,
          decoController: _decoCtrl,
          pulseController: _pulseCtrl,
          baseOpacity: t.isDark ? 1.0 : 1.8,
        ),
        Positioned.fill(child: IgnorePointer(child: CustomPaint(
          painter: RfGridPainter(
            color: (t.isDark ? Colors.white : Colors.black).withValues(alpha: 0.015),
          ),
        ))),
        SafeArea(
          child: Column(children: [
            // Top bar
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  RfThemeToggle(t: t),
                ],
              ),
            ),
            // Content
            Expanded(
              child: Consumer<EventsProvider>(
                builder: (context, provider, _) {
                  final event = _findEvent(provider);
                  if (event == null && !provider.isLoading) {
                    return _buildEventNotFound(t);
                  }
                  if (event == null) {
                    return const Center(child: CircularProgressIndicator());
                  }
                  return SingleChildScrollView(
                    physics: const BouncingScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(28, 32, 28, 40),
                    child: Column(
                      children: [
                        _buildSuccessAnimation(),
                        const SizedBox(height: 32),
                        _buildTitle(t),
                        const SizedBox(height: 24),
                        _buildEventCard(event, t, provider),
                        const SizedBox(height: 24),
                        _buildActionButtons(t, event),
                      ],
                    ),
                  );
                },
              ),
            ),
          ]),
        ),
      ]),
    );
  }

  dynamic _findEvent(EventsProvider provider) {
    try {
      return provider.events.firstWhere((e) => e.id == widget.eventId);
    } catch (_) {
      return null;
    }
  }

  Widget _buildSuccessAnimation() {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.0, end: 1.0),
      duration: const Duration(milliseconds: 800),
      builder: (context, value, child) {
        return Container(
          width: 120,
          height: 120,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                AppColors.teal.withValues(alpha: value),
                AppColors.sky.withValues(alpha: value),
              ],
            ),
            boxShadow: [
              BoxShadow(
                color: AppColors.teal.withValues(alpha: 0.4 * value),
                blurRadius: 30,
                spreadRadius: 5,
              ),
            ],
          ),
          child: Center(
            child: Icon(
              Icons.check_rounded,
              color: Colors.white.withValues(alpha: value),
              size: 60 * value,
            ),
          ),
        );
      },
    );
  }

  Widget _buildTitle(RfTheme t) {
    return ShaderMask(
      shaderCallback: (b) => AppColors.titleGradient.createShader(b),
      child: Text(
        '¡Evento confirmado!',
        style: GoogleFonts.outfit(
          fontSize: 32,
          fontWeight: FontWeight.w800,
          color: Colors.white,
          height: 1.15,
          letterSpacing: -0.5,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildEventCard(dynamic event, RfTheme t, EventsProvider provider) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: t.isDark ? Colors.white.withValues(alpha: 0.06) : Colors.white.withValues(alpha: 0.93),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: t.isDark ? 0.0 : 0.07),
            blurRadius: 28,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          // Event name
          Row(
            children: [
              Container(
                width: 50,
                height: 50,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.hotPink, AppColors.violet],
                  ),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: const Icon(Icons.celebration_rounded, color: Colors.white, size: 24),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      event.name.isEmpty ? 'Mi Evento' : event.name,
                      style: GoogleFonts.outfit(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        color: t.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _getStatusLabel(event.status),
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: AppColors.teal,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          // Divider
          Container(height: 1, color: t.borderFaint),
          const SizedBox(height: 20),
          // Event date
          if (event.eventDate != null) ...[
            _buildInfoRow(
              Icons.calendar_today_rounded,
              'Fecha',
              _formatDate(event.eventDate!),
              t,
            ),
            const SizedBox(height: 12),
          ],
          // Items count
          _buildInfoRow(
            Icons.inventory_2_outlined,
            'Artículos',
            '${provider.currentEventItems.length} items',
            t,
          ),
          const SizedBox(height: 12),
          // Total paid
          _buildInfoRow(
            Icons.payments_rounded,
            'Total pagado',
            'RD\$${_calculateTotal(provider).toStringAsFixed(0)}',
            t,
            valueColor: AppColors.teal,
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value, RfTheme t, {Color? valueColor}) {
    return Row(
      children: [
        Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            color: t.isDark ? Colors.white.withValues(alpha: 0.06) : const Color(0xFFF5F6FF),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: t.textDim, size: 18),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Text(
            label,
            style: GoogleFonts.dmSans(
              fontSize: 13,
              fontWeight: FontWeight.w500,
              color: t.textMuted,
            ),
          ),
        ),
        Text(
          value,
          style: GoogleFonts.dmSans(
            fontSize: 14,
            fontWeight: FontWeight.w700,
            color: valueColor ?? t.textPrimary,
          ),
        ),
      ],
    );
  }

  Widget _buildActionButtons(RfTheme t, dynamic event) {
    return Column(
      children: [
        // Share button
        GestureDetector(
          onTap: () => _shareEvent(event),
          child: Container(
            width: double.infinity,
            height: 54,
            decoration: BoxDecoration(
              color: t.isDark ? Colors.white.withValues(alpha: 0.06) : Colors.white.withValues(alpha: 0.93),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: t.borderFaint),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.share_rounded, color: t.textPrimary, size: 20),
                const SizedBox(width: 8),
                Text(
                  'Compartir',
                  style: GoogleFonts.dmSans(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: t.textPrimary,
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 14),
        // View my events button
        RfLuxeButton(
          label: 'Ver mis eventos',
          onTap: () => _navigateToEvents(),
        ),
        const SizedBox(height: 14),
        // Back to catalog
        RfLuxeButton(
          label: 'Volver al catálogo',
          onTap: () => _navigateToCatalog(),
          filled: false,
          t: t,
        ),
      ],
    );
  }

  Widget _buildEventNotFound(RfTheme t) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: AppColors.coral.withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.event_busy_rounded, color: AppColors.coral, size: 40),
            ),
            const SizedBox(height: 24),
            Text(
              'Evento no encontrado',
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'No pudimos encontrar los detalles de este evento.',
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 14,
                color: t.textDim,
              ),
            ),
            const SizedBox(height: 32),
            RfLuxeButton(
              label: 'Volver al inicio',
              onTap: () => _navigateToEvents(),
            ),
          ],
        ),
      ),
    );
  }

  String _getStatusLabel(String status) {
    switch (status) {
      case 'planning': return 'Borrador';
      case 'requested': return 'En revisión';
      case 'adjusted': return 'Cotización lista';
      case 'confirmed': return 'Confirmado';
      case 'paid': return 'Pagado';
      case 'completed': return 'Finalizado';
      default: return status;
    }
  }

  String _formatDate(DateTime date) {
    final months = [
      'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
      'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'
    ];
    return '${date.day} ${months[date.month - 1]} ${date.year}';
  }

  double _calculateTotal(EventsProvider provider) {
    double total = 0;
    for (var item in provider.currentEventItems) {
      if (item.price != null) {
        total += item.price! * item.quantity;
      }
    }
    return total;
  }

  Future<void> _shareEvent(dynamic event) async {
    final eventName = event.name.isEmpty ? 'Mi Evento' : event.name;
    final eventDate = event.eventDate != null ? _formatDate(event.eventDate!) : 'Fecha por confirmar';
    await _shareService.shareEvent(
      eventName: eventName,
      eventDate: eventDate,
      location: '',
      itemCount: context.read<EventsProvider>().currentEventItems.length,
      totalEstimate: _calculateTotal(context.read<EventsProvider>()),
      eventId: event.id,
    );
  }

  void _navigateToEvents() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShell(initialIndex: 1)),
      (route) => false,
    );
  }

  void _navigateToCatalog() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShell(initialIndex: 0)),
      (route) => false,
    );
  }
}