import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../shop/presentation/cart_provider.dart';
import '../../../categories/presentation/categories_provider.dart';
import '../../../categories/data/category_models.dart';
import '../../../shop/presentation/screens/cart_screen.dart';
import '../../../products/presentation/screens/products_list_screen.dart';
import '../../../profile/presentation/screens/profile_screen.dart';
import '../../../events/presentation/screens/events_list_screen.dart';
import '../../../events/presentation/screens/event_calendar_screen.dart';
import '../../../suppliers/presentation/screens/supplier_list_screen.dart';

// ── Rose Gold Luxe palette (local, does not modify app_theme.dart) ──────────
class _RoseColors {
  static const primary = Color(0xFFDB2777);
  static const secondary = Color(0xFFF472B6);
  static const accent = Color(0xFFA16207);
  static const background = Color(0xFFFDF2F8);
  static const text = Color(0xFF831843);
  static const muted = Color(0xFF64748B);
  static const border = Color(0xFFFBCFE8);
  static const goldGradient = LinearGradient(
    colors: [Color(0xFFA16207), Color(0xFFD4A017), Color(0xFFA16207)],
  );

}

/// Rose Gold Luxe home screen -- glassmorphism + wedding/event palette.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen>
    with TickerProviderStateMixin {
  late final AnimationController _staggerController;
  late final List<Animation<double>> _sectionAnimations;
  late final AnimationController _fabBounceController;
  late final Animation<double> _fabBounce;
  late final AnimationController _fabLabelController;
  late final Animation<double> _fabLabelOpacity;

  static const _sectionCount = 7;

  @override
  void initState() {
    super.initState();
    _staggerController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 200 + _sectionCount * 100),
    );

    _sectionAnimations = List.generate(_sectionCount, (i) {
      final start = i * 0.1;
      final end = (start + 0.4).clamp(0.0, 1.0);
      return CurvedAnimation(
        parent: _staggerController,
        curve: Interval(start, end, curve: Curves.easeOut),
      );
    });

    _staggerController.forward();

    // Bouncy FAB animation
    _fabBounceController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _fabBounce = Tween<double>(begin: 0, end: -5).animate(
      CurvedAnimation(parent: _fabBounceController, curve: Curves.easeInOut),
    );

    // FAB label fade
    _fabLabelController = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 2400),
    )..repeat(reverse: true);
    _fabLabelOpacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _fabLabelController, curve: Curves.easeInOut),
    );

    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CategoriesProvider>().fetchCategories();
    });
  }

  @override
  void dispose() {
    _staggerController.dispose();
    _fabBounceController.dispose();
    _fabLabelController.dispose();
    super.dispose();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  Widget _staggered(int index, Widget child) {
    return FadeTransition(
      opacity: _sectionAnimations[index],
      child: SlideTransition(
        position: Tween<Offset>(
          begin: const Offset(0, 0.05),
          end: Offset.zero,
        ).animate(_sectionAnimations[index]),
        child: child,
      ),
    );
  }

  Widget _goldShimmerText(String text, {double fontSize = 20}) {
    return ShaderMask(
      shaderCallback: (bounds) => _RoseColors.goldGradient.createShader(bounds),
      child: Text(
        text,
        style: GoogleFonts.playfairDisplay(
          fontSize: fontSize,
          fontWeight: FontWeight.w700,
          color: Colors.white,
        ),
      ),
    );
  }

  BoxDecoration _glassDecoration({
    double opacity = 0.65,
    double radius = 20,
    Color? shadowColor,
  }) {
    return BoxDecoration(
      color: Colors.white.withOpacity(opacity),
      borderRadius: BorderRadius.circular(radius),
      border: Border.all(color: Colors.white.withOpacity(0.2)),
      boxShadow: [
        BoxShadow(
          color: (shadowColor ?? _RoseColors.primary).withOpacity(0.1),
          blurRadius: 20,
        ),
      ],
    );
  }

  String _fallbackImage(String categoryName) {
    final lower = categoryName.toLowerCase();
    if (lower.contains('silla') || lower.contains('chair')) {
      return 'assets/images/product_tiffany_chair.jpeg';
    }
    if (lower.contains('mesa') || lower.contains('table')) {
      return 'assets/images/product_round_table.jpg';
    }
    if (lower.contains('flor') || lower.contains('flower') || lower.contains('centro')) {
      return 'assets/images/decor_floral_centerpiece.jpg';
    }
    if (lower.contains('baby')) {
      return 'assets/images/event_baby_shower.jpg';
    }
    if (lower.contains('xv') || lower.contains('quince')) {
      return 'assets/images/event_quinceanera.jpg';
    }
    if (lower.contains('navid') || lower.contains('christmas')) {
      return 'assets/images/event_christmas_setup.jpg';
    }
    if (lower.contains('gradu')) {
      return 'assets/images/event_graduation.jpg';
    }
    if (lower.contains('safari')) {
      return 'assets/images/event_safari_party.jpg';
    }
    return 'assets/images/event_pink_arch.jpg';
  }

  // ── Build ────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _RoseColors.background,
      body: CustomScrollView(
        slivers: [
          // 1  Header
          SliverToBoxAdapter(child: _staggered(0, _buildHeader())),
          // 2  Search
          SliverToBoxAdapter(child: _staggered(1, _buildSearchBar())),
          // 3  Hero
          SliverToBoxAdapter(child: _staggered(2, _buildHeroCard())),
          // 4  Quick Stats
          SliverToBoxAdapter(child: _staggered(3, _buildQuickStats())),
          // 5  Services
          SliverToBoxAdapter(child: _staggered(4, _buildServicesSection())),
          // 6  Categories
          SliverToBoxAdapter(
            child: _staggered(5, _buildCategoriesSectionHeader()),
          ),
          _buildCategoriesGrid(),
          // 7  Trending
          SliverToBoxAdapter(child: _staggered(6, _buildTrendingSection())),
          // Bottom spacing
          const SliverToBoxAdapter(child: SizedBox(height: 100)),
        ],
      ),
      bottomNavigationBar: _buildBottomNav(),
    );
  }

  // ── 1. Header ────────────────────────────────────────────────────────────

  Widget _buildHeader() {
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
        child: Row(
          children: [
            // Logo
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: _RoseColors.accent, width: 1.5),
                image: const DecorationImage(
                  image: AssetImage('assets/images/logo_rosafiesta.png'),
                  fit: BoxFit.cover,
                ),
              ),
            ),
            const SizedBox(width: 10),
            Text(
              'Rosa Fiesta',
              style: GoogleFonts.playfairDisplay(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                fontStyle: FontStyle.italic,
                color: _RoseColors.text,
              ),
            ),
            const Spacer(),
            // Cart badge
            Consumer<CartProvider>(
              builder: (context, cart, _) {
                return Stack(
                  clipBehavior: Clip.none,
                  children: [
                    _headerIcon(Icons.shopping_bag_outlined, () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const CartScreen()),
                      );
                    }),
                    if (cart.itemCount > 0)
                      Positioned(
                        right: -2,
                        top: -2,
                        child: Container(
                          padding: const EdgeInsets.all(4),
                          decoration: const BoxDecoration(
                            color: _RoseColors.primary,
                            shape: BoxShape.circle,
                          ),
                          child: Text(
                            '${cart.itemCount}',
                            style: GoogleFonts.inter(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                  ],
                );
              },
            ),
            const SizedBox(width: 8),
            _headerIcon(Icons.person_outline, () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const ProfileScreen()),
              );
            }),
          ],
        ),
      ),
    );
  }

  Widget _headerIcon(IconData icon, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: _RoseColors.secondary.withOpacity(0.12),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: _RoseColors.primary, size: 22),
      ),
    );
  }

  // ── 2. Search bar ────────────────────────────────────────────────────────

  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 15, sigmaY: 15),
          child: Container(
            height: 52,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.6),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: _RoseColors.border),
            ),
            child: TextField(
              style: GoogleFonts.inter(fontSize: 14, color: _RoseColors.text),
              decoration: InputDecoration(
                hintText: 'Buscar decoraciones, temas...',
                hintStyle: GoogleFonts.inter(
                  fontSize: 14,
                  color: _RoseColors.muted,
                ),
                prefixIcon: const Icon(
                  Icons.local_florist_outlined,
                  color: _RoseColors.primary,
                  size: 20,
                ),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 14,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  // ── 3. Hero card ─────────────────────────────────────────────────────────

  Widget _buildHeroCard() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: SizedBox(
          height: 220,
          child: Stack(
            fit: StackFit.expand,
            children: [
              Image.asset(
                'assets/images/event_pink_arch.jpg',
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  color: _RoseColors.secondary.withOpacity(0.2),
                ),
              ),
              // Gradient overlay
              Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black.withOpacity(0.1),
                      Colors.black.withOpacity(0.65),
                    ],
                  ),
                ),
              ),
              // Glass effect
              Positioned.fill(
                child: BackdropFilter(
                  filter: ImageFilter.blur(sigmaX: 1, sigmaY: 1),
                  child: const SizedBox.shrink(),
                ),
              ),
              // Content
              Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.end,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Crea Momentos\nMagicos',
                      style: GoogleFonts.playfairDisplay(
                        fontSize: 32,
                        fontWeight: FontWeight.w700,
                        fontStyle: FontStyle.italic,
                        color: Colors.white,
                        height: 1.15,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Container(
                      width: 48,
                      height: 2,
                      color: _RoseColors.accent,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Decoracion exclusiva para cada celebracion',
                      style: GoogleFonts.inter(
                        fontSize: 13,
                        color: Colors.white.withOpacity(0.85),
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

  // ── 4. Quick stats ───────────────────────────────────────────────────────

  Widget _buildQuickStats() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: Row(
        children: [
          Expanded(child: _statCard('127+', 'Eventos')),
          const SizedBox(width: 12),
          Expanded(child: _statCard('48', 'Proveedores')),
          const SizedBox(width: 12),
          Expanded(child: _statCard('4.9\u2605', 'Rating')),
        ],
      ),
    );
  }

  Widget _statCard(String value, String label) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(16),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 15, sigmaY: 15),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: _glassDecoration(radius: 16),
          child: Column(
            children: [
              ShaderMask(
                shaderCallback: (bounds) =>
                    _RoseColors.goldGradient.createShader(bounds),
                child: Text(
                  value,
                  style: GoogleFonts.playfairDisplay(
                    fontSize: 22,
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                  ),
                ),
              ),
              const SizedBox(height: 2),
              Text(
                label,
                style: GoogleFonts.inter(
                  fontSize: 12,
                  color: _RoseColors.muted,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── 5. Services horizontal scroll ────────────────────────────────────────

  Widget _buildServicesSection() {
    final services = [
      _ServiceItem('Decoracion', Icons.palette, 'assets/images/icon_floral.jpg'),
      _ServiceItem('Mobiliario', Icons.chair, 'assets/images/icon_furniture.jpg'),
      _ServiceItem('Dulces', Icons.cake, 'assets/images/icon_candy.jpg'),
      _ServiceItem('Servicios', Icons.room_service, 'assets/images/icon_services.jpg'),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 24, 20, 12),
          child: _goldShimmerText('Nuestros Servicios'),
        ),
        SizedBox(
          height: 100,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            scrollDirection: Axis.horizontal,
            itemCount: services.length,
            separatorBuilder: (_, __) => const SizedBox(width: 12),
            itemBuilder: (context, index) {
              final s = services[index];
              return _servicePill(s);
            },
          ),
        ),
      ],
    );
  }

  Widget _servicePill(_ServiceItem service) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(50),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 15, sigmaY: 15),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: _glassDecoration(radius: 50),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Circular thumbnail
              ClipOval(
                child: SizedBox(
                  width: 48,
                  height: 48,
                  child: Image.asset(
                    service.image,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => Container(
                      color: _RoseColors.secondary.withOpacity(0.2),
                      child: Icon(service.icon, color: _RoseColors.primary),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Text(
                service.label,
                style: GoogleFonts.inter(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: _RoseColors.text,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── 6. Categories ────────────────────────────────────────────────────────

  Widget _buildCategoriesSectionHeader() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 24, 20, 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          _goldShimmerText('Categorias'),
          GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const ProductsListScreen(),
                ),
              );
            },
            child: Text(
              'Ver todo',
              style: GoogleFonts.inter(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: _RoseColors.primary,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoriesGrid() {
    return SliverPadding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      sliver: Consumer<CategoriesProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const SliverToBoxAdapter(
              child: SizedBox(
                height: 120,
                child: Center(
                  child: CircularProgressIndicator(
                    color: _RoseColors.primary,
                  ),
                ),
              ),
            );
          }
          if (provider.error != null) {
            return SliverToBoxAdapter(
              child: Center(
                child: Text(
                  'Error: ${provider.error}',
                  style: GoogleFonts.inter(color: _RoseColors.muted),
                ),
              ),
            );
          }

          final categories = provider.categories;
          if (categories.isEmpty) {
            return SliverToBoxAdapter(
              child: Center(
                child: Text(
                  'Sin categorias',
                  style: GoogleFonts.inter(color: _RoseColors.muted),
                ),
              ),
            );
          }

          return SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              childAspectRatio: 0.85,
              crossAxisSpacing: 14,
              mainAxisSpacing: 14,
            ),
            delegate: SliverChildBuilderDelegate(
              (context, index) => _buildCategoryCard(categories[index]),
              childCount: categories.length,
            ),
          );
        },
      ),
    );
  }

  Widget _buildCategoryCard(Category category) {
    final image = (category.imageUrl != null && category.imageUrl!.isNotEmpty)
        ? NetworkImage(category.imageUrl!) as ImageProvider
        : AssetImage(_fallbackImage(category.name));

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => ProductsListScreen(categoryId: category.id),
          ),
        );
      },
      child: ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: Stack(
          fit: StackFit.expand,
          children: [
            Image(
              image: image,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Container(
                color: _RoseColors.secondary.withOpacity(0.15),
              ),
            ),
            // Glass overlay at bottom
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(
                  bottom: Radius.circular(20),
                ),
                child: BackdropFilter(
                  filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 12,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.35),
                      border: Border(
                        top: BorderSide(
                          color: Colors.white.withOpacity(0.2),
                        ),
                      ),
                    ),
                    child: Text(
                      category.name,
                      style: GoogleFonts.playfairDisplay(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: _RoseColors.text,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ),
              ),
            ),
            // Soft rose shadow border
            Positioned.fill(
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: _RoseColors.border.withOpacity(0.5)),
                  boxShadow: [
                    BoxShadow(
                      color: _RoseColors.primary.withOpacity(0.1),
                      blurRadius: 20,
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

  // ── 7. Trending ──────────────────────────────────────────────────────────

  Widget _buildTrendingSection() {
    final trending = [
      _TrendingItem('Graduaciones', 'assets/images/event_graduation.jpg'),
      _TrendingItem('Gender Reveal', 'assets/images/event_gender_reveal.jpg'),
      _TrendingItem('Quinceaneras', 'assets/images/event_quinceanera.jpg'),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 24, 20, 12),
          child: _goldShimmerText('Tendencias'),
        ),
        SizedBox(
          height: 200,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            scrollDirection: Axis.horizontal,
            itemCount: trending.length,
            separatorBuilder: (_, __) => const SizedBox(width: 14),
            itemBuilder: (context, index) {
              final item = trending[index];
              return _trendingCard(item);
            },
          ),
        ),
      ],
    );
  }

  Widget _trendingCard(_TrendingItem item) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(20),
      child: SizedBox(
        width: 180,
        child: Stack(
          fit: StackFit.expand,
          children: [
            Image.asset(
              item.image,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Container(
                color: _RoseColors.secondary.withOpacity(0.15),
              ),
            ),
            // Glass overlay at bottom
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: ClipRRect(
                child: BackdropFilter(
                  filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 10,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.35),
                      border: Border(
                        top: BorderSide(
                          color: Colors.white.withOpacity(0.2),
                        ),
                      ),
                    ),
                    child: Text(
                      item.title,
                      style: GoogleFonts.playfairDisplay(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: _RoseColors.text,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            // Border
            Positioned.fill(
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: Colors.white.withOpacity(0.2),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Bottom Nav ───────────────────────────────────────────────────────────

  Widget _buildBottomNav() {
    return ClipRRect(
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.75),
            border: Border(
              top: BorderSide(color: _RoseColors.border.withOpacity(0.5)),
            ),
          ),
          child: SafeArea(
            child: Padding(
              padding:
                  const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  _navItem(Icons.home_outlined, 'Inicio', isActive: true),
                  _navItem(Icons.storefront_outlined, 'Catalogo', onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const ProductsListScreen(),
                      ),
                    );
                  }),
                  _buildFab(),
                  _navItem(Icons.calendar_today_outlined, 'Eventos',
                      onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const EventsListScreen(),
                      ),
                    );
                  }),
                  _navItem(Icons.handshake_outlined, 'Proveedores',
                      onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const SupplierListScreen(),
                      ),
                    );
                  }),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _navItem(
    IconData icon,
    String label, {
    bool isActive = false,
    VoidCallback? onTap,
  }) {
    final color = isActive ? _RoseColors.primary : _RoseColors.muted;
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 24),
          const SizedBox(height: 4),
          Text(
            label,
            style: GoogleFonts.inter(
              fontSize: 10,
              fontWeight: isActive ? FontWeight.w700 : FontWeight.w500,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFab() {
    return AnimatedBuilder(
      animation: _fabBounce,
      builder: (context, _) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Floating label
          FadeTransition(
            opacity: _fabLabelOpacity,
            child: Container(
              margin: const EdgeInsets.only(bottom: 4),
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
              decoration: BoxDecoration(
                color: _RoseColors.primary.withOpacity(0.9),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                'Crear evento',
                style: GoogleFonts.inter(
                  color: Colors.white, fontSize: 9,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
          // Bouncy button
          Transform.translate(
            offset: Offset(0, -14 + _fabBounce.value),
            child: GestureDetector(
              onTap: () => Navigator.push(context,
                MaterialPageRoute(builder: (_) => const EventCalendarScreen())),
              child: Container(
                width: 56, height: 56,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      _RoseColors.primary,
                      Color(0xFFA16207),
                      _RoseColors.secondary,
                    ],
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: _RoseColors.primary.withOpacity(0.35),
                      blurRadius: 18, offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: const Icon(Icons.add_rounded, color: Colors.white, size: 28),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Data classes ──────────────────────────────────────────────────────────────

class _ServiceItem {
  final String label;
  final IconData icon;
  final String image;
  const _ServiceItem(this.label, this.icon, this.image);
}

class _TrendingItem {
  final String title;
  final String image;
  const _TrendingItem(this.title, this.image);
}
