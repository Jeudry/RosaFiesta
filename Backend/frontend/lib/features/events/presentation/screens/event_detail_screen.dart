import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/event_model.dart';
import '../../presentation/events_provider.dart';
import '../../../guests/presentation/screens/guest_list_screen.dart';
import '../../../tasks/presentation/screens/event_task_list_screen.dart';
import 'budget_analysis_screen.dart';
import '../../presentation/timeline_provider.dart';
import '../../../guests/presentation/guests_provider.dart';
import '../../../tasks/presentation/tasks_provider.dart';
import '../../../core/services/pdf_export_service.dart';


class EventDetailScreen extends StatefulWidget {
  final Event event;

  const EventDetailScreen({super.key, required this.event});

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<EventsProvider>(context, listen: false).fetchEventItems(widget.event.id);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.event.name),
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
          double realBudget = 0;
          for (var item in provider.currentEventItems) {
            if (item.price != null) {
              realBudget += item.price! * item.quantity;
            }
          }

          return Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildDetailRow(Icons.calendar_today, 'Fecha', '${widget.event.date.day}/${widget.event.date.month}/${widget.event.date.year}'),
                _buildDetailRow(Icons.location_on, 'Ubicación', widget.event.location),
                GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => GuestListScreen(eventId: widget.event.id)),
                    );
                  },
                  child: _buildDetailRow(Icons.people, 'Invitados', '${widget.event.guestCount}', color: Colors.blue),
                ),
                GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => EventTaskListScreen(eventId: widget.event.id)),
                    );
                  },
                  child: _buildDetailRow(Icons.check_circle_outline, 'Tareas', 'Ver checklist', color: Colors.blue),
                ),
                GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => EventTimelineScreen(eventId: widget.event.id)),
                    );
                  },
                  child: _buildDetailRow(Icons.timer_outlined, 'Cronograma', 'Ver planificación por horas', color: Colors.blue),
                ),
                
                // Budget Comparison
                InkWell(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => BudgetAnalysisScreen(event: widget.event)),
                    );
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Column(
                      children: [
                        Row(
                          children: [
                            Expanded(child: _buildDetailRow(Icons.attach_money, 'Presupuesto Est.', '\$${widget.event.budget.toStringAsFixed(2)}')),
                            const SizedBox(width: 16),
                            Expanded(child: _buildDetailRow(Icons.money_off, 'Presupuesto Real', '\$${realBudget.toStringAsFixed(2)}', color: realBudget > widget.event.budget ? Colors.red : Colors.green)),
                            const Icon(Icons.chevron_right, color: Colors.blue),
                          ],
                        ),
                        if (widget.event.additionalCosts > 0)
                           _buildDetailRow(Icons.add_circle_outline, 'Costos Adicionales (Admin)', '\$${widget.event.additionalCosts.toStringAsFixed(2)}', color: Colors.orange),
                        if (widget.event.additionalCosts > 0)
                          _buildDetailRow(Icons.summarize, 'Total Final', '\$${(realBudget + widget.event.additionalCosts).toStringAsFixed(2)}', color: Colors.blue),
                      ],
                    ),
                  ),
                ),
                
                if (widget.event.adminNotes != null && widget.event.adminNotes!.isNotEmpty)
                  _buildDetailRow(Icons.note, 'Notas de Admin', widget.event.adminNotes!, color: Colors.orange),

                _buildDetailRow(Icons.info, 'Estado', _getStatusLabel(widget.event.status)),
                
                const Divider(height: 32),
                
                // Action Buttons based on status
                _buildActionButtons(context, provider),

                const Divider(height: 32),
                
                const Text(
                  'Mobiliario y Decoración',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Expanded(
                  child: provider.isLoading
                      ? const Center(child: CircularProgressIndicator())
                      : provider.currentEventItems.isEmpty
                          ? const Center(child: Text('No hay productos agregados'))
                          : ListView.builder(
                              itemCount: provider.currentEventItems.length,
                              itemBuilder: (context, index) {
                                final item = provider.currentEventItems[index];
                                return ListTile(
                                  leading: const Icon(Icons.chair), // Placeholder icon
                                  title: Text(item.article?.nameTemplate ?? 'Producto desconocido'),
                                  subtitle: Text('${item.quantity} x \$${item.price?.toStringAsFixed(2) ?? "N/A"}'),
                                  trailing: IconButton(
                                    icon: const Icon(Icons.delete, color: Colors.red),
                                    onPressed: () {
                                      provider.removeItemFromEvent(widget.event.id, item.id);
                                    },
                                  ),
                                );
                              },
                            ),
                ),
              ],
            ),
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
      case 'confirmed': return 'Confirmado / Reservado';
      case 'completed': return 'Evento Finalizado';
      default: return status;
    }
  }

  Widget _buildActionButtons(BuildContext context, EventsProvider provider) {
    // We would check AuthProvider here to see if user is Admin
    // For now, let's show user buttons
    
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
          label: const Text('Confirmar y Reservar'),
          onPressed: () => _confirmQuotation(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
        ),
      );
    }

    return const SizedBox.shrink();
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
