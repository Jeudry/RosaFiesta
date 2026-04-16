import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';

class RecurringEventsScreen extends StatefulWidget {
  const RecurringEventsScreen({super.key});

  @override
  State<RecurringEventsScreen> createState() => _RecurringEventsScreenState();
}

class _RecurringEventsScreenState extends State<RecurringEventsScreen> {
  List<dynamic> _recurring = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadRecurring();
  }

  Future<void> _loadRecurring() async {
    setState(() => _loading = true);
    try {
      final resp = await apiClient.getRecurringEvents();
      _recurring = resp.data['data'] ?? [];
    } catch (e) {
      _recurring = [];
    }
    setState(() => _loading = false);
  }

  String _frequencyLabel(String freq) {
    switch (freq) {
      case 'weekly':
        return 'Semanal';
      case 'biweekly':
        return 'Quincenal';
      case 'monthly':
        return 'Mensual';
      default:
        return freq;
    }
  }

  Future<void> _createRecurring() async {
    final nameController = TextEditingController();
    final locationController = TextEditingController();
    final guestController = TextEditingController(text: '50');
    final budgetController = TextEditingController(text: '0');
    String frequency = 'monthly';

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          title: const Text('Nuevo Evento Recurrente'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                AdminTextField(label: 'Nombre', controller: nameController),
                const SizedBox(height: 12),
                AdminTextField(label: 'Ubicación', controller: locationController),
                const SizedBox(height: 12),
                AdminTextField(label: 'Cantidad invitados', controller: guestController, keyboardType: TextInputType.number),
                const SizedBox(height: 12),
                AdminTextField(label: 'Presupuesto (RD\$)', controller: budgetController, keyboardType: TextInputType.number),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  decoration: const InputDecoration(labelText: 'Frecuencia'),
                  value: frequency,
                  items: const [
                    DropdownMenuItem(value: 'weekly', child: Text('Semanal')),
                    DropdownMenuItem(value: 'biweekly', child: Text('Quincenal')),
                    DropdownMenuItem(value: 'monthly', child: Text('Mensual')),
                  ],
                  onChanged: (v) => setDialogState(() => frequency = v ?? 'monthly'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
            ElevatedButton(
              onPressed: () => Navigator.pop(ctx, {
                'name': nameController.text,
                'location': locationController.text,
                'guest_count': int.tryParse(guestController.text) ?? 50,
                'budget': double.tryParse(budgetController.text) ?? 0,
                'frequency': frequency,
                'start_date': DateTime.now().toIso8601String().split('T').first,
                'next_run_date': DateTime.now().add(const Duration(days: 30)).toIso8601String().split('T').first,
                'auto_create': false,
              }),
              child: const Text('Crear'),
            ),
          ],
        ),
      ),
    );

    if (result != null) {
      await apiClient.createRecurringEvent(result);
      await _loadRecurring();
    }
  }

  Future<void> _generateEvent(String id) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Generar Evento'),
        content: const Text('¿Crear un evento desde esta plantilla recurrente?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
          ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Generar')),
        ],
      ),
    );
    if (confirmed == true) {
      await apiClient.generateRecurringEvent(id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Evento generado')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Eventos Recurrentes',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _recurring.isEmpty
              ? EmptyState(
                  icon: Icons.repeat_outlined,
                  title: 'No hay eventos recurrentes',
                  action: AdminButton(label: 'Crear Recurrente', onTap: _createRecurring),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _recurring.length,
                  itemBuilder: (ctx, i) {
                    final r = _recurring[i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Container(
                                  width: 48,
                                  height: 48,
                                  decoration: BoxDecoration(
                                    color: AppColors.violet.withValues(alpha: 0.1),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: const Icon(Icons.repeat, color: AppColors.violet),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(r['name'] ?? '', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                                      Text(r['location'] ?? '', style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
                                    ],
                                  ),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.delete_outline),
                                  onPressed: () async {
                                    final confirmed = await showDialog<bool>(
                                      context: context,
                                      builder: (ctx) => AlertDialog(
                                        title: const Text('Eliminar'),
                                        content: const Text('¿Eliminar este patrón recurrente?'),
                                        actions: [
                                          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                          ElevatedButton(style: ElevatedButton.styleFrom(backgroundColor: AppColors.error), onPressed: () => Navigator.pop(ctx, true), child: const Text('Eliminar')),
                                        ],
                                      ),
                                    );
                                    if (confirmed == true) {
                                      await apiClient.deleteRecurringEvent(r['id']);
                                      await _loadRecurring();
                                    }
                                  },
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                _InfoChip(icon: Icons.repeat, label: _frequencyLabel(r['frequency'] ?? '')),
                                const SizedBox(width: 8),
                                _InfoChip(icon: Icons.people, label: '${r['guest_count'] ?? 0} invitados'),
                                const SizedBox(width: 8),
                                _InfoChip(icon: Icons.attach_money, label: 'RD\$${r['budget'] ?? 0}'),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Row(
                              children: [
                                Icon(Icons.calendar_today, size: 14, color: AppColors.textMuted),
                                const SizedBox(width: 4),
                                Text(
                                  'Próximo: ${r['next_run_date']?.toString().split('T').first ?? ''}',
                                  style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            SizedBox(
                              width: double.infinity,
                              child: OutlinedButton.icon(
                                icon: const Icon(Icons.play_arrow),
                                label: const Text('Generar Evento'),
                                onPressed: () => _generateEvent(r['id']),
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createRecurring,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _InfoChip extends StatelessWidget {
  final IconData icon;
  final String label;

  const _InfoChip({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 12, color: AppColors.primary),
          const SizedBox(width: 4),
          Text(label, style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.primary)),
        ],
      ),
    );
  }
}
