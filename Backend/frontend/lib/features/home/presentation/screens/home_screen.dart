import 'dart:async';
import 'dart:ui';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/services/voice_search_service.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../about/presentation/screens/about_screen.dart';
import '../../../active_event/presentation/active_event_provider.dart';
import '../../../active_event/presentation/screens/mi_evento_screen.dart';
import '../../../events/presentation/screens/event_calendar_screen.dart';
import '../../../events/presentation/screens/events_list_screen.dart';
import '../../../products/data/product_models.dart';
import '../../../products/presentation/products_provider.dart';
import '../../../products/presentation/screens/product_detail_screen.dart';
import '../../../products/presentation/screens/products_list_screen.dart';
import '../../../shell/main_shell.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
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
  final bool _showAiTooltip = true;
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
      vsync: this,
      duration: const Duration(milliseconds: 5500),
    )..repeat();
    _decoController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 20),
    )..repeat();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    )..repeat(reverse: true);
    _gradientController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 12),
    )..repeat();

    _scrollController = ScrollController()..addListener(_onScroll);
    _trendingPageController = PageController(viewportFraction: 0.88);

    _trendingAutoScroll = Timer.periodic(
      const Duration(seconds: 4),
      (_) => _autoAdvanceTrending(),
    );

    // AI FAB glow pulse
    _aiFabGlowController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);
    // AI tooltip fade
    _aiTooltipController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 400),
    );

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final productsProvider = context.read<ProductsProvider>();
      if (productsProvider.products.isEmpty) {
        productsProvider.fetchProducts(refresh: true);
      }
      // Voice search result → trigger product search
      VoiceSearchService().onResult = (text) {
        if (text.trim().isEmpty) return;
        productsProvider.search(text);
        if (kDebugMode) print('Voice search: $text');
      };
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
    if (l.contains('flor') ||
        l.contains('flower') ||
        l.contains('centro') ||
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
    _Trend(
      'Graduaciones',
      'assets/images/event_graduation.jpg',
      const [AppColors.violet, Color(0xFF6366F1)],
      '2.4K eventos',
      Icons.school_rounded,
    ),
    _Trend(
      'Gender Reveal',
      'assets/images/event_gender_reveal.jpg',
      const [AppColors.hotPink, AppColors.coral],
      '1.8K eventos',
      Icons.favorite_rounded,
    ),
    _Trend(
      'Quinceañeras',
      'assets/images/event_quinceanera.jpg',
      const [AppColors.teal, AppColors.sky],
      '3.1K eventos',
      Icons.auto_awesome_rounded,
    ),
    _Trend(
      'Baby Shower',
      'assets/images/event_baby_shower.jpg',
      const [AppColors.amber, Color(0xFFFF8C00)],
      '1.2K eventos',
      Icons.child_friendly_rounded,
    ),
  ];

  // ── Build ────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Material(
      type: MaterialType.transparency,
      child: Stack(
        children: [
          CustomScrollView(
            controller: _scrollController,
            slivers: [
              SliverToBoxAdapter(child: _staggered(0, _buildHeader(t))),
              SliverToBoxAdapter(child: _staggered(1, _buildSearchBar(t))),
              SliverToBoxAdapter(child: _staggered(2, _buildHeroBanner(t))),
              SliverToBoxAdapter(
                child: _staggered(3, _buildServicesSection(t)),
              ),
              SliverToBoxAdapter(child: _staggered(4, _buildTrendingSlider(t))),
              SliverToBoxAdapter(child: _staggered(5, _buildOffersSection(t))),
              const SliverToBoxAdapter(child: SizedBox(height: 100)),
            ],
          ),
          // Sticky header on scroll
          if (_showStickyHeader) _buildStickyHeader(t),
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
                padding: const EdgeInsets.symmetric(
                  horizontal: 20,
                  vertical: 10,
                ),
                child: _topBarContent(t, compact: true),
              ),
            ),
          ),
        ),
      ),
    );
  }

  // ── Header (minimal topbar: profile left, icons + cart right) ───────────

  Widget _buildHeader(RfTheme t) {
    return SafeArea(
      bottom: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 14, 20, 0),
        child: _topBarContent(t),
      ),
    );
  }

  Widget _topBarContent(RfTheme t, {bool compact = false}) {
    final logoSize = compact ? 40.0 : 52.0;
    final titleSize = compact ? 22.0 : 30.0;
    final iconSize = compact ? 40.0 : 46.0;
    final cartSize = compact ? 40.0 : 46.0;

    return Row(
      children: [
        // Logo + RosaFiesta name (tappable → AboutScreen)
        Expanded(
          child: GestureDetector(
            onTap: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const AboutScreen()),
            ),
            behavior: HitTestBehavior.opaque,
            child: Row(
              children: [
                Container(
                  width: logoSize,
                  height: logoSize,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: AppColors.hotPink.withOpacity(0.4),
                      width: 2,
                    ),
                    image: const DecorationImage(
                      image: AssetImage('assets/images/logo_rosafiesta.png'),
                      fit: BoxFit.cover,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Flexible(
                  child: ShaderMask(
                    shaderCallback: (b) =>
                        AppColors.titleGradient.createShader(b),
                    child: Text(
                      'RosaFiesta',
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.outfit(
                        fontSize: titleSize,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                        height: 1,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        // Theme toggle
        _circleIconButton(
          t.isDark ? Icons.dark_mode_rounded : Icons.wb_sunny_rounded,
          t,
          () => context.read<ThemeProvider>().toggle(),
          size: iconSize,
          iconColor: t.isDark
              ? const Color(0xFF7C8BF5)
              : const Color(0xFFFFB800),
        ),
        const SizedBox(width: 10),
        // Notifications
        _circleIconButton(
          Icons.notifications_rounded,
          t,
          () {},
          size: iconSize,
          showDot: true,
        ),
        const SizedBox(width: 10),
        // "Mi evento" button — cart-style card with gradient, label, badge, and notification dot
        Consumer<ActiveEventProvider>(
          builder: (context, active, _) {
            return GestureDetector(
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const MiEventoScreen()),
              ),
              child: Container(
                height: cartSize,
                padding: const EdgeInsets.symmetric(horizontal: 14),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppColors.hotPink, AppColors.violet],
                  ),
                  borderRadius: BorderRadius.circular(cartSize / 2),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.hotPink.withOpacity(0.35),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Stack(
                      clipBehavior: Clip.none,
                      children: [
                        Icon(
                          Icons.celebration_rounded,
                          color: Colors.white,
                          size: iconSize - 8,
                        ),
                        // Notification dot — pulses gently when items > 0
                        if (active.itemCount > 0)
                          Positioned(
                            right: -4,
                            top: -4,
                            child: _notificationPulse(t),
                          ),
                      ],
                    ),
                    const SizedBox(width: 6),
                    Text(
                      'Mi evento',
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Container(
                      constraints: const BoxConstraints(minWidth: 22),
                      height: 22,
                      padding: const EdgeInsets.symmetric(horizontal: 6),
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(11),
                      ),
                      child: Text(
                        '${active.itemCount}',
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          fontWeight: FontWeight.w800,
                          color: AppColors.hotPink,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _circleIconButton(
    IconData icon,
    RfTheme t,
    VoidCallback onTap, {
    double size = 48,
    double iconSize = 22,
    bool showDot = false,
    Color? iconColor,
    bool elevated = false,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          shape: BoxShape.circle,
          border: Border.all(color: t.borderFaint),
          boxShadow: elevated
              ? [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.06),
                    blurRadius: 14,
                    offset: const Offset(0, 4),
                  ),
                ]
              : null,
        ),
        child: Stack(
          clipBehavior: Clip.none,
          alignment: Alignment.center,
          children: [
            Icon(icon, color: iconColor ?? t.textPrimary, size: iconSize),
            if (showDot)
              Positioned(
                top: 9,
                right: 11,
                child: Container(
                  width: 9,
                  height: 9,
                  decoration: BoxDecoration(
                    color: AppColors.coral,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: t.isDark ? t.card : Colors.white,
                      width: 1.5,
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  // ── Search Bar ──────────────────────────────────────────────────────────

  Widget _buildSearchBar(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: Row(
        children: [
          // Search field
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
                  const Icon(Icons.search, color: Color(0xFF8D8E90), size: 28),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      style: GoogleFonts.dmSans(
                        fontSize: 17,
                        color: t.textPrimary,
                      ),
                      decoration: InputDecoration(
                        hintText: 'Buscar decoraciones, temas...',
                        hintStyle: GoogleFonts.dmSans(
                          fontSize: 17,
                          color: const Color(0xFF8D8E90),
                        ),
                        filled: false,
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(
                          vertical: 16,
                        ),
                        isCollapsed: true,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 10),
          // Mic button — voice search
          Consumer<VoiceSearchService>(
            builder: (context, voice, _) {
              final listening = voice.isListening;
              return GestureDetector(
                onTap: () async {
                  if (listening) {
                    await voice.stopListening();
                    setState(() {});
                  } else {
                    await voice.startListening();
                    setState(() {});
                  }
                },
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: listening
                        ? AppColors.hotPink.withOpacity(0.15)
                        : (t.isDark ? t.card : Colors.white),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: listening
                          ? AppColors.hotPink.withOpacity(0.4)
                          : t.borderFaint,
                      width: listening ? 1.5 : 1,
                    ),
                  ),
                  child: listening
                      ? _buildMicWaveform(t)
                      : const Icon(
                          Icons.mic_outlined,
                          color: Color(0xFF8D8E90),
                          size: 28,
                        ),
                ),
              );
            },
          ),
        ],
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
              right: -20,
              top: -20,
              child: Container(
                width: 120,
                height: 120,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white.withOpacity(0.1),
                ),
              ),
            ),
            Positioned(
              right: 30,
              bottom: -30,
              child: Container(
                width: 80,
                height: 80,
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
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 4,
                    ),
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
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const EventCalendarScreen(),
                      ),
                    ),
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 20,
                        vertical: 10,
                      ),
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

  // ── Nuestros Servicios (pill style: icon left, text right) ──────────────

  Widget _buildServicesSection(RfTheme t) {
    final services = [
      _Svc('Decoración', Icons.palette_rounded, const [
        AppColors.hotPink,
        AppColors.coral,
      ]),
      _Svc('Mobiliario', Icons.chair_rounded, const [
        AppColors.violet,
        Color(0xFF6366F1),
      ]),
      _Svc('Dulces', Icons.cake_rounded, const [AppColors.teal, AppColors.sky]),
      _Svc('Servicios', Icons.room_service_rounded, const [
        AppColors.amber,
        Color(0xFFFF8C00),
      ]),
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
            width: 44,
            height: 44,
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
                gradient: i == _trendingPage ? AppColors.buttonGradient : null,
                color: i == _trendingPage ? null : t.textDim.withOpacity(0.3),
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
              Image.asset(
                item.image,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) =>
                    Container(color: item.colors.first.withOpacity(0.2)),
              ),
              Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [Colors.transparent, Colors.black.withOpacity(0.7)],
                    stops: const [0.3, 1.0],
                  ),
                ),
              ),
              // Content
              Positioned(
                left: 20,
                right: 20,
                bottom: 20,
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 3,
                            ),
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
                        horizontal: 12,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                          color: Colors.white.withOpacity(0.2),
                        ),
                      ),
                      child: Column(
                        children: [
                          Icon(item.statIcon, color: Colors.white, size: 18),
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

  // ── Ofertas ─────────────────────────────────────────────────────────────

  Widget _buildOffersSection(RfTheme t) {
    return Consumer<ProductsProvider>(
      builder: (context, provider, _) {
        // Pick up to 6 products to show as "offers"; fabricate a fake
        // original price for visual effect until the backend supports
        // a real discount/promotion field.
        final offers = provider.products.take(6).toList();
        if (offers.isEmpty) return const SizedBox.shrink();
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionHeader(
              'Ofertas del mes',
              t,
              onSeeAll: () => MainShell.of(context)?.goToTab(1),
            ),
            SizedBox(
              height: 258,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 20),
                itemCount: offers.length,
                separatorBuilder: (_, __) => const SizedBox(width: 12),
                itemBuilder: (ctx, i) {
                  // Staggered fake discount % (30 / 25 / 20 / 15 / 25 / 30)
                  const discounts = [30, 25, 20, 15, 25, 30];
                  return _offerCard(
                    offers[i],
                    discounts[i % discounts.length],
                    t,
                  );
                },
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _offerCard(Product product, int discountPct, RfTheme t) {
    final variant = product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    final originalPrice = price / (1 - discountPct / 100);
    return GestureDetector(
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => ProductDetailScreen(productId: product.id),
        ),
      ),
      child: Container(
        width: 180,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: t.borderFaint),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.06),
              blurRadius: 16,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image with discount badge
            Padding(
              padding: const EdgeInsets.all(8),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: AspectRatio(
                  aspectRatio: 1.15,
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (imageUrl != null && imageUrl.isNotEmpty)
                        Image.network(
                          imageUrl,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            color: AppColors.hotPink.withOpacity(0.08),
                          ),
                        )
                      else
                        Container(color: AppColors.hotPink.withOpacity(0.08)),
                      Positioned(
                        top: 8,
                        left: 8,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 9,
                            vertical: 5,
                          ),
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [AppColors.hotPink, AppColors.coral],
                            ),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            '-$discountPct%',
                            style: GoogleFonts.outfit(
                              fontSize: 11,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                              letterSpacing: 0.3,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    product.nameTemplate,
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
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      ShaderMask(
                        shaderCallback: (b) => const LinearGradient(
                          colors: [AppColors.violet, AppColors.hotPink],
                        ).createShader(b),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              price.toStringAsFixed(0),
                              style: GoogleFonts.outfit(
                                fontSize: 20,
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
                                  fontSize: 10,
                                  fontWeight: FontWeight.w700,
                                  color: Colors.white,
                                  letterSpacing: 0.5,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 6),
                      Padding(
                        padding: const EdgeInsets.only(bottom: 2),
                        child: Text(
                          originalPrice.toStringAsFixed(0),
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                            color: t.textDim,
                            decoration: TextDecoration.lineThrough,
                            decorationColor: t.textDim,
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

  // ── Section Header ──────────────────────────────────────────────────────

  Widget _buildSectionHeader(
    String title,
    RfTheme t, {
    VoidCallback? onSeeAll,
  }) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 14),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            title,
            style: GoogleFonts.outfit(
              fontSize: 20,
              fontWeight: FontWeight.w700,
              color: t.textPrimary,
            ),
          ),
          if (onSeeAll != null)
            GestureDetector(
              onTap: onSeeAll,
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  color: AppColors.hotPink.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  'Ver todo',
                  style: GoogleFonts.dmSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: AppColors.hotPink,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _notificationPulse(RfTheme t) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.6, end: 1.0),
      duration: const Duration(milliseconds: 900),
      curve: Curves.easeInOut,
      builder: (context, val, _) {
        return Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(
            color: AppColors.coral.withOpacity(val),
            shape: BoxShape.circle,
            border: Border.all(
              color: t.isDark ? t.base : Colors.white,
              width: 1.5,
            ),
            boxShadow: [
              BoxShadow(
                color: AppColors.coral.withOpacity(0.4 * val),
                blurRadius: 6 * val,
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _dot([Color color = AppColors.violet]) {
    return Container(
      width: 4,
      height: 4,
      decoration: BoxDecoration(color: color, shape: BoxShape.circle),
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
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: t.textDim.withOpacity(0.3),
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 20),
            _moreMenuItem(Icons.settings_outlined, 'Configuración', t, () {}),
            _moreMenuItem(Icons.help_outline_rounded, 'Ayuda', t, () {}),
          ],
        ),
      ),
    );
  }

  Widget _moreMenuItem(
    IconData icon,
    String label,
    RfTheme t,
    VoidCallback onTap,
  ) {
    return ListTile(
      leading: Container(
        width: 42,
        height: 42,
        decoration: BoxDecoration(
          color: AppColors.hotPink.withOpacity(0.08),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(icon, color: AppColors.hotPink, size: 22),
      ),
      title: Text(
        label,
        style: GoogleFonts.dmSans(
          fontSize: 15,
          fontWeight: FontWeight.w600,
          color: t.textPrimary,
        ),
      ),
      trailing: Icon(Icons.chevron_right_rounded, color: t.textDim, size: 22),
      onTap: () {
        Navigator.pop(context);
        onTap();
      },
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    );
  }

  // ── Floating Pill Bottom Bar ──────────────────────────────────────────

  Widget _buildBottomNav(RfTheme t) {
    final bottomPad = MediaQuery.of(context).padding.bottom;

    return Padding(
      padding: EdgeInsets.fromLTRB(16, 0, 16, bottomPad + 20),
      child: Container(
        height: 70,
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(35),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.08),
              blurRadius: 24,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 6),
          child: Row(
            children: [
              _navItem(
                Icons.home_rounded,
                'Inicio',
                t,
                isActive: true,
                iconSize: 32,
              ),
              _navItem(
                Icons.storefront_outlined,
                'Catálogo',
                t,
                iconSize: 30,
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const ProductsListScreen()),
                ),
              ),
              _navItem(
                Icons.event_outlined,
                'Eventos',
                t,
                iconSize: 28,
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const EventsListScreen()),
                ),
              ),
              _navItem(
                Icons.calendar_month_outlined,
                'Calendario',
                t,
                iconSize: 28,
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => const EventCalendarScreen(),
                  ),
                ),
              ),
              _navItem(
                Icons.more_horiz_rounded,
                'Más',
                t,
                iconSize: 30,
                onTap: () => _showMoreMenu(t),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _navItem(
    IconData icon,
    String label,
    RfTheme t, {
    bool isActive = false,
    VoidCallback? onTap,
    double iconSize = 28,
  }) {
    return Expanded(
      flex: isActive ? 3 : 1,
      child: GestureDetector(
        onTap: onTap,
        behavior: HitTestBehavior.opaque,
        child: isActive
            ? Container(
                margin: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppColors.violet, AppColors.hotPink],
                  ),
                  borderRadius: BorderRadius.circular(28),
                ),
                alignment: Alignment.center,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(icon, color: Colors.white, size: iconSize),
                    const SizedBox(width: 8),
                    Text(
                      label,
                      style: GoogleFonts.dmSans(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
              )
            : Icon(icon, color: t.textDim, size: iconSize),
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
    canvas.drawPath(
      path,
      Paint()
        ..color = const Color(0x26000000)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 0.8,
    );
  }

  @override
  bool shouldRepaint(_ChatBubblePainter old) => color != old.color;
}

  Widget _buildMicWaveform(RfTheme t) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(7, (i) {
        final h = 8.0 +
            (i == 3 ? 20 : i % 2 == 0 ? 14 : 9);
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 2),
          child: AnimatedContainer(
            duration: Duration(milliseconds: 150 + i * 30),
            width: 3,
            height: h,
            decoration: BoxDecoration(
              color: AppColors.hotPink.withOpacity(0.5 + (i == 3 ? 0.4 : 0.15)),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
        );
      }),
    );
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
