import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/event_model.dart';
import '../events_provider.dart';
import '../../../guests/presentation/screens/guest_list_screen.dart';
import '../../../tasks/presentation/screens/event_task_list_screen.dart';
import 'budget_analysis_screen.dart';
import '../../presentation/timeline_provider.dart';
import '../../../guests/presentation/guests_provider.dart';
import '../../../tasks/presentation/tasks_provider.dart';
import '../../../core/services/pdf_export_service.dart';
import 'checkout_screen.dart';
import '../widgets/quotation_chat_widget.dart';


class EventDetailScreen extends StatefulWidget {
  final String eventId;

  const EventDetailScreen({super.key, required this.eventId});

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<EventsProvider>(context, listen: false).fetchEventItems(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Detalle de Reserva'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Detalles', icon: Icon(Icons.description)),
              Tab(text: 'Chat', icon: Icon(Icons.chat)),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.picture_as_pdf),
              onPressed: () => _exportToPdf(context),
              tooltip: 'Exportar a PDF',
            ),
          ],
        ),
        body: Consumer<EventsProvider>(
          builder: (context, provider, child) {
            final event = provider.events.firstWhere((e) => e.id == widget.eventId);

            return TabBarView(
              children: [
                // Tab 1: Details
                SingleChildScrollView(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    children: [
                      _buildControlGroup([
                        _buildNavAction(
                          icon: Icons.check_circle_outline,
                          label: 'Tareas',
                          subtitle: 'Ver checklist',
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => EventTaskListScreen(eventId: widget.eventId)),
                          ),
                        ),
                        const Divider(height: 1),
                        _buildNavAction(
                          icon: Icons.timer_outlined,
                          label: 'Cronograma',
                          subtitle: 'Ver planificación por horas',
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => EventTimelineScreen(eventId: widget.eventId)),
                          ),
                        ),
                      ]),
                      
                      const SizedBox(height: 20),

                      // Budget Comparison Group
                      _buildControlGroup([
                        _buildBudgetView(event, provider),
                      ]),

                      const SizedBox(height: 20),

                      _buildControlGroup([
                        if (event.adminNotes != null && event.adminNotes!.isNotEmpty)
                          _buildDetailRow(Icons.note, 'Notas de Admin', event.adminNotes!, color: Colors.orange),
                        _buildDetailRow(Icons.info, 'Estado', _getStatusLabel(event.status)),
                        if (event.status == 'paid')
                           _buildDetailRow(Icons.verified, 'Pago', 'Completado via ${event.paymentMethod ?? "N/A"}', color: Colors.green),
                      ]),

                      const SizedBox(height: 24),
                      
                      // Action Buttons based on status
                      _buildActionButtons(context, provider, event),

                      const SizedBox(height: 32),
                      
                      const Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          'Mobiliario y Decoración',
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800, letterSpacing: -0.5),
                        ),
                      ),
                      const SizedBox(height: 16),
                      provider.isLoading
                          ? const Center(child: CircularProgressIndicator())
                          : provider.currentEventItems.isEmpty
                              ? const Center(child: Text('No hay productos agregados'))
                              : ListView.builder(
                                  shrinkWrap: true,
                                  physics: const NeverScrollableScrollPhysics(),
                                  itemCount: provider.currentEventItems.length,
                                  itemBuilder: (context, index) {
                                    final item = provider.currentEventItems[index];
                                    return Container(
                                      margin: const EdgeInsets.only(bottom: 12),
                                      decoration: BoxDecoration(
                                        color: Colors.white,
                                        borderRadius: BorderRadius.circular(16),
                                        boxShadow: AppDecorations.softShadow,
                                      ),
                                      child: ListTile(
                                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                                        leading: Container(
                                          padding: const EdgeInsets.all(8),
                                          decoration: BoxDecoration(
                                            color: AppColors.primary.withOpacity(0.1),
                                            borderRadius: BorderRadius.circular(8),
                                          ),
                                          child: const Icon(Icons.chair, color: AppColors.primary),
                                        ),
                                        title: Text(
                                          item.article?.nameTemplate ?? 'Producto desconocido',
                                          style: const TextStyle(fontWeight: FontWeight.bold),
                                        ),
                                        subtitle: Text('${item.quantity} x \$${item.price?.toStringAsFixed(2) ?? "0.00"}'),
                                        trailing: IconButton(
                                          icon: const Icon(Icons.delete_outline, color: Colors.redAccent),
                                          onPressed: () => provider.removeItemFromEvent(event.id, item.id),
                                        ),
                                      ),
                                    );
                                  },
                                ),
                    ],
                  ),
                ),
                
                // Tab 2: Chat (Real-time)
                QuotationChatWidget(eventId: widget.eventId),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildControlGroup(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppDecorations.softShadow,
      ),
      child: Column(children: children),
    );
  }

  Widget _buildNavAction({required IconData icon, required String label, required String subtitle, required VoidCallback onTap}) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: AppColors.accent.withOpacity(0.1), borderRadius: BorderRadius.circular(12)),
              child: Icon(icon, color: AppColors.accent),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(label, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  Text(subtitle, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.grey),
          ],
        ),
      ),
    );
  }

  Widget _buildBudgetView(Event event, EventsProvider provider) {
    return InkWell(
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (context) => BudgetAnalysisScreen(event: event))),
      borderRadius: BorderRadius.circular(24),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Análisis de Presupuesto', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                const Icon(Icons.analytics_outlined, color: AppColors.primary),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(child: _buildBudgetTile('Estimado', '\$${event.budget.toStringAsFixed(2)}', Colors.grey)),
                Container(width: 1, height: 40, color: Colors.grey.shade100, margin: const EdgeInsets.symmetric(horizontal: 16)),
                Expanded(child: _buildBudgetTile('Real', '\$${provider.realBudget.toStringAsFixed(2)}', provider.realBudget > event.budget ? Colors.red : Colors.green)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBudgetTile(String label, String value, Color color) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 10, color: Colors.grey, fontWeight: FontWeight.bold)),
        Text(value, style: TextStyle(fontSize: 18, color: color, fontWeight: FontWeight.w900)),
      ],
    );
  }
          );
        },
      ),
    );
  }

  Future<void> _exportToPdf(BuildContext context) async {
    final eventsProvider = context.read<EventsProvider>();
    final guestsProvider = context.read<GuestsProvider>();
    final tasksProvider = context.read<EventTasksProvider>();
    final timelineProvider = context.read<TimelineProvider>();

    // Show loading dialog
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );

    try {
      // Ensure data is loaded
      await Future.wait([
        guestsProvider.fetchGuests(widget.event.id),
        tasksProvider.fetchTasks(widget.event.id),
        timelineProvider.fetchTimeline(widget.event.id),
      ]);

      if (context.mounted) {
        Navigator.pop(context); // Close loading dialog
        
        await PdfExportService.generateEventReport(
          event: widget.event,
          products: eventsProvider.currentEventItems,
          guests: guestsProvider.guests,
          tasks: tasksProvider.tasks,
          timeline: timelineProvider.items,
        );
      }
    } catch (e) {
      if (context.mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al exportar: $e')),
        );
      }
    }
  }

  String _getStatusLabel(String status) {
    switch (status) {
      case 'planning': return 'Borrador (Planeando)';
      case 'requested': return 'En Revisión (Cotización Solicitada)';
      case 'adjusted': return 'Cotización Lista (Revisar Ajustes)';
      case 'confirmed': return 'Confirmado (Pendiente de Pago)';
      case 'paid': return 'Pagado y Reservado';
      case 'completed': return 'Evento Finalizado';
      default: return status;
    }
  }

  Widget _buildActionButtons(BuildContext context, EventsProvider provider) {
    if (widget.event.status == 'planning') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.send),
          label: const Text('Solicitar Cotización a Rosa Fiesta'),
          onPressed: () => _requestQuotation(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.pink, foregroundColor: Colors.white),
        ),
      );
    }
    
    if (widget.event.status == 'adjusted') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.check_circle),
          label: const Text('Aceptar Cotización'),
          onPressed: () => _confirmQuotation(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
        ),
      );
    }

    if (widget.event.status == 'confirmed') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.payment),
          label: const Text('Pagar para Reservar'),
          onPressed: () => _navigateToCheckout(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.blue, foregroundColor: Colors.white),
        ),
      );
    }

    if (widget.event.status == 'paid') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.picture_as_pdf),
          label: const Text('Descargar Factura / Invoice'),
          onPressed: () => _exportToPdf(context),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
        ),
      );
    }

    return const SizedBox.shrink();
  }

  void _navigateToCheckout(BuildContext context, EventsProvider provider) {
    double realBudget = 0;
    for (var item in provider.currentEventItems) {
      if (item.price != null) {
        realBudget += item.price! * item.quantity;
      }
    }
    final total = realBudget + widget.event.additionalCosts;

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CheckoutScreen(event: widget.event, totalAmount: total),
      ),
    );
  }

  Future<void> _requestQuotation(BuildContext context, EventsProvider provider) async {
    final success = await provider.requestQuote(widget.event.id);
    if (success && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cotización solicitada. Rosa Fiesta revisará tu evento.')),
      );
      Navigator.pop(context); // Go back to refresh list
    }
  }

  Future<void> _confirmQuotation(BuildContext context, EventsProvider provider) async {
    final success = await provider.confirmQuote(widget.event.id);
    if (success && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('¡Evento confirmado y reservado!')),
      );
      Navigator.pop(context);
    }
  }

  Widget _buildDetailRow(IconData icon, String label, String value, {Color? color}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Icon(icon, color: Colors.grey),
          const SizedBox(width: 16),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
              Text(value, style: TextStyle(fontSize: 16, color: color, fontWeight: color != null ? FontWeight.bold : FontWeight.normal)),
            ],
          ),
        ],
      ),
    );
  }
}
