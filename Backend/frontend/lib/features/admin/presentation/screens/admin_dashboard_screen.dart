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
          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Text(
                    'Funciones Administrativas',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                ),
                // New Card for Business Analytics
                _buildAdminCard(
                  context,
                  'Dashboard de Analíticas',
                  Icons.bar_chart,
                  Colors.purple,
                  () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => const AdminAnalyticsScreen()),
                    );
                  },
                ),
                const Divider(), // Separator
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Text(
                    'Cotizaciones Pendientes',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                ),
                if (pendingQuotes.isEmpty)
                  const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.check_circle_outline, size: 64, color: Colors.grey),
                        SizedBox(height: 16),
                        Text('No hay cotizaciones pendientes', style: TextStyle(color: Colors.grey)),
                      ],
                    ),
                  )
                else
                  ListView.builder(
                    shrinkWrap: true, // Important for nested ListView in SingleChildScrollView
                    physics: const NeverScrollableScrollPhysics(), // Disable scrolling for nested ListView
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
                  ),
              ],
            ),
          );
        },
      ),
    );
  }

  // Helper method to build admin cards
  Widget _buildAdminCard(BuildContext context, String title, IconData icon, Color color, VoidCallback onTap) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: ListTile(
        leading: Icon(icon, color: color, size: 36),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
        trailing: const Icon(Icons.arrow_forward_ios),
        onTap: onTap,
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
