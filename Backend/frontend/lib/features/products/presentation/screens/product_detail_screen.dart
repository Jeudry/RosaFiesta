import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../products_provider.dart';
import '../../data/product_models.dart';
import '../../../active_event/presentation/active_event_provider.dart';
import '../reviews_provider.dart';
import '../../../events/presentation/events_provider.dart';

class ProductDetailScreen extends StatefulWidget {
  final String productId;
  const ProductDetailScreen({super.key, required this.productId});
  @override
  State<ProductDetailScreen> createState() => _ProductDetailScreenState();
}

class _ProductDetailScreenState extends State<ProductDetailScreen>
    with TickerProviderStateMixin {
  int _qty = 1;
  bool _descExpanded = false;
  int _selectedVariantIdx = 0;
  late final TabController _tabCtrl;
  late final PageController _imagePageCtrl;
  int _currentImagePage = 0;

  static const _tabLabels = ['Descripción', 'Specs', 'Reseñas', 'Similares'];
  static const _tabIcons = [
    Icons.description_outlined,
    Icons.tune_rounded,
    Icons.star_border_rounded,
    Icons.grid_view_rounded,
  ];

  @override
  void initState() {
    super.initState();
    _tabCtrl = TabController(length: _tabLabels.length, vsync: this);
    _imagePageCtrl = PageController();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProductsProvider>().fetchProductDetails(widget.productId);
      context.read<ReviewsProvider>().fetchReviews(widget.productId);
      _startImageAutoScroll();
    });
  }

  void _startImageAutoScroll() {
    Future.delayed(const Duration(seconds: 10), () {
      if (!mounted) return;
      _advanceImage();
    });
  }

  void _advanceImage() {
    if (!mounted) return;
    final product = context.read<ProductsProvider>().selectedProduct;
    if (product == null) return;
    final images = product.variants
        .where((v) => v.imageUrl != null && v.imageUrl!.isNotEmpty)
        .toList();
    if (images.length <= 1) return;
    final next = (_currentImagePage + 1) % images.length;
    _imagePageCtrl.animateToPage(next,
        duration: const Duration(milliseconds: 500),
        curve: Curves.easeInOut);
    Future.delayed(const Duration(seconds: 7), () => _advanceImage());
  }

  @override
  void dispose() {
    _tabCtrl.dispose();
    _imagePageCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      body: Consumer<ProductsProvider>(
        builder: (context, provider, _) {
          if (provider.isLoading) {
            return Center(child: CircularProgressIndicator(
                color: AppColors.hotPink, strokeWidth: 2.5));
          }
          if (provider.error != null) {
            return Center(child: Text(provider.error!,
                style: GoogleFonts.dmSans(color: t.textMuted)));
          }
          final product = provider.selectedProduct;
          if (product == null) {
            return Center(child: Text('Producto no encontrado',
                style: GoogleFonts.dmSans(color: t.textMuted)));
          }
          final variant = product.variants.isNotEmpty
              ? product.variants[_selectedVariantIdx.clamp(0, product.variants.length - 1)]
              : null;
          final price = variant?.rentalPrice ?? 0;
          final imageUrl = variant?.imageUrl;

          return Stack(
            children: [
              CustomScrollView(
                slivers: [
                  SliverToBoxAdapter(child: _buildImageHero(imageUrl, product, t)),
                  SliverToBoxAdapter(child: _buildHeader(product, price, t)),
                  if (product.variants.isNotEmpty)
                    SliverToBoxAdapter(
                        child: _buildVariantSelector(product, t)),
                  SliverToBoxAdapter(child: _buildTabBar(t)),
                  SliverToBoxAdapter(
                    child: _buildTabContent(product, variant, t),
                  ),
                  const SliverToBoxAdapter(child: SizedBox(height: 110)),
                ],
              ),
              _buildFloatingNav(t),
              _buildBottomBar(product, variant, price, t),
            ],
          );
        },
      ),
    );
  }

  // ── Image Hero ───────────────────────────────────────────────────────────

  List<String> _getMediaUrls(Product product, String? imageUrl) {
    final urls = product.variants
        .where((v) => v.imageUrl != null && v.imageUrl!.isNotEmpty)
        .map((v) => v.imageUrl!)
        .toList();
    if (urls.isEmpty && imageUrl != null && imageUrl.isNotEmpty) {
      urls.add(imageUrl);
    }
    return urls;
  }

  bool _isVideo(String url) =>
      url.endsWith('.mp4') || url.endsWith('.mov') || url.endsWith('.webm') ||
      url.contains('video');

  Widget _buildImageHero(String? imageUrl, Product product, RfTheme t) {
    final media = _getMediaUrls(product, imageUrl);
    // Max thumbnails to show before +N
    const maxThumbs = 4;

    return Stack(
      children: [
        // Main image/carousel
        SizedBox(
          height: 540,
          child: media.isNotEmpty
              ? PageView.builder(
                  controller: _imagePageCtrl,
                  itemCount: media.length,
                  onPageChanged: (i) =>
                      setState(() => _currentImagePage = i),
                  itemBuilder: (_, i) => _isVideo(media[i])
                      ? Container(
                          color: Colors.black,
                          child: Center(
                            child: Icon(Icons.play_circle_rounded,
                                color: Colors.white.withOpacity(0.8),
                                size: 64),
                          ),
                        )
                      : Image.network(media[i],
                          fit: BoxFit.cover, width: double.infinity,
                          errorBuilder: (_, __, ___) => _placeholder()),
                )
              : _placeholder(),
        ),
        // Curved bottom
        Positioned(
          bottom: 0, left: 0, right: 0,
          child: Container(
            height: 32,
            decoration: BoxDecoration(
              color: t.base,
              borderRadius: const BorderRadius.vertical(
                  top: Radius.circular(28)),
            ),
          ),
        ),
        // Thumbnail strip with dark background
        if (media.length > 1)
          Positioned(
            bottom: 44, left: 0, right: 0,
            child: Center(
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.45),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                ...List.generate(
                  media.length <= maxThumbs ? media.length : maxThumbs,
                  (i) {
                    // Last visible thumb shows +N if overflow
                    final isOverflow = i == maxThumbs - 1 &&
                        media.length > maxThumbs;
                    final active = i == _currentImagePage;
                    final url = media[i];
                    final isVid = _isVideo(url);

                    return GestureDetector(
                      onTap: () {
                        if (isOverflow) {
                          _showMediaGallery(context, media, t);
                        } else {
                          _imagePageCtrl.animateToPage(i,
                              duration: const Duration(milliseconds: 300),
                              curve: Curves.easeInOut);
                        }
                      },
                      child: Container(
                        width: 68, height: 68,
                        margin: const EdgeInsets.symmetric(horizontal: 5),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: active
                                ? AppColors.hotPink
                                : Colors.white.withOpacity(0.5),
                            width: active ? 2.5 : 1.5,
                          ),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.2),
                              blurRadius: 8,
                            ),
                          ],
                        ),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(10),
                          child: Stack(
                            fit: StackFit.expand,
                            children: [
                              isVid
                                  ? Container(color: Colors.black87)
                                  : Image.network(url, fit: BoxFit.cover,
                                      errorBuilder: (_, __, ___) =>
                                          Container(color: Colors.grey)),
                              if (isVid)
                                Center(
                                  child: Icon(Icons.play_arrow_rounded,
                                      color: Colors.white, size: 22),
                                ),
                              if (isOverflow)
                                Container(
                                  color: Colors.black.withOpacity(0.6),
                                  child: Center(
                                    child: Text(
                                      '+${media.length - maxThumbs + 1}',
                                      style: GoogleFonts.outfit(
                                        fontSize: 16,
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
                    );
                  },
                ),
              ],
            ),
          ),
            ),
          ),
        // Fullscreen button — same height & size as back button
        Positioned(
          top: 0, right: 0,
          child: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: GestureDetector(
                onTap: () {
                  if (media.isNotEmpty) {
                    _openFullscreen(context, media, _currentImagePage);
                  }
                },
                child: Container(
                  width: 44, height: 44,
                  decoration: BoxDecoration(
                    color: (t.isDark ? Colors.black : Colors.white).withOpacity(0.85),
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(color: Colors.black.withOpacity(0.1),
                          blurRadius: 12, offset: const Offset(0, 4)),
                    ],
                  ),
                  child: Icon(Icons.fullscreen_rounded,
                      color: t.textPrimary, size: 20),
                ),
              ),
            ),
          ),
        ),
        // VENTA badge
        if (product.type == 'Sale')
          Positioned(
            top: 60, left: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                    colors: [AppColors.amber, Color(0xFFFF8C00)]),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text('VENTA',
                  style: GoogleFonts.dmSans(
                    fontSize: 11, fontWeight: FontWeight.w800,
                    color: Colors.white, letterSpacing: 0.8,
                  )),
            ),
          ),
      ],
    );
  }

  // ── Media Gallery Panel ──────────────────────────────────────────────────

  void _showMediaGallery(BuildContext ctx, List<String> media, RfTheme t) {
    showModalBottomSheet(
      context: ctx,
      backgroundColor: t.base,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(24))),
      builder: (_) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        maxChildSize: 0.9,
        minChildSize: 0.4,
        expand: false,
        builder: (_, scrollCtrl) => Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Todas las fotos',
                      style: GoogleFonts.outfit(
                          fontSize: 18, fontWeight: FontWeight.w700,
                          color: t.textPrimary)),
                  GestureDetector(
                    onTap: () => Navigator.pop(ctx),
                    child: Icon(Icons.close_rounded,
                        color: t.textMuted, size: 24),
                  ),
                ],
              ),
            ),
            Expanded(
              child: GridView.builder(
                controller: scrollCtrl,
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 32),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  crossAxisSpacing: 8,
                  mainAxisSpacing: 8,
                ),
                itemCount: media.length,
                itemBuilder: (_, i) {
                  final isVid = _isVideo(media[i]);
                  return GestureDetector(
                    onTap: () {
                      Navigator.pop(ctx);
                      _imagePageCtrl.jumpToPage(i);
                    },
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Stack(
                        fit: StackFit.expand,
                        children: [
                          isVid
                              ? Container(color: Colors.black87)
                              : Image.network(media[i], fit: BoxFit.cover,
                                  errorBuilder: (_, __, ___) =>
                                      Container(color: Colors.grey)),
                          if (isVid)
                            Center(
                              child: Icon(Icons.play_circle_rounded,
                                  color: Colors.white.withOpacity(0.8),
                                  size: 36),
                            ),
                          if (i == _currentImagePage)
                            Container(
                              decoration: BoxDecoration(
                                border: Border.all(
                                    color: AppColors.hotPink, width: 3),
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Fullscreen Viewer ────────────────────────────────────────────────────

  void _openFullscreen(BuildContext ctx, List<String> media, int initial) {
    Navigator.of(ctx).push(
      PageRouteBuilder(
        opaque: false,
        pageBuilder: (_, __, ___) => _FullscreenViewer(
          media: media,
          initialIndex: initial,
        ),
        transitionsBuilder: (_, anim, __, child) =>
            FadeTransition(opacity: anim, child: child),
      ),
    );
  }

  Widget _placeholder() => Container(
        color: AppColors.hotPink.withOpacity(0.06),
        child: const Center(
          child: Icon(Icons.image_not_supported_rounded,
              color: AppColors.hotPink, size: 48),
        ),
      );

  // ── Floating Nav ─────────────────────────────────────────────────────────

  Widget _buildFloatingNav(RfTheme t) {
    return Positioned(
      top: 0, left: 0, right: 0,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
          child: Align(
            alignment: Alignment.centerLeft,
            child: _pill(Icons.arrow_back_ios_new_rounded, t,
                onTap: () => Navigator.pop(context)),
          ),
        ),
      ),
    );
  }

  Widget _pill(IconData icon, RfTheme t, {VoidCallback? onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 44, height: 44,
        decoration: BoxDecoration(
          color: (t.isDark ? Colors.black : Colors.white).withOpacity(0.85),
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(color: Colors.black.withOpacity(0.1),
                blurRadius: 12, offset: const Offset(0, 4)),
          ],
        ),
        child: Icon(icon, color: t.textPrimary, size: 20),
      ),
    );
  }

  // ── Header (name, price, rating) ─────────────────────────────────────────

  Widget _buildHeader(Product product, double price, RfTheme t) {
    final isLowStock = product.stockQuantity > 0 &&
        product.stockQuantity <= (product.stockQuantity * 0.1).ceil().clamp(1, 5);

    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(product.nameTemplate,
              style: GoogleFonts.outfit(
                fontSize: 26, fontWeight: FontWeight.w800,
                color: t.textPrimary, height: 1.2,
              )),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  ShaderMask(
                    shaderCallback: (b) => const LinearGradient(
                        colors: [AppColors.violet, AppColors.hotPink])
                        .createShader(b),
                    child: Text('RD\$ ${price.toStringAsFixed(0)}',
                        style: GoogleFonts.outfit(
                          fontSize: 32, fontWeight: FontWeight.w800,
                          color: Colors.white, height: 1,
                        )),
                  ),
                    ],
              ),
              _buildRatingChip(product, t),
            ],
          ),
          if (isLowStock) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: AppColors.coral.withOpacity(0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.coral.withOpacity(0.2)),
              ),
              child: Row(mainAxisSize: MainAxisSize.min, children: [
                Icon(Icons.local_fire_department_rounded,
                    color: AppColors.coral, size: 16),
                const SizedBox(width: 6),
                Text('¡Quedan pocas unidades!',
                    style: GoogleFonts.dmSans(
                        fontSize: 12, fontWeight: FontWeight.w700,
                        color: AppColors.coral)),
              ]),
            ),
          ],
          const SizedBox(height: 16),
          Divider(color: t.borderFaint, height: 1),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildRatingChip(Product product, RfTheme t) {
    const starColor = Color(0xFFFFB800);
    final rating = product.averageRating;
    return Row(mainAxisSize: MainAxisSize.min, children: [
      ...List.generate(5, (i) {
        if (i < rating.floor()) {
          return const Icon(Icons.star_rounded, color: starColor, size: 22);
        } else if (i < rating.ceil() && rating % 1 > 0) {
          return const Icon(Icons.star_half_rounded, color: starColor, size: 22);
        }
        return Icon(Icons.star_rounded,
            color: starColor.withOpacity(0.25), size: 22);
      }),
      const SizedBox(width: 8),
      Text(
        '${product.reviewCount} ${product.reviewCount == 1 ? 'reseña' : 'reseñas'}',
        style: GoogleFonts.dmSans(
          fontSize: 16, fontWeight: FontWeight.w700,
          color: AppColors.hotPink,
          decoration: TextDecoration.underline,
          decorationColor: AppColors.hotPink,
        ),
      ),
    ]);
  }

  // ── Variant Selector ──────────────────────────────────────────────────────

  // Map variant name to a display color (best-effort from name)
  Color _variantColor(String name) {
    final n = name.toLowerCase();
    if (n.contains('rosa') || n.contains('pink')) return AppColors.hotPink;
    if (n.contains('rojo') || n.contains('red')) return AppColors.coral;
    if (n.contains('azul') || n.contains('blue')) return AppColors.sky;
    if (n.contains('verde') || n.contains('green')) return AppColors.teal;
    if (n.contains('morado') || n.contains('violet') || n.contains('purple')) return AppColors.violet;
    if (n.contains('amarillo') || n.contains('gold') || n.contains('dorado')) return AppColors.amber;
    if (n.contains('blanco') || n.contains('white')) return Colors.white;
    if (n.contains('negro') || n.contains('black')) return const Color(0xFF1A1A2E);
    return AppColors.violet;
  }

  Widget _buildVariantSelector(Product product, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
      child: Row(
        children: [
          Text('Opciones',
              style: GoogleFonts.dmSans(
                  fontSize: 13, fontWeight: FontWeight.w600,
                  color: t.textDim)),
          const SizedBox(width: 14),
          ...List.generate(product.variants.length, (i) {
            final v = product.variants[i];
            final selected = i == _selectedVariantIdx;
            final color = _variantColor(v.name);
            return Padding(
              padding: const EdgeInsets.only(right: 10),
              child: GestureDetector(
                onTap: () => setState(() => _selectedVariantIdx = i),
                child: Container(
                  width: 32, height: 32,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: selected ? color : Colors.transparent,
                    border: Border.all(color: color, width: 2.5),
                  ),
                  child: selected
                      ? Center(child: Icon(Icons.check_rounded,
                          color: color == Colors.white
                              ? Colors.black
                              : Colors.white,
                          size: 16))
                      : null,
                ),
              ),
            );
          }),
        ],
      ),
    );
  }

  // ── Tab Bar ──────────────────────────────────────────────────────────────

  Widget _buildTabBar(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Container(
        height: 54,
        decoration: BoxDecoration(
          color: t.isDark
              ? Colors.white.withOpacity(0.04)
              : const Color(0xFFF3F4F6),
          borderRadius: BorderRadius.circular(16),
        ),
        child: TabBar(
          controller: _tabCtrl,
          onTap: (_) => setState(() {}),
          isScrollable: true,
          tabAlignment: TabAlignment.start,
          padding: const EdgeInsets.all(5),
          labelPadding: const EdgeInsets.symmetric(horizontal: 14),
          indicator: BoxDecoration(
            gradient: const LinearGradient(
                colors: [AppColors.violet, AppColors.hotPink]),
            borderRadius: BorderRadius.circular(12),
          ),
          indicatorSize: TabBarIndicatorSize.tab,
          dividerColor: Colors.transparent,
          labelColor: Colors.white,
          unselectedLabelColor: t.textMuted,
          labelStyle: GoogleFonts.dmSans(
              fontSize: 14, fontWeight: FontWeight.w700),
          unselectedLabelStyle: GoogleFonts.dmSans(
              fontSize: 14, fontWeight: FontWeight.w600),
          tabs: List.generate(_tabLabels.length, (i) => Tab(
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(_tabIcons[i], size: 16),
                const SizedBox(width: 6),
                Text(_tabLabels[i]),
              ],
            ),
          )),
        ),
      ),
    );
  }

  // ── Tab Content ──────────────────────────────────────────────────────────

  Widget _buildTabContent(Product product, ProductVariant? variant, RfTheme t) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 200),
      child: Padding(
        key: ValueKey(_tabCtrl.index),
        padding: const EdgeInsets.fromLTRB(24, 20, 24, 0),
        child: switch (_tabCtrl.index) {
          0 => _tabDescription(product, t),
          1 => _tabSpecs(product, variant, t),
          2 => _tabReviews(product, t),
          3 => _tabSimilar(t),
          _ => const SizedBox.shrink(),
        },
      ),
    );
  }

  // ── Tab: Descripción ─────────────────────────────────────────────────────

  Widget _tabDescription(Product product, RfTheme t) {
    final desc = product.descriptionTemplate ?? '';
    if (desc.isEmpty) {
      return Text('Sin descripción disponible.',
          style: GoogleFonts.dmSans(fontSize: 14, color: t.textDim));
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Stack(
          children: [
            Text(desc,
                maxLines: _descExpanded ? 200 : 3,
                overflow: TextOverflow.ellipsis,
                style: GoogleFonts.dmSans(
                    fontSize: 14, color: t.textMuted, height: 1.6)),
            if (!_descExpanded && desc.length > 100)
              Positioned(
                bottom: 0, left: 0, right: 0,
                child: Container(
                  height: 28,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [t.base.withOpacity(0.0), t.base],
                    ),
                  ),
                ),
              ),
          ],
        ),
        if (desc.length > 100) ...[
          const SizedBox(height: 6),
          GestureDetector(
            onTap: () => setState(() => _descExpanded = !_descExpanded),
            child: Text(
              _descExpanded ? 'Ver menos' : 'Leer más',
              style: GoogleFonts.dmSans(
                fontSize: 13, fontWeight: FontWeight.w700,
                color: AppColors.hotPink,
              ),
            ),
          ),
        ],
      ],
    );
  }

  // ── Tab: Especificaciones ────────────────────────────────────────────────

  Widget _tabSpecs(Product product, ProductVariant? variant, RfTheme t) {
    final specs = <String, String>{
      'Tipo': product.type == 'Rental' ? 'Alquiler' : 'Venta',
      if (variant != null) 'SKU': variant.sku,
      if (variant != null) 'Variante': variant.name,
      if (variant?.description != null && variant!.description!.isNotEmpty)
        'Detalle': variant.description!,
      if (variant != null && variant.dimensions.isNotEmpty) ...{
        if (variant.dimensions.first.height != null)
          'Alto': '${variant.dimensions.first.height} cm',
        if (variant.dimensions.first.width != null)
          'Ancho': '${variant.dimensions.first.width} cm',
        if (variant.dimensions.first.depth != null)
          'Profundidad': '${variant.dimensions.first.depth} cm',
        if (variant.dimensions.first.weight != null)
          'Peso': '${variant.dimensions.first.weight} kg',
      },
      if (variant?.attributes.isNotEmpty == true)
        ...variant!.attributes.map((k, v) =>
            MapEntry(k[0].toUpperCase() + k.substring(1), v.toString())),
    };

    if (specs.isEmpty) {
      return Text('Sin especificaciones disponibles.',
          style: GoogleFonts.dmSans(fontSize: 14, color: t.textDim));
    }

    return Column(
      children: specs.entries.map((e) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                width: 120,
                child: Text(e.key,
                    style: GoogleFonts.dmSans(
                        fontSize: 13, fontWeight: FontWeight.w600,
                        color: t.textDim)),
              ),
              Expanded(
                child: Text(e.value,
                    style: GoogleFonts.dmSans(
                        fontSize: 14, fontWeight: FontWeight.w600,
                        color: t.textPrimary)),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  // ── Tab: Reseñas ─────────────────────────────────────────────────────────

  Widget _tabReviews(Product product, RfTheme t) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('${product.reviewCount} ${product.reviewCount == 1 ? 'reseña' : 'reseñas'}',
                style: GoogleFonts.dmSans(
                    fontSize: 14, fontWeight: FontWeight.w600,
                    color: t.textMuted)),
            GestureDetector(
              onTap: () => _showAddReviewDialog(context, product.id),
              child: Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: t.borderFaint),
                ),
                child: Row(mainAxisSize: MainAxisSize.min, children: [
                  Icon(Icons.edit_rounded,
                      color: AppColors.hotPink, size: 14),
                  const SizedBox(width: 6),
                  Text('Escribir',
                      style: GoogleFonts.dmSans(
                          fontSize: 12, fontWeight: FontWeight.w700,
                          color: AppColors.hotPink)),
                ]),
              ),
            ),
          ],
        ),
        const SizedBox(height: 14),
        Consumer<ReviewsProvider>(
          builder: (context, rp, _) {
            if (rp.isLoading && rp.reviews.isEmpty) {
              return Padding(
                padding: const EdgeInsets.all(20),
                child: Center(child: CircularProgressIndicator(
                    color: AppColors.hotPink, strokeWidth: 2)),
              );
            }
            if (rp.reviews.isEmpty) {
              return Container(
                width: double.infinity,
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: t.isDark
                      ? Colors.white.withOpacity(0.03)
                      : const Color(0xFFF9FAFB),
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Column(children: [
                  Icon(Icons.rate_review_outlined,
                      color: t.textDim, size: 32),
                  const SizedBox(height: 8),
                  Text('Sin reseñas aún',
                      style: GoogleFonts.dmSans(
                          fontSize: 14, fontWeight: FontWeight.w600,
                          color: t.textMuted)),
                  const SizedBox(height: 2),
                  Text('Sé el primero en opinar',
                      style: GoogleFonts.dmSans(
                          fontSize: 12, color: t.textDim)),
                ]),
              );
            }
            return Column(
              children: rp.reviews.map((r) => _reviewCard(r, t)).toList(),
            );
          },
        ),
      ],
    );
  }

  Widget _reviewCard(Review review, RfTheme t) {
    const starColor = Color(0xFFFFB800);
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.04)
            : const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          Row(children: [
            Container(
              width: 32, height: 32,
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                    colors: [AppColors.violet, AppColors.hotPink]),
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  (review.user?.userName ?? 'U')[0].toUpperCase(),
                  style: GoogleFonts.outfit(
                      fontSize: 14, fontWeight: FontWeight.w800,
                      color: Colors.white),
                ),
              ),
            ),
            const SizedBox(width: 10),
            Text(review.user?.userName ?? 'Usuario',
                style: GoogleFonts.dmSans(
                    fontSize: 14, fontWeight: FontWeight.w700,
                    color: t.textPrimary)),
          ]),
          Text('${review.created.day}/${review.created.month}/${review.created.year}',
              style: GoogleFonts.dmSans(fontSize: 11, color: t.textDim)),
        ]),
        const SizedBox(height: 8),
        Row(children: List.generate(5, (i) => Icon(Icons.star_rounded,
            color: i < review.rating
                ? starColor
                : starColor.withOpacity(0.2),
            size: 14))),
        const SizedBox(height: 8),
        Text(review.comment,
            style: GoogleFonts.dmSans(
                fontSize: 13, color: t.textMuted, height: 1.4)),
      ]),
    );
  }

  // ── Tab: Similares ───────────────────────────────────────────────────────

  Widget _tabSimilar(RfTheme t) {
    // Uses the products list from the provider (excluding current)
    return Consumer<ProductsProvider>(
      builder: (context, provider, _) {
        final products = provider.products
            .where((p) => p.id != widget.productId)
            .take(6)
            .toList();

        if (products.isEmpty) {
          return Text('No hay productos similares.',
              style: GoogleFonts.dmSans(fontSize: 14, color: t.textDim));
        }

        return GridView.count(
          crossAxisCount: 2,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisSpacing: 12,
          mainAxisSpacing: 12,
          childAspectRatio: 0.75,
          children: products.map((p) {
            final v = p.variants.isNotEmpty ? p.variants.first : null;
            final img = v?.imageUrl;
            final pr = v?.rentalPrice ?? 0;
            return GestureDetector(
              onTap: () => Navigator.pushReplacement(
                context,
                MaterialPageRoute(
                    builder: (_) => ProductDetailScreen(productId: p.id)),
              ),
              child: Container(
                decoration: BoxDecoration(
                  color: t.isDark ? t.card : Colors.white,
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: t.borderFaint),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: ClipRRect(
                        borderRadius: const BorderRadius.vertical(
                            top: Radius.circular(18)),
                        child: img != null && img.isNotEmpty
                            ? Image.network(img, fit: BoxFit.cover,
                                width: double.infinity,
                                errorBuilder: (_, __, ___) =>
                                    _placeholder())
                            : _placeholder(),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(p.nameTemplate,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: GoogleFonts.dmSans(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w700,
                                  color: t.textPrimary)),
                          const SizedBox(height: 4),
                          ShaderMask(
                            shaderCallback: (b) => const LinearGradient(
                                colors: [AppColors.violet, AppColors.hotPink])
                                .createShader(b),
                            child: Text('RD\$ ${pr.toStringAsFixed(0)}',
                                style: GoogleFonts.outfit(
                                    fontSize: 16,
                                    fontWeight: FontWeight.w800,
                                    color: Colors.white)),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            );
          }).toList(),
        );
      },
    );
  }

  // ── Bottom Bar ───────────────────────────────────────────────────────────

  Widget _buildBottomBar(
      Product product, ProductVariant? variant, double price, RfTheme t) {
    return Positioned(
      left: 0, right: 0, bottom: 0,
      child: Container(
        padding: EdgeInsets.fromLTRB(
            24, 16, 24, MediaQuery.of(context).padding.bottom + 16),
        decoration: BoxDecoration(
          color: t.base,
          border: Border(top: BorderSide(color: t.borderFaint)),
          boxShadow: [
            BoxShadow(color: Colors.black.withOpacity(0.05),
                blurRadius: 20, offset: const Offset(0, -8)),
          ],
        ),
        child: Row(children: [
          // Favorite button
          GestureDetector(
            onTap: () {}, // TODO: toggle favorite
            child: Container(
              width: 50, height: 50,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.favorite_border_rounded,
                  color: AppColors.hotPink, size: 22),
            ),
          ),
          const SizedBox(width: 8),
          // Share button
          GestureDetector(
            onTap: () => _showShareModal(context, product, t),
            child: Container(
              width: 50, height: 50,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.share_rounded,
                  color: t.textMuted, size: 20),
            ),
          ),
          const SizedBox(width: 10),
          // Qty selector
          Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: t.borderFaint),
            ),
            child: Row(children: [
              _qtyBtn(Icons.remove_rounded, t,
                  onTap: _qty > 1 ? () => setState(() => _qty--) : null),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Text('$_qty',
                    style: GoogleFonts.outfit(
                        fontSize: 18, fontWeight: FontWeight.w800,
                        color: t.textPrimary)),
              ),
              _qtyBtn(Icons.add_rounded, t,
                  onTap: () => setState(() => _qty++)),
            ]),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: GestureDetector(
              onTap: () async {
                if (variant == null) return;
                try {
                  await context.read<ActiveEventProvider>().addItem(
                        product,
                        variant: variant,
                        quantity: _qty,
                      );
                  if (!mounted) return;
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                    content: Row(children: [
                      const Icon(Icons.celebration_rounded,
                          color: Colors.white, size: 18),
                      const SizedBox(width: 8),
                      Text('Agregado a tu evento',
                          style: GoogleFonts.dmSans(
                              fontWeight: FontWeight.w600)),
                    ]),
                    backgroundColor: AppColors.teal,
                    behavior: SnackBarBehavior.floating,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ));
                } catch (e) {
                  if (!mounted) return;
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                    content: Text('Error: $e'),
                    backgroundColor: AppColors.coral,
                  ));
                }
              },
              child: Container(
                height: 54,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                      colors: [AppColors.violet, AppColors.hotPink]),
                  borderRadius: BorderRadius.circular(18),
                  boxShadow: [
                    BoxShadow(color: AppColors.hotPink.withOpacity(0.3),
                        blurRadius: 16, offset: const Offset(0, 6)),
                  ],
                ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.celebration_rounded,
                          color: Colors.white, size: 20),
                      const SizedBox(width: 10),
                      Flexible(
                        child: FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Text('Agregar a mi evento',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: GoogleFonts.dmSans(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w800,
                                  color: Colors.white)),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ]),
      ),
    );
  }

  Widget _qtyBtn(IconData icon, RfTheme t, {VoidCallback? onTap}) {
    final on = onTap != null;
    return GestureDetector(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Icon(icon,
            color: on ? t.textPrimary : t.textDim.withOpacity(0.3),
            size: 20),
      ),
    );
  }

  // ── Share Modal ───────────────────────────────────────────────────────────

  void _showShareModal(BuildContext context, Product product, RfTheme t) {
    showModalBottomSheet(
      context: context,
      backgroundColor: t.base,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(24))),
      builder: (_) => Padding(
        padding: const EdgeInsets.fromLTRB(24, 16, 24, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40, height: 4,
              decoration: BoxDecoration(
                color: t.textDim.withOpacity(0.3),
                borderRadius: BorderRadius.circular(2)),
            ),
            const SizedBox(height: 20),
            Text('Compartir',
                style: GoogleFonts.outfit(
                    fontSize: 18, fontWeight: FontWeight.w700,
                    color: t.textPrimary)),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _shareOption(Icons.link_rounded, 'Copiar link', t,
                    onTap: () {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                    content: Text('Link copiado',
                        style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                    backgroundColor: AppColors.teal,
                    behavior: SnackBarBehavior.floating,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ));
                }),
                _shareOption(Icons.chat_rounded, 'WhatsApp', t,
                    color: const Color(0xFF25D366)),
                _shareOption(Icons.camera_alt_rounded, 'Instagram', t,
                    color: const Color(0xFFE1306C)),
                _shareOption(Icons.facebook_rounded, 'Facebook', t,
                    color: const Color(0xFF1877F2)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _shareOption(IconData icon, String label, RfTheme t,
      {Color? color, VoidCallback? onTap}) {
    return GestureDetector(
      onTap: onTap ?? () => Navigator.pop(context),
      child: Column(
        children: [
          Container(
            width: 56, height: 56,
            decoration: BoxDecoration(
              color: (color ?? t.textMuted).withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color ?? t.textMuted, size: 24),
          ),
          const SizedBox(height: 8),
          Text(label,
              style: GoogleFonts.dmSans(
                  fontSize: 11, fontWeight: FontWeight.w600,
                  color: t.textMuted)),
        ],
      ),
    );
  }

  // ── Dialogs ──────────────────────────────────────────────────────────────

  void _showAddReviewDialog(BuildContext context, String articleId) {
    int selectedRating = 5;
    final commentController = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20)),
          title: Text('Escribir Reseña',
              style: GoogleFonts.outfit(fontWeight: FontWeight.w700)),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(5, (i) => IconButton(
                icon: Icon(Icons.star_rounded,
                    color: i < selectedRating
                        ? const Color(0xFFFFB800)
                        : const Color(0xFFFFB800).withOpacity(0.2),
                    size: 32),
                onPressed: () => setState(() => selectedRating = i + 1),
              )),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: commentController,
              decoration: InputDecoration(
                hintText: 'Tu comentario...',
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14)),
              ),
              maxLines: 3,
            ),
          ]),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Cancelar',
                  style: GoogleFonts.dmSans(color: Colors.grey)),
            ),
            ElevatedButton(
              onPressed: () async {
                if (commentController.text.isEmpty) return;
                await context.read<ReviewsProvider>().addReview(
                    articleId, selectedRating, commentController.text);
                if (context.mounted) {
                  Navigator.pop(context);
                  context.read<ProductsProvider>()
                      .fetchProductDetails(articleId);
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.hotPink,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12)),
              ),
              child: Text('Enviar',
                  style: GoogleFonts.dmSans(
                      fontWeight: FontWeight.w700, color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Fullscreen Image/Video Viewer ──────────────────────────────────────────

class _FullscreenViewer extends StatefulWidget {
  final List<String> media;
  final int initialIndex;
  const _FullscreenViewer({required this.media, required this.initialIndex});
  @override
  State<_FullscreenViewer> createState() => _FullscreenViewerState();
}

class _FullscreenViewerState extends State<_FullscreenViewer> {
  late final PageController _ctrl;
  late int _current;

  bool _isVideo(String url) =>
      url.endsWith('.mp4') || url.endsWith('.mov') || url.endsWith('.webm') ||
      url.contains('video');

  @override
  void initState() {
    super.initState();
    _current = widget.initialIndex;
    _ctrl = PageController(initialPage: _current);
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Swipeable media
          PageView.builder(
            controller: _ctrl,
            itemCount: widget.media.length,
            onPageChanged: (i) => setState(() => _current = i),
            itemBuilder: (_, i) {
              final url = widget.media[i];
              if (_isVideo(url)) {
                return Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.play_circle_rounded,
                          color: Colors.white.withOpacity(0.7), size: 80),
                      const SizedBox(height: 12),
                      Text('Reproducir video',
                          style: GoogleFonts.dmSans(
                              color: Colors.white70, fontSize: 14)),
                    ],
                  ),
                );
              }
              return InteractiveViewer(
                minScale: 0.5,
                maxScale: 4.0,
                child: Center(
                  child: Image.network(url, fit: BoxFit.contain,
                      errorBuilder: (_, __, ___) => const Icon(
                          Icons.broken_image_rounded,
                          color: Colors.white38, size: 48)),
                ),
              );
            },
          ),
          // Top bar
          Positioned(
            top: 0, left: 0, right: 0,
            child: SafeArea(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    GestureDetector(
                      onTap: () => Navigator.pop(context),
                      child: Container(
                        width: 40, height: 40,
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.1),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.close_rounded,
                            color: Colors.white, size: 22),
                      ),
                    ),
                    Text(
                      '${_current + 1} / ${widget.media.length}',
                      style: GoogleFonts.dmSans(
                        color: Colors.white70,
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
