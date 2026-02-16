import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../events/presentation/events_provider.dart';
import '../../events/data/event_model.dart';

class AdminDashboardScreen extends StatelessWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Panel Administrativo'),
        backgroundColor: Colors.indigo,
        foregroundColor: Colors.white,
      ),
      body: Consumer<EventsProvider>(
        builder: (context, provider, child) {
          final pendingQuotes = provider.events.where((e) => e.status == 'requested').toList();

          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (pendingQuotes.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.check_circle_outline, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('No hay cotizaciones pendientes', style: TextStyle(color: Colors.grey)),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: pendingQuotes.length,
            itemBuilder: (context, index) {
              final event = pendingQuotes[index];
              return Card(
                margin: const EdgeInsets.all(8.0),
                child: ListTile(
                  title: Text(event.name),
                  subtitle: Text('Cliente ID: ${event.userId}\nFecha: ${event.date.day}/${event.date.month}/${event.date.year}'),
                  trailing: const Icon(Icons.edit_note, color: Colors.indigo),
                  onTap: () => _showAdjustmentDialog(context, provider, event),
                ),
              );
            },
          );
        },
      ),
    );
  }

  void _showAdjustmentDialog(BuildContext context, EventsProvider provider, Event event) {
    final costsController = TextEditingController(text: event.additionalCosts.toString());
    final notesController = TextEditingController(text: event.adminNotes ?? '');

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Ajustar Cotización: ${event.name}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: costsController,
              decoration: const InputDecoration(labelText: 'Costos Adicionales (\$)'),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: notesController,
              decoration: const InputDecoration(labelText: 'Notas para el Cliente'),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () async {
              final costs = double.tryParse(costsController.text) ?? 0.0;
              final notes = notesController.text;
              
              final success = await provider.adjustQuote(event.id, costs, notes);
              if (success && context.mounted) {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Cotización ajustada y enviada al cliente.')),
                );
              }
            },
            child: const Text('Guardar y Notificar'),
          ),
        ],
      ),
    );
  }
}
