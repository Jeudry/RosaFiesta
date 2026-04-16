import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class EmailTemplatesScreen extends StatefulWidget {
  const EmailTemplatesScreen({super.key});

  @override
  State<EmailTemplatesScreen> createState() => _EmailTemplatesScreenState();
}

class _EmailTemplatesScreenState extends State<EmailTemplatesScreen> {
  List<dynamic> _templates = [];
  bool _loading = true;

  final List<Map<String, dynamic>> _templateTypes = [
    {'id': 'user_invitation', 'name': 'Invitacion de registro', 'icon': Icons.person_add},
    {'id': 'reminder_7d', 'name': 'Recordatorio 7 dias', 'icon': Icons.event},
    {'id': 'reminder_24h', 'name': 'Recordatorio 24h', 'icon': Icons.schedule},
    {'id': 'thank_you', 'name': 'Agradecimiento post-evento', 'icon': Icons.favorite},
    {'id': 'reset_password', 'name': 'Reset password', 'icon': Icons.lock_reset},
    {'id': 'contract_confirmed', 'name': 'Contrato confirmado', 'icon': Icons.description},
  ];

  @override
  void initState() {
    super.initState();
    _loadTemplates();
  }

  Future<void> _loadTemplates() async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.getEmailTemplates();
      _templates = response.data['data'] ?? [];
    } catch (e) {
      _templates = [];
    }
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Email Templates',
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
                  orElse: () => {'id': type['id'], 'subject': '', 'body': ''},
                );

                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: InkWell(
                    onTap: () => _editTemplate(type['id'] as String, type['name'] as String, template['subject'] ?? '', template['body'] ?? ''),
                    borderRadius: BorderRadius.circular(12),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Container(
                            width: 40,
                            height: 40,
                            decoration: BoxDecoration(
                              color: AppColors.primary.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Icon(type['icon'] as IconData, color: AppColors.primary, size: 20),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(type['name'] as String, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                                Text(
                                  template['subject'] ?? 'Sin configurar',
                                  style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                  maxLines: 1,
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

  void _editTemplate(String id, String name, String subject, String body) {
    final subjectController = TextEditingController(text: subject);
    final bodyController = TextEditingController(text: body);

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
            AdminTextField(label: 'Asunto', controller: subjectController),
            const SizedBox(height: 12),
            AdminTextField(label: 'Cuerpo', controller: bodyController, lines: 6),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () async {
                      await apiClient.sendTestEmail(id);
                      if (ctx.mounted) {
                        ScaffoldMessenger.of(ctx).showSnackBar(const SnackBar(content: Text('Email de prueba enviado')));
                      }
                    },
                    child: const Text('Enviar test'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () async {
                      await apiClient.updateEmailTemplate(id, {
                        'subject': subjectController.text,
                        'body': bodyController.text,
                      });
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
          ],
        ),
      ),
    );
  }
}