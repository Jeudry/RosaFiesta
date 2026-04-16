import 'package:flutter/material.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';

class ExportMenu extends StatelessWidget {
  const ExportMenu({super.key});

  void _export(BuildContext context, String type) async {
    try {
      // For web, we need to use a different approach
      // For now, just show a snackbar
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Exportando $type...')),
      );
      // In a real implementation, you would:
      // 1. Make the request to get the CSV/Excel bytes
      // 2. Use a file saver package to download
      // 3. Or open the URL in a new tab for web
      final url = '/v1/admin/analytics/export/$type?format=csv';
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Descarga iniciada: $url')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al exportar: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<String>(
      icon: const Icon(Icons.download),
      tooltip: 'Exportar',
      onSelected: (type) => _export(context, type),
      itemBuilder: (ctx) => [
        const PopupMenuItem(
          value: 'events',
          child: Row(
            children: [
              Icon(Icons.event, size: 20),
              SizedBox(width: 8),
              Text('Eventos (CSV)'),
            ],
          ),
        ),
        const PopupMenuItem(
          value: 'clients',
          child: Row(
            children: [
              Icon(Icons.people, size: 20),
              SizedBox(width: 8),
              Text('Clientes (CSV)'),
            ],
          ),
        ),
        const PopupMenuItem(
          value: 'articles',
          child: Row(
            children: [
              Icon(Icons.inventory_2, size: 20),
              SizedBox(width: 8),
              Text('Inventario (CSV)'),
            ],
          ),
        ),
      ],
    );
  }
}
