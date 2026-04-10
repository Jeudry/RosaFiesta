import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../favorites_provider.dart';
import '../../../products/presentation/widgets/product_card.dart';

/// Mis favoritos — grid de artículos favoritados por el usuario.
///
/// Content-only screen: el [MainShell] envuelve con bottom nav, FAB de IA
/// y fondo animado.
class FavoritesScreen extends StatefulWidget {
  const FavoritesScreen({super.key});

  @override
  State<FavoritesScreen> createState() => _FavoritesScreenState();
}

class _FavoritesScreenState extends State<FavoritesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = context.read<FavoritesProvider>();
      if (provider.favorites.isEmpty && !provider.isLoading) {
        provider.fetchFavorites();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final provider = context.watch<FavoritesProvider>();

    return SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ShaderMask(
                  shaderCallback: (bounds) =>
                      AppColors.titleGradient.createShader(bounds),
                  child: Text(
                    'Mis favoritos',
                    style: GoogleFonts.outfit(
                      fontSize: 32,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      height: 1.1,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  provider.favorites.isEmpty
                      ? 'Toca el corazón en cualquier artículo para guardarlo aquí'
                      : '${provider.favorites.length} ${provider.favorites.length == 1 ? "artículo guardado" : "artículos guardados"}',
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    color: t.textMuted,
                  ),
                ),
              ],
            ),
          ),
          Expanded(child: _buildBody(provider, t)),
        ],
      ),
    );
  }

  Widget _buildBody(FavoritesProvider provider, RfTheme t) {
    if (provider.isLoading && provider.favorites.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.error != null && provider.favorites.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline_rounded,
                  size: 48, color: AppColors.coral),
              const SizedBox(height: 12),
              Text(
                provider.error!,
                textAlign: TextAlign.center,
                style: GoogleFonts.dmSans(
                  fontSize: 14,
                  color: t.textMuted,
                ),
              ),
              const SizedBox(height: 16),
              RfLuxeButton(
                label: 'Reintentar',
                onTap: () => provider.fetchFavorites(),
              ),
            ],
          ),
        ),
      );
    }

    if (provider.favorites.isEmpty) {
      return _EmptyFavorites(t: t);
    }

    return RefreshIndicator(
      onRefresh: () => provider.fetchFavorites(),
      child: GridView.builder(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 120),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 0.63,
        ),
        itemCount: provider.favorites.length,
        itemBuilder: (context, index) {
          return ProductCard(product: provider.favorites[index]);
        },
      ),
    );
  }
}

class _EmptyFavorites extends StatelessWidget {
  final RfTheme t;
  const _EmptyFavorites({required this.t});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(32, 0, 32, 80),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 110,
              height: 110,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    AppColors.hotPink.withOpacity(0.18),
                    AppColors.violet.withOpacity(0.18),
                  ],
                ),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.favorite_rounded,
                color: AppColors.hotPink,
                size: 56,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Aún no tienes favoritos',
              textAlign: TextAlign.center,
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Explora el catálogo y toca el corazón en los artículos que quieras guardar para más tarde.',
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 14,
                color: t.textMuted,
                height: 1.45,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
