import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';

class EventTemplatesScreen extends StatefulWidget {
  const EventTemplatesScreen({super.key});

  @override
  State<EventTemplatesScreen> createState() => _EventTemplatesScreenState();
}

class _EventTemplatesScreenState extends State<EventTemplatesScreen> {
  List<dynamic> _templates = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadTemplates();
  }

  Future<void> _loadTemplates() async {
    setState(() => _loading = true);
    try {
      final resp = await apiClient.getEventTypes();
      _templates = resp.data['data'] ?? [];
    } catch (e) {
      _templates = [];
    }
    setState(() => _loading = false);
  }

  Future<void> _createTemplate() async {
    final nameController = TextEditingController();
    final descController = TextEditingController();
    final budgetMinController = TextEditingController();
    final budgetMaxController = TextEditingController();
    final guestController = TextEditingController(text: '50');
    final colorController = TextEditingController(text: '#FF3CAC');
    final iconController = TextEditingController(text: '🎉');

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Nueva Plantilla'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              AdminTextField(label: 'Nombre', controller: nameController),
              const SizedBox(height: 12),
              AdminTextField(label: 'Descripción', controller: descController, lines: 2),
              const SizedBox(height: 12),
              AdminTextField(label: 'Presupuesto mínimo (RD\$)', controller: budgetMinController, keyboardType: TextInputType.number),
              const SizedBox(height: 12),
              AdminTextField(label: 'Presupuesto máximo (RD\$)', controller: budgetMaxController, keyboardType: TextInputType.number),
              const SizedBox(height: 12),
              AdminTextField(label: 'Cantidad invitados', controller: guestController, keyboardType: TextInputType.number),
              const SizedBox(height: 12),
              AdminTextField(label: 'Color (hex)', controller: colorController),
              const SizedBox(height: 12),
              AdminTextField(label: 'Icono (emoji)', controller: iconController),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, {
              'name': nameController.text,
              'description': descController.text,
              'suggested_budget_min': double.tryParse(budgetMinController.text) ?? 0,
              'suggested_budget_max': double.tryParse(budgetMaxController.text) ?? 0,
              'default_guest_count': int.tryParse(guestController.text) ?? 50,
              'color': colorController.text,
              'icon': iconController.text,
            }),
            child: const Text('Crear'),
          ),
        ],
      ),
    );

    if (result != null) {
      await apiClient.createEventType(result);
      await _loadTemplates();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Plantillas de Eventos',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _templates.isEmpty
              ? EmptyState(
                  icon: Icons.auto_awesome_outlined,
                  title: 'No hay plantillas',
                  action: AdminButton(label: 'Crear Plantilla', onTap: _createTemplate),
                )
              : GridView.builder(
                  padding: const EdgeInsets.all(16),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    childAspectRatio: 1.1,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                  ),
                  itemCount: _templates.length,
                  itemBuilder: (ctx, i) {
                    final t = _templates[i];
                    final budgetMin = t['suggested_budget_min'] ?? 0;
                    final budgetMax = t['suggested_budget_max'] ?? 0;
                    return Card(
                      clipBehavior: Clip.antiAlias,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      child: InkWell(
                        onTap: () {},
                        child: Stack(
                          children: [
                            Container(
                              decoration: BoxDecoration(
                                gradient: LinearGradient(
                                  colors: [
                                    Color(int.parse(t['color']?.replaceFirst('#', '0xFF') ?? '0xFFFF3CAC')).withValues(alpha: 0.2),
                                    AppColors.primary.withValues(alpha: 0.1),
                                  ],
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                ),
                              ),
                            ),
                            Padding(
                              padding: const EdgeInsets.all(16),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    children: [
                                      Text(t['icon'] ?? '🎉', style: const TextStyle(fontSize: 32)),
                                      const Spacer(),
                                      IconButton(
                                        icon: const Icon(Icons.delete_outline, size: 20),
                                        onPressed: () async {
                                          final confirmed = await showDialog<bool>(
                                            context: context,
                                            builder: (ctx) => AlertDialog(
                                              title: const Text('Eliminar'),
                                              content: const Text('¿Eliminar esta plantilla?'),
                                              actions: [
                                                TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                                ElevatedButton(style: ElevatedButton.styleFrom(backgroundColor: AppColors.error), onPressed: () => Navigator.pop(ctx, true), child: const Text('Eliminar')),
                                              ],
                                            ),
                                          );
                                          if (confirmed == true) {
                                            await apiClient.deleteEventType(t['id']);
                                            await _loadTemplates();
                                          }
                                        },
                                      ),
                                    ],
                                  ),
                                  const Spacer(),
                                  Text(t['name'] ?? '', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700)),
                                  const SizedBox(height: 4),
                                  Text(
                                    'RD\$$budgetMin - RD\$$budgetMax',
                                    style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                  ),
                                  Text(
                                    '${t['default_guest_count'] ?? 50} invitados',
                                    style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createTemplate,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}
