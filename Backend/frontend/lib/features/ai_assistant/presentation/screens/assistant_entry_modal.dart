import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import 'package:url_launcher/url_launcher.dart' show LaunchMode;
import '../assistant_provider.dart';
import 'assistant_screen.dart';

/// Bottom sheet that lets the user pick how they want to talk to RosaFiesta:
///  • WhatsApp with the human owner
///  • Chat with the AI assistant
///  • Voice with the AI assistant
class AssistantEntryModal extends StatelessWidget {
  const AssistantEntryModal({super.key});

  static const _whatsappUrl =
      'https://wa.me/18299424971?text=Hola%2C%20me%20gustar%C3%ADa%20planificar%20un%20evento%20con%20RosaFiesta';

  static Future<void> show(BuildContext context) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const AssistantEntryModal(),
    );
  }

  Future<void> _launchWhatsApp(BuildContext context) async {
    Navigator.of(context).pop();
    final uri = Uri.parse(_whatsappUrl);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  void _openAssistant(BuildContext context, AssistantMode mode) {
    final provider = context.read<AssistantProvider>();
    provider.reset();
    provider.setMode(mode);
    Navigator.of(context).pop();
    AssistantScreen.open(context);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Container(
      decoration: BoxDecoration(
        color: t.base,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        border: Border.all(color: t.borderFaint),
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Drag handle
              Center(
                child: Container(
                  width: 44, height: 4,
                  decoration: BoxDecoration(
                    color: t.textDim.withOpacity(0.3),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 22),
              ShaderMask(
                shaderCallback: (b) =>
                    AppColors.titleGradient.createShader(b),
                child: Text(
                  '¿Cómo quieres planificar\ntu evento?',
                  style: GoogleFonts.outfit(
                    fontSize: 26,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                    height: 1.15,
                  ),
                ),
              ),
              const SizedBox(height: 6),
              Text(
                'Elige cómo prefieres hablar con nosotros.',
                style: GoogleFonts.dmSans(
                  fontSize: 14,
                  color: t.textMuted,
                ),
              ),
              const SizedBox(height: 22),
              _channelCard(
                context: context,
                t: t,
                title: 'WhatsApp con la dueña',
                subtitle:
                    'Habla directamente con María, la dueña, por WhatsApp.',
                iconAsset: 'assets/icons/whatsapp.png',
                iconBgColor: const Color(0xFF25D366),
                onTap: () => _launchWhatsApp(context),
              ),
              const SizedBox(height: 12),
              _channelCard(
                context: context,
                t: t,
                title: 'Chat con Rosa IA',
                subtitle:
                    'Conversa con nuestra asistente y arma tu evento paso a paso.',
                iconData: Icons.chat_bubble_rounded,
                gradient: const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                badge: 'Rápido',
                onTap: () => _openAssistant(context, AssistantMode.chat),
              ),
              const SizedBox(height: 12),
              _channelCard(
                context: context,
                t: t,
                title: 'Voz con Rosa IA',
                subtitle: 'Solo habla, nosotros te escuchamos y te guiamos.',
                iconData: Icons.graphic_eq_rounded,
                gradient: const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.amber, Color(0xFFFF8C00)],
                ),
                badge: 'Manos libres',
                onTap: () => _openAssistant(context, AssistantMode.voice),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _channelCard({
    required BuildContext context,
    required RfTheme t,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
    String? iconAsset,
    IconData? iconData,
    Color? iconBgColor,
    LinearGradient? gradient,
    String? badge,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 18,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 60, height: 60,
              decoration: BoxDecoration(
                color: iconBgColor,
                gradient: gradient,
                borderRadius: BorderRadius.circular(18),
                boxShadow: gradient != null
                    ? [
                        BoxShadow(
                          color: gradient.colors.first.withOpacity(0.3),
                          blurRadius: 14,
                          offset: const Offset(0, 6),
                        ),
                      ]
                    : null,
              ),
              child: iconAsset != null
                  ? Padding(
                      padding: const EdgeInsets.all(12),
                      child: Image.asset(iconAsset, fit: BoxFit.contain),
                    )
                  : Icon(iconData, color: Colors.white, size: 30),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Row(
                    children: [
                      Flexible(
                        child: Text(
                          title,
                          style: GoogleFonts.outfit(
                            fontSize: 17,
                            fontWeight: FontWeight.w800,
                            color: t.textPrimary,
                          ),
                        ),
                      ),
                      if (badge != null) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 3),
                          decoration: BoxDecoration(
                            color: AppColors.teal.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            badge,
                            style: GoogleFonts.dmSans(
                              fontSize: 10,
                              fontWeight: FontWeight.w800,
                              color: AppColors.teal,
                              letterSpacing: 0.3,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: GoogleFonts.dmSans(
                      fontSize: 13,
                      color: t.textMuted,
                      height: 1.35,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 8),
            Icon(Icons.chevron_right_rounded, color: t.textDim, size: 26),
          ],
        ),
      ),
    );
  }
}
