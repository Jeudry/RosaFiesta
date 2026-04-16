import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:intl/intl.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../providers/dashboard_provider.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  DateTime _focusedDay = DateTime.now();
  DateTime _selectedDay = DateTime.now();
  CalendarFormat _calendarFormat = CalendarFormat.week;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DashboardProvider>().loadDashboard();
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DashboardProvider>();
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return AdminScaffold(
      title: 'Dashboard',
      body: provider.loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () => provider.loadDashboard(),
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Stats grid
                    GridView.count(
                      crossAxisCount: 2,
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: 1.4,
                      children: [
                        StatCard(
                          title: 'Eventos hoy',
                          value: '${provider.todayCount}',
                          icon: Icons.today,
                          color: AppColors.primary,
                          onTap: () => Navigator.pushNamed(context, '/events', arguments: {'filter': 'today'}),
                        ),
                        StatCard(
                          title: 'Eventos esta semana',
                          value: '${provider.weekCount}',
                          icon: Icons.date_range,
                          color: AppColors.teal,
                        ),
                        StatCard(
                          title: 'Ingresos del mes',
                          value: _formatCurrency(provider.monthRevenue),
                          icon: Icons.attach_money,
                          color: AppColors.success,
                        ),
                        StatCard(
                          title: 'Cotizaciones pendientes',
                          value: '${provider.pendingQuotes}',
                          icon: Icons.pending_actions,
                          color: AppColors.warning,
                          onTap: () => Navigator.pushNamed(context, '/quotes'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),

                    // Alerts
                    if (provider.alerts.isNotEmpty) ...[
                      SectionHeader(title: 'Alertas Críticas'),
                      const SizedBox(height: 8),
                      ...provider.alerts.map((alert) {
                        IconData icon;
                        Color color;
                        switch (alert['type']) {
                          case 'payment':
                            icon = Icons.payment;
                            color = AppColors.error;
                            break;
                          case 'confirm':
                            icon = Icons.event_busy;
                            color = AppColors.warning;
                            break;
                          default:
                            icon = Icons.inventory;
                            color = AppColors.warning;
                        }
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: AlertCard(
                            title: alert['title'],
                            subtitle: '${alert['count']} elemento(s)',
                            icon: icon,
                            color: color,
                            onTap: () {
                              // Navigate to relevant section
                            },
                          ),
                        );
                      }),
                      const SizedBox(height: 16),
                    ],

                    // Quick actions
                    SectionHeader(title: 'Accesos Rápidos'),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        _QuickAction(
                          icon: Icons.add_circle_outline,
                          label: 'Nuevo Evento',
                          onTap: () => Navigator.pushNamed(context, '/events/create'),
                        ),
                        _QuickAction(
                          icon: Icons.search,
                          label: 'Buscar Cliente',
                          onTap: () => Navigator.pushNamed(context, '/clients'),
                        ),
                        _QuickAction(
                          icon: Icons.request_quote,
                          label: 'Nueva Cotización',
                          onTap: () => Navigator.pushNamed(context, '/quotes/create'),
                        ),
                        _QuickAction(
                          icon: Icons.pending_actions,
                          label: 'Ver Pendientes',
                          onTap: () => Navigator.pushNamed(context, '/quotes'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),

                    // Mini calendar
                    SectionHeader(title: 'Próximos 7 días'),
                    const SizedBox(height: 8),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(8),
                        child: TableCalendar(
                          firstDay: DateTime.now().subtract(const Duration(days: 30)),
                          lastDay: DateTime.now().add(const Duration(days: 90)),
                          focusedDay: _focusedDay,
                          selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
                          calendarFormat: _calendarFormat,
                          headerStyle: HeaderStyle(
                            formatButtonVisible: true,
                            titleCentered: true,
                            formatButtonShowsNext: false,
                            formatButtonDecoration: BoxDecoration(
                              border: Border.all(color: AppColors.primary),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            titleTextStyle: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.w600),
                          ),
                          calendarStyle: CalendarStyle(
                            todayDecoration: BoxDecoration(color: AppColors.primary.withValues(alpha: 0.3), shape: BoxShape.circle),
                            selectedDecoration: const BoxDecoration(color: AppColors.primary, shape: BoxShape.circle),
                            markerDecoration: const BoxDecoration(color: AppColors.accent, shape: BoxShape.circle),
                          ),
                          eventLoader: (day) {
                            // Filter events for this day
                            return provider.weekEvents.where((e) {
                              final eventDate = DateTime.tryParse(e['date'] ?? '');
                              return eventDate != null && isSameDay(eventDate, day);
                            }).toList();
                          },
                          onDaySelected: (selectedDay, focusedDay) {
                            setState(() {
                              _selectedDay = selectedDay;
                              _focusedDay = focusedDay;
                            });
                          },
                          onFormatChanged: (format) {
                            setState(() => _calendarFormat = format);
                          },
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),

                    // Today's events list
                    if (provider.todayEvents.isNotEmpty) ...[
                      SectionHeader(title: 'Eventos de Hoy'),
                      const SizedBox(height: 8),
                      ...provider.todayEvents.map((event) => _EventListItem(event: event)),
                    ],
                  ],
                ),
              ),
            ),
    );
  }

  String _formatCurrency(int amount) {
    if (amount >= 1000000) {
      return 'RD\$${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return 'RD\$${(amount / 1000).toStringAsFixed(0)}K';
    }
    return 'RD\$$amount';
  }
}

class _QuickAction extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _QuickAction({required this.icon, required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          border: Border.all(color: AppColors.borderDark),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 18, color: AppColors.primary),
            const SizedBox(width: 8),
            Text(label, style: GoogleFonts.dmSans(fontSize: 13, fontWeight: FontWeight.w500)),
          ],
        ),
      ),
    );
  }
}

class _EventListItem extends StatelessWidget {
  final Map<String, dynamic> event;

  const _EventListItem({required this.event});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        onTap: () => Navigator.pushNamed(context, '/events/${event['id']}'),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: AppColors.primary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.event, color: AppColors.primary, size: 20),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(event['client_name'] ?? 'Evento', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                    Text(
                      '${event['date']} - ${event['time'] ?? ''}',
                      style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                    ),
                  ],
                ),
              ),
              _buildStatusBadge(event['status']),
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
      case 'confirmed':
        return StatusBadge.confirmed();
      case 'paid':
        return StatusBadge.paid();
      case 'completed':
        return StatusBadge.completed();
      default:
        return StatusBadge.draft();
    }
  }
}
