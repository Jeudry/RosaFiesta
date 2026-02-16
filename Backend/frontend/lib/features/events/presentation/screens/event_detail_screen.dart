import 'package:flutter/material.dart';
import '../../data/event_model.dart';
import '../../presentation/events_provider.dart';
import '../../../guests/presentation/screens/guest_list_screen.dart';
import '../../../tasks/presentation/screens/event_task_list_screen.dart';


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
      appBar: AppBar(title: Text(widget.event.name)),
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
                
                // Budget Comparison
                Row(
                  children: [
                    Expanded(child: _buildDetailRow(Icons.attach_money, 'Presupuesto Est.', '\$${widget.event.budget.toStringAsFixed(2)}')),
                    const SizedBox(width: 16),
                    Expanded(child: _buildDetailRow(Icons.money_off, 'Presupuesto Real', '\$${realBudget.toStringAsFixed(2)}', color: realBudget > widget.event.budget ? Colors.red : Colors.green)),
                  ],
                ),
                
                _buildDetailRow(Icons.info, 'Estado', widget.event.status),
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
