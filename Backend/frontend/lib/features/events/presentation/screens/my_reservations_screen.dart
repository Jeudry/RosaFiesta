import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/api_client.dart';
import 'package:frontend/core/config/env_config.dart';
import 'event_detail_screen.dart';

class ReservationSummary {
  final String id;
  final String name;
  final String? date;
  final String status;
  final String paymentStatus;
  final int totalQuote;
  final int depositPaid;
  final int remaining;
  final bool contractReady;
  final bool receiptReady;
  final bool hasPhotos;
  final int guestCount;
  final bool reviewGiven;
  final String location;

  ReservationSummary({
    required this.id,
    required this.name,
    this.date,
    required this.status,
    required this.paymentStatus,
    required this.totalQuote,
    required this.depositPaid,
    required this.remaining,
    required this.contractReady,
    required this.receiptReady,
    required this.hasPhotos,
    required this.guestCount,
    required this.reviewGiven,
    required this.location,
  });

  factory ReservationSummary.fromJson(Map<String, dynamic> json) {
    return ReservationSummary(
      id: json['id'],
      name: json['name'] ?? '',
      date: json['date'],
      status: json['status'] ?? '',
      paymentStatus: json['payment_status'] ?? '',
      totalQuote: json['total_quote'] ?? 0,
      depositPaid: json['deposit_paid'] ?? 0,
      remaining: json['remaining'] ?? 0,
      contractReady: json['contract_ready'] ?? false,
      receiptReady: json['receipt_ready'] ?? false,
      hasPhotos: json['has_photos'] ?? false,
      guestCount: json['guest_count'] ?? 0,
      reviewGiven: json['review_given'] ?? false,
      location: json['location'] ?? '',
    );
  }

  DateTime? get eventDate {
    if (date == null || date!.isEmpty) return null;
    try {
      return DateTime.parse(date!);
    } catch (_) {
      return null;
    }
  }

  bool get isPast {
    final d = eventDate;
    if (d == null) return false;
    return d.isBefore(DateTime.now());
  }

  bool get isUpcoming => !isPast;

  String get statusLabel {
    switch (status) {
      case 'planning': return 'Planeando';
      case 'requested': return 'En Revision';
      case 'adjusted': return 'Cotización Lista';
      case 'confirmed': return 'Confirmado';
      case 'paid': return 'Pagado';
      case 'completed': return 'Completado';
      case 'cancelled': return 'Cancelado';
      case 'rejected': return 'Rechazado';
      default: return status;
    }
  }

  Color statusColor(RfTheme t) {
    switch (status) {
      case 'planning': return AppColors.sky;
      case 'requested': return AppColors.amber;
      case 'adjusted': return AppColors.coral;
      case 'confirmed': return AppColors.teal;
      case 'paid': return AppColors.hotPink;
      case 'completed': return AppColors.violet;
      case 'cancelled': return AppColors.coral;
      case 'rejected': return AppColors.coral;
      default: return t.textDim;
    }
  }
}

class MyReservationsScreen extends StatefulWidget {
  const MyReservationsScreen({super.key});

  static void open(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const MyReservationsScreen()),
    );
  }

  @override
  State<MyReservationsScreen> createState() => _MyReservationsScreenState();
}

class _MyReservationsScreenState extends State<MyReservationsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<ReservationSummary> _reservations = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _fetchReservations();
  }

  Future<void> _fetchReservations() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final response = await ApiClient.get('/events/my-reservations');
      final List<dynamic> data = response as List<dynamic>;
      setState(() {
        _reservations = data.map((json) => ReservationSummary.fromJson(json)).toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  List<ReservationSummary> get _filteredReservations {
    switch (_tabController.index) {
      case 0:
        return _reservations.where((r) => r.isUpcoming).toList();
      case 1:
        return _reservations.where((r) => r.isPast).toList();
      case 2:
      default:
        return _reservations;
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l = AppLocalizations.of(context)!;
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            l.myReservations,
            style: GoogleFonts.outfit(
              fontSize: 22,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ),
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppColors.hotPink,
          unselectedLabelColor: t.textDim,
          indicatorColor: AppColors.hotPink,
          indicatorWeight: 3,
          tabs: [
            Tab(text: l.upcoming),
            Tab(text: l.past),
            Tab(text: l.all),
          ],
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!, style: GoogleFonts.dmSans(color: t.textPrimary)))
              : _filteredReservations.isEmpty
                  ? _buildEmptyState(l, t)
                  : _buildList(t, l),
    );
  }

  Widget _buildEmptyState(AppLocalizations l, RfTheme t) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                color: AppColors.hotPink.withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.event_note, size: 48, color: AppColors.hotPink),
            ),
            const SizedBox(height: 24),
            Text(
              l.noReservationsYet,
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 16,
                color: t.textMuted,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 24),
            GestureDetector(
              onTap: () {
                Navigator.pop(context);
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.hotPink, AppColors.violet],
                  ),
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Text(
                  l.exploreCatalog,
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildList(RfTheme t, AppLocalizations l) {
    return RefreshIndicator(
      onRefresh: _fetchReservations,
      child: ListView.separated(
        padding: const EdgeInsets.all(20),
        itemCount: _filteredReservations.length,
        separatorBuilder: (_, __) => const SizedBox(height: 16),
        itemBuilder: (context, index) {
          return _ReservationCard(
            reservation: _filteredReservations[index],
            t: t,
            l: l,
          );
        },
      ),
    );
  }
}

class _ReservationCard extends StatelessWidget {
  final ReservationSummary reservation;
  final RfTheme t;
  final AppLocalizations l;

  const _ReservationCard({
    required this.reservation,
    required this.t,
    required this.l,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => EventDetailScreen(eventId: reservation.id),
        ),
      ),
      child: Container(
        decoration: BoxDecoration(
          color: t.card,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.04),
              blurRadius: 16,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header row
                  Row(
                    children: [
                      Container(
                        width: 52,
                        height: 52,
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(
                            colors: [AppColors.hotPink, AppColors.violet],
                          ),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: const Icon(Icons.event, color: Colors.white, size: 26),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              reservation.name,
                              style: GoogleFonts.outfit(
                                fontSize: 17,
                                fontWeight: FontWeight.w700,
                                color: t.textPrimary,
                              ),
                            ),
                            const SizedBox(height: 2),
                            if (reservation.date != null && reservation.eventDate != null)
                              Text(
                                '${reservation.eventDate!.day}/${reservation.eventDate!.month}/${reservation.eventDate!.year}',
                                style: GoogleFonts.dmSans(
                                  fontSize: 13,
                                  color: AppColors.hotPink,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                          ],
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                        decoration: BoxDecoration(
                          color: reservation.statusColor(t).withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          reservation.statusLabel,
                          style: GoogleFonts.dmSans(
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                            color: reservation.statusColor(t),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),

                  // Location
                  if (reservation.location.isNotEmpty)
                    Row(
                      children: [
                        Icon(Icons.location_on_outlined, size: 16, color: t.textDim),
                        const SizedBox(width: 4),
                        Expanded(
                          child: Text(
                            reservation.location,
                            style: GoogleFonts.dmSans(fontSize: 13, color: t.textMuted),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  const SizedBox(height: 12),

                  // Payment progress bar
                  if (reservation.totalQuote > 0) ...[
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          l.paymentProgress(reservation.depositPaid, reservation.remaining),
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: t.textMuted,
                          ),
                        ),
                        Text(
                          'RD\$${reservation.totalQuote}',
                          style: GoogleFonts.outfit(
                            fontSize: 14,
                            fontWeight: FontWeight.w800,
                            color: AppColors.hotPink,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(6),
                      child: LinearProgressIndicator(
                        value: reservation.totalQuote > 0
                            ? reservation.depositPaid / reservation.totalQuote
                            : 0,
                        minHeight: 6,
                        backgroundColor: t.borderFaint,
                        valueColor: const AlwaysStoppedAnimation(AppColors.teal),
                      ),
                    ),
                    const SizedBox(height: 12),
                  ],

                  // Action buttons row
                  Row(
                    children: [
                      if (reservation.contractReady)
                        Expanded(
                          child: _ActionButton(
                            icon: Icons.description_outlined,
                            label: l.downloadContract,
                            onTap: () => _downloadContract(context),
                            color: AppColors.violet,
                            t: t,
                          ),
                        ),
                      if (reservation.contractReady && reservation.hasPhotos)
                        const SizedBox(width: 8),
                      if (reservation.hasPhotos)
                        Expanded(
                          child: _ActionButton(
                            icon: Icons.photo_library_outlined,
                            label: l.viewPhotos,
                            onTap: () => _viewPhotos(context),
                            color: AppColors.sky,
                            t: t,
                          ),
                        ),
                      if ((reservation.hasPhotos || reservation.contractReady) &&
                          (reservation.status == 'completed' && !reservation.reviewGiven))
                        const SizedBox(width: 8),
                      if (reservation.status == 'completed' && !reservation.reviewGiven)
                        Expanded(
                          child: _ActionButton(
                            icon: Icons.star_outline,
                            label: l.leaveReview,
                            onTap: () => _leaveReview(context),
                            color: AppColors.amber,
                            t: t,
                          ),
                        ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _downloadContract(BuildContext context) {
    final url = '${EnvConfig.apiUrl}/events/${reservation.id}/contract';
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Descargando contrato...', style: GoogleFonts.dmSans()),
        backgroundColor: AppColors.violet,
      ),
    );
    // In a real app, you would use url_launcher or dio to download the PDF
  }

  void _viewPhotos(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => EventDetailScreen(eventId: reservation.id),
      ),
    );
  }

  void _leaveReview(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => EventDetailScreen(eventId: reservation.id),
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final Color color;
  final RfTheme t;

  const _ActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
    required this.color,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 10),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withValues(alpha: 0.2)),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 16, color: color),
            const SizedBox(width: 6),
            Flexible(
              child: Text(
                label,
                style: GoogleFonts.dmSans(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: color,
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}