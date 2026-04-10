import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../categories_provider.dart';
import '../../data/category_models.dart';
import '../../../products/presentation/screens/products_list_screen.dart';

/// Categorías — grid de todas las categorías del catálogo.
///
/// Content-only screen: el MainShell se encarga de la bottom bar, el FAB
/// de la IA y el fondo animado.
class CategoriesScreen extends StatefulWidget {
  const CategoriesScreen({super.key});

  @override
  State<CategoriesScreen> createState() => _CategoriesScreenState();
}

class _CategoriesScreenState extends State<CategoriesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = context.read<CategoriesProvider>();
      if (provider.categories.isEmpty) {
        provider.fetchCategories();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;
    final provider = context.watch<CategoriesProvider>();

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
                    'Todas las categorías',
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
                  'Explora nuestro catálogo por tipo de artículo',
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    color: t.textMuted,
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: _buildBody(provider, t),
          ),
        ],
      ),
    );
  }

  Widget _buildBody(CategoriesProvider provider, RfTheme t) {
    if (provider.isLoading && provider.categories.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.error != null && provider.categories.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.error_outline_rounded,
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
                onTap: () => provider.fetchCategories(),
              ),
            ],
          ),
        ),
      );
    }

    if (provider.categories.isEmpty) {
      return Center(
        child: Text(
          'Aún no hay categorías disponibles',
          style: GoogleFonts.dmSans(fontSize: 14, color: t.textMuted),
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () => provider.fetchCategories(),
      child: GridView.builder(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 120),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          mainAxisSpacing: 14,
          crossAxisSpacing: 14,
          childAspectRatio: 1.15,
        ),
        itemCount: provider.categories.length,
        itemBuilder: (context, index) {
          return _CategoryCard(
            category: provider.categories[index],
            t: t,
            colorIndex: index,
          );
        },
      ),
    );
  }
}

class _CategoryCard extends StatelessWidget {
  final Category category;
  final RfTheme t;
  final int colorIndex;

  const _CategoryCard({
    required this.category,
    required this.t,
    required this.colorIndex,
  });

  static const _palette = [
    [AppColors.hotPink, AppColors.coral],
    [AppColors.teal, AppColors.sky],
    [AppColors.amber, AppColors.coral],
    [AppColors.violet, AppColors.hotPink],
    [AppColors.sky, AppColors.violet],
    [AppColors.coral, AppColors.amber],
  ];

  @override
  Widget build(BuildContext context) {
    final colors = _palette[colorIndex % _palette.length];
    final hasImage =
        category.imageUrl != null && category.imageUrl!.isNotEmpty;

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => ProductsListScreen(categoryId: category.id),
          ),
        );
      },
      child: Container(
        clipBehavior: Clip.antiAlias,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: t.borderFaint, width: 1),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Stack(
          fit: StackFit.expand,
          children: [
            // Layer 1: image background (if available)
            if (hasImage)
              Image.network(
                category.imageUrl!,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => _IconFallback(
                  iconName: category.icon,
                  colors: colors,
                ),
                loadingBuilder: (context, child, progress) {
                  if (progress == null) return child;
                  return Container(
                    color: colors[0].withOpacity(0.08),
                  );
                },
              )
            else
              _IconFallback(iconName: category.icon, colors: colors),

            // Layer 2: dark gradient overlay so the name is readable on top
            if (hasImage)
              Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black.withOpacity(0.05),
                      Colors.black.withOpacity(0.55),
                    ],
                  ),
                ),
              ),

            // Layer 3: name + icon badge
            Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Icon badge top-left
                  Container(
                    width: 38,
                    height: 38,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: colors,
                      ),
                      borderRadius: BorderRadius.circular(12),
                      boxShadow: [
                        BoxShadow(
                          color: colors[0].withOpacity(0.35),
                          blurRadius: 8,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Icon(
                      _iconFor(category.icon),
                      color: Colors.white,
                      size: 20,
                    ),
                  ),
                  // Name bottom-left
                  Text(
                    category.name,
                    style: GoogleFonts.outfit(
                      fontSize: 17,
                      fontWeight: FontWeight.w800,
                      color: hasImage ? Colors.white : t.textPrimary,
                      height: 1.15,
                      shadows: hasImage
                          ? [
                              Shadow(
                                color: Colors.black.withOpacity(0.5),
                                blurRadius: 6,
                                offset: const Offset(0, 1),
                              ),
                            ]
                          : null,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Fallback shown when there's no image — gradient background with the icon
/// large in the center.
class _IconFallback extends StatelessWidget {
  final String? iconName;
  final List<Color> colors;
  const _IconFallback({required this.iconName, required this.colors});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            colors[0].withOpacity(0.18),
            colors[1].withOpacity(0.18),
          ],
        ),
      ),
      alignment: Alignment.center,
      child: Icon(
        _iconFor(iconName),
        color: colors[0],
        size: 56,
      ),
    );
  }
}

/// Maps backend icon string identifiers to Material [IconData].
/// Add new entries here as the backend introduces more category icons.
IconData _iconFor(String? name) {
  switch (name) {
    case 'chair':
      return Icons.chair_rounded;
    case 'auto_awesome':
      return Icons.auto_awesome_rounded;
    case 'lightbulb':
      return Icons.lightbulb_rounded;
    case 'local_florist':
      return Icons.local_florist_rounded;
    case 'celebration':
      return Icons.celebration_rounded;
    case 'table_restaurant':
      return Icons.table_restaurant_rounded;
    case 'cake':
      return Icons.cake_rounded;
    case 'auto_awesome_motion':
      return Icons.auto_awesome_motion_rounded;
    default:
      return Icons.category_rounded;
  }
}
