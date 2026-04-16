import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Notificaciones',
      showBack: true,
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _ConfigTile(
            icon: Icons.email_outlined,
            title: 'Plantillas de Email',
            subtitle: 'Editar mensajes automáticos de email',
            onTap: () => Navigator.pushNamed(context, '/notifications/email'),
          ),
          const SizedBox(height: 8),
          _ConfigTile(
            icon: Icons.chat_outlined,
            title: 'Plantillas de WhatsApp',
            subtitle: 'Editar mensajes de WhatsApp',
            onTap: () => Navigator.pushNamed(context, '/notifications/whatsapp'),
          ),
          const SizedBox(height: 8),
          _ConfigTile(
            icon: Icons.schedule_outlined,
            title: 'Triggers',
            subtitle: 'Configurar cuándo se envían las notificaciones',
            onTap: () {},
          ),
          const SizedBox(height: 24),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Notificaciones Automáticas', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 16),
                  _TriggerToggle(label: 'Recordatorio 7 días antes', enabled: true),
                  _TriggerToggle(label: 'Recordatorio 24h antes', enabled: true),
                  _TriggerToggle(label: 'Agradecimiento post-evento', enabled: true),
                  _TriggerToggle(label: 'Pago recibido', enabled: true),
                  _TriggerToggle(label: 'Cotización ajustada', enabled: true),
                  _TriggerToggle(label: 'Cotización aprobada/rechazada', enabled: true),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ConfigTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _ConfigTile({required this.icon, required this.title, required this.subtitle, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: AppColors.primary, size: 20),
        ),
        title: Text(title, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle, style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
        trailing: const Icon(Icons.chevron_right, size: 18),
        onTap: onTap,
      ),
    );
  }
}

class _TriggerToggle extends StatefulWidget {
  final String label;
  final bool enabled;

  const _TriggerToggle({required this.label, required this.enabled});

  @override
  State<_TriggerToggle> createState() => _TriggerToggleState();
}

class _TriggerToggleState extends State<_TriggerToggle> {
  late bool _enabled;

  @override
  void initState() {
    super.initState();
    _enabled = widget.enabled;
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Expanded(child: Text(widget.label, style: GoogleFonts.dmSans())),
          Switch(value: _enabled, onChanged: (v) => setState(() => _enabled = v)),
        ],
      ),
    );
  }
}
