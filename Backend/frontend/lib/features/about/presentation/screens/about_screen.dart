import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

/// Acerca de — Company info screen.
///
/// Holds everything that doesn't need to clutter the home: stats, contact info,
/// location with map, social media, and company description.
class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key});

  // Company constants
  static const _phoneRaw = '+1 (829) 942-4971';
  static const _phoneTel = '+18299424971';
  static const _whatsappUrl =
      'https://wa.me/18299424971?text=Hola%2C%20me%20gustar%C3%ADa%20planificar%20un%20evento%20con%20RosaFiesta';
  static const _instagramUrl = 'https://www.instagram.com/rosafiesta.rd';
  static const _facebookUrl = 'https://www.facebook.com/rosafiesta.rd';
  static const _address = 'Santo Domingo, República Dominicana';
  static const _mapsUrl =
      'https://www.google.com/maps/search/?api=1&query=RosaFiesta+Santo+Domingo';

  Future<void> _launch(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(child: _buildTopBar(context, t)),
            SliverToBoxAdapter(child: _buildHero(t)),
            SliverToBoxAdapter(child: _buildStats(t)),
            SliverToBoxAdapter(child: _buildContactCard(context, t)),
            SliverToBoxAdapter(child: _buildSocials(t)),
            SliverToBoxAdapter(child: _buildLocationCard(t)),
            SliverToBoxAdapter(child: _buildDescription(t)),
            const SliverToBoxAdapter(child: SizedBox(height: 40)),
          ],
        ),
      ),
    );
  }

  // ── Top bar ─────────────────────────────────────────────────────────────

  Widget _buildTopBar(BuildContext context, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => Navigator.of(context).maybePop(),
            child: Container(
              width: 44, height: 44,
              decoration: BoxDecoration(
                color: t.isDark ? t.card : Colors.white,
                shape: BoxShape.circle,
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.arrow_back_rounded,
                  size: 20, color: t.textPrimary),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              'Acerca de',
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: t.textPrimary,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Hero (logo + tagline) ───────────────────────────────────────────────

  Widget _buildHero(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 0),
      child: Column(
        children: [
          Container(
            width: 104, height: 104,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                  color: AppColors.hotPink.withOpacity(0.4), width: 3),
              image: const DecorationImage(
                image: AssetImage('assets/images/logo_rosafiesta.png'),
                fit: BoxFit.cover,
              ),
              boxShadow: [
                BoxShadow(
                  color: AppColors.hotPink.withOpacity(0.25),
                  blurRadius: 30,
                  offset: const Offset(0, 12),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),
          ShaderMask(
            shaderCallback: (b) =>
                AppColors.titleGradient.createShader(b),
            child: Text(
              'RosaFiesta',
              style: GoogleFonts.outfit(
                fontSize: 32,
                fontWeight: FontWeight.w800,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Decoración y planificación de eventos',
            textAlign: TextAlign.center,
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
            ),
          ),
        ],
      ),
    );
  }

  // ── Stats row ───────────────────────────────────────────────────────────

  Widget _buildStats(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 0),
      child: Row(
        children: [
          Expanded(
              child: _statCard('127', 'Eventos',
                  Icons.celebration_rounded, AppColors.hotPink, t)),
          const SizedBox(width: 10),
          Expanded(
              child: _statCard('8', 'Años',
                  Icons.workspace_premium_rounded, AppColors.violet, t)),
          const SizedBox(width: 10),
          Expanded(
              child: _statCard('4.9', 'Rating', Icons.star_rounded,
                  const Color(0xFFFFB800), t)),
        ],
      ),
    );
  }

  Widget _statCard(
      String value, String label, IconData icon, Color color, RfTheme t) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                value,
                style: GoogleFonts.outfit(
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                  color: t.textPrimary,
                ),
              ),
              const SizedBox(width: 4),
              Icon(icon, color: color, size: 20),
            ],
          ),
          const SizedBox(height: 3),
          Text(
            label,
            style: GoogleFonts.dmSans(
              fontSize: 12,
              fontWeight: FontWeight.w500,
              color: t.textMuted,
            ),
          ),
        ],
      ),
    );
  }

  // ── Contact card ────────────────────────────────────────────────────────

  Widget _buildContactCard(BuildContext context, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 0),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: t.borderFaint),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Contacto',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                Container(
                  width: 44, height: 44,
                  decoration: BoxDecoration(
                    color: AppColors.hotPink.withOpacity(0.12),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.phone_rounded,
                      color: AppColors.hotPink, size: 22),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Teléfono',
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          color: t.textMuted,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        _phoneRaw,
                        style: GoogleFonts.dmSans(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: t.textPrimary,
                        ),
                      ),
                    ],
                  ),
                ),
                GestureDetector(
                  onTap: () => _launch('tel:$_phoneTel'),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 14, vertical: 10),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(colors: [
                        AppColors.violet,
                        AppColors.hotPink,
                      ]),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Text(
                      'Llamar',
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // ── Social media ────────────────────────────────────────────────────────

  Widget _buildSocials(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: Row(
        children: [
          Expanded(
            child: _socialTile(
              label: 'WhatsApp',
              assetPath: 'assets/icons/whatsapp.png',
              onTap: () => _launch(_whatsappUrl),
              t: t,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: _socialTile(
              label: 'Instagram',
              assetPath: 'assets/icons/instagram.png',
              onTap: () => _launch(_instagramUrl),
              t: t,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: _socialTile(
              label: 'Facebook',
              materialIcon: Icons.facebook_rounded,
              color: const Color(0xFF1877F2),
              onTap: () => _launch(_facebookUrl),
              t: t,
            ),
          ),
        ],
      ),
    );
  }

  Widget _socialTile({
    required String label,
    required VoidCallback onTap,
    required RfTheme t,
    String? assetPath,
    IconData? materialIcon,
    Color? color,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: t.borderFaint),
        ),
        child: Column(
          children: [
            SizedBox(
              width: 46, height: 46,
              child: assetPath != null
                  ? Image.asset(assetPath, fit: BoxFit.contain)
                  : Container(
                      decoration: BoxDecoration(
                        color: color,
                        shape: BoxShape.circle,
                      ),
                      child: Icon(materialIcon,
                          color: Colors.white, size: 24),
                    ),
            ),
            const SizedBox(height: 10),
            Text(
              label,
              style: GoogleFonts.dmSans(
                fontSize: 12,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Location ────────────────────────────────────────────────────────────

  Widget _buildLocationCard(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: GestureDetector(
        onTap: () => _launch(_mapsUrl),
        child: Container(
          decoration: BoxDecoration(
            color: t.isDark ? t.card : Colors.white,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: t.borderFaint),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Map preview (fake tile with gradient + marker + grid)
              ClipRRect(
                borderRadius:
                    const BorderRadius.vertical(top: Radius.circular(22)),
                child: Container(
                  height: 160,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: t.isDark
                          ? [const Color(0xFF1F2937), const Color(0xFF0F172A)]
                          : [const Color(0xFFE0F2FE), const Color(0xFFF0FDFA)],
                    ),
                  ),
                  child: Stack(
                    children: [
                      // Fake streets grid
                      Positioned.fill(
                        child: CustomPaint(
                          painter: _MapGridPainter(
                              color: t.isDark
                                  ? Colors.white.withOpacity(0.06)
                                  : Colors.black.withOpacity(0.08)),
                        ),
                      ),
                      // Marker pin
                      Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Container(
                              width: 44, height: 44,
                              decoration: BoxDecoration(
                                gradient: const LinearGradient(colors: [
                                  AppColors.hotPink,
                                  AppColors.violet,
                                ]),
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: AppColors.hotPink.withOpacity(0.45),
                                    blurRadius: 20,
                                    spreadRadius: 2,
                                  ),
                                ],
                              ),
                              child: const Icon(Icons.location_on_rounded,
                                  color: Colors.white, size: 26),
                            ),
                            const SizedBox(height: 4),
                            Container(
                              width: 6, height: 6,
                              decoration: BoxDecoration(
                                color: AppColors.hotPink.withOpacity(0.5),
                                shape: BoxShape.circle,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Nuestra ubicación',
                            style: GoogleFonts.outfit(
                              fontSize: 16,
                              fontWeight: FontWeight.w700,
                              color: t.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            _address,
                            style: GoogleFonts.dmSans(
                              fontSize: 13,
                              color: t.textMuted,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 10),
                      decoration: BoxDecoration(
                        color: AppColors.hotPink.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.map_rounded,
                              color: AppColors.hotPink, size: 16),
                          const SizedBox(width: 5),
                          Text(
                            'Abrir',
                            style: GoogleFonts.dmSans(
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                              color: AppColors.hotPink,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── Company description ─────────────────────────────────────────────────

  Widget _buildDescription(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: t.borderFaint),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Sobre nosotros',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 10),
            Text(
              'RosaFiesta es una empresa dominicana dedicada a la decoración '
              'y planificación de eventos. Desde bodas y quinceañeras hasta '
              'cumpleaños, baby showers y eventos corporativos, acompañamos a '
              'nuestros clientes en cada detalle para crear momentos '
              'inolvidables. Con más de 8 años de experiencia y cientos de '
              'eventos realizados, combinamos creatividad, calidad y un '
              'servicio cercano para que solo te preocupes por disfrutar.',
              style: GoogleFonts.dmSans(
                fontSize: 14,
                height: 1.55,
                color: t.textMuted,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MapGridPainter extends CustomPainter {
  final Color color;
  _MapGridPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 1.2;
    const step = 28.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
    // Some thicker "main streets"
    final main = Paint()
      ..color = color.withOpacity((color.opacity * 2).clamp(0, 1))
      ..strokeWidth = 2.5;
    canvas.drawLine(
        Offset(0, size.height * 0.35),
        Offset(size.width, size.height * 0.55),
        main);
    canvas.drawLine(
        Offset(size.width * 0.6, 0),
        Offset(size.width * 0.4, size.height),
        main);
  }

  @override
  bool shouldRepaint(covariant _MapGridPainter oldDelegate) =>
      oldDelegate.color != color;
}
