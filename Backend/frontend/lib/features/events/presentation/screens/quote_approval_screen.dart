import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/api_client.dart';
import 'package:frontend/features/events/presentation/events_provider.dart';
import 'package:frontend/features/events/data/event_model.dart';
import 'package:intl/intl.dart';

class QuoteApprovalScreen extends StatefulWidget {
  final String eventId;

  const QuoteApprovalScreen({super.key, required this.eventId});

  @override
  State<QuoteApprovalScreen> createState() => _QuoteApprovalScreenState();
}

class _QuoteApprovalScreenState extends State<QuoteApprovalScreen> {
  Event? _event;
  List<EventItem> _items = [];
  bool _isLoading = true;
  String? _error;
  bool _approveLoading = false;
  bool _rejectLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchData();
  }

  Future<void> _fetchData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final event = await context
          .read<EventsProvider>()
          .fetchEventDetails(widget.eventId);
      final items = await ApiClient.get('/events/${widget.eventId}/items');
      final List<dynamic> itemsList = items;
      setState(() {
        _event = event;
        _items = itemsList.map((json) => EventItem.fromJson(json)).toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _approveQuote() async {
    setState(() => _approveLoading = true);
    try {
      await ApiClient.post('/events/${widget.eventId}/approve-quote', {});
      if (mounted) _showSuccessDialog();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error al aprobar: ${e.toString()}'),
            backgroundColor: AppColors.coral,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _approveLoading = false);
    }
  }

  Future<void> _rejectQuote() async {
    setState(() => _rejectLoading = true);
    try {
      await ApiClient.post('/events/${widget.eventId}/reject-quote', {});
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Cotización rechazada'),
            backgroundColor: AppColors.coral,
          ),
        );
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error al rechazar: ${e.toString()}'),
            backgroundColor: AppColors.coral,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _rejectLoading = false);
    }
  }

  void _showSuccessDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => Dialog(
        backgroundColor: Colors.transparent,
        child: Container(
          padding: const EdgeInsets.all(32),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            color: t.isDark ? AppColors.darkCard : Colors.white,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 80,
                height: 80,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: AppColors.buttonGradient,
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.hotPink.withValues(alpha: 0.4),
                      blurRadius: 24,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.check_rounded,
                  color: Colors.white,
                  size: 40,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                '¡Cotización aprobada!',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 22,
                  fontWeight: FontWeight.w700,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                'Tu evento ha sido confirmado. Te enviaremos los detalles por WhatsApp.',
                style: GoogleFonts.dmSans(
                  color: t.textMuted,
                  fontSize: 14,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 28),
              GestureDetector(
                onTap: () {
                  Navigator.of(context).pop();
                  Navigator.of(context).pop();
                },
                child: Container(
                  width: double.infinity,
                  height: 52,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    gradient: AppColors.buttonGradient,
                  ),
                  child: Center(
                    child: Text(
                      'Aceptar',
                      style: GoogleFonts.outfit(
                        color: Colors.white,
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                      ),
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

  late final RfTheme t;

  @override
  Widget build(BuildContext context) {
    t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              color: t.isDark
                  ? Colors.white.withValues(alpha: 0.06)
                  : Colors.black.withValues(alpha: 0.04),
            ),
            child: Icon(Icons.arrow_back_ios_new_rounded,
                color: t.textPrimary, size: 18),
          ),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          'Tu Cotización',
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.w700,
          ),
        ),
        centerTitle: true,
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.hotPink),
      );
    }
    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.error_outline_rounded,
                  color: AppColors.coral, size: 48),
              const SizedBox(height: 16),
              Text(
                _error!,
                style: GoogleFonts.dmSans(color: t.textMuted),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      );
    }
    if (_event == null) {
      return const SizedBox.shrink();
    }

    final status = _event!.status;
    if (status != 'adjusted') {
      return _buildStatusBanner(status);
    }
    return _buildApprovalUI();
  }

  Widget _buildStatusBanner(String status) {
    Color bannerColor;
    Color iconColor;
    String icon;
    String message;

    switch (status) {
      case 'paid':
        bannerColor = AppColors.teal;
        iconColor = Colors.white;
        icon = '✓';
        message = 'Cotización aprobada';
        break;
      case 'rejected':
        bannerColor = AppColors.coral;
        iconColor = Colors.white;
        icon = '✗';
        message = 'Cotización rechazada';
        break;
      default:
        bannerColor = t.isDark
            ? Colors.white.withValues(alpha: 0.08)
            : Colors.black.withValues(alpha: 0.06);
        iconColor = t.textMuted;
        icon = '●';
        message = 'Estado: ${status.toUpperCase()}';
    }

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          const Spacer(),
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: bannerColor,
            ),
            child: Center(
              child: Text(
                icon,
                style: TextStyle(color: iconColor, fontSize: 36),
              ),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            message,
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.w700,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),
          Text(
            'Ya no hay acciones disponibles para esta cotización.',
            style: GoogleFonts.dmSans(
              color: t.textMuted,
              fontSize: 14,
            ),
            textAlign: TextAlign.center,
          ),
          const Spacer(),
        ],
      ),
    );
  }

  Widget _buildApprovalUI() {
    final dateFormat = DateFormat('EEEE, d MMMM yyyy', 'es');

    // Calculate totals
    double subtotal = 0;
    for (var item in _items) {
      subtotal += item.lineTotal;
    }
    final additionalCosts = _event!.additionalCosts;
    final total = subtotal + additionalCosts;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Event info card
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              color: t.card,
              border: Border.all(color: t.borderFaint),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _event!.name,
                  style: GoogleFonts.outfit(
                    color: t.textPrimary,
                    fontSize: 20,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                if (_event!.date != null) ...[
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Icon(Icons.calendar_today_rounded,
                          color: t.textDim, size: 16),
                      const SizedBox(width: 8),
                      Text(
                        dateFormat.format(_event!.date!),
                        style: GoogleFonts.dmSans(
                          color: t.textMuted,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ],
                const SizedBox(height: 6),
                Row(
                  children: [
                    Icon(Icons.location_on_rounded,
                        color: t.textDim, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _event!.location,
                        style: GoogleFonts.dmSans(
                          color: t.textMuted,
                          fontSize: 14,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Row(
                  children: [
                    Icon(Icons.people_rounded, color: t.textDim, size: 16),
                    const SizedBox(width: 8),
                    Text(
                      '${_event!.guestCount} invitados',
                      style: GoogleFonts.dmSans(
                        color: t.textMuted,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Items list
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              color: t.card,
              border: Border.all(color: t.borderFaint),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Artículos',
                  style: GoogleFonts.outfit(
                    color: t.textPrimary,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 16),
                ..._items.map((item) => _buildItemRow(item)),
                Divider(color: t.borderFaint, height: 32),
                _buildTotalRow('Subtotal', subtotal),
                if (additionalCosts > 0)
                  _buildTotalRow('Costos adicionales', additionalCosts),
                const SizedBox(height: 12),
                _buildGrandTotalRow('Total', total),
              ],
            ),
          ),

          // Admin notes
          if (_event!.adminNotes != null &&
              _event!.adminNotes!.isNotEmpty) ...[
            const SizedBox(height: 20),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                color: t.isDark
                    ? AppColors.amber.withValues(alpha: 0.08)
                    : AppColors.amber.withValues(alpha: 0.12),
                border: Border.all(
                  color: AppColors.amber.withValues(alpha: 0.3),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(Icons.info_outline_rounded,
                          color: AppColors.amber, size: 18),
                      const SizedBox(width: 8),
                      Text(
                        'Notas del administrador',
                        style: GoogleFonts.outfit(
                          color: AppColors.amber,
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(
                    _event!.adminNotes!,
                    style: GoogleFonts.dmSans(
                      color: t.textMuted,
                      fontSize: 14,
                    ),
                  ),
                ],
              ),
            ),
          ],

          const SizedBox(height: 32),

          // Action buttons
          _buildApproveButton(),
          const SizedBox(height: 12),
          _buildRejectButton(),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildItemRow(EventItem item) {
    final name = item.variant?.name ?? item.article?.nameTemplate ?? 'Artículo';
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: GoogleFonts.dmSans(
                    color: t.textPrimary,
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  '${item.quantity} × \$${item.unitPrice.toStringAsFixed(2)}',
                  style: GoogleFonts.dmSans(
                    color: t.textDim,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          Text(
            '\$${item.lineTotal.toStringAsFixed(2)}',
            style: GoogleFonts.dmSans(
              color: t.textPrimary,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTotalRow(String label, double amount) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: GoogleFonts.dmSans(
              color: t.textMuted,
              fontSize: 14,
            ),
          ),
          Text(
            '\$${amount.toStringAsFixed(2)}',
            style: GoogleFonts.dmSans(
              color: t.textMuted,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGrandTotalRow(String label, double amount) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.w700,
          ),
        ),
        ShaderMask(
          shaderCallback: (bounds) =>
              AppColors.titleGradient.createShader(bounds),
          child: Text(
            '\$${amount.toStringAsFixed(2)}',
            style: GoogleFonts.outfit(
              color: Colors.white,
              fontSize: 22,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildApproveButton() {
    return GestureDetector(
      onTap: _approveLoading ? null : _approveQuote,
      child: Container(
        width: double.infinity,
        height: 54,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: const LinearGradient(
            colors: [AppColors.teal, Color(0xFF00A87A)],
            begin: Alignment.centerLeft,
            end: Alignment.centerRight,
          ),
          boxShadow: [
            BoxShadow(
              color: AppColors.teal.withValues(alpha: 0.35),
              blurRadius: 20,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Center(
          child: _approveLoading
              ? const SizedBox(
                  width: 22,
                  height: 22,
                  child: CircularProgressIndicator(
                      color: Colors.white, strokeWidth: 2.5),
                )
              : Text(
                  'Aprobar Cotización',
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

  Widget _buildRejectButton() {
    return GestureDetector(
      onTap: _rejectLoading ? null : _rejectQuote,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 400),
        width: double.infinity,
        height: 54,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          color: t.isDark
              ? Colors.white.withValues(alpha: 0.04)
              : Colors.black.withValues(alpha: 0.04),
          border: Border.all(color: t.borderFaint, width: 1.5),
        ),
        alignment: Alignment.center,
        child: _rejectLoading
            ? SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                    color: t.textPrimary, strokeWidth: 2.5),
              )
            : Text(
                'Rechazar',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  letterSpacing: 0.5,
                ),
              ),
      ),
    );
  }
}