import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class WhatsAppTemplatesScreen extends StatefulWidget {
  const WhatsAppTemplatesScreen({super.key});

  @override
  State<WhatsAppTemplatesScreen> createState() => _WhatsAppTemplatesScreenState();
}

class _WhatsAppTemplatesScreenState extends State<WhatsAppTemplatesScreen> {
  List<dynamic> _templates = [];
  bool _loading = true;

  final _templateTypes = <Map<String, dynamic>>[
    {'id': 'quote_sent', 'name': 'Cotizacion enviada', 'icon': Icons.request_quote},
    {'id': 'quote_approved', 'name': 'Cotizacion aprobada', 'icon': Icons.check_circle},
    {'id': 'quote_rejected', 'name': 'Cotizacion rechazada', 'icon': Icons.cancel},
    {'id': 'reminder', 'name': 'Recordatorio', 'icon': Icons.alarm},
    {'id': 'thank_you', 'name': 'Agradecimiento', 'icon': Icons.favorite},
  ];

  @override
  void initState() {
    super.initState();
    _loadTemplates();
  }

  Future<void> _loadTemplates() async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.getWhatsAppTemplates();
      _templates = response.data['data'] ?? [];
    } catch (e) {
      _templates = [];
    }
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'WhatsApp Templates',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: _templateTypes.length,
              itemBuilder: (ctx, i) {
                final type = _templateTypes[i];
                final template = _templates.firstWhere(
                  (t) => t['id'] == type['id'],
                  orElse: () => {'id': type['id'], 'message': ''},
                );

                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: InkWell(
                    onTap: () => _editTemplate(type['id'] as String, type['name'] as String, template['message'] ?? ''),
                    borderRadius: BorderRadius.circular(12),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Container(
                            width: 40,
                            height: 40,
                            decoration: BoxDecoration(
                              color: AppColors.success.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Icon(type['icon'] as IconData, color: AppColors.success, size: 20),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(type['name'] as String, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                                Text(
                                  template['message'] ?? 'Sin configurar',
                                  style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ],
                            ),
                          ),
                          const Icon(Icons.edit, size: 18, color: AppColors.textMuted),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
    );
  }

  void _editTemplate(String id, String name, String message) {
    final messageController = TextEditingController(text: message);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => Padding(
        padding: EdgeInsets.fromLTRB(16, 16, 16, MediaQuery.of(ctx).viewInsets.bottom + 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Editar: $name', style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600)),
            const SizedBox(height: 16),
            AdminTextField(label: 'Mensaje', controller: messageController, lines: 5),
            const SizedBox(height: 8),
            Text(
              'Usa {{nombre}} para el nombre del cliente, {{evento}} para el tipo de evento, etc.',
              style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () async {
                  await apiClient.updateWhatsAppTemplate(id, {'message': messageController.text});
                  if (ctx.mounted) {
                    Navigator.pop(ctx);
                    _loadTemplates();
                  }
                },
                child: const Text('Guardar'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
