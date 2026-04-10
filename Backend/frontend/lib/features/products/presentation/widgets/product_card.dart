import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../data/product_models.dart';
import '../screens/product_detail_screen.dart';
import '../../../active_event/presentation/active_event_provider.dart';
import '../../../favorites/presentation/favorites_provider.dart';

/// Shared product card used by the catalog and the favorites screen.
///
/// - Heart icon toggles via [FavoritesProvider].
/// - "+" icon adds the product to the user's draft event via
///   [ActiveEventProvider]. Both interactions stop event propagation so
///   the card tap (open detail) does not fire on the same gesture.
class ProductCard extends StatelessWidget {
  final Product product;
  final VoidCallback? onAddToCart;
  final VoidCallback? onTap;

  const ProductCard({
    super.key,
    required this.product,
    this.onAddToCart,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final variant = product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    final lowStock =
        product.stockQuantity > 0 && product.stockQuantity < 10;

    final isFav =
        context.watch<FavoritesProvider>().isFavorite(product.id);

    return GestureDetector(
      onTap: onTap ??
          () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) =>
                      ProductDetailScreen(productId: product.id),
                ),
              ),
      child: Container(
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.06),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: ClipRRect(
                borderRadius:
                    const BorderRadius.vertical(top: Radius.circular(22)),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    if (imageUrl != null && imageUrl.isNotEmpty)
                      Image.network(
                        imageUrl,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          color: AppColors.hotPink.withOpacity(0.08),
                          child: const Icon(
                            Icons.image_not_supported_rounded,
                            color: AppColors.hotPink,
                            size: 32,
                          ),
                        ),
                        loadingBuilder: (context, child, progress) {
                          if (progress == null) return child;
                          return Container(
                            color: AppColors.hotPink.withOpacity(0.06),
                          );
                        },
                      )
                    else
                      Container(
                        color: AppColors.hotPink.withOpacity(0.08),
                        child: const Icon(
                          Icons.image_not_supported_rounded,
                          color: AppColors.hotPink,
                          size: 32,
                        ),
                      ),
                    if (product.type == 'Sale')
                      Positioned(
                        top: 8,
                        left: 8,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(colors: [
                              AppColors.amber,
                              Color(0xFFFF8C00),
                            ]),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            'VENTA',
                            style: GoogleFonts.dmSans(
                              fontSize: 9,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                              letterSpacing: 0.8,
                            ),
                          ),
                        ),
                      ),
                    if (lowStock)
                      Positioned(
                        top: 8,
                        right: 8,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: AppColors.coral,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            '¡Solo ${product.stockQuantity}!',
                            style: GoogleFonts.dmSans(
                              fontSize: 9,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                    // Favorite button — functional
                    Positioned(
                      bottom: 8,
                      right: 8,
                      child: GestureDetector(
                        behavior: HitTestBehavior.opaque,
                        onTap: () {
                          context
                              .read<FavoritesProvider>()
                              .toggle(product);
                        },
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 180),
                          width: 34,
                          height: 34,
                          decoration: BoxDecoration(
                            color: isFav
                                ? AppColors.hotPink
                                : Colors.white.withOpacity(0.92),
                            borderRadius: BorderRadius.circular(10),
                            boxShadow: isFav
                                ? [
                                    BoxShadow(
                                      color: AppColors.hotPink
                                          .withOpacity(0.35),
                                      blurRadius: 8,
                                      offset: const Offset(0, 2),
                                    ),
                                  ]
                                : null,
                          ),
                          child: Icon(
                            isFav
                                ? Icons.favorite_rounded
                                : Icons.favorite_border_rounded,
                            color: isFav
                                ? Colors.white
                                : AppColors.hotPink.withOpacity(0.85),
                            size: 19,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(14, 8, 14, 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    product.nameTemplate,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.dmSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: t.textPrimary,
                      height: 1.2,
                    ),
                  ),
                  if (product.descriptionTemplate != null &&
                      product.descriptionTemplate!.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(
                      product.descriptionTemplate!,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        color: t.textMuted,
                        height: 1.3,
                      ),
                    ),
                  ],
                  const SizedBox(height: 3),
                  _StarRating(product: product, t: t),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Flexible(
                        child: ShaderMask(
                          shaderCallback: (b) =>
                              const LinearGradient(colors: [
                            AppColors.violet,
                            AppColors.hotPink,
                          ]).createShader(b),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                price.toStringAsFixed(0),
                                style: GoogleFonts.outfit(
                                  fontSize: 24,
                                  fontWeight: FontWeight.w800,
                                  color: Colors.white,
                                  height: 1,
                                ),
                              ),
                              const SizedBox(width: 3),
                              Padding(
                                padding: const EdgeInsets.only(top: 4),
                                child: Text(
                                  r'RD$',
                                  style: GoogleFonts.outfit(
                                    fontSize: 11,
                                    fontWeight: FontWeight.w700,
                                    color: Colors.white,
                                    letterSpacing: 0.5,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      GestureDetector(
                        behavior: HitTestBehavior.opaque,
                        onTap: () async {
                          // External hook wins so callers (e.g. detail
                          // page or A/B branches) can override behavior.
                          if (onAddToCart != null) {
                            onAddToCart!();
                            return;
                          }
                          await context
                              .read<ActiveEventProvider>()
                              .addItem(product);
                          if (!context.mounted) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              duration: const Duration(seconds: 2),
                              content: Text(
                                  'Agregado a tu evento: ${product.nameTemplate}'),
                            ),
                          );
                        },
                        child: Container(
                          width: 28,
                          height: 28,
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(colors: [
                              AppColors.violet,
                              AppColors.hotPink,
                            ]),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(
                            Icons.add_rounded,
                            color: Colors.white,
                            size: 18,
                          ),
                        ),
                      ),
                    ],
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

class _StarRating extends StatelessWidget {
  final Product product;
  final RfTheme t;
  const _StarRating({required this.product, required this.t});

  @override
  Widget build(BuildContext context) {
    const starColor = Color(0xFFFFB800);
    final rating = product.averageRating;
    final hasReviews = product.reviewCount > 0;

    if (!hasReviews) {
      return Row(
        children: [
          ...List.generate(
              5,
              (i) => Icon(
                    Icons.star_rounded,
                    color: starColor.withOpacity(0.55),
                    size: 18,
                  )),
          const SizedBox(width: 6),
          Text(
            '(0)',
            style: GoogleFonts.dmSans(
              fontSize: 12,
              fontWeight: FontWeight.w500,
              color: t.textMuted,
            ),
          ),
        ],
      );
    }

    return Row(
      children: [
        ...List.generate(5, (i) {
          final filled = i < rating.floor();
          final half =
              i == rating.floor() && rating - rating.floor() >= 0.5;
          return Icon(
            half
                ? Icons.star_half_rounded
                : (filled ? Icons.star_rounded : Icons.star_rounded),
            color: filled || half ? starColor : starColor.withOpacity(0.25),
            size: 18,
          );
        }),
        const SizedBox(width: 5),
        Text(
          '(${product.reviewCount})',
          style: GoogleFonts.dmSans(
            fontSize: 12,
            fontWeight: FontWeight.w500,
            color: t.textMuted,
          ),
        ),
      ],
    );
  }
}
