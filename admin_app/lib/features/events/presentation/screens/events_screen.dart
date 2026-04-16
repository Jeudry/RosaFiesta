import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../providers/events_provider.dart';

class EventsScreen extends StatefulWidget {
  const EventsScreen({super.key});

  @override
  State<EventsScreen> createState() => _EventsScreenState();
}

class _EventsScreenState extends State<EventsScreen> {
  final _searchController = TextEditingController();
  String? _statusFilter;

  final _statuses = [
    {'value': null, 'label': 'Todos'},
    {'value': 'draft', 'label': 'Borrador'},
    {'value': 'pending_quote', 'label': 'Pendiente Cotización'},
    {'value': 'pending', 'label': 'Pendiente'},
    {'value': 'confirmed', 'label': 'Confirmado'},
    {'value': 'paid', 'label': 'Pagado'},
    {'value': 'completed', 'label': 'Completado'},
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<EventsProvider>().loadEvents(refresh: true);
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<EventsProvider>();

    return AdminScaffold(
      title: 'Eventos',
      body: Column(
        children: [
          // Search and filters
          Container(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                AdminSearchField(
                  controller: _searchController,
                  hint: 'Buscar por cliente, dirección...',
                  onChanged: (q) => provider.search(q),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  height: 36,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: _statuses.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 8),
                    itemBuilder: (ctx, i) {
                      final s = _statuses[i];
                      final isSelected = _statusFilter == s['value'];
                      return FilterChip(
                        label: Text(s['label'] as String),
                        selected: isSelected,
                        onSelected: (_) {
                          setState(() => _statusFilter = s['value'] as String?);
                          provider.setStatusFilter(s['value'] as String?);
                        },
                        selectedColor: AppColors.primary.withValues(alpha: 0.2),
                        checkmarkColor: AppColors.primary,
                      );
                    },
                  ),
                ),
              ],
            ),
          ),

          // Events list
          Expanded(
            child: provider.events.isEmpty && !provider.loading
                ? EmptyState(
                    icon: Icons.event_outlined,
                    title: 'No hay eventos',
                    subtitle: 'Crea un nuevo evento para comenzar',
                    action: AdminButton(
                      label: 'Crear Evento',
                      onTap: () => Navigator.pushNamed(context, '/events/create'),
                    ),
                  )
                : NotificationListener<ScrollNotification>(
                    onNotification: (scroll) {
                      if (scroll is ScrollEndNotification && scroll.metrics.extentAfter < 200) {
                        provider.loadEvents();
                      }
                      return false;
                    },
                    child: RefreshIndicator(
                      onRefresh: () => provider.loadEvents(refresh: true),
                      child: ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        itemCount: provider.events.length + (provider.loading ? 1 : 0),
                        itemBuilder: (ctx, i) {
                          if (i == provider.events.length) {
                            return const Center(
                              child: Padding(
                                padding: EdgeInsets.all(16),
                                child: CircularProgressIndicator(),
                              ),
                            );
                          }
                          return _EventCard(event: provider.events[i]);
                        },
                      ),
                    ),
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.pushNamed(context, '/events/create'),
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _EventCard extends StatelessWidget {
  final Map<String, dynamic> event;

  const _EventCard({required this.event});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => Navigator.pushNamed(context, '/events/${event['id']}'),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          event['client_name'] ?? 'Cliente sin nombre',
                          style: GoogleFonts.dmSans(fontSize: 15, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          event['address'] ?? 'Sin dirección',
                          style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                        ),
                      ],
                    ),
                  ),
                  _buildStatusBadge(event['status']),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(Icons.calendar_today, size: 14, color: AppColors.textMuted),
                  const SizedBox(width: 6),
                  Text(
                    event['date'] ?? '',
                    style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textSecondary),
                  ),
                  if (event['time'] != null) ...[
                    const SizedBox(width: 16),
                    Icon(Icons.access_time, size: 14, color: AppColors.textMuted),
                    const SizedBox(width: 6),
                    Text(
                      event['time'],
                      style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textSecondary),
                    ),
                  ],
                  const Spacer(),
                  if (event['total'] != null)
                    Text(
                      'RD\$${event['total']}',
                      style: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.w600, color: AppColors.primary),
                    ),
                ],
              ),
              if (event['pending_amount'] != null && event['pending_amount'] > 0) ...[
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.warning.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    'Pendiente: RD\$${event['pending_amount']}',
                    style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.warning),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusBadge(String? status) {
    switch (status) {
      case 'pending':
        return StatusBadge.pending();
      case 'pending_quote':
        return StatusBadge(label: 'Cotización', color: AppColors.violet);
      case 'confirmed':
        return StatusBadge.confirmed();
      case 'paid':
        return StatusBadge.paid();
      case 'completed':
        return StatusBadge.completed();
      case 'adjusted':
        return StatusBadge(label: 'Ajustado', color: AppColors.violet);
      default:
        return StatusBadge.draft();
    }
  }
}
