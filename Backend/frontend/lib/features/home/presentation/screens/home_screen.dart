import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../../shop/presentation/cart_provider.dart';
import '../../../categories/presentation/categories_provider.dart';
import '../../../categories/data/category_models.dart';
import '../../../shop/presentation/screens/cart_screen.dart';
import '../../../products/presentation/screens/products_list_screen.dart';
import '../../../profile/presentation/screens/profile_screen.dart';
import '../../../events/presentation/screens/events_list_screen.dart';
import '../../../events/presentation/screens/event_calendar_screen.dart';
import '../../../suppliers/presentation/screens/supplier_list_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen>
    with TickerProviderStateMixin {
  late final AnimationController _staggerController;
  late final List<Animation<double>> _sectionAnimations;
  late final AnimationController _floatController;
  late final AnimationController _decoController;
  late final AnimationController _pulseController;
  late final AnimationController _gradientController;
  late final ScrollController _scrollController;
  late final PageController _trendingPageController;
  Timer? _trendingAutoScroll;
  int _trendingPage = 0;

  bool _showStickyHeader = false;
  double _lastScrollOffset = 0;
  late final AnimationController _aiFabGlowController;
  late final AnimationController _aiTooltipController;
  bool _showAiTooltip = true;
  Timer? _aiTooltipDismiss;

  static const _sectionCount = 8;

  @override
  void initState() {
    super.initState();
    _staggerController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 300 + _sectionCount * 120),
    );
    _sectionAnimations = List.generate(_sectionCount, (i) {
      final start = i * 0.09;
      final end = (start + 0.35).clamp(0.0, 1.0);
      return CurvedAnimation(
        parent: _staggerController,
        curve: Interval(start, end, curve: Curves.easeOutCubic),
      );
    });
    _staggerController.forward();

    _floatController = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 5500))
      ..repeat();
    _decoController = AnimationController(
        vsync: this, duration: const Duration(seconds: 20))
      ..repeat();
    _pulseController = AnimationController(
        vsync: this, duration: const Duration(seconds: 3))
      ..repeat(reverse: true);
    _gradientController = AnimationController(
        vsync: this, duration: const Duration(seconds: 12))
      ..repeat();

    _scrollController = ScrollController()..addListener(_onScroll);
    _trendingPageController = PageController(viewportFraction: 0.88);

    _trendingAutoScroll = Timer.periodic(
      const Duration(seconds: 4),
      (_) => _autoAdvanceTrending(),
    );

    // AI FAB glow pulse
    _aiFabGlowController = AnimationController(
        vsync: this, duration: const Duration(seconds: 2))
      ..repeat(reverse: true);
    // AI tooltip fade
    _aiTooltipController = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 400));

    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CategoriesProvider>().fetchCategories();
      // Show tooltip after entrance animations finish
      Future.delayed(const Duration(milliseconds: 1500), () {
        if (mounted && _showAiTooltip) {
          _aiTooltipController.forward();
          _aiTooltipDismiss = Timer(const Duration(seconds: 8), () {
            if (mounted) _aiTooltipController.reverse();
          });
        }
      });
    });
  }

  void _onScroll() {
    final offset = _scrollController.offset;
    final scrollingUp = offset < _lastScrollOffset;
    final pastThreshold = offset > 100;
    final show = scrollingUp && pastThreshold;
    _lastScrollOffset = offset;
    if (show != _showStickyHeader) setState(() => _showStickyHeader = show);
  }

  void _autoAdvanceTrending() {
    if (!_trendingPageController.hasClients) return;
    _trendingPage = (_trendingPage + 1) % _trendingItems.length;
    _trendingPageController.animateToPage(
      _trendingPage,
      duration: const Duration(milliseconds: 500),
      curve: Curves.easeInOut,
    );
  }

  @override
  void dispose() {
    _staggerController.dispose();
    _floatController.dispose();
    _decoController.dispose();
    _pulseController.dispose();
    _gradientController.dispose();
    _scrollController.dispose();
    _trendingPageController.dispose();
    _trendingAutoScroll?.cancel();
    _aiFabGlowController.dispose();
    _aiTooltipController.dispose();
    _aiTooltipDismiss?.cancel();
    super.dispose();
  }

  Widget _staggered(int index, Widget child) {
    if (index >= _sectionAnimations.length) return child;
    return FadeTransition(
      opacity: _sectionAnimations[index],
      child: SlideTransition(
        position: Tween<Offset>(
          begin: const Offset(0, 0.06),
          end: Offset.zero,
        ).animate(_sectionAnimations[index]),
        child: child,
      ),
    );
  }

  String _fallbackImage(String name) {
    final l = name.toLowerCase();
    if (l.contains('silla') || l.contains('chair') || l.contains('furni')) {
      return 'assets/images/product_tiffany_chair.jpeg';
    }
    if (l.contains('mesa') || l.contains('table')) {
      return 'assets/images/product_round_table.jpg';
    }
    if (l.contains('flor') || l.contains('flower') || l.contains('centro') ||
        l.contains('decor')) {
      return 'assets/images/decor_floral_centerpiece.jpg';
    }
    if (l.contains('baby')) return 'assets/images/event_baby_shower.jpg';
    if (l.contains('xv') || l.contains('quince')) {
      return 'assets/images/event_quinceanera.jpg';
    }
    if (l.contains('navid') || l.contains('christmas')) {
      return 'assets/images/event_christmas_setup.jpg';
    }
    if (l.contains('gradu')) return 'assets/images/event_graduation.jpg';
    if (l.contains('safari')) return 'assets/images/event_safari_party.jpg';
    return 'assets/images/event_pink_arch.jpg';
  }

  // ── Trending data ───────────────────────────────────────────────────────

  static final _trendingItems = [
    _Trend('Graduaciones', 'assets/images/event_graduation.jpg',
        const [AppColors.violet, Color(0xFF6366F1)],
        '2.4K eventos', Icons.school_rounded),
    _Trend('Gender Reveal', 'assets/images/event_gender_reveal.jpg',
        const [AppColors.hotPink, AppColors.coral],
        '1.8K eventos', Icons.favorite_rounded),
    _Trend('Quinceañeras', 'assets/images/event_quinceanera.jpg',
        const [AppColors.teal, AppColors.sky],
        '3.1K eventos', Icons.auto_awesome_rounded),
    _Trend('Baby Shower', 'assets/images/event_baby_shower.jpg',
        const [AppColors.amber, Color(0xFFFF8C00)],
        '1.2K eventos', Icons.child_friendly_rounded),
  ];

  // ── Build ────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          RfGradientOrbs(
            controller: _gradientController,
            color1: AppColors.hotPink,
            color2: AppColors.violet,
            isDark: isDark,
          ),
          RfDecoLayer(
            floatController: _floatController,
            decoController: _decoController,
            pulseController: _pulseController,
            baseOpacity: isDark ? 1.0 : 1.8,
          ),
          Positioned.fill(
            child: IgnorePointer(
              child: CustomPaint(
                painter: RfGridPainter(
                    color: (isDark ? Colors.white : Colors.black)
                        .withOpacity(0.012)),
              ),
            ),
          ),
          CustomScrollView(
            controller: _scrollController,
            slivers: [
              SliverToBoxAdapter(child: _staggered(0, _buildHeader(t))),
              SliverToBoxAdapter(child: _staggered(1, _buildSearchBar(t))),
              SliverToBoxAdapter(child: _staggered(2, _buildHeroBanner(t))),
              SliverToBoxAdapter(child: _staggered(3, _buildQuickStats(t))),
              SliverToBoxAdapter(
                  child: _staggered(4, _buildServicesSection(t))),
              SliverToBoxAdapter(
                  child: _staggered(5, _buildTrendingSlider(t))),
              SliverToBoxAdapter(
                child: _staggered(6, _buildSectionHeader(
                    'Categorías', t, onSeeAll: () {
                  Navigator.push(context, MaterialPageRoute(
                      builder: (_) => const ProductsListScreen()));
                })),
              ),
              _buildCategoriesGrid(t),
              const SliverToBoxAdapter(child: SizedBox(height: 100)),
            ],
          ),
          // Sticky header on scroll
          if (_showStickyHeader) _buildStickyHeader(t),
          // AI Assistant FAB with tooltip
          Positioned(
            right: 16,
            bottom: 110,
            child: Stack(
              clipBehavior: Clip.none,
              alignment: Alignment.bottomRight,
              children: [
                // Tooltip bubble (above the button)
                Positioned(
                  bottom: 68,
                  right: 0,
                  child: FadeTransition(
                    opacity: _aiTooltipController,
                    child: SlideTransition(
                      position: Tween<Offset>(
                        begin: const Offset(0, 0.3),
                        end: Offset.zero,
                      ).animate(CurvedAnimation(
                        parent: _aiTooltipController,
                        curve: Curves.easeOutCubic,
                      )),
                      child: CustomPaint(
                        painter: _ChatBubblePainter(
                          color: t.card,
                          borderRadius: 18,
                          tailSize: 20,
                        ),
                        child: Container(
                          width: 240,
                          padding: const EdgeInsets.fromLTRB(16, 16, 16, 34),
                          child: Text(
                            'Soy tu asistente con inteligencia artificial, \u00a1puedo ayudarte a planificar el evento completo! \u{1F389}',
                            style: GoogleFonts.dmSans(
                              fontSize: 14,
                              fontWeight: FontWeight.w500,
                              color: t.textPrimary,
                              height: 1.4,
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                // Glowing circle button
                GestureDetector(
                  onTap: () {
                    _aiTooltipDismiss?.cancel();
                    _aiTooltipController.reverse();
                    _aiFabGlowController.stop();
                    // TODO: open AI assistant chat
                  },
                  child: AnimatedBuilder(
                    animation: _aiFabGlowController,
                    builder: (context, _) => Container(
                      width: 62, height: 62,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: const LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [AppColors.violet, AppColors.hotPink],
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.hotPink.withOpacity(
                                0.1 + _aiFabGlowController.value * 0.2),
                            blurRadius: 20,
                            spreadRadius: _aiFabGlowController.value * 3,
                          ),
                        ],
                      ),
                      child: const Icon(Icons.support_agent_rounded,
                          color: Colors.white, size: 28),
                    ),
                  ),
                ),
              ],
            ),
          ),
          // Bottom nav
          Positioned(
            left: 0, right: 0, bottom: 0,
            child: _buildBottomNav(t),
          ),
        ],
      ),
    );
  }

  // ── Sticky Header ───────────────────────────────────────────────────────

  Widget _buildStickyHeader(RfTheme t) {
    return Positioned(
      top: 0,
      left: 0,
      right: 0,
      child: ClipRRect(
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 30, sigmaY: 30),
          child: Container(
            color: t.card.withOpacity(0.85),
            child: SafeArea(
              bottom: false,
              child: Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                child: Row(
                  children: [
                    Container(
                      width: 34, height: 34,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(
                            color: AppColors.hotPink.withOpacity(0.4),
                            width: 1.5),
                        image: const DecorationImage(
                          image:
                              AssetImage('assets/images/logo_rosafiesta.png'),
                          fit: BoxFit.cover,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ShaderMask(
                      shaderCallback: (b) =>
                          AppColors.titleGradient.createShader(b),
                      child: Text(
                        'RosaFiesta',
                        style: GoogleFonts.outfit(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    const Spacer(),
                    _responsiveThemeToggle(t),
                    const SizedBox(width: 6),
                    _iconButton(Icons.notifications_outlined, t, () {}),
                    const SizedBox(width: 6),
                    _iconButton(Icons.shopping_bag_outlined, t, () {
                      Navigator.push(context, MaterialPageRoute(
                          builder: (_) => const CartScreen()));
                    }),
                    const SizedBox(width: 6),
                    _avatarButton(t),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  // ── Header ──────────────────────────────────────────────────────────────

  Widget _buildHeader(RfTheme t) {
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
        child: Row(
          children: [
            Container(
              width: 42, height: 42,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                    color: AppColors.hotPink.withOpacity(0.4), width: 2),
                image: const DecorationImage(
                  image: AssetImage('assets/images/logo_rosafiesta.png'),
                  fit: BoxFit.cover,
                ),
              ),
            ),
            const SizedBox(width: 10),
            ShaderMask(
              shaderCallback: (b) =>
                  AppColors.titleGradient.createShader(b),
              child: Text(
                'RosaFiesta',
                style: GoogleFonts.outfit(
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                  color: Colors.white,
                ),
              ),
            ),
            const Spacer(),
            _responsiveThemeToggle(t),
            const SizedBox(width: 6),
            _iconButton(Icons.notifications_outlined, t, () {
              // TODO: notifications screen
            }),
            const SizedBox(width: 6),
            Consumer<CartProvider>(
              builder: (context, cart, _) {
                return Stack(
                  clipBehavior: Clip.none,
                  children: [
                    _iconButton(Icons.shopping_bag_outlined, t, () {
                      Navigator.push(context, MaterialPageRoute(
                          builder: (_) => const CartScreen()));
                    }),
                    if (cart.itemCount > 0)
                      Positioned(
                        right: -4, top: -4,
                        child: Container(
                          width: 18, height: 18,
                          decoration: const BoxDecoration(
                            gradient: AppColors.buttonGradient,
                            shape: BoxShape.circle,
                          ),
                          alignment: Alignment.center,
                          child: Text('${cart.itemCount}',
                              style: GoogleFonts.dmSans(
                                  fontSize: 10,
                                  fontWeight: FontWeight.w700,
                                  color: Colors.white)),
                        ),
                      ),
                  ],
                );
              },
            ),
            const SizedBox(width: 6),
            _avatarButton(t),
          ],
        ),
      ),
    );
  }

  Widget _responsiveThemeToggle(RfTheme t) {
    final width = MediaQuery.of(context).size.width;
    if (width < 380) {
      // Small screens: just the icon, no text
      return GestureDetector(
        onTap: () => context.read<ThemeProvider>().toggle(),
        child: Container(
          width: 40, height: 40,
          decoration: BoxDecoration(
            color: t.card.withOpacity(0.7),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: t.borderFaint),
          ),
          child: t.isDark
              ? const Icon(Icons.dark_mode_rounded,
                  color: Color(0xFF7C8BF5), size: 20)
              : ShaderMask(
                  shaderCallback: (b) => const LinearGradient(
                    colors: [Color(0xFFFFB800), Color(0xFFFF8C00)],
                  ).createShader(b),
                  child: const Icon(Icons.wb_sunny_rounded,
                      color: Colors.white, size: 20),
                ),
        ),
      );
    }
    return RfThemeToggle(t: t);
  }

  Widget _iconButton(IconData icon, RfTheme t, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40, height: 40,
        decoration: BoxDecoration(
          color: t.card.withOpacity(0.7),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: t.borderFaint),
        ),
        child: Icon(icon, color: t.textMuted, size: 20),
      ),
    );
  }

  Widget _avatarButton(RfTheme t) {
    return GestureDetector(
      onTap: () => Navigator.push(context,
          MaterialPageRoute(builder: (_) => const ProfileScreen())),
      child: Container(
        width: 42, height: 42,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(
            color: AppColors.hotPink.withOpacity(0.5),
            width: 2,
          ),
          image: const DecorationImage(
            image: AssetImage('assets/images/example_of_user_2.jpg'),
            fit: BoxFit.cover,
          ),
        ),
      ),
    );
  }

  // ── Search Bar ──────────────────────────────────────────────────────────

  Widget _buildSearchBar(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: Container(
        height: 52,
        decoration: BoxDecoration(
          color: t.card,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.04),
              blurRadius: 12,
            ),
          ],
        ),
        child: TextField(
          style: GoogleFonts.dmSans(fontSize: 14, color: t.textPrimary),
          decoration: InputDecoration(
            hintText: 'Buscar decoraciones, temas...',
            hintStyle: GoogleFonts.dmSans(fontSize: 14, color: t.textDim),
            prefixIcon: Icon(Icons.search_rounded,
                color: t.textDim, size: 22),
            suffixIcon: Container(
              margin: const EdgeInsets.all(6),
              width: 40, height: 40,
              decoration: BoxDecoration(
                gradient: AppColors.buttonGradient,
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(Icons.tune_rounded,
                  color: Colors.white, size: 20),
            ),
            border: InputBorder.none,
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          ),
        ),
      ),
    );
  }

  // ── Hero Banner ─────────────────────────────────────────────────────────

  Widget _buildHeroBanner(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 0),
      child: Container(
        height: 200,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [AppColors.hotPink, AppColors.violet, Color(0xFF6366F1)],
          ),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.3),
              blurRadius: 24,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Stack(
          children: [
            Positioned(
              right: -20, top: -20,
              child: Container(
                width: 120, height: 120,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white.withOpacity(0.1),
                ),
              ),
            ),
            Positioned(
              right: 30, bottom: -30,
              child: Container(
                width: 80, height: 80,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white.withOpacity(0.08),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      'NUEVA TEMPORADA 2026',
                      style: GoogleFonts.dmSans(
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                        letterSpacing: 1.2,
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'Crea momentos\ninolvidables',
                    style: GoogleFonts.outfit(
                      fontSize: 26,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      height: 1.15,
                    ),
                  ),
                  const SizedBox(height: 14),
                  GestureDetector(
                    onTap: () => Navigator.push(context, MaterialPageRoute(
                        builder: (_) => const EventCalendarScreen())),
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 20, vertical: 10),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(24),
                      ),
                      child: Text(
                        'Explorar paquetes',
                        style: GoogleFonts.dmSans(
                          fontSize: 13,
                          fontWeight: FontWeight.w700,
                          color: AppColors.hotPink,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Quick Stats ─────────────────────────────────────────────────────────

  Widget _buildQuickStats(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: Row(
        children: [
          Expanded(child: _statCard('127', 'Eventos',
              Icons.celebration_rounded, AppColors.hotPink, t)),
          const SizedBox(width: 10),
          Expanded(child: _statCard('48', 'Proveedores',
              Icons.handshake_rounded, AppColors.violet, t)),
          const SizedBox(width: 10),
          Expanded(child: _statCard('4.9', 'Rating',
              Icons.star_rounded, const Color(0xFFFFC107), t)),
        ],
      ),
    );
  }

  Widget _statCard(String value, String label, IconData? icon, Color color,
      RfTheme t) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
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
                  fontSize: 20,
                  fontWeight: FontWeight.w800,
                  color: t.textPrimary,
                ),
              ),
              if (icon != null) ...[
                const SizedBox(width: 4),
                Icon(icon, color: color, size: 18),
              ],
            ],
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: GoogleFonts.dmSans(
              fontSize: 11,
              fontWeight: FontWeight.w500,
              color: t.textMuted,
            ),
          ),
        ],
      ),
    );
  }

  // ── Nuestros Servicios (pill style: icon left, text right) ──────────────

  Widget _buildServicesSection(RfTheme t) {
    final services = [
      _Svc('Decoración', Icons.palette_rounded,
          const [AppColors.hotPink, AppColors.coral]),
      _Svc('Mobiliario', Icons.chair_rounded,
          const [AppColors.violet, Color(0xFF6366F1)]),
      _Svc('Dulces', Icons.cake_rounded,
          const [AppColors.teal, AppColors.sky]),
      _Svc('Servicios', Icons.room_service_rounded,
          const [AppColors.amber, Color(0xFFFF8C00)]),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionHeader('Nuestros Servicios', t),
        SizedBox(
          height: 56,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            scrollDirection: Axis.horizontal,
            itemCount: services.length,
            separatorBuilder: (_, __) => const SizedBox(width: 10),
            itemBuilder: (context, i) => _servicePill(services[i], t),
          ),
        ),
      ],
    );
  }

  Widget _servicePill(_Svc svc, RfTheme t) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: t.borderFaint),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 44, height: 44,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: svc.colors,
              ),
              shape: BoxShape.circle,
            ),
            child: Icon(svc.icon, color: Colors.white, size: 22),
          ),
          const SizedBox(width: 10),
          Text(
            svc.label,
            style: GoogleFonts.dmSans(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: t.textPrimary,
            ),
          ),
          const SizedBox(width: 14),
        ],
      ),
    );
  }

  // ── Trending Slider ─────────────────────────────────────────────────────

  Widget _buildTrendingSlider(RfTheme t) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionHeader('Tendencias', t),
        SizedBox(
          height: 220,
          child: PageView.builder(
            controller: _trendingPageController,
            itemCount: _trendingItems.length,
            onPageChanged: (i) => setState(() => _trendingPage = i),
            itemBuilder: (context, i) =>
                _trendingSlideCard(_trendingItems[i], t),
          ),
        ),
        const SizedBox(height: 12),
        // Page indicator
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(
            _trendingItems.length,
            (i) => AnimatedContainer(
              duration: const Duration(milliseconds: 300),
              margin: const EdgeInsets.symmetric(horizontal: 3),
              width: i == _trendingPage ? 24 : 8,
              height: 8,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(4),
                gradient: i == _trendingPage
                    ? AppColors.buttonGradient
                    : null,
                color: i == _trendingPage
                    ? null
                    : t.textDim.withOpacity(0.3),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _trendingSlideCard(_Trend item, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: item.colors.first.withOpacity(0.2),
              blurRadius: 20,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(24),
          child: Stack(
            fit: StackFit.expand,
            children: [
              Image.asset(item.image, fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                    color: item.colors.first.withOpacity(0.2)),
              ),
              Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.transparent,
                      Colors.black.withOpacity(0.7),
                    ],
                    stops: const [0.3, 1.0],
                  ),
                ),
              ),
              // Content
              Positioned(
                left: 20, right: 20, bottom: 20,
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              gradient: LinearGradient(colors: item.colors),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: Text(
                              'Trending',
                              style: GoogleFonts.dmSans(
                                fontSize: 10,
                                fontWeight: FontWeight.w700,
                                color: Colors.white,
                              ),
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            item.title,
                            style: GoogleFonts.outfit(
                              fontSize: 22,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                            ),
                          ),
                        ],
                      ),
                    ),
                    // Stats badge
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                            color: Colors.white.withOpacity(0.2)),
                      ),
                      child: Column(
                        children: [
                          Icon(item.statIcon,
                              color: Colors.white, size: 18),
                          const SizedBox(height: 2),
                          Text(
                            item.stat,
                            style: GoogleFonts.dmSans(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: Colors.white,
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

  // ── Section Header ──────────────────────────────────────────────────────

  Widget _buildSectionHeader(String title, RfTheme t,
      {VoidCallback? onSeeAll}) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 14),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title,
              style: GoogleFonts.outfit(
                fontSize: 20,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              )),
          if (onSeeAll != null)
            GestureDetector(
              onTap: onSeeAll,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: AppColors.hotPink.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text('Ver todo',
                    style: GoogleFonts.dmSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.hotPink,
                    )),
              ),
            ),
        ],
      ),
    );
  }

  // ── Categories Grid ─────────────────────────────────────────────────────

  Widget _buildCategoriesGrid(RfTheme t) {
    return SliverPadding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      sliver: Consumer<CategoriesProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return SliverToBoxAdapter(
              child: SizedBox(
                height: 120,
                child: Center(
                  child: CircularProgressIndicator(
                      color: AppColors.hotPink, strokeWidth: 2),
                ),
              ),
            );
          }
          if (provider.error != null) {
            return SliverToBoxAdapter(
              child: Center(child: Text('Error: ${provider.error}',
                  style: GoogleFonts.dmSans(color: t.textMuted))),
            );
          }
          final cats = provider.categories;
          if (cats.isEmpty) {
            return SliverToBoxAdapter(
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 40),
                child: Column(children: [
                  Icon(Icons.category_outlined, color: t.textDim, size: 40),
                  const SizedBox(height: 8),
                  Text('Sin categorías',
                      style: GoogleFonts.dmSans(color: t.textMuted)),
                ]),
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
              (ctx, i) => _categoryCard(cats[i], t),
              childCount: cats.length,
            ),
          );
        },
      ),
    );
  }

  Widget _categoryCard(Category cat, RfTheme t) {
    final image = (cat.imageUrl != null && cat.imageUrl!.isNotEmpty)
        ? NetworkImage(cat.imageUrl!) as ImageProvider
        : AssetImage(_fallbackImage(cat.name));

    return GestureDetector(
      onTap: () => Navigator.push(context, MaterialPageRoute(
          builder: (_) => ProductsListScreen(categoryId: cat.id))),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          color: t.card,
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.06),
              blurRadius: 16,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(20),
          child: Column(children: [
            Expanded(
              flex: 3,
              child: Stack(fit: StackFit.expand, children: [
                Image(image: image, fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                      color: AppColors.hotPink.withOpacity(0.1))),
                Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.transparent,
                        Colors.black.withOpacity(0.15),
                      ],
                    ),
                  ),
                ),
              ]),
            ),
            Expanded(
              flex: 1,
              child: Container(
                alignment: Alignment.center,
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Text(cat.name,
                    style: GoogleFonts.dmSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: t.textPrimary,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.center),
              ),
            ),
          ]),
        ),
      ),
    );
  }

  // ── More Menu ────────────────────────────────────────────────────────

  void _showMoreMenu(RfTheme t) {
    showModalBottomSheet(
      context: context,
      backgroundColor: t.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40, height: 4,
              decoration: BoxDecoration(
                color: t.textDim.withOpacity(0.3),
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 20),
            _moreMenuItem(Icons.handshake_outlined, 'Proveedores', t,
                () => Navigator.push(context, MaterialPageRoute(
                    builder: (_) => const SupplierListScreen()))),
            _moreMenuItem(Icons.bar_chart_rounded, 'Estadísticas', t, () {}),
            _moreMenuItem(Icons.settings_outlined, 'Configuración', t, () {}),
            _moreMenuItem(Icons.help_outline_rounded, 'Ayuda', t, () {}),
          ],
        ),
      ),
    );
  }

  Widget _moreMenuItem(IconData icon, String label, RfTheme t,
      VoidCallback onTap) {
    return ListTile(
      leading: Container(
        width: 42, height: 42,
        decoration: BoxDecoration(
          color: AppColors.hotPink.withOpacity(0.08),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(icon, color: AppColors.hotPink, size: 22),
      ),
      title: Text(label, style: GoogleFonts.dmSans(
        fontSize: 15, fontWeight: FontWeight.w600, color: t.textPrimary)),
      trailing: Icon(Icons.chevron_right_rounded, color: t.textDim, size: 22),
      onTap: () { Navigator.pop(context); onTap(); },
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    );
  }

  // ── Bottom Nav with notch ─────────────────────────────────────────────

  Widget _buildBottomNav(RfTheme t) {
    const fabSize = 72.0;
    const fabRadius = fabSize / 2;  // 36
    const gap = 12.0;
    const notchRadius = fabRadius;  // clipper adds gap internally
    const navHeight = 72.0;

    final bottomPad = MediaQuery.of(context).padding.bottom;

    return SizedBox(
      height: navHeight + bottomPad + fabRadius,
      child: Stack(
        clipBehavior: Clip.none,
        alignment: Alignment.bottomCenter,
        children: [
          // Nav bar pinned to bottom, only covers navHeight + bottomPad
          Positioned(
            left: 0, right: 0, bottom: 0,
            height: navHeight + bottomPad,
            child: CustomPaint(
              foregroundPainter: _NotchBorderPainter(
                notchRadius: notchRadius,
                gap: gap,
                borderColor: t.borderFaint,
              ),
              child: ClipPath(
                clipper: _NotchClipper(notchRadius: notchRadius, gap: gap),
                child: Container(
                  color: t.card,
                  padding: EdgeInsets.only(bottom: bottomPad),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    child: Row(
                      children: [
                        Expanded(child: _navItem(
                            Icons.home_rounded, 'Inicio', t,
                            isActive: true)),
                        Expanded(child: _navItem(
                            Icons.storefront_outlined, 'Catálogo', t,
                            onTap: () => Navigator.push(context,
                                MaterialPageRoute(builder: (_) =>
                                    const ProductsListScreen())))),
                        const SizedBox(width: fabSize + 12),
                        Expanded(child: _navItem(
                            Icons.calendar_today_outlined, 'Eventos', t,
                            onTap: () => Navigator.push(context,
                                MaterialPageRoute(builder: (_) =>
                                    const EventsListScreen())))),
                        Expanded(child: _navItem(
                            Icons.more_horiz_rounded, 'Más', t,
                            onTap: () {
                              _showMoreMenu(t);
                            })),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
          // FAB centered on notch
          Positioned(
            top: 0,
            child: GestureDetector(
              onTap: () => Navigator.push(context, MaterialPageRoute(
                  builder: (_) => const EventCalendarScreen())),
              child: Container(
                width: fabSize, height: fabSize,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: AppColors.buttonGradient,
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.hotPink.withOpacity(0.3),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: const Icon(Icons.add_rounded,
                    color: Colors.white, size: 36),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _navItem(IconData icon, String label, RfTheme t,
      {bool isActive = false, VoidCallback? onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (isActive)
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                gradient: AppColors.buttonGradient,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: Colors.white, size: 22),
            )
          else
            Padding(
              padding: const EdgeInsets.all(8),
              child: Icon(icon, color: t.textDim, size: 22),
            ),
          const SizedBox(height: 2),
          Text(label,
              style: GoogleFonts.dmSans(
                fontSize: 10,
                fontWeight: isActive ? FontWeight.w700 : FontWeight.w500,
                color: isActive ? AppColors.hotPink : t.textDim,
              ),
              overflow: TextOverflow.ellipsis),
        ],
      ),
    );
  }
}

// ── Chat Bubble Painter ───────────────────────────────────────────────────────

class _ChatBubblePainter extends CustomPainter {
  final Color color;
  final double borderRadius;
  final double tailSize;

  _ChatBubblePainter({
    required this.color,
    this.borderRadius = 18,
    this.tailSize = 20,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final r = borderRadius;
    final w = size.width;
    final h = size.height;
    final tail = tailSize;
    // Body height (without tail)
    final bodyH = h - tail;

    final path = Path()
      // Top-left corner
      ..moveTo(r, 0)
      ..lineTo(w - r, 0)
      ..arcToPoint(Offset(w, r), radius: Radius.circular(r))
      // Right side down
      ..lineTo(w, bodyH - r)
      ..arcToPoint(Offset(w - r, bodyH), radius: Radius.circular(r))
      // Bottom-right to tail
      ..lineTo(w - 12, bodyH)
      // Tail triangle pointing down-right
      ..lineTo(w - 8, h)
      ..lineTo(w - 28, bodyH)
      // Bottom-left
      ..lineTo(r, bodyH)
      ..arcToPoint(Offset(0, bodyH - r), radius: Radius.circular(r))
      // Left side up
      ..lineTo(0, r)
      ..arcToPoint(Offset(r, 0), radius: Radius.circular(r))
      ..close();

    canvas.drawPath(path, Paint()..color = color);
    // Border around the whole shape
    canvas.drawPath(path, Paint()
      ..color = const Color(0x26000000)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.8);
  }

  @override
  bool shouldRepaint(_ChatBubblePainter old) => color != old.color;
}

// ── Notch Border Painter ──────────────────────────────────────────────────────

class _NotchBorderPainter extends CustomPainter {
  final double notchRadius;
  final double gap;
  final Color borderColor;

  _NotchBorderPainter({
    required this.notchRadius,
    required this.gap,
    required this.borderColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final cx = size.width / 2;
    final r = notchRadius + gap;

    // Top line with gap for circle
    final leftLine = Path()
      ..moveTo(0, 0)
      ..lineTo(cx - r, 0);
    final rightLine = Path()
      ..moveTo(cx + r, 0)
      ..lineTo(size.width, 0);
    // Semicircle arc (bottom half of circle)
    final arc = Path()
      ..addArc(
        Rect.fromCircle(center: Offset(cx, 0), radius: r),
        0,        // start angle (3 o'clock)
        3.14159,  // sweep pi (bottom semicircle)
      );

    final paint = Paint()
      ..color = borderColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5;
    canvas.drawPath(leftLine, paint);
    canvas.drawPath(rightLine, paint);
    canvas.drawPath(arc, paint);
  }

  @override
  bool shouldRepaint(_NotchBorderPainter old) => borderColor != old.borderColor;
}

// ── Notch Clipper ─────────────────────────────────────────────────────────────

class _NotchClipper extends CustomClipper<Path> {
  final double notchRadius;
  final double gap;

  _NotchClipper({required this.notchRadius, this.gap = 6});

  @override
  Path getClip(Size size) {
    final cx = size.width / 2;
    final r = notchRadius + gap;

    // Full rectangle
    final rect = Path()
      ..addRect(Rect.fromLTWH(0, 0, size.width, size.height));

    // Circle to subtract - centered at top edge
    final circle = Path()
      ..addOval(Rect.fromCircle(center: Offset(cx, 0), radius: r));

    // Subtract circle from rectangle = perfect circular notch
    final path = Path.combine(PathOperation.difference, rect, circle);

    return path;
  }

  @override
  bool shouldReclip(_NotchClipper old) =>
      notchRadius != old.notchRadius || gap != old.gap;
}

// ── Data ──────────────────────────────────────────────────────────────────────

class _Svc {
  final String label;
  final IconData icon;
  final List<Color> colors;
  const _Svc(this.label, this.icon, this.colors);
}

class _Trend {
  final String title;
  final String image;
  final List<Color> colors;
  final String stat;
  final IconData statIcon;
  const _Trend(this.title, this.image, this.colors, this.stat, this.statIcon);
}
