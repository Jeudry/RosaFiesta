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
                    // 1. Executive Welcome Header
                    Row(
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '¡Hola, Administrador! 👋',
                              style: GoogleFonts.outfit(
                                fontSize: 22,
                                fontWeight: FontWeight.w800,
                                color: isDark ? Colors.white : const Color(0xFF2C1A4D),
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              'Aquí tienes el resumen de operaciones de hoy.',
                              style: GoogleFonts.dmSans(
                                fontSize: 12,
                                color: isDark ? const Color(0xFF8B8BAA) : const Color(0xFF6B7280),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),

                    // Stats grid (Slightly smaller, 1.28 aspect ratio!)
                    GridView.count(
                      crossAxisCount: 2,
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: 1.28,
                      children: [
                        StatCard(
                          title: 'Eventos de hoy',
                          value: '${provider.todayCount}',
                          icon: Icons.today,
                          color: AppColors.hotPink,
                          onTap: () => Navigator.pushNamed(context, '/events', arguments: {'filter': 'today'}),
                        ),
                        StatCard(
                          title: 'Eventos de la semana',
                          value: '${provider.weekCount}',
                          icon: Icons.date_range,
                          color: AppColors.violet,
                        ),
                        StatCard(
                          title: 'Ingresos del mes',
                          value: _formatCurrency(provider.monthRevenue),
                          icon: Icons.attach_money,
                          color: AppColors.teal,
                        ),
                        StatCard(
                          title: 'Cotizaciones pendientes',
                          value: '${provider.pendingQuotes}',
                          icon: Icons.pending_actions,
                          color: AppColors.amber,
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

                    // Quick actions 2x2 Grid (Instead of horizontal listview!)
                    SectionHeader(title: 'Accesos Rápidos'),
                    const SizedBox(height: 12),
                    GridView.count(
                      crossAxisCount: 2,
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      mainAxisSpacing: 10,
                      crossAxisSpacing: 10,
                      childAspectRatio: 2.65,
                      children: [
                        _buildCarouselPill(
                          icon: Icons.add_circle_outline_rounded,
                          label: 'Nuevo Evento',
                          color: const Color(0xFFFF3CAC),
                          onTap: () => Navigator.pushNamed(context, '/events/create'),
                        ),
                        _buildCarouselPill(
                          icon: Icons.search_rounded,
                          label: 'Buscar Cliente',
                          color: const Color(0xFF00D4AA),
                          onTap: () => Navigator.pushNamed(context, '/clients'),
                        ),
                        _buildCarouselPill(
                          icon: Icons.request_quote_rounded,
                          label: 'Nueva Cotización',
                          color: const Color(0xFF8B5CF6),
                          onTap: () => Navigator.pushNamed(context, '/quotes/create'),
                        ),
                        _buildCarouselPill(
                          icon: Icons.pending_actions_rounded,
                          label: 'Ver Pendientes',
                          color: const Color(0xFFFFB800),
                          onTap: () => Navigator.pushNamed(context, '/quotes'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),

                    // 2. Operaciones Workflow Stepper (Dynamic from database!)
                    SectionHeader(title: 'Hitos y Tareas de Hoy'),
                    const SizedBox(height: 8),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (provider.todayEvents.isNotEmpty) ...[
                              // Dynamic events from the database
                              for (int i = 0; i < provider.todayEvents.length; i++) ...[
                                _buildStepperStep(
                                  context: context,
                                  isCompleted: provider.todayEvents[i]['status'] == 'completed',
                                  isActive: provider.todayEvents[i]['status'] == 'confirmed' || provider.todayEvents[i]['status'] == 'paid',
                                  title: provider.todayEvents[i]['client_name'] ?? 'Evento',
                                  subtitle: 'Hoy a las ${provider.todayEvents[i]['time'] ?? '12:00 PM'} - Estado: ${provider.todayEvents[i]['status']}',
                                  icon: Icons.celebration_rounded,
                                  color: AppColors.teal,
                                ),
                                if (i < provider.todayEvents.length - 1)
                                  _buildStepperConnector(color: AppColors.teal.withOpacity(0.3)),
                              ],
                              // Connector to general administrative tasks
                              _buildStepperConnector(color: AppColors.primary.withOpacity(0.3)),
                            ],
                            // Dynamic task 1: Quotes
                            _buildStepperStep(
                              context: context,
                              isCompleted: provider.pendingQuotes == 0,
                              isActive: provider.pendingQuotes > 0,
                              title: 'Revisar Cotizaciones Pendientes',
                              subtitle: provider.pendingQuotes > 0
                                  ? 'En progreso - ${provider.pendingQuotes} cotizaciones pendientes hoy'
                                  : 'Completado - No hay cotizaciones pendientes hoy',
                              icon: Icons.pending_actions_outlined,
                              color: AppColors.primary,
                            ),
                            _buildStepperConnector(color: AppColors.primary.withOpacity(0.3)),
                            // Dynamic task 2: Cash flow
                            _buildStepperStep(
                              context: context,
                              isCompleted: provider.monthRevenue > 0,
                              isActive: provider.monthRevenue == 0,
                              title: 'Conciliación de Ingresos del Mes',
                              subtitle: provider.monthRevenue > 0
                                  ? 'Ingresos acumulados: ${_formatCurrency(provider.monthRevenue)}'
                                  : 'Pendiente - Registrar primeros ingresos del mes',
                              icon: Icons.account_balance_wallet_outlined,
                              color: AppColors.teal,
                            ),
                            _buildStepperConnector(color: AppColors.primary.withOpacity(0.3)),
                            // Dynamic task 3: Standard audit close
                            _buildStepperStep(
                              context: context,
                              title: 'Cierre de Caja del Día',
                              subtitle: 'Programado para las 08:00 PM',
                              icon: Icons.history_toggle_off_outlined,
                              color: AppColors.amber,
                            ),
                          ],
                        ),
                      ),
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
                            formatButtonTextStyle: GoogleFonts.outfit(color: AppColors.primary, fontSize: 12, fontWeight: FontWeight.w600),
                            titleTextStyle: GoogleFonts.outfit(fontSize: 15, fontWeight: FontWeight.w700),
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
                    // UX Scroll padding to float over BottomNavBar
                    const SizedBox(height: 110),
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

  Widget _buildStepperStep({
    required BuildContext context,
    bool isCompleted = false,
    bool isActive = false,
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
  }) {
    final titleColor = isCompleted ? const Color(0xFF5A5A80) : const Color(0xFF2C1A4D);
    final isDark = Theme.of(context).brightness == Brightness.dark;
    
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Column(
          children: [
            Container(
              width: 32,
              height: 32,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: isCompleted
                    ? color.withOpacity(0.12)
                    : isActive
                        ? color.withOpacity(0.15)
                        : Colors.white.withOpacity(0.3),
                border: Border.all(
                  color: isCompleted || isActive ? color : Colors.grey.withOpacity(0.3),
                  width: 1.8,
                ),
                boxShadow: isActive
                    ? [
                        BoxShadow(
                          color: color.withOpacity(0.15),
                          blurRadius: 6,
                          offset: const Offset(0, 2),
                        )
                      ]
                    : null,
              ),
              child: Icon(
                isCompleted ? Icons.check : icon,
                size: 14,
                color: isCompleted || isActive ? color : Colors.grey,
              ),
            ),
          ],
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: isActive 
                  ? (isDark ? Colors.white.withOpacity(0.04) : color.withOpacity(0.04))
                  : Colors.transparent,
              borderRadius: BorderRadius.circular(16),
              border: isActive
                  ? Border.all(color: color.withOpacity(0.15), width: 1.2)
                  : null,
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: GoogleFonts.outfit(
                          fontSize: 13.5,
                          fontWeight: isCompleted || isActive ? FontWeight.w700 : FontWeight.w600,
                          color: isDark ? (isCompleted ? const Color(0xFF8B8BAA) : Colors.white) : titleColor,
                          decoration: isCompleted ? TextDecoration.lineThrough : null,
                        ),
                      ),
                      const SizedBox(height: 3),
                      Text(
                        subtitle,
                        style: GoogleFonts.dmSans(
                          fontSize: 11,
                          color: isCompleted 
                              ? (isDark ? Colors.white54 : Colors.grey) 
                              : (isDark ? const Color(0xFF8B8BAA) : AppColors.textSecondary),
                        ),
                      ),
                    ],
                  ),
                ),
                if (isActive)
                  Icon(
                    Icons.chevron_right_rounded, 
                    size: 18, 
                    color: color,
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildStepperConnector({required Color color}) {
    return Padding(
      padding: const EdgeInsets.only(left: 15),
      child: Container(
        width: 2,
        height: 24,
        color: color,
      ),
    );
  }

  Widget _buildCarouselPill({
    required IconData icon,
    required String label,
    required Color color,
    required VoidCallback onTap,
  }) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: isDark ? Colors.white.withOpacity(0.04) : Colors.white.withOpacity(0.68),
          borderRadius: BorderRadius.circular(30),
          border: Border.all(
            color: color.withOpacity(0.16),
            width: 1.5,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.02),
              blurRadius: 10,
              offset: const Offset(0, 4),
            )
          ]
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: color.withOpacity(0.12),
              ),
              child: Icon(
                icon,
                size: 16,
                color: color,
              ),
            ),
            const SizedBox(width: 8),
            Text(
              label,
              style: GoogleFonts.outfit(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: isDark ? Colors.white70 : const Color(0xFF2C1A4D),
              ),
            ),
          ],
        ),
      ),
    );
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
