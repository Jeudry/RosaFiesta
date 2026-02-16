import 'package:flutter/material.dart';
import '../../data/event_model.dart';

class EventDetailScreen extends StatelessWidget {
  final Event event;

  const EventDetailScreen({super.key, required this.event});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(event.name)),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDetailRow(Icons.calendar_today, 'Fecha', '${event.date.day}/${event.date.month}/${event.date.year}'),
            _buildDetailRow(Icons.location_on, 'Ubicación', event.location),
            _buildDetailRow(Icons.people, 'Invitados', '${event.guestCount}'),
            _buildDetailRow(Icons.attach_money, 'Presupuesto', '\$${event.budget.toStringAsFixed(2)}'),
            _buildDetailRow(Icons.info, 'Estado', event.status),
            const Divider(height: 32),
            const Text(
              'Opciones',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 16,
              children: [
                ActionChip(
                  avatar: const Icon(Icons.checklist),
                  label: const Text('Checklist (Próximamente)'),
                  onPressed: () {},
                ),
                ActionChip(
                    avatar: const Icon(Icons.chair),
                    label: const Text('Ver Mobiliario'),
                    onPressed: () {
                         // TODO: Navigate to products filtered by event context?
                         ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Próximamente: Vincular productos")));
                    }
                ),
              ],
            )
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(IconData icon, String label, String value) {
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
              Text(value, style: const TextStyle(fontSize: 16)),
            ],
          ),
        ],
      ),
    );
  }
}
