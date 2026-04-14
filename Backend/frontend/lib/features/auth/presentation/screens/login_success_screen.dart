import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../auth_provider.dart';
import '../../data/models.dart';
import '../../../shell/main_shell.dart';
import '../../../events/data/events_repository.dart';
import '../../../events/data/event_photo_model.dart';

class LoginSuccessScreen extends StatelessWidget {
  const LoginSuccessScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final auth = context.watch<AuthProvider>();
    final events = auth.pendingEvents;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          RfGradientOrbs(
            controller: AnimationController(
              vsync: NavigatorState(),
              duration: const Duration(seconds: 12),
            )..repeat(),
            color1: AppColors.hotPink,
            color2: AppColors.violet,
            isDark: t.isDark,
          ),
          SafeArea(
            child: Column(
              children: [
                const SizedBox(height: 40),
                // Header
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  child: Column(
                    children: [
                      Container(
                        width: 72,
                        height: 72,
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(
                            colors: [AppColors.hotPink, AppColors.violet],
                          ),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Icon(
                          Icons.check_circle_rounded,
                          color: Colors.white,
                          size: 40,
                        ),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        '¡Bienvenido!',
                        style: GoogleFonts.outfit(
                          fontSize: 28,
                          fontWeight: FontWeight.w900,
                          color: t.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        auth.user?.email ?? '',
                        style: GoogleFonts.dmSans(
                          fontSize: 14,
                          color: t.textMuted,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 32),
                // Events section
                Expanded(
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20),
                    decoration: BoxDecoration(
                      color: t.isDark ? t.card.withOpacity(0.8) : Colors.white,
                      borderRadius: BorderRadius.circular(24),
                      border: Border.all(color: t.borderFaint),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.fromLTRB(20, 20, 20, 12),
                          child: Row(
                            children: [
                              Icon(
                                Icons.celebration_rounded,
                                color: AppColors.hotPink,
                                size: 22,
                              ),
                              const SizedBox(width: 8),
                              Text(
                                'Tus Eventos',
                                style: GoogleFonts.outfit(
                                  fontSize: 18,
                                  fontWeight: FontWeight.w700,
                                  color: t.textPrimary,
                                ),
                              ),
                            ],
                          ),
                        ),
                        if (events.any((e) => e.isApproved))
                          _PhotoGallerySection(
                            eventId: events.firstWhere((e) => e.isApproved).id,
                            t: t,
                          ),
                        const Divider(height: 1),
                        Expanded(
                          child: events.isEmpty
                              ? Center(
                                  child: Column(
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      Icon(
                                        Icons.event_available_rounded,
                                        color: t.textDim,
                                        size: 48,
                                      ),
                                      const SizedBox(height: 12),
                                      Text(
                                        'Sin eventos pendientes',
                                        style: GoogleFonts.dmSans(
                                          color: t.textMuted,
                                          fontSize: 14,
                                        ),
                                      ),
                                    ],
                                  ),
                                )
                              : ListView.separated(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 16,
                                    vertical: 8,
                                  ),
                                  itemCount: events.length,
                                  separatorBuilder: (_, __) =>
                                      const SizedBox(height: 8),
                                  itemBuilder: (context, i) =>
                                      _EventCard(event: events[i], t: t),
                                ),
                        ),
                        // Continue button
                        Padding(
                          padding: const EdgeInsets.all(16),
                          child: SizedBox(
                            width: double.infinity,
                            child: RfLuxeButton(
                              label: 'Continuar',
                              onTap: () {
                                Navigator.of(context).pushAndRemoveUntil(
                                  MaterialPageRoute(
                                    builder: (_) => const MainShell(),
                                  ),
                                  (route) => false,
                                );
                              },
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _EventCard extends StatelessWidget {
  final PendingEvent event;
  final RfTheme t;

  const _EventCard({required this.event, required this.t});

  @override
  Widget build(BuildContext context) {
    final statusColor = _statusColor(event.status);
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: t.isDark ? Colors.white.withOpacity(0.04) : const Color(0xFFF8F9FA),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: statusColor.withOpacity(0.15),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              _statusIcon(event.status),
              color: statusColor,
              size: 22,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  event.name.isNotEmpty ? event.name : 'Evento sin nombre',
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 2),
                Row(
                  children: [
                    if (event.date != null) ...[
                      Icon(
                        Icons.calendar_today_rounded,
                        size: 12,
                        color: t.textDim,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        event.date!,
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          color: t.textDim,
                        ),
                      ),
                      const SizedBox(width: 8),
                    ],
                    _StatusBadge(status: event.status),
                  ],
                ),
              ],
            ),
          ),
          if (event.isApproved)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF00C853), Color(0xFF00D4AA)],
                ),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                'Aprobado',
                style: GoogleFonts.dmSans(
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  color: Colors.white,
                ),
              ),
            ),
        ],
      ),
    );
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'confirmed':
      case 'paid':
        return const Color(0xFF00C853);
      case 'adjusted':
      case 'requested':
        return const Color(0xFFFFB800);
      case 'draft':
      case 'planning':
        return const Color(0xFF4FC3F7);
      case 'completed':
        return AppColors.teal;
      case 'cancelled':
        return AppColors.coral;
      default:
        return const Color(0xFF9E9E9E);
    }
  }

  IconData _statusIcon(String status) {
    switch (status) {
      case 'confirmed':
      case 'paid':
        return Icons.check_circle_rounded;
      case 'adjusted':
      case 'requested':
        return Icons.hourglass_top_rounded;
      case 'draft':
      case 'planning':
        return Icons.edit_calendar_rounded;
      case 'completed':
        return Icons.celebration_rounded;
      case 'cancelled':
        return Icons.cancel_rounded;
      default:
        return Icons.event_rounded;
    }
  }
}

class _StatusBadge extends StatelessWidget {
  final String status;

  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    String label;
    Color bgColor;

    switch (status) {
      case 'confirmed':
        label = 'Confirmado';
        bgColor = const Color(0xFF00C853);
        break;
      case 'paid':
        label = 'Pagado';
        bgColor = AppColors.teal;
        break;
      case 'adjusted':
        label = 'Por pagar';
        bgColor = const Color(0xFFFFB800);
        break;
      case 'requested':
        label = 'Pendiente';
        bgColor = const Color(0xFFFFB800);
        break;
      case 'draft':
        label = 'Borrador';
        bgColor = const Color(0xFF4FC3F7);
        break;
      case 'planning':
        label = 'Planificando';
        bgColor = const Color(0xFF4FC3F7);
        break;
      case 'completed':
        label = 'Completado';
        bgColor = AppColors.teal;
        break;
      case 'cancelled':
        label = 'Cancelado';
        bgColor = AppColors.coral;
        break;
      default:
        label = status;
        bgColor = const Color(0xFF9E9E9E);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: bgColor.withOpacity(0.15),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: GoogleFonts.dmSans(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: bgColor,
        ),
      ),
    );
  }
}

class _PhotoGallerySection extends StatefulWidget {
  final String eventId;
  final RfTheme t;

  const _PhotoGallerySection({required this.eventId, required this.t});

  @override
  State<_PhotoGallerySection> createState() => _PhotoGallerySectionState();
}

class _PhotoGallerySectionState extends State<_PhotoGallerySection> {
  final _repository = EventsRepository();
  List<EventPhoto> _photos = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadPhotos();
  }

  Future<void> _loadPhotos() async {
    try {
      final photos = await _repository.getEventPhotos(widget.eventId);
      if (mounted) {
        setState(() {
          _photos = photos;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Padding(
        padding: EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        child: SizedBox(
          height: 100,
          child: Center(child: CircularProgressIndicator()),
        ),
      );
    }

    if (_photos.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
          child: Row(
            children: [
              Icon(
                Icons.photo_library_rounded,
                color: AppColors.amber,
                size: 18,
              ),
              const SizedBox(width: 6),
              Text(
                'Fotos del evento',
                style: GoogleFonts.dmSans(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: widget.t.textMuted,
                ),
              ),
            ],
          ),
        ),
        SizedBox(
          height: 100,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 20),
            itemCount: _photos.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, i) {
              final photo = _photos[i];
              return ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.network(
                  photo.url,
                  width: 100,
                  height: 100,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    width: 100,
                    height: 100,
                    color: widget.t.borderFaint,
                    child: Icon(
                      Icons.broken_image_rounded,
                      color: widget.t.textDim,
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        const SizedBox(height: 12),
        const Divider(height: 1),
      ],
    );
  }
}