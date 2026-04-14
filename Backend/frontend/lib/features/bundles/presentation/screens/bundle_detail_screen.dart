import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../../data/bundle_model.dart';
import '../bundles_provider.dart';

/// Bundle detail screen with adjustable items list.
class BundleDetailScreen extends StatefulWidget {
  final String bundleId;

  const BundleDetailScreen({
    super.key,
    required this.bundleId,
  });

  @override
  State<BundleDetailScreen> createState() => _BundleDetailScreenState();
}

class _BundleDetailScreenState extends State<BundleDetailScreen> {
  final Map<String, bool> _selectedItems = {};
  final Map<String, int> _quantities = {};

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BundlesProvider>().fetchBundle(widget.bundleId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      body: Consumer<BundlesProvider>(
        builder: (context, provider, child) {
          final bundle = provider.selectedBundle;

          if (provider.isLoading && bundle == null) {
            return const Center(child: CircularProgressIndicator());
          }

          if (bundle == null) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.error_outline, size: 48, color: AppColors.coral),
                  const SizedBox(height: 16),
                  Text(
                    'Paquete no encontrado',
                    style: GoogleFonts.dmSans(fontSize: 16, color: t.textMuted),
                  ),
                ],
              ),
            );
          }

          // Initialize selections and quantities when bundle loads
          if (_selectedItems.isEmpty) {
            for (final item in bundle.items) {
              _selectedItems[item.id] = !item.isOptional; // Default: selected unless optional
              _quantities[item.id] = item.quantity;
            }
          }

          return CustomScrollView(
            slivers: [
              SliverAppBar(
                expandedHeight: 280,
                pinned: true,
                backgroundColor: t.isDark ? const Color(0xFF1A1A2E) : Colors.white,
                leading: GestureDetector(
                  onTap: () => Navigator.pop(context),
                  child: Container(
                    margin: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: t.isDark
                          ? Colors.white.withValues(alpha: 0.1)
                          : Colors.black.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      Icons.arrow_back_rounded,
                      color: t.textPrimary,
                    ),
                  ),
                ),
                flexibleSpace: FlexibleSpaceBar(
                  background: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (bundle.imageUrl.isNotEmpty)
                        Image.network(
                          bundle.imageUrl,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            color: AppColors.violet.withValues(alpha: 0.2),
                            child: const Icon(
                              Icons.inventory_2_rounded,
                              size: 80,
                              color: AppColors.violet,
                            ),
                          ),
                        )
                      else
                        Container(
                          color: AppColors.violet.withValues(alpha: 0.2),
                          child: const Icon(
                            Icons.inventory_2_rounded,
                            size: 80,
                            color: AppColors.violet,
                          ),
                        ),
                      Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              Colors.transparent,
                              Colors.black.withValues(alpha: 0.7),
                            ],
                          ),
                        ),
                      ),
                      Positioned(
                        left: 20,
                        right: 20,
                        bottom: 20,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            if (bundle.discountPercent > 0)
                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 10, vertical: 4),
                                margin: const EdgeInsets.only(bottom: 8),
                                decoration: BoxDecoration(
                                  gradient: const LinearGradient(
                                    colors: [AppColors.teal, Color(0xFF00A88A)],
                                  ),
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Text(
                                  '${bundle.discountPercent.toStringAsFixed(0)}% DESC.',
                                  style: GoogleFonts.dmSans(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w800,
                                    color: Colors.white,
                                  ),
                                ),
                              ),
                            Text(
                              bundle.name,
                              style: GoogleFonts.outfit(
                                fontSize: 28,
                                fontWeight: FontWeight.w800,
                                color: Colors.white,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              'desde RD\$${bundle.minPrice.toStringAsFixed(0)}',
                              style: GoogleFonts.dmSans(
                                fontSize: 16,
                                color: Colors.white.withValues(alpha: 0.9),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        bundle.description,
                        style: GoogleFonts.dmSans(
                          fontSize: 14,
                          color: t.textMuted,
                          height: 1.5,
                        ),
                      ),
                      const SizedBox(height: 24),
                      Row(
                        children: [
                          Icon(
                            Icons.checklist_rounded,
                            color: AppColors.teal,
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Artículos del paquete',
                            style: GoogleFonts.dmSans(
                              fontSize: 16,
                              fontWeight: FontWeight.w700,
                              color: t.textPrimary,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Desmarca los artículos opcionales que no quieras',
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          color: t.textMuted,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              SliverList(
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final item = bundle.items[index];
                    final isSelected = _selectedItems[item.id] ?? true;
                    final quantity = _quantities[item.id] ?? item.quantity;

                    return _BundleItemTile(
                      item: item,
                      isSelected: isSelected,
                      quantity: quantity,
                      onSelectionChanged: (selected) {
                        setState(() => _selectedItems[item.id] = selected);
                      },
                      onQuantityChanged: (qty) {
                        setState(() => _quantities[item.id] = qty);
                      },
                    );
                  },
                  childCount: bundle.items.length,
                ),
              ),
              const SliverToBoxAdapter(
                child: SizedBox(height: 120),
              ),
            ],
          );
        },
      ),
      bottomSheet: Consumer<BundlesProvider>(
        builder: (context, provider, child) {
          final bundle = provider.selectedBundle;
          if (bundle == null) return const SizedBox.shrink();

          final selectedItems = bundle.items.where((item) {
            return _selectedItems[item.id] ?? false;
          }).toList();

          if (selectedItems.isEmpty) return const SizedBox.shrink();

          return Container(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
            decoration: BoxDecoration(
              color: t.isDark ? const Color(0xFF1A1A2E) : Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 20,
                  offset: const Offset(0, -4),
                ),
              ],
            ),
            child: SafeArea(
              child: RfLuxeButton(
                label: 'Agregar al evento',
                onTap: () async {
                  await provider.addBundleToEvent(
                    context,
                    bundle.id,
                    selectedItems.map((item) {
                      return BundleItem(
                        id: item.id,
                        bundleId: item.bundleId,
                        articleId: item.articleId,
                        quantity: _quantities[item.id] ?? item.quantity,
                        isOptional: item.isOptional,
                        article: item.article,
                      );
                    }).toList(),
                  );
                  if (context.mounted) Navigator.pop(context);
                },
                loading: provider.isLoading,
              ),
            ),
          );
        },
      ),
    );
  }
}

class _BundleItemTile extends StatelessWidget {
  final BundleItem item;
  final bool isSelected;
  final int quantity;
  final ValueChanged<bool> onSelectionChanged;
  final ValueChanged<int> onQuantityChanged;

  const _BundleItemTile({
    required this.item,
    required this.isSelected,
    required this.quantity,
    required this.onSelectionChanged,
    required this.onQuantityChanged,
  });

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final article = item.article;
    final variant = article?.variants.isNotEmpty == true ? article!.variants.first : null;

    return Opacity(
      opacity: isSelected ? 1.0 : 0.5,
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected
                ? AppColors.hotPink.withValues(alpha: 0.3)
                : t.borderFaint,
          ),
        ),
        child: Row(
          children: [
            Checkbox(
              value: isSelected,
              onChanged: (val) => onSelectionChanged(val ?? false),
              activeColor: AppColors.hotPink,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(4),
              ),
            ),
            if (variant?.imageUrl != null && variant!.imageUrl!.isNotEmpty)
              ClipRRect(
                borderRadius: BorderRadius.circular(10),
                child: Image.network(
                  variant.imageUrl!,
                  width: 56,
                  height: 56,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    width: 56,
                    height: 56,
                    decoration: BoxDecoration(
                      color: AppColors.violet.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(
                      Icons.image_rounded,
                      color: AppColors.violet,
                    ),
                  ),
                ),
              )
            else
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: AppColors.violet.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(
                  Icons.inventory_2_rounded,
                  color: AppColors.violet,
                ),
              ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          article?.nameTemplate ?? 'Artículo',
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: GoogleFonts.dmSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: t.textPrimary,
                          ),
                        ),
                      ),
                      if (item.isOptional)
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: AppColors.amber.withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            'Opcional',
                            style: GoogleFonts.dmSans(
                              fontSize: 10,
                              fontWeight: FontWeight.w600,
                              color: AppColors.amber,
                            ),
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'RD\$${variant?.rentalPrice.toStringAsFixed(2) ?? '0.00'} / día',
                    style: GoogleFonts.dmSans(
                      fontSize: 13,
                      color: AppColors.hotPink,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                GestureDetector(
                  onTap: () {
                    if (quantity > 1) {
                      onQuantityChanged(quantity - 1);
                    }
                  },
                  child: Container(
                    width: 28,
                    height: 28,
                    decoration: BoxDecoration(
                      color: t.isDark
                          ? Colors.white.withValues(alpha: 0.1)
                          : Colors.black.withValues(alpha: 0.05),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(
                      Icons.remove,
                      size: 16,
                      color: t.textPrimary,
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: Text(
                    quantity.toString(),
                    style: GoogleFonts.dmSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                      color: t.textPrimary,
                    ),
                  ),
                ),
                GestureDetector(
                  onTap: () => onQuantityChanged(quantity + 1),
                  child: Container(
                    width: 28,
                    height: 28,
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [AppColors.violet, AppColors.hotPink],
                      ),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(
                      Icons.add,
                      size: 16,
                      color: Colors.white,
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
}
