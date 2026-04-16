import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class AIConfigScreen extends StatefulWidget {
  const AIConfigScreen({super.key});

  @override
  State<AIConfigScreen> createState() => _AIConfigScreenState();
}

class _AIConfigScreenState extends State<AIConfigScreen> {
  Map<String, dynamic>? _config;
  bool _loading = true;
  bool _saving = false;

  final _welcomeController = TextEditingController();
  final _confirmationController = TextEditingController();
  bool _autoApprove = false;
  int _minAutoApproveAmount = 5000;

  @override
  void initState() {
    super.initState();
    _loadConfig();
  }

  Future<void> _loadConfig() async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.getAIConfig();
      _config = response.data['data'];
      _welcomeController.text = _config?['welcome_message'] ?? '';
      _confirmationController.text = _config?['confirmation_message'] ?? '';
      _autoApprove = _config?['auto_approve'] ?? false;
      _minAutoApproveAmount = _config?['min_auto_approve_amount'] ?? 5000;
    } catch (e) {
      _config = {};
    }
    setState(() => _loading = false);
  }

  Future<void> _saveConfig() async {
    setState(() => _saving = true);
    try {
      await apiClient.updateAIConfig({
        'welcome_message': _welcomeController.text,
        'confirmation_message': _confirmationController.text,
        'auto_approve': _autoApprove,
        'min_auto_approve_amount': _minAutoApproveAmount,
      });
    } catch (e) {
      // Handle error
    }
    setState(() => _saving = false);
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Config IA Rosa',
      showBack: true,
      actions: [
        TextButton(
          onPressed: _saving ? null : _saveConfig,
          child: _saving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Guardar'),
        ),
      ],
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Mensajes', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          AdminTextField(
                            label: 'Mensaje de bienvenida',
                            controller: _welcomeController,
                            lines: 3,
                            hint: 'El mensaje que Rosa IA muestra al inicio',
                          ),
                          const SizedBox(height: 12),
                          AdminTextField(
                            label: 'Mensaje de confirmación',
                            controller: _confirmationController,
                            lines: 3,
                            hint: 'Mensaje al final del flujo',
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Configuración de Cotización', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          SwitchListTile(
                            contentPadding: EdgeInsets.zero,
                            title: const Text('Auto-aprobar cotizaciones'),
                            subtitle: const Text('Aprobar automáticamente cotizaciones menores al monto mínimo'),
                            value: _autoApprove,
                            onChanged: (v) => setState(() => _autoApprove = v),
                          ),
                          const SizedBox(height: 12),
                          AdminTextField(
                            label: 'Monto mínimo para auto-aprobar (RD\$)',
                            controller: TextEditingController(text: _minAutoApproveAmount.toString()),
                            keyboardType: TextInputType.number,
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Pasos del Flow', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          _FlowStep(step: 1, label: 'Bienvenida', description: _config?['step_1'] ?? ''),
                          _FlowStep(step: 2, label: 'Tipo de evento', description: _config?['step_2'] ?? ''),
                          _FlowStep(step: 3, label: 'Fecha', description: _config?['step_3'] ?? ''),
                          _FlowStep(step: 4, label: 'Ubicación', description: _config?['step_4'] ?? ''),
                          _FlowStep(step: 5, label: 'Invitados', description: _config?['step_5'] ?? ''),
                          _FlowStep(step: 6, label: 'Estilo/Colores', description: _config?['step_6'] ?? ''),
                          _FlowStep(step: 7, label: 'Presupuesto', description: _config?['step_7'] ?? ''),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  SizedBox(
                    width: double.infinity,
                    child: AdminButton(
                      label: 'Ver Historial de Conversaciones',
                      onTap: () => Navigator.pushNamed(context, '/ai-config/history'),
                      outlined: true,
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}

class _FlowStep extends StatelessWidget {
  final int step;
  final String label;
  final String description;

  const _FlowStep({required this.step, required this.label, required this.description});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 28,
            height: 28,
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Center(
              child: Text('$step', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, color: AppColors.primary, fontSize: 12)),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, fontSize: 13)),
                Text(description.isEmpty ? 'No configurado' : description, style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
              ],
            ),
          ),
          const Icon(Icons.edit, size: 16, color: AppColors.textMuted),
        ],
      ),
    );
  }
}
