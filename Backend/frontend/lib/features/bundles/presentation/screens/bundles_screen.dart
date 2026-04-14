import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../bundles_provider.dart';
import '../../data/bundle_model.dart';
import 'bundle_detail_screen.dart';

/// Bundles browser screen - shows grid of themed bundles.
class BundlesScreen extends StatefulWidget {
  const BundlesScreen({super.key});

  @override
  State<BundlesScreen> createState() => _BundlesScreenState();
}

class _BundlesScreenState extends State<BundlesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BundlesProvider>().fetchBundles();
    });
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
              child: ShaderMask(
                shaderCallback: (b) => const LinearGradient(
                  colors: [AppColors.violet, AppColors.hotPink],
                ).createShader(b),
                child: Text(
                  'Paquetes',
                  style: GoogleFonts.outfit(
                    fontSize: 36,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 4, 20, 16),
              child: Text(
                'Colecciones temáticas para tu evento',
                style: GoogleFonts.dmSans(
                  fontSize: 14,
                  color: t.textMuted,
                ),
              ),
            ),
            Expanded(
              child: Consumer<BundlesProvider>(
                builder: (context, provider, child) {
                  if (provider.isLoading && provider.bundles.isEmpty) {
                    return const Center(
                      child: CircularProgressIndicator(),
                    );
                  }

                  if (provider.error != null && provider.bundles.isEmpty) {
                    return Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            Icons.error_outline_rounded,
                            size: 48,
                            color: AppColors.coral,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'Error al cargar paquetes',
                            style: GoogleFonts.dmSans(
                              fontSize: 16,
                              color: t.textMuted,
                            ),
                          ),
                          const SizedBox(height: 8),
                          TextButton(
                            onPressed: () => provider.fetchBundles(),
                            child: const Text('Reintentar'),
                          ),
                        ],
                      ),
                    );
                  }

                  if (provider.bundles.isEmpty) {
                    return Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            Icons.inventory_2_outlined,
                            size: 48,
                            color: t.textMuted,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'No hay paquetes disponibles',
                            style: GoogleFonts.dmSans(
                              fontSize: 16,
                              color: t.textMuted,
                            ),
                          ),
                        ],
                      ),
                    );
                  }

                  return GridView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 100),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      mainAxisSpacing: 16,
                      crossAxisSpacing: 16,
                      childAspectRatio: 0.72,
                    ),
                    itemCount: provider.bundles.length,
                    itemBuilder: (context, index) {
                      final bundle = provider.bundles[index];
                      return _BundleCard(
                        bundle: bundle,
                        onTap: () => _openBundleDetail(bundle),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _openBundleDetail(Bundle bundle) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => BundleDetailScreen(bundleId: bundle.id),
      ),
    );
  }
}

class _BundleCard extends StatelessWidget {
  final Bundle bundle;
  final VoidCallback onTap;

  const _BundleCard({
    required this.bundle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withValues(alpha: 0.06),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              flex: 3,
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    if (bundle.imageUrl.isNotEmpty)
                      Image.network(
                        bundle.imageUrl,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          color: AppColors.violet.withValues(alpha: 0.1),
                          child: const Icon(
                            Icons.inventory_2_rounded,
                            color: AppColors.violet,
                            size: 40,
                          ),
                        ),
                      )
                    else
                      Container(
                        color: AppColors.violet.withValues(alpha: 0.1),
                        child: const Icon(
                          Icons.inventory_2_rounded,
                          color: AppColors.violet,
                          size: 40,
                        ),
                      ),
                    if (bundle.discountPercent > 0)
                      Positioned(
                        top: 8,
                        left: 8,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [AppColors.teal, Color(0xFF00A88A)],
                            ),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            '-${bundle.discountPercent.toStringAsFixed(0)}%',
                            style: GoogleFonts.dmSans(
                              fontSize: 11,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
            Expanded(
              flex: 2,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      bundle.name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.dmSans(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: t.textPrimary,
                        height: 1.2,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      bundle.description,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.dmSans(
                        fontSize: 11,
                        color: t.textMuted,
                        height: 1.3,
                      ),
                    ),
                    const Spacer(),
                    ShaderMask(
                      shaderCallback: (b) => const LinearGradient(
                        colors: [AppColors.violet, AppColors.hotPink],
                      ).createShader(b),
                      child: Text(
                        'desde RD\$${bundle.minPrice.toStringAsFixed(0)}',
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
