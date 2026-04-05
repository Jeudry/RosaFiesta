import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../products_provider.dart';
import '../../../categories/presentation/categories_provider.dart';
import '../../../shell/main_shell.dart';
import '../../../shop/presentation/cart_provider.dart';
import '../../../shop/presentation/screens/cart_screen.dart';
import '../../data/product_models.dart';
import 'product_detail_screen.dart';

/// Catálogo — Candy Pop redesign.
///
/// Content-only screen: the MainShell wraps it with the persistent bottom
/// bar, AI assistant FAB and animated background.
class ProductsListScreen extends StatefulWidget {
  final String? categoryId;
  const ProductsListScreen({super.key, this.categoryId});

  @override
  State<ProductsListScreen> createState() => _ProductsListScreenState();
}

class _ProductsListScreenState extends State<ProductsListScreen> {
  String _searchQuery = '';
  String _activeFilter = 'Todos';
  String? _activeCategoryId;
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final FocusNode _searchFocus = FocusNode();

  static const _filters = [
    _FilterChip('Todos', Icons.apps_rounded),
    _FilterChip('Más popular', Icons.local_fire_department_rounded),
    _FilterChip('Disponibles', Icons.check_circle_outline_rounded),
    _FilterChip('Precio ↓', Icons.arrow_downward_rounded),
    _FilterChip('Precio ↑', Icons.arrow_upward_rounded),
  ];

  static const _suggestions = [
    'Sillas Tiffany',
    'Mesas redondas',
    'Flores',
    'Globos',
    'Luces string',
    'Gender Reveal',
    'Candelabros',
    'Neón LED',
  ];

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    _searchFocus.addListener(() => setState(() {}));
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProductsProvider>().fetchProducts(refresh: true);
      context.read<CategoriesProvider>().fetchCategories();
    });
  }

  bool get _isSearching => _searchFocus.hasFocus || _searchQuery.isNotEmpty;

  void _onScroll() {
    if (!_scrollController.hasClients) return;
    final max = _scrollController.position.maxScrollExtent;
    final current = _scrollController.offset;
    if (max - current < 400) {
      context.read<ProductsProvider>().fetchMoreProducts();
    }
  }

  @override
  void dispose() {
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    _searchController.dispose();
    _searchFocus.dispose();
    super.dispose();
  }

  List<Product> _filterProducts(List<Product> all) {
    var list = all.where((p) {
      // Category filter
      if (_activeCategoryId != null && p.categoryId != _activeCategoryId) {
        return false;
      }
      // Search filter
      if (_searchQuery.isNotEmpty) {
        final q = _searchQuery.toLowerCase();
        if (!p.nameTemplate.toLowerCase().contains(q) &&
            !(p.descriptionTemplate ?? '').toLowerCase().contains(q)) {
          return false;
        }
      }
      return true;
    }).toList();

    // Sort by active filter
    switch (_activeFilter) {
      case 'Más popular':
        list.sort((a, b) => b.averageRating.compareTo(a.averageRating));
        break;
      case 'Disponibles':
        list = list.where((p) => p.stockQuantity > 0).toList();
        break;
      case 'Precio ↓':
        list.sort((a, b) {
          final pa = a.variants.isNotEmpty ? a.variants.first.rentalPrice : 0;
          final pb = b.variants.isNotEmpty ? b.variants.first.rentalPrice : 0;
          return pa.compareTo(pb);
        });
        break;
      case 'Precio ↑':
        list.sort((a, b) {
          final pa = a.variants.isNotEmpty ? a.variants.first.rentalPrice : 0;
          final pb = b.variants.isNotEmpty ? b.variants.first.rentalPrice : 0;
          return pb.compareTo(pa);
        });
        break;
    }
    return list;
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Material(
      type: MaterialType.transparency,
      child: Consumer<ProductsProvider>(
        builder: (context, provider, _) {
          final products = _filterProducts(provider.products);
          return CustomScrollView(
            controller: _scrollController,
            slivers: [
              SliverToBoxAdapter(child: _buildTopBar(t)),
              const SliverToBoxAdapter(child: SizedBox(height: 8)),
              SliverToBoxAdapter(child: _buildSearchBar(t)),
              if (_isSearching) ...[
                SliverToBoxAdapter(child: _buildSuggestions(t)),
                SliverToBoxAdapter(child: _buildFilterChips(t)),
                SliverToBoxAdapter(
                    child: _buildPreviewResults(products, t)),
              ] else ...[
                if (provider.isLoading && products.isEmpty)
                  const SliverFillRemaining(
                    hasScrollBody: false,
                    child: Center(
                      child: CircularProgressIndicator(
                        color: AppColors.hotPink,
                        strokeWidth: 2,
                      ),
                    ),
                  )
                else if (products.isEmpty)
                  SliverToBoxAdapter(child: _buildEmptyState(t))
                else
                  SliverToBoxAdapter(
                      child: _buildArticlesCard(products, t)),
              ],
              if (provider.isLoadingMore)
                const SliverToBoxAdapter(
                  child: Padding(
                    padding: EdgeInsets.symmetric(vertical: 20),
                    child: Center(
                      child: SizedBox(
                        width: 24, height: 24,
                        child: CircularProgressIndicator(
                          color: AppColors.hotPink,
                          strokeWidth: 2,
                        ),
                      ),
                    ),
                  ),
                ),
              const SliverToBoxAdapter(child: SizedBox(height: 160)),
            ],
          );
        },
      ),
    );
  }

  // ── Top Bar ─────────────────────────────────────────────────────────────

  Widget _buildTopBar(RfTheme t) {
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
        child: Row(
          children: [
            _topBarIcon(Icons.arrow_back_rounded, t, () {
              MainShell.of(context)?.goToTab(0);
            }),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                'Catálogo',
                style: GoogleFonts.outfit(
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                  color: t.textPrimary,
                ),
              ),
            ),
            _topBarIcon(
              t.isDark
                  ? Icons.dark_mode_rounded
                  : Icons.wb_sunny_rounded,
              t,
              () => context.read<ThemeProvider>().toggle(),
            ),
            const SizedBox(width: 6),
            _topBarIcon(Icons.notifications_rounded, t, () {},
                showDot: true),
            const SizedBox(width: 6),
            Consumer<CartProvider>(
              builder: (context, cart, _) {
                return Stack(
                  clipBehavior: Clip.none,
                  children: [
                    _topBarIcon(Icons.shopping_cart_outlined, t, () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => const CartScreen()));
                    }),
                    if (cart.itemCount > 0)
                      Positioned(
                        right: -2, top: -2,
                        child: Container(
                          width: 18, height: 18,
                          decoration: const BoxDecoration(
                            gradient: AppColors.buttonGradient,
                            shape: BoxShape.circle,
                          ),
                          alignment: Alignment.center,
                          child: Text(
                            '${cart.itemCount}',
                            style: GoogleFonts.dmSans(
                                fontSize: 10,
                                fontWeight: FontWeight.w700,
                                color: Colors.white),
                          ),
                        ),
                      ),
                  ],
                );
              },
            ),
            const SizedBox(width: 6),
            Container(
              width: 40, height: 40,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                    color: AppColors.hotPink.withOpacity(0.4),
                    width: 1.5),
                image: const DecorationImage(
                  image: AssetImage('assets/images/logo_rosafiesta.png'),
                  fit: BoxFit.cover,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _topBarIcon(IconData icon, RfTheme t, VoidCallback onTap,
      {bool showDot = false}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40, height: 40,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          shape: BoxShape.circle,
          border: Border.all(color: t.borderFaint),
        ),
        child: Stack(
          clipBehavior: Clip.none,
          alignment: Alignment.center,
          children: [
            Icon(icon, size: 20, color: t.textPrimary),
            if (showDot)
              Positioned(
                top: 7, right: 9,
                child: Container(
                  width: 8, height: 8,
                  decoration: BoxDecoration(
                    color: AppColors.coral,
                    shape: BoxShape.circle,
                    border:
                        Border.all(color: t.isDark ? t.card : Colors.white, width: 1.5),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  // ── Artículos Card (category tabs + grid) ───────────────────────────────

  Widget _buildArticlesCard(List<Product> products, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 18, 16, 0),
      child: Container(
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(28),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.05),
              blurRadius: 24,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        padding: const EdgeInsets.fromLTRB(16, 18, 16, 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Artículos',
                  style: GoogleFonts.outfit(
                    fontSize: 22,
                    fontWeight: FontWeight.w800,
                    color: t.textPrimary,
                  ),
                ),
                Text(
                  '${products.length} productos',
                  style: GoogleFonts.dmSans(
                    fontSize: 13,
                    fontWeight: FontWeight.w500,
                    color: t.textMuted,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            // Category tabs
            _buildCategoryTabs(t),
            const SizedBox(height: 16),
            // Grid
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              childAspectRatio: 0.58,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
              children: products.map((p) => _productCard(p, t)).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCategoryTabs(RfTheme t) {
    return Consumer<CategoriesProvider>(
      builder: (context, provider, _) {
        final cats = provider.categories;
        return SizedBox(
          height: 38,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: cats.length + 1,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, i) {
              final label = i == 0 ? 'Todas' : cats[i - 1].name;
              final id = i == 0 ? null : cats[i - 1].id;
              final isActive = _activeCategoryId == id;
              return GestureDetector(
                onTap: () => setState(() => _activeCategoryId = id),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    gradient: isActive
                        ? const LinearGradient(colors: [
                            AppColors.violet,
                            AppColors.hotPink,
                          ])
                        : null,
                    color: isActive
                        ? null
                        : (t.isDark
                            ? Colors.white.withOpacity(0.04)
                            : const Color(0xFFF3F4F6)),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    label,
                    style: GoogleFonts.dmSans(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: isActive ? Colors.white : t.textPrimary,
                    ),
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }

  // ── Search Mode: Preview Results (horizontal) ───────────────────────────

  Widget _buildPreviewResults(List<Product> products, RfTheme t) {
    if (products.isEmpty) {
      return Padding(
        padding: const EdgeInsets.fromLTRB(20, 32, 20, 20),
        child: Text(
          _searchQuery.isNotEmpty
              ? 'Sin resultados para "$_searchQuery"'
              : 'Escribe para buscar',
          style: GoogleFonts.dmSans(
            fontSize: 14,
            color: t.textMuted,
          ),
        ),
      );
    }
    return Padding(
      padding: const EdgeInsets.only(top: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
            child: Text(
              'Resultados',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
          ),
          SizedBox(
            height: 250,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 20),
              itemCount: products.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (context, i) => _previewCard(products[i], t),
            ),
          ),
        ],
      ),
    );
  }

  Widget _previewCard(Product product, RfTheme t) {
    final variant = product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    return GestureDetector(
      onTap: () => Navigator.push(
          context,
          MaterialPageRoute(
              builder: (_) => ProductDetailScreen(productId: product.id))),
      child: Container(
        width: 160,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: t.borderFaint),
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                flex: 5,
                child: imageUrl != null && imageUrl.isNotEmpty
                    ? Image.network(
                        imageUrl,
                        width: double.infinity,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          color: AppColors.hotPink.withOpacity(0.08),
                          child: const Icon(
                              Icons.image_not_supported_rounded,
                              color: AppColors.hotPink),
                        ),
                      )
                    : Container(color: AppColors.hotPink.withOpacity(0.08)),
              ),
              Expanded(
                flex: 4,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        product.nameTemplate,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: GoogleFonts.dmSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w700,
                          color: t.textPrimary,
                          height: 1.2,
                        ),
                      ),
                      ShaderMask(
                        shaderCallback: (b) => const LinearGradient(colors: [
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
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                                color: Colors.white,
                                height: 1,
                              ),
                            ),
                            const SizedBox(width: 3),
                            Padding(
                              padding: const EdgeInsets.only(top: 3),
                              child: Text(
                                r'RD$',
                                style: GoogleFonts.outfit(
                                  fontSize: 9,
                                  fontWeight: FontWeight.w700,
                                  color: Colors.white,
                                  letterSpacing: 0.5,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── Search Bar ──────────────────────────────────────────────────────────

  Widget _buildSearchBar(RfTheme t) {
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 10, 20, 0),
        child: Row(
        children: [
          Expanded(
            child: Container(
              height: 56,
              decoration: BoxDecoration(
                color: t.isDark ? t.card : Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                children: [
                  const SizedBox(width: 16),
                  const Icon(Icons.search,
                      color: Color(0xFF8D8E90), size: 28),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      controller: _searchController,
                      onChanged: (v) => setState(() => _searchQuery = v),
                      style: GoogleFonts.dmSans(
                          fontSize: 16, color: t.textPrimary),
                      decoration: InputDecoration(
                        hintText: 'Buscar sillas, mesas, flores...',
                        hintStyle: GoogleFonts.dmSans(
                            fontSize: 16, color: const Color(0xFF8D8E90)),
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        contentPadding:
                            const EdgeInsets.symmetric(vertical: 16),
                        isCollapsed: true,
                      ),
                    ),
                  ),
                  if (_searchQuery.isNotEmpty)
                    GestureDetector(
                      onTap: () {
                        _searchController.clear();
                        setState(() => _searchQuery = '');
                      },
                      child: Padding(
                        padding: const EdgeInsets.only(right: 12),
                        child: Icon(Icons.close_rounded,
                            color: t.textDim, size: 22),
                      ),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 10),
          Container(
            width: 56, height: 56,
            decoration: BoxDecoration(
              color: t.isDark ? t.card : Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: t.borderFaint),
            ),
            child: const Icon(Icons.mic_outlined,
                color: Color(0xFF8D8E90), size: 28),
          ),
        ],
      ),
      ),
    );
  }

  // ── Filter Chips ────────────────────────────────────────────────────────

  Widget _buildFilterChips(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.only(top: 18),
      child: SizedBox(
        height: 40,
        child: ListView.separated(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          scrollDirection: Axis.horizontal,
          itemCount: _filters.length,
          separatorBuilder: (_, __) => const SizedBox(width: 8),
          itemBuilder: (context, i) {
            final f = _filters[i];
            final isActive = _activeFilter == f.label;
            return GestureDetector(
              onTap: () => setState(() => _activeFilter = f.label),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 14),
                decoration: BoxDecoration(
                  gradient: isActive
                      ? const LinearGradient(colors: [
                          AppColors.violet,
                          AppColors.hotPink,
                        ])
                      : null,
                  color: isActive ? null : (t.isDark ? t.card : Colors.white),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: isActive
                        ? Colors.transparent
                        : t.borderFaint,
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(f.icon,
                        size: 16,
                        color: isActive ? Colors.white : t.textMuted),
                    const SizedBox(width: 6),
                    Text(
                      f.label,
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: isActive ? Colors.white : t.textPrimary,
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  // ── Suggestion Badges ───────────────────────────────────────────────────

  Widget _buildSuggestions(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 0, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.trending_up_rounded,
                  color: AppColors.hotPink, size: 18),
              const SizedBox(width: 6),
              Text(
                'Búsquedas populares',
                style: GoogleFonts.dmSans(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: t.textMuted,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          SizedBox(
            height: 36,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: _suggestions.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              padding: const EdgeInsets.only(right: 20),
              itemBuilder: (context, i) {
                final s = _suggestions[i];
                return GestureDetector(
                  onTap: () {
                    _searchController.text = s;
                    setState(() => _searchQuery = s);
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 14, vertical: 8),
                    decoration: BoxDecoration(
                      color: AppColors.hotPink.withOpacity(0.08),
                      borderRadius: BorderRadius.circular(18),
                      border: Border.all(
                        color: AppColors.hotPink.withOpacity(0.15),
                      ),
                    ),
                    child: Text(
                      s,
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.hotPink,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  // ── Product Card ────────────────────────────────────────────────────────

  Widget _productCard(Product product, RfTheme t) {
    final variant = product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    final lowStock = product.stockQuantity > 0 && product.stockQuantity < 10;

    return GestureDetector(
      onTap: () => Navigator.push(context, MaterialPageRoute(
          builder: (_) => ProductDetailScreen(productId: product.id))),
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
        child: ClipRRect(
          borderRadius: BorderRadius.circular(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Image with badges
              Expanded(
                flex: 5,
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    if (imageUrl != null && imageUrl.isNotEmpty)
                      Image.network(
                        imageUrl,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          color: AppColors.hotPink.withOpacity(0.08),
                          child: const Icon(Icons.image_not_supported_rounded,
                              color: AppColors.hotPink, size: 32),
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
                        child: const Icon(Icons.image_not_supported_rounded,
                            color: AppColors.hotPink, size: 32),
                      ),
                    // Type badge (Alquiler/Venta)
                    Positioned(
                      top: 10, left: 10,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(colors: [
                            AppColors.violet,
                            AppColors.hotPink,
                          ]),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          product.type == 'Rental' ? 'ALQUILER' : 'VENTA',
                          style: GoogleFonts.dmSans(
                            fontSize: 9,
                            fontWeight: FontWeight.w800,
                            color: Colors.white,
                            letterSpacing: 0.8,
                          ),
                        ),
                      ),
                    ),
                    // Low stock badge
                    if (lowStock)
                      Positioned(
                        top: 10, right: 10,
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
                    // Favorite button
                    Positioned(
                      bottom: 8, right: 8,
                      child: Container(
                        width: 32, height: 32,
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.9),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(Icons.favorite_border_rounded,
                            color: AppColors.hotPink.withOpacity(0.8),
                            size: 18),
                      ),
                    ),
                  ],
                ),
              ),
              // Info
              Expanded(
                flex: 4,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            product.nameTemplate,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: GoogleFonts.dmSans(
                              fontSize: 18,
                              fontWeight: FontWeight.w700,
                              color: t.textPrimary,
                              height: 1.2,
                            ),
                          ),
                          const SizedBox(height: 5),
                          if (product.descriptionTemplate != null &&
                              product.descriptionTemplate!.isNotEmpty)
                            Text(
                              product.descriptionTemplate!,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: GoogleFonts.dmSans(
                                fontSize: 14,
                                color: t.textMuted,
                                height: 1.35,
                              ),
                            ),
                          const SizedBox(height: 8),
                          _buildStarRating(product, t),
                        ],
                      ),
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
                          Container(
                            width: 28, height: 28,
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(colors: [
                                AppColors.violet,
                                AppColors.hotPink,
                              ]),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.add_rounded,
                                color: Colors.white, size: 18),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStarRating(Product product, RfTheme t) {
    // Warm golden yellow — more vibrant than Material amber
    const starColor = Color(0xFFFFB800);
    final rating = product.averageRating;
    final hasReviews = product.reviewCount > 0;

    if (!hasReviews) {
      return Row(
        children: [
          ...List.generate(5, (i) => Icon(
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
          final half = i == rating.floor() && rating - rating.floor() >= 0.5;
          return Icon(
            half
                ? Icons.star_half_rounded
                : (filled ? Icons.star_rounded : Icons.star_rounded),
            color: filled || half
                ? starColor
                : starColor.withOpacity(0.25),
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

  // ── Empty State ─────────────────────────────────────────────────────────

  Widget _buildEmptyState(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 40, 20, 20),
      child: Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: t.borderFaint),
        ),
        child: Column(
          children: [
            Container(
              width: 72, height: 72,
              decoration: BoxDecoration(
                color: AppColors.hotPink.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.search_off_rounded,
                  color: AppColors.hotPink, size: 36),
            ),
            const SizedBox(height: 16),
            Text(
              _searchQuery.isNotEmpty
                  ? 'Sin resultados'
                  : 'No hay productos',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              _searchQuery.isNotEmpty
                  ? 'No encontramos nada para "$_searchQuery"'
                  : 'Pronto agregaremos más productos',
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 13,
                color: t.textMuted,
              ),
            ),
            if (_searchQuery.isNotEmpty) ...[
              const SizedBox(height: 16),
              GestureDetector(
                onTap: () {
                  _searchController.clear();
                  setState(() => _searchQuery = '');
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 18, vertical: 10),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [
                      AppColors.violet,
                      AppColors.hotPink,
                    ]),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    'Limpiar búsqueda',
                    style: GoogleFonts.dmSans(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _FilterChip {
  final String label;
  final IconData icon;
  const _FilterChip(this.label, this.icon);
}
