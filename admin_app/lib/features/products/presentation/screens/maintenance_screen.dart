import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';

class MaintenanceScreen extends StatefulWidget {
  const MaintenanceScreen({super.key});

  @override
  State<MaintenanceScreen> createState() => _MaintenanceScreenState();
}

class _MaintenanceScreenState extends State<MaintenanceScreen> {
  List<dynamic> _logs = [];
  bool _loading = true;
  String? _selectedStatus;
  String? _selectedType;

  @override
  void initState() {
    super.initState();
    _loadLogs();
  }

  Future<void> _loadLogs() async {
    setState(() => _loading = true);
    try {
      final resp = await apiClient.getMaintenanceLogs(status: _selectedStatus, type: _selectedType);
      _logs = resp.data['data'] ?? [];
    } catch (e) {
      _logs = [];
    }
    setState(() => _loading = false);
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'completed':
        return AppColors.success;
      case 'in_progress':
        return AppColors.amber;
      case 'scheduled':
        return AppColors.sky;
      case 'cancelled':
        return Colors.grey;
      default:
        return Colors.grey;
    }
  }

  String _statusLabel(String status) {
    switch (status) {
      case 'completed':
        return 'Completado';
      case 'in_progress':
        return 'En Progreso';
      case 'scheduled':
        return 'Programado';
      case 'cancelled':
        return 'Cancelado';
      default:
        return status;
    }
  }

  String _typeLabel(String type) {
    switch (type) {
      case 'cleaning':
        return 'Limpieza';
      case 'repair':
        return 'Reparación';
      case 'inspection':
        return 'Inspección';
      case 'replacement':
        return 'Reemplazo';
      default:
        return type;
    }
  }

  Future<void> _createLog() async {
    final articleIdController = TextEditingController();
    final descController = TextEditingController();
    final performedByController = TextEditingController();

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Registrar Mantenimiento'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              AdminTextField(label: 'Article ID', controller: articleIdController),
              const SizedBox(height: 12),
              AdminTextField(label: 'Descripción', controller: descController, lines: 2),
              const SizedBox(height: 12),
              AdminTextField(label: 'Realizado por', controller: performedByController),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                decoration: const InputDecoration(labelText: 'Tipo'),
                items: const [
                  DropdownMenuItem(value: 'cleaning', child: Text('Limpieza')),
                  DropdownMenuItem(value: 'repair', child: Text('Reparación')),
                  DropdownMenuItem(value: 'inspection', child: Text('Inspección')),
                  DropdownMenuItem(value: 'replacement', child: Text('Reemplazo')),
                ],
                onChanged: (v) {},
              ),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, {
              'article_id': articleIdController.text,
              'description': descController.text,
              'performed_by': performedByController.text,
              'maintenance_type': 'cleaning',
            }),
            child: const Text('Crear'),
          ),
        ],
      ),
    );

    if (result != null) {
      await apiClient.createMaintenanceLog(result);
      await _loadLogs();
    }
  }

  Future<void> _markComplete(String id) async {
    await apiClient.updateMaintenanceLog(id, {'status': 'completed'});
    await _loadLogs();
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Mantenimiento',
      showBack: true,
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: DropdownButtonFormField<String>(
                    decoration: const InputDecoration(labelText: 'Estado'),
                    value: _selectedStatus,
                    items: const [
                      DropdownMenuItem(value: '', child: Text('Todos')),
                      DropdownMenuItem(value: 'scheduled', child: Text('Programado')),
                      DropdownMenuItem(value: 'in_progress', child: Text('En Progreso')),
                      DropdownMenuItem(value: 'completed', child: Text('Completado')),
                    ],
                    onChanged: (v) {
                      setState(() => _selectedStatus = v == '' ? null : v);
                      _loadLogs();
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: DropdownButtonFormField<String>(
                    decoration: const InputDecoration(labelText: 'Tipo'),
                    value: _selectedType,
                    items: const [
                      DropdownMenuItem(value: '', child: Text('Todos')),
                      DropdownMenuItem(value: 'cleaning', child: Text('Limpieza')),
                      DropdownMenuItem(value: 'repair', child: Text('Reparación')),
                      DropdownMenuItem(value: 'inspection', child: Text('Inspección')),
                    ],
                    onChanged: (v) {
                      setState(() => _selectedType = v == '' ? null : v);
                      _loadLogs();
                    },
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : _logs.isEmpty
                    ? Center(child: Text('No hay registros', style: GoogleFonts.dmSans(color: Colors.grey)))
                    : RefreshIndicator(
                        onRefresh: _loadLogs,
                        child: ListView.builder(
                          padding: const EdgeInsets.symmetric(horizontal: 16),
                          itemCount: _logs.length,
                          itemBuilder: (ctx, i) {
                            final log = _logs[i];
                            return Card(
                              margin: const EdgeInsets.only(bottom: 8),
                              child: ListTile(
                                leading: CircleAvatar(
                                  backgroundColor: _statusColor(log['status'] ?? '').withValues(alpha: 0.15),
                                  child: Icon(
                                    log['maintenance_type'] == 'cleaning'
                                        ? Icons.cleaning_services
                                        : log['maintenance_type'] == 'repair'
                                            ? Icons.build
                                            : Icons.search,
                                    color: _statusColor(log['status'] ?? ''),
                                  ),
                                ),
                                title: Text(log['article_id']?.toString().substring(0, 8) ?? ''),
                                subtitle: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(_typeLabel(log['maintenance_type'] ?? ''), style: GoogleFonts.dmSans(fontSize: 12)),
                                    if (log['description'] != null && log['description'].toString().isNotEmpty)
                                      Text(log['description'], style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted), maxLines: 1, overflow: TextOverflow.ellipsis),
                                  ],
                                ),
                                trailing: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                      decoration: BoxDecoration(
                                        color: _statusColor(log['status'] ?? '').withValues(alpha: 0.15),
                                        borderRadius: BorderRadius.circular(4),
                                      ),
                                      child: Text(_statusLabel(log['status'] ?? ''), style: GoogleFonts.dmSans(fontSize: 10, color: _statusColor(log['status'] ?? ''))),
                                    ),
                                    if (log['status'] == 'scheduled' || log['status'] == 'in_progress')
                                      IconButton(
                                        icon: const Icon(Icons.check_circle_outline, size: 20),
                                        onPressed: () => _markComplete(log['id']),
                                      ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createLog,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}
