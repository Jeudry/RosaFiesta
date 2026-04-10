import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../../products/data/product_models.dart';
import '../../../products/presentation/screens/product_detail_screen.dart';
import '../../../shop/presentation/cart_provider.dart';
import '../../../shop/presentation/screens/cart_screen.dart';
import '../assistant_provider.dart';
import 'sketch_canvas_screen.dart';

/// Full-screen guided assistant experience.
///
/// Each conversation step is rendered as a centered question with visual
/// options (chips, product cards, action buttons) — not a chat scroll.
class AssistantScreen extends StatefulWidget {
  const AssistantScreen({super.key});

  static void open(BuildContext context) {
    final provider = context.read<AssistantProvider>();
    if (provider.messages.isEmpty) provider.start();
    Navigator.of(context).push(
      PageRouteBuilder(
        transitionDuration: const Duration(milliseconds: 350),
        reverseTransitionDuration: const Duration(milliseconds: 250),
        pageBuilder: (_, __, ___) => const AssistantScreen(),
        transitionsBuilder: (_, anim, __, child) {
          return SlideTransition(
            position: Tween<Offset>(
              begin: const Offset(0, 0.12),
              end: Offset.zero,
            ).animate(CurvedAnimation(
                parent: anim, curve: Curves.easeOutCubic)),
            child: FadeTransition(opacity: anim, child: child),
          );
        },
      ),
    );
  }

  @override
  State<AssistantScreen> createState() => _AssistantScreenState();
}

class _AssistantScreenState extends State<AssistantScreen>
    with TickerProviderStateMixin {
  late final AnimationController _orbController;
  bool _isListening = false;
  int _activeBottomIdx = -1; // -1=none, 0=chatear, 1=hablar, 2=volver
  final TextEditingController _chatController = TextEditingController();
  String _transcript = '';
  bool _hasSketch = false;
  final Set<String> _omittedCategories = {};

  @override
  void initState() {
    super.initState();
    _orbController = AnimationController(
        vsync: this, duration: const Duration(seconds: 2))
      ..repeat(reverse: true);
  }

  @override
  void dispose() {
    _orbController.dispose();
    _chatController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          // Ambient gradient background — voice-assistant feel
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _orbController,
              builder: (_, __) {
                final shift = _orbController.value * 0.15;
                return Container(
                  decoration: BoxDecoration(
                    gradient: RadialGradient(
                      center: Alignment(0.0, -0.3 + shift),
                      radius: 1.4,
                      colors: isDark
                          ? [
                              AppColors.violet.withOpacity(0.15),
                              AppColors.hotPink.withOpacity(0.08),
                              t.base,
                            ]
                          : [
                              AppColors.hotPink.withOpacity(0.08),
                              AppColors.violet.withOpacity(0.05),
                              t.base,
                            ],
                      stops: const [0.0, 0.45, 1.0],
                    ),
                  ),
                );
              },
            ),
          ),
          SafeArea(
            child: Consumer<AssistantProvider>(
              builder: (context, provider, _) {
                // Auto-minimize when provider signals it
                if (provider.shouldMinimize) {
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    provider.clearMinimize();
                    if (mounted) Navigator.of(context).pop();
                  });
                }
                return Column(
                  children: [
                    _buildTopBar(t),
                    Expanded(child: _buildStepContent(provider, t)),
                    _buildBottomBar(provider, t),
                  ],
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  // ── Top Bar ─────────────────────────────────────────────────────────────

  // Steps for the progress indicator
  static const _stepLabels = [
    ConversationStep.greeting,
    ConversationStep.suggestArticles,
    ConversationStep.categories,
    ConversationStep.eventDetails,
    ConversationStep.summary,
    ConversationStep.done,
  ];

  Widget _buildTopBar(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      child: Row(
        children: [
          // Minimize button (left)
          GestureDetector(
            onTap: () => Navigator.of(context).pop(),
            child: Container(
              width: 56, height: 56,
              decoration: BoxDecoration(
                color: t.isDark ? t.card : Colors.white,
                shape: BoxShape.circle,
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.keyboard_arrow_down_rounded,
                  color: t.textPrimary, size: 30),
            ),
          ),
          const Spacer(),
          // "Ver progreso" button (center) with step dots below
          Consumer<AssistantProvider>(
            builder: (context, provider, _) {
              final currentIdx = _stepLabels
                  .indexOf(provider.step)
                  .clamp(0, _stepLabels.length - 1);
              return GestureDetector(
                onTap: () => Navigator.push(context,
                    MaterialPageRoute(builder: (_) => const CartScreen())),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      height: 44,
                      padding: const EdgeInsets.symmetric(horizontal: 18),
                      decoration: BoxDecoration(
                        color: t.isDark ? t.card : Colors.white,
                        borderRadius: BorderRadius.circular(22),
                        border: Border.all(color: t.borderFaint),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.checklist_rounded,
                              color: AppColors.hotPink, size: 18),
                          const SizedBox(width: 8),
                          Text('Ver progreso',
                              style: GoogleFonts.dmSans(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                                color: t.textPrimary,
                              )),
                        ],
                      ),
                    ),
                    const SizedBox(height: 6),
                    // Step dots under the progress button
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: List.generate(_stepLabels.length, (i) {
                        final isActive = i == currentIdx;
                        final isPast = i < currentIdx;
                        return Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 3),
                          child: AnimatedContainer(
                            duration: const Duration(milliseconds: 250),
                            width: isActive ? 18 : 6,
                            height: 6,
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(3),
                              color: isActive
                                  ? AppColors.hotPink
                                  : isPast
                                      ? AppColors.hotPink.withOpacity(0.4)
                                      : t.textDim.withOpacity(0.2),
                            ),
                          ),
                        );
                      }),
                    ),
                  ],
                ),
              );
            },
          ),
          const Spacer(),
          // Cart button (right)
          Consumer<CartProvider>(
            builder: (context, cart, _) {
              return Stack(
                clipBehavior: Clip.none,
                children: [
                  GestureDetector(
                    onTap: () => Navigator.push(context,
                        MaterialPageRoute(
                            builder: (_) => const CartScreen())),
                    child: Container(
                      width: 56, height: 56,
                      decoration: BoxDecoration(
                        color: t.isDark ? t.card : Colors.white,
                        shape: BoxShape.circle,
                        border: Border.all(color: t.borderFaint),
                      ),
                      child: Icon(Icons.shopping_cart_outlined,
                          color: t.textPrimary, size: 26),
                    ),
                  ),
                  if (cart.itemCount > 0)
                    Positioned(
                      right: -2, top: -2,
                      child: Container(
                        width: 22, height: 22,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: AppColors.coral,
                          shape: BoxShape.circle,
                          border: Border.all(
                              color: t.isDark ? t.card : Colors.white,
                              width: 2),
                        ),
                        child: Text('${cart.itemCount}',
                            style: GoogleFonts.dmSans(
                                fontSize: 11,
                                fontWeight: FontWeight.w800,
                                color: Colors.white)),
                      ),
                    ),
                ],
              );
            },
          ),
        ],
      ),
    );
  }

  // ── Step Content ────────────────────────────────────────────────────────

  Widget _buildStepContent(AssistantProvider provider, RfTheme t) {
    // Build the current visible step from the latest messages
    final messages = provider.messages;
    if (messages.isEmpty) return const SizedBox.shrink();

    // Find the last assistant text message and the last chips/products
    String? question;
    List<String>? chips;
    List<Product>? products;
    final productSections = <AssistantMessage>[];

    bool passedUserMsg = false;
    for (int i = messages.length - 1; i >= 0; i--) {
      final m = messages[i];
      if (m.kind == MessageKind.chips && chips == null) chips = m.chips;
      if (m.kind == MessageKind.productSuggestions) {
        productSections.insert(0, m);
        products ??= m.products;
      }
      if (m.kind == MessageKind.assistantText && question == null) {
        question = m.text;
      }
      if (m.kind == MessageKind.userText) {
        if (passedUserMsg) break;
        passedUserMsg = true;
      }
    }

    final hasChips = chips != null && chips.isNotEmpty &&
        provider.step != ConversationStep.done;

    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 350),
      switchInCurve: Curves.easeOutCubic,
      child: Column(
        key: ValueKey(question),
        children: [
          // Scrollable content with fade-out at bottom
          Expanded(
            child: Stack(
              children: [
                SingleChildScrollView(
                  padding: const EdgeInsets.fromLTRB(24, 20, 24, 0),
                  child: Column(
                    children: [
                      if (provider.isThinking)
                        _buildThinking(t)
                      else ...[
                        // Rosa IA orb above question
                        AnimatedBuilder(
                          animation: _orbController,
                          builder: (_, __) {
                            final glow = 0.15 + _orbController.value * 0.2;
                            return Container(
                              width: 64, height: 64,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                gradient: const LinearGradient(
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                  colors: [AppColors.hotPink, AppColors.violet],
                                ),
                                boxShadow: [
                                  BoxShadow(
                                    color: AppColors.hotPink.withOpacity(glow),
                                    blurRadius: 22,
                                    spreadRadius: 4,
                                  ),
                                ],
                              ),
                              child: const Icon(Icons.support_agent_rounded,
                                  color: Colors.white, size: 34),
                            );
                          },
                        ),
                        const SizedBox(height: 20),
                        // Question
                        if (question != null)
                          Text(
                            question,
                            textAlign: TextAlign.center,
                            style: GoogleFonts.outfit(
                              fontSize: 30,
                              fontWeight: FontWeight.w800,
                              color: t.textPrimary,
                              height: 1.25,
                            ),
                          ),
                        // Step 4: Categories checklist (above products)
                        if (provider.step == ConversationStep.categories) ...[
                          const SizedBox(height: 20),
                          _buildCategoriesChecklist(provider, t),
                        ],
                        // Product card with tabs
                        if (productSections.isNotEmpty) ...[
                          const SizedBox(height: 20),
                          _buildProductsCard(productSections, t),
                        ],
                        // Step 5: Event details form
                        if (provider.step == ConversationStep.eventDetails) ...[
                          const SizedBox(height: 20),
                          _buildEventDetailsForm(provider, t),
                        ],
                        // Step 6: Order summary
                        if (provider.step == ConversationStep.summary) ...[
                          const SizedBox(height: 20),
                          _buildOrderSummary(provider, t),
                        ],
                        // Step 7: Done confirmation
                        if (provider.step == ConversationStep.done) ...[
                          const SizedBox(height: 20),
                          _buildDoneConfirmation(t),
                        ],
                        // Event grid goes inside scroll for greeting step
                        if (hasChips &&
                            provider.step == ConversationStep.greeting) ...[
                          const SizedBox(height: 28),
                          _buildEventGrid(chips!, provider, t),
                        ],
                      ],
                      // Bottom padding so content doesn't hide behind fade
                      SizedBox(height: hasChips &&
                          provider.step != ConversationStep.greeting ? 24 : 20),
                    ],
                  ),
                ),
                // Fade-out gradient at bottom edge
                if (hasChips && provider.step != ConversationStep.greeting)
                  Positioned(
                    left: 0, right: 0, bottom: 0,
                    child: IgnorePointer(
                      child: Container(
                        height: 40,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              t.base.withOpacity(0.0),
                              t.base,
                            ],
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          // Action chips pinned below the scroll area
          if (hasChips && provider.step != ConversationStep.greeting)
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 8, 24, 16),
              child: _buildActionChips(chips!, provider, t),
            ),
        ],
      ),
    );
  }

  static const _eventImages = <String, String>{
    'Boda': 'assets/images/wedding.jpg',
    'Cumpleaños': 'assets/images/birthday.webp',
    'Baby Shower': 'assets/images/baby_shower.jpg',
    'Gender Reveal': 'assets/images/gender_reveal.jpg',
    'Corporativo': 'assets/images/corporativo.jpg',
  };

  Widget _buildEventGrid(
      List<String> events, AssistantProvider provider, RfTheme t) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      childAspectRatio: 1.35,
      children: events.map((e) {
        final imagePath = _eventImages[e];
        final hasImage = imagePath != null;
        return GestureDetector(
          onTap: () => provider.handleChipTap(e),
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(22),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.12),
                  blurRadius: 16,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(22),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  // Background image or fallback gradient
                  if (hasImage)
                    Image.asset(imagePath, fit: BoxFit.cover)
                  else
                    Container(
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [AppColors.hotPink, AppColors.violet],
                        ),
                      ),
                      child: Icon(Icons.auto_awesome_rounded,
                          size: 50, color: Colors.white.withOpacity(0.3)),
                    ),
                  // Dark gradient overlay for text readability
                  Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.transparent,
                          Colors.black.withOpacity(0.55),
                        ],
                        stops: const [0.3, 1.0],
                      ),
                    ),
                  ),
                  // Label
                  Positioned(
                    left: 14, right: 14, bottom: 14,
                    child: Text(
                      e,
                      style: GoogleFonts.outfit(
                        fontSize: 17,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                        shadows: [
                          Shadow(
                            color: Colors.black.withOpacity(0.4),
                            blurRadius: 8,
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
      }).toList(),
    );
  }

  // ── Step 4: Categories Checklist ──────────────────────────────────────

  static const _categoryOptions = [
    {'name': 'Sillas', 'icon': Icons.chair_rounded},
    {'name': 'Mesas', 'icon': Icons.table_restaurant_rounded},
    {'name': 'Iluminación', 'icon': Icons.lightbulb_rounded},
    {'name': 'Mantelería', 'icon': Icons.texture_rounded},
    {'name': 'Centros de mesa', 'icon': Icons.local_florist_rounded},
    {'name': 'Arcos y backdrops', 'icon': Icons.filter_frames_rounded},
    {'name': 'Globos', 'icon': Icons.bubble_chart_rounded},
    {'name': 'Dulces y pastel', 'icon': Icons.cake_rounded},
  ];

  Widget _buildCategoriesChecklist(AssistantProvider provider, RfTheme t) {
    // Split categories into active and omitted
    final active = _categoryOptions
        .where((c) => !_omittedCategories.contains(c['name']))
        .toList();
    final omitted = _categoryOptions
        .where((c) => _omittedCategories.contains(c['name']))
        .toList();

    return Container(
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('¿Qué más necesitas?',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              )),
          const SizedBox(height: 14),
          // Active categories
          ...active.map((cat) {
            final name = cat['name'] as String;
            final icon = cat['icon'] as IconData;
            final chosen = _categoryOptions.indexOf(cat) < 2;
            return _buildCategoryRow(
              name: name,
              icon: icon,
              chosen: chosen,
              omitted: false,
              provider: provider,
              t: t,
            );
          }),
          // Omitted section
          if (omitted.isNotEmpty) ...[
            const SizedBox(height: 8),
            Divider(color: t.borderFaint, height: 1),
            const SizedBox(height: 12),
            Text('Omitidos',
                style: GoogleFonts.dmSans(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: t.textDim,
                )),
            const SizedBox(height: 8),
            ...omitted.map((cat) {
              final name = cat['name'] as String;
              final icon = cat['icon'] as IconData;
              return _buildCategoryRow(
                name: name,
                icon: icon,
                chosen: false,
                omitted: true,
                provider: provider,
                t: t,
              );
            }),
          ],
        ],
      ),
    );
  }

  Widget _buildCategoryRow({
    required String name,
    required IconData icon,
    required bool chosen,
    required bool omitted,
    required AssistantProvider provider,
    required RfTheme t,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        decoration: BoxDecoration(
          color: omitted
              ? (t.isDark
                  ? Colors.white.withOpacity(0.02)
                  : const Color(0xFFF3F4F6))
              : chosen
                  ? AppColors.teal.withOpacity(0.08)
                  : (t.isDark
                      ? Colors.white.withOpacity(0.03)
                      : const Color(0xFFF9FAFB)),
          borderRadius: BorderRadius.circular(16),
          border: chosen
              ? Border.all(color: AppColors.teal.withOpacity(0.3))
              : null,
        ),
        child: Row(
          children: [
            Icon(icon,
                color: omitted
                    ? t.textDim
                    : chosen
                        ? AppColors.teal
                        : t.textMuted,
                size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Text(name,
                  style: GoogleFonts.dmSans(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: omitted ? t.textDim : t.textPrimary,
                  )),
            ),
            if (omitted)
              // Restore button for omitted items
              GestureDetector(
                onTap: () =>
                    setState(() => _omittedCategories.remove(name)),
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: t.borderFaint),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.restore_rounded,
                          color: t.textMuted, size: 14),
                      const SizedBox(width: 4),
                      Text('Restaurar',
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: t.textMuted,
                          )),
                    ],
                  ),
                ),
              )
            else if (chosen) ...[
              // Selected: "Ver elegidos" + "Agregar más"
              GestureDetector(
                onTap: () {
                  // TODO: Show selected items for this category
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    color: AppColors.teal.withOpacity(0.1),
                  ),
                  child: Text('Ver elegidos',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: AppColors.teal,
                      )),
                ),
              ),
              const SizedBox(width: 6),
              GestureDetector(
                onTap: () =>
                    provider.handleChipTap('Más sugerencias'),
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                        color: AppColors.teal.withOpacity(0.3)),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.add_rounded,
                          color: AppColors.teal, size: 14),
                      const SizedBox(width: 2),
                      Text('Agregar',
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: AppColors.teal,
                          )),
                    ],
                  ),
                ),
              ),
            ] else ...[
              // Not selected: "Elegir" + "Omitir"
              GestureDetector(
                onTap: () =>
                    provider.handleChipTap('Más sugerencias'),
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    gradient: const LinearGradient(
                        colors: [AppColors.violet, AppColors.hotPink]),
                  ),
                  child: Text('Elegir',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                      )),
                ),
              ),
              const SizedBox(width: 6),
              GestureDetector(
                onTap: () =>
                    setState(() => _omittedCategories.add(name)),
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: t.borderFaint),
                  ),
                  child: Text('Omitir',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: t.textDim,
                      )),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  // ── Step 5: Event Details Form ──────────────────────────────────────────

  Widget _buildEventDetailsForm(AssistantProvider provider, RfTheme t) {
    return Container(
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
      ),
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Detalles del evento',
              style: GoogleFonts.outfit(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              )),
          const SizedBox(height: 16),
          _formField(Icons.calendar_today_rounded, 'Fecha del evento',
              'Seleccionar fecha...', t),
          const SizedBox(height: 12),
          _formField(Icons.people_outline_rounded, 'Cantidad de personas',
              'Ej: 50 (opcional)', t),
          const SizedBox(height: 12),
          _formField(Icons.location_on_outlined, 'Ubicación',
              'Buscar dirección...', t),
          const SizedBox(height: 12),
          _formField(Icons.description_outlined, 'Descripción adicional',
              'Detalles, temática, colores...', t, multiline: true),
          const SizedBox(height: 16),
          // Sketch option
          GestureDetector(
            onTap: () async {
              final result = await Navigator.of(context).push<bool>(
                PageRouteBuilder(
                  transitionDuration: const Duration(milliseconds: 300),
                  reverseTransitionDuration: const Duration(milliseconds: 200),
                  pageBuilder: (_, __, ___) => const SketchCanvasScreen(),
                  transitionsBuilder: (_, anim, __, child) {
                    return SlideTransition(
                      position: Tween<Offset>(
                        begin: const Offset(0, 0.15),
                        end: Offset.zero,
                      ).animate(CurvedAnimation(
                          parent: anim, curve: Curves.easeOutCubic)),
                      child: FadeTransition(opacity: anim, child: child),
                    );
                  },
                ),
              );
              if (result == true && mounted) {
                setState(() => _hasSketch = true);
              }
            },
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: _hasSketch
                    ? AppColors.teal.withOpacity(0.08)
                    : AppColors.violet.withOpacity(0.06),
                borderRadius: BorderRadius.circular(18),
                border: Border.all(
                    color: _hasSketch
                        ? AppColors.teal.withOpacity(0.3)
                        : AppColors.violet.withOpacity(0.15)),
              ),
              child: Row(
                children: [
                  Container(
                    width: 48, height: 48,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(colors: _hasSketch
                          ? [AppColors.teal, AppColors.sky]
                          : [AppColors.violet, AppColors.hotPink]),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Icon(
                        _hasSketch
                            ? Icons.check_rounded
                            : Icons.draw_rounded,
                        color: Colors.white, size: 24),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Boceto de distribución',
                            style: GoogleFonts.dmSans(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: t.textPrimary,
                            )),
                        const SizedBox(height: 2),
                        Text(
                            _hasSketch
                                ? 'Boceto guardado. Toca para editar.'
                                : 'Dibuja cómo imaginas el espacio y la IA te dará ejemplos.',
                            style: GoogleFonts.dmSans(
                              fontSize: 12,
                              color: _hasSketch
                                  ? AppColors.teal
                                  : t.textMuted,
                              height: 1.3,
                            )),
                      ],
                    ),
                  ),
                  Icon(
                      _hasSketch
                          ? Icons.edit_rounded
                          : Icons.chevron_right_rounded,
                      color: _hasSketch
                          ? AppColors.teal
                          : t.textDim,
                      size: 24),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _formField(IconData icon, String label, String hint, RfTheme t,
      {bool multiline = false}) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.04)
            : const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Row(
        crossAxisAlignment: multiline
            ? CrossAxisAlignment.start
            : CrossAxisAlignment.center,
        children: [
          Padding(
            padding: EdgeInsets.only(top: multiline ? 14 : 0),
            child: Icon(icon, color: t.textMuted, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: TextField(
              maxLines: multiline ? 3 : 1,
              style: GoogleFonts.dmSans(
                  fontSize: 15, color: t.textPrimary),
              decoration: InputDecoration(
                labelText: label,
                labelStyle: GoogleFonts.dmSans(
                    fontSize: 13, color: t.textMuted),
                hintText: hint,
                hintStyle: GoogleFonts.dmSans(
                    fontSize: 14, color: t.textDim),
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
                contentPadding:
                    const EdgeInsets.symmetric(vertical: 12),
                isCollapsed: true,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Step 6: Order Summary ───────────────────────────────────────────────

  Widget _buildOrderSummary(AssistantProvider provider, RfTheme t) {
    // Mock summary from cart + event details
    return Container(
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
      ),
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.receipt_long_rounded,
                  color: AppColors.hotPink, size: 22),
              const SizedBox(width: 10),
              Text('Resumen del evento',
                  style: GoogleFonts.outfit(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                  )),
            ],
          ),
          const SizedBox(height: 16),
          // Event info
          _summaryRow(Icons.celebration_rounded, 'Evento',
              provider.eventType ?? 'No especificado', t),
          const SizedBox(height: 10),
          _summaryRow(Icons.calendar_today_rounded, 'Fecha',
              'Por confirmar', t),
          const SizedBox(height: 10),
          _summaryRow(Icons.location_on_outlined, 'Ubicación',
              'Por confirmar', t),
          const SizedBox(height: 16),
          Divider(color: t.borderFaint),
          const SizedBox(height: 12),
          // Items from cart (mock)
          Text('Artículos seleccionados',
              style: GoogleFonts.dmSans(
                fontSize: 14,
                fontWeight: FontWeight.w700,
                color: t.textMuted,
              )),
          const SizedBox(height: 10),
          Consumer<CartProvider>(
            builder: (context, cart, _) {
              if (cart.itemCount == 0) {
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  child: Text('Aún no has agregado artículos',
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        color: t.textDim,
                        fontStyle: FontStyle.italic,
                      )),
                );
              }
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 8),
                child: Text('${cart.itemCount} artículo(s) en el carrito',
                    style: GoogleFonts.dmSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: t.textPrimary,
                    )),
              );
            },
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.hotPink.withOpacity(0.06),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Row(
              children: [
                Icon(Icons.info_outline_rounded,
                    color: AppColors.hotPink, size: 18),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                      'Al solicitar cotización, el equipo de RosaFiesta revisará tu evento y te contactará con el precio final.',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        color: t.textMuted,
                        height: 1.35,
                      )),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _summaryRow(
      IconData icon, String label, String value, RfTheme t) {
    return Row(
      children: [
        Icon(icon, color: AppColors.hotPink, size: 18),
        const SizedBox(width: 10),
        Text('$label: ',
            style: GoogleFonts.dmSans(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: t.textMuted,
            )),
        Expanded(
          child: Text(value,
              style: GoogleFonts.dmSans(
                fontSize: 14,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              )),
        ),
      ],
    );
  }

  // ── Step 7: Done Confirmation ───────────────────────────────────────────

  Widget _buildDoneConfirmation(RfTheme t) {
    return Column(
      children: [
        Container(
          width: 90, height: 90,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.teal.withOpacity(0.12),
          ),
          child: const Icon(Icons.check_circle_rounded,
              color: AppColors.teal, size: 50),
        ),
        const SizedBox(height: 20),
        Text('¡Solicitud enviada!',
            style: GoogleFonts.outfit(
              fontSize: 24,
              fontWeight: FontWeight.w800,
              color: t.textPrimary,
            )),
        const SizedBox(height: 10),
        Text(
            'El equipo de RosaFiesta revisará tu evento y te enviará una cotización por correo y WhatsApp.',
            textAlign: TextAlign.center,
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
              height: 1.5,
            )),
        const SizedBox(height: 24),
        GestureDetector(
          onTap: () => Navigator.of(context).pop(),
          child: Container(
            height: 52,
            padding: const EdgeInsets.symmetric(horizontal: 32),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                  colors: [AppColors.violet, AppColors.hotPink]),
              borderRadius: BorderRadius.circular(26),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.home_rounded,
                    color: Colors.white, size: 20),
                const SizedBox(width: 8),
                Text('Volver al inicio',
                    style: GoogleFonts.dmSans(
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    )),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildActionChips(
      List<String> chips, AssistantProvider provider, RfTheme t) {
    // If chips are Yes/No style (2 items), render as big action buttons
    if (chips.length == 2) {
      return Row(
        children: [
          Expanded(
            child: _actionButton(
              chips[0], provider, t,
              filled: false,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _actionButton(
              chips[1], provider, t,
              filled: true,
            ),
          ),
        ],
      );
    }
    // Otherwise render as wrapped chips
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      alignment: WrapAlignment.center,
      children: chips.map((c) {
        return GestureDetector(
          onTap: () => provider.handleChipTap(c),
          child: Container(
            padding:
                const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
            decoration: BoxDecoration(
              color: t.isDark ? t.card : Colors.white,
              borderRadius: BorderRadius.circular(22),
              border:
                  Border.all(color: AppColors.hotPink.withOpacity(0.3)),
              boxShadow: [
                BoxShadow(
                  color: AppColors.hotPink.withOpacity(0.06),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Text(
              c,
              style: GoogleFonts.dmSans(
                fontSize: 16,
                fontWeight: FontWeight.w700,
                color: AppColors.hotPink,
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _actionButton(
    String label,
    AssistantProvider provider,
    RfTheme t, {
    required bool filled,
  }) {
    return GestureDetector(
      onTap: () => provider.handleChipTap(label),
      child: Container(
        height: 56,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          gradient: filled
              ? const LinearGradient(
                  colors: [AppColors.violet, AppColors.hotPink])
              : null,
          color: filled ? null : (t.isDark ? t.card : Colors.white),
          borderRadius: BorderRadius.circular(28),
          border: filled
              ? null
              : Border.all(color: t.borderFaint, width: 1.5),
        ),
        child: Text(
          label,
          style: GoogleFonts.dmSans(
            fontSize: 17,
            fontWeight: FontWeight.w800,
            color: filled ? Colors.white : t.textPrimary,
          ),
        ),
      ),
    );
  }

  Widget _buildProductsCard(
      List<AssistantMessage> sections, RfTheme t) {
    // Merge all products for the grid, keep section titles as tabs
    final tabs = sections
        .where((s) => s.sectionTitle != null)
        .map((s) => s.sectionTitle!)
        .toList();
    if (tabs.isEmpty) tabs.add('Sugerencias');

    return StatefulBuilder(
      builder: (context, setInnerState) {
        // Track which tab is active inside the card
        final activeTab = ValueNotifier<int>(0);
        return ValueListenableBuilder<int>(
          valueListenable: activeTab,
          builder: (context, tabIdx, _) {
            final currentProducts = tabIdx < sections.length
                ? sections[tabIdx].products
                : sections.first.products;

            return Container(
              decoration: BoxDecoration(
                color: t.isDark ? t.card : Colors.white,
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: t.borderFaint),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.04),
                    blurRadius: 16,
                    offset: const Offset(0, 6),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Tab chips
                  if (tabs.length > 1)
                    Padding(
                      padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
                      child: SizedBox(
                        height: 38,
                        child: ListView.separated(
                          scrollDirection: Axis.horizontal,
                          itemCount: tabs.length,
                          separatorBuilder: (_, __) =>
                              const SizedBox(width: 8),
                          itemBuilder: (context, i) {
                            final isActive = tabIdx == i;
                            return GestureDetector(
                              onTap: () => activeTab.value = i,
                              child: Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 16),
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
                                  borderRadius: BorderRadius.circular(19),
                                ),
                                child: Text(
                                  tabs[i],
                                  style: GoogleFonts.dmSans(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w700,
                                    color: isActive
                                        ? Colors.white
                                        : t.textPrimary,
                                  ),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ),
                  // Products — single horizontal row
                  Padding(
                    padding: const EdgeInsets.fromLTRB(12, 12, 0, 16),
                    child: SizedBox(
                      height: 280,
                      child: ListView.separated(
                        scrollDirection: Axis.horizontal,
                        itemCount: currentProducts.length,
                        separatorBuilder: (_, __) =>
                            const SizedBox(width: 10),
                        padding: const EdgeInsets.only(right: 12),
                        itemBuilder: (context, i) =>
                            _productCardCompact(currentProducts[i], t),
                      ),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _productCardCompact(Product product, RfTheme t) {
    final variant =
        product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    final variantId = variant?.id.toString();
    final rating = product.averageRating;
    final reviewCount = product.reviewCount;
    final desc = product.descriptionTemplate ?? '';

    return Container(
      width: 180,
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.04)
            : const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          GestureDetector(
            onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (_) =>
                        ProductDetailScreen(productId: product.id))),
            child: Padding(
              padding: const EdgeInsets.all(6),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(14),
                child: AspectRatio(
                  aspectRatio: 1.3,
                  child: imageUrl != null && imageUrl.isNotEmpty
                      ? Image.network(imageUrl,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                              color: AppColors.hotPink.withOpacity(0.08)))
                      : Container(
                          color: AppColors.hotPink.withOpacity(0.08)),
                ),
              ),
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(10, 0, 10, 10),
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
                    ),
                  ),
                  if (desc.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(
                      desc,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.dmSans(
                        fontSize: 11,
                        color: t.textMuted,
                        height: 1.3,
                      ),
                    ),
                  ],
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      ...List.generate(5, (i) => Icon(
                            Icons.star_rounded,
                            color: i < rating.floor()
                                ? const Color(0xFFFFB800)
                                : const Color(0xFFFFB800).withOpacity(0.25),
                            size: 13,
                          )),
                      const SizedBox(width: 3),
                      Text(
                        '(${_formatCount(reviewCount)})',
                        style: GoogleFonts.dmSans(
                            fontSize: 10, color: t.textMuted),
                      ),
                    ],
                  ),
                  const Spacer(),
                  Row(
                    children: [
                      ShaderMask(
                        shaderCallback: (b) =>
                            const LinearGradient(colors: [
                          AppColors.violet,
                          AppColors.hotPink,
                        ]).createShader(b),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(price.toStringAsFixed(0),
                                style: GoogleFonts.outfit(
                                  fontSize: 18,
                                  fontWeight: FontWeight.w800,
                                  color: Colors.white,
                                  height: 1,
                                )),
                            const SizedBox(width: 2),
                            Padding(
                              padding: const EdgeInsets.only(top: 2),
                              child: Text(r'RD$',
                                  style: GoogleFonts.outfit(
                                    fontSize: 9,
                                    fontWeight: FontWeight.w700,
                                    color: Colors.white,
                                  )),
                            ),
                          ],
                        ),
                      ),
                      const Spacer(),
                      GestureDetector(
                        onTap: () async {
                          final cart = context.read<CartProvider>();
                          await cart.addItem(
                              product.id.toString(), variantId, 1);
                          if (!mounted) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                            content: Text(
                                '${product.nameTemplate} agregado',
                                style: GoogleFonts.dmSans(
                                    fontWeight: FontWeight.w600)),
                            backgroundColor: AppColors.teal,
                            behavior: SnackBarBehavior.floating,
                            duration: const Duration(seconds: 2),
                          ));
                        },
                        child: Container(
                          width: 30, height: 30,
                          decoration: const BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: LinearGradient(colors: [
                              AppColors.violet,
                              AppColors.hotPink,
                            ]),
                          ),
                          child: const Icon(Icons.add_rounded,
                              color: Colors.white, size: 18),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProductCards(List<Product> products, RfTheme t) {
    return SizedBox(
      height: 310,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 4),
        itemCount: products.length,
        separatorBuilder: (_, __) => const SizedBox(width: 12),
        itemBuilder: (context, i) => _productCard(products[i], t),
      ),
    );
  }

  Widget _productCard(Product product, RfTheme t) {
    final variant =
        product.variants.isNotEmpty ? product.variants.first : null;
    final imageUrl = variant?.imageUrl;
    final price = variant?.rentalPrice ?? 0;
    final variantId = variant?.id.toString();
    final rating = product.averageRating;
    final reviewCount = product.reviewCount;

    return Container(
      width: 200,
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 16,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Image
          GestureDetector(
            onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (_) =>
                        ProductDetailScreen(productId: product.id))),
            child: Padding(
              padding: const EdgeInsets.all(8),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: AspectRatio(
                  aspectRatio: 1.15,
                  child: imageUrl != null && imageUrl.isNotEmpty
                      ? Image.network(imageUrl,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                              color: AppColors.hotPink.withOpacity(0.08)))
                      : Container(
                          color: AppColors.hotPink.withOpacity(0.08)),
                ),
              ),
            ),
          ),
          // Info
          Padding(
            padding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  product.nameTemplate,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: GoogleFonts.dmSans(
                    fontSize: 15,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                    height: 1.2,
                  ),
                ),
                const SizedBox(height: 6),
                // Stars + rating
                Row(
                  children: [
                    ...List.generate(5, (i) {
                      final filled = i < rating.floor();
                      return Icon(
                        filled ? Icons.star_rounded : Icons.star_rounded,
                        color: filled
                            ? const Color(0xFFFFB800)
                            : const Color(0xFFFFB800).withOpacity(0.25),
                        size: 16,
                      );
                    }),
                    const SizedBox(width: 4),
                    Text(
                      reviewCount > 0
                          ? '(${_formatCount(reviewCount)})'
                          : '(0)',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        color: t.textMuted,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    ShaderMask(
                      shaderCallback: (b) =>
                          const LinearGradient(colors: [
                        AppColors.violet,
                        AppColors.hotPink,
                      ]).createShader(b),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(price.toStringAsFixed(0),
                              style: GoogleFonts.outfit(
                                fontSize: 20,
                                fontWeight: FontWeight.w800,
                                color: Colors.white,
                                height: 1,
                              )),
                          const SizedBox(width: 3),
                          Padding(
                            padding: const EdgeInsets.only(top: 3),
                            child: Text(r'RD$',
                                style: GoogleFonts.outfit(
                                  fontSize: 10,
                                  fontWeight: FontWeight.w700,
                                  color: Colors.white,
                                )),
                          ),
                        ],
                      ),
                    ),
                    const Spacer(),
                    GestureDetector(
                      onTap: () async {
                        final cart = context.read<CartProvider>();
                        await cart.addItem(
                            product.id.toString(), variantId, 1);
                        if (!mounted) return;
                        ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                          content: Text(
                              '${product.nameTemplate} agregado',
                              style: GoogleFonts.dmSans(
                                  fontWeight: FontWeight.w600)),
                          backgroundColor: AppColors.teal,
                          behavior: SnackBarBehavior.floating,
                          duration: const Duration(seconds: 2),
                        ));
                      },
                      child: Container(
                        width: 34, height: 34,
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: LinearGradient(colors: [
                            AppColors.violet,
                            AppColors.hotPink,
                          ]),
                        ),
                        child: const Icon(Icons.add_rounded,
                            color: Colors.white, size: 20),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatCount(int count) {
    if (count >= 1000) return '${(count / 1000).toStringAsFixed(1)}k';
    return count.toString();
  }

  Widget _buildThinking(RfTheme t) {
    return Column(
      children: [
        const SizedBox(height: 60),
        // Big pulsing orb while thinking
        AnimatedBuilder(
          animation: _orbController,
          builder: (_, __) {
            final scale = 1 + _orbController.value * 0.1;
            final glow = 0.3 + _orbController.value * 0.3;
            return Transform.scale(
              scale: scale,
              child: Container(
                width: 100, height: 100,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: const RadialGradient(
                    colors: [
                      Color(0xFFFFB3E6),
                      AppColors.hotPink,
                      AppColors.violet,
                    ],
                    stops: [0.0, 0.45, 1.0],
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.hotPink.withOpacity(glow),
                      blurRadius: 40,
                      spreadRadius: 10,
                    ),
                  ],
                ),
                child: const Icon(Icons.support_agent_rounded,
                    color: Colors.white, size: 44),
              ),
            );
          },
        ),
        const SizedBox(height: 24),
        Text(
          'Pensando…',
          style: GoogleFonts.dmSans(
            fontSize: 16,
            color: t.textMuted,
          ),
        ),
      ],
    );
  }

  // ── Bottom Controls ──────────────────────────────────────────────────────

  Widget _buildBottomBar(AssistantProvider provider, RfTheme t) {
    final screenW = MediaQuery.of(context).size.width;
    final compact = screenW < 400;

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Input box — replaces buttons when active
          if (_activeBottomIdx == 0)
            _buildChatInput(provider, t)
          else if (_activeBottomIdx == 1 && _isListening)
            _buildListeningBox(t)
          else
          // 3 pill buttons — only visible when no input is active
          Row(
            children: [
              _bottomPill(
                idx: 0,
                icon: Icons.chat_bubble_outline_rounded,
                label: 'Chatear',
                compact: compact,
                onTap: () => setState(() {
                  _activeBottomIdx = _activeBottomIdx == 0 ? -1 : 0;
                  _isListening = false;
                }),
                t: t,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _bottomPill(
                  idx: 1,
                  icon: Icons.mic_rounded,
                  label: 'Hablar',
                  compact: compact,
                  expanded: true,
                  onTap: () => setState(() {
                    if (_activeBottomIdx == 1 && _isListening) {
                      _isListening = false;
                      _activeBottomIdx = -1;
                    } else {
                      _activeBottomIdx = 1;
                      _isListening = true;
                      _transcript = '';
                    }
                  }),
                  t: t,
                  showWaveform: _isListening && _activeBottomIdx == 1,
                ),
              ),
              const SizedBox(width: 10),
              _bottomPill(
                idx: 2,
                icon: Icons.arrow_back_rounded,
                label: 'Volver',
                compact: compact,
                onTap: () => Navigator.of(context).pop(),
                t: t,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildChatInput(AssistantProvider provider, RfTheme t) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(20, 16, 14, 14),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          ConstrainedBox(
            constraints: const BoxConstraints(minHeight: 90),
            child: TextField(
              controller: _chatController,
              autofocus: true,
              maxLines: 5,
              minLines: 3,
              style: GoogleFonts.dmSans(
                  fontSize: 16, color: t.textPrimary, height: 1.5),
              decoration: InputDecoration(
                hintText: 'Escribe tu mensaje...',
                hintStyle: GoogleFonts.dmSans(
                    fontSize: 16, color: t.textDim),
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
                contentPadding: EdgeInsets.zero,
                isCollapsed: true,
              ),
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              // Descartar
              GestureDetector(
                onTap: () {
                  _chatController.clear();
                  setState(() => _activeBottomIdx = -1);
                },
                child: Container(
                  height: 44,
                  padding: const EdgeInsets.symmetric(horizontal: 18),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(22),
                    border: Border.all(color: t.borderFaint),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.close_rounded,
                          color: t.textMuted, size: 18),
                      const SizedBox(width: 6),
                      Text('Descartar',
                          style: GoogleFonts.dmSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                            color: t.textMuted,
                          )),
                    ],
                  ),
                ),
              ),
              const Spacer(),
              // Enviar
              GestureDetector(
                onTap: () {
                  final v = _chatController.text;
                  if (v.trim().isEmpty) return;
                  provider.sendText(v);
                  _chatController.clear();
                  setState(() => _activeBottomIdx = -1);
                },
                child: Container(
                  height: 44,
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [
                      AppColors.violet,
                      AppColors.hotPink,
                    ]),
                    borderRadius: BorderRadius.circular(22),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.send_rounded,
                          color: Colors.white, size: 18),
                      const SizedBox(width: 8),
                      Text('Enviar',
                          style: GoogleFonts.dmSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                            color: Colors.white,
                          )),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildListeningBox(RfTheme t) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(18, 14, 12, 14),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 8, height: 8,
                decoration: const BoxDecoration(
                  color: AppColors.coral,
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Text('Escuchando...',
                  style: GoogleFonts.dmSans(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    color: AppColors.hotPink,
                  )),
              const SizedBox(width: 10),
              // Waveform visual
              ...List.generate(7, (i) {
                final h =
                    4.0 + (i == 3 ? 14 : i % 2 == 0 ? 10 : 6);
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 1.5),
                  child: Container(
                    width: 2.5, height: h,
                    decoration: BoxDecoration(
                      color: AppColors.hotPink.withOpacity(
                          0.3 + (i == 3 ? 0.4 : 0.1)),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                );
              }),
              const Spacer(),
              // Stop button
              GestureDetector(
                onTap: () => setState(() {
                  _isListening = false;
                  _activeBottomIdx = -1;
                }),
                child: Container(
                  width: 36, height: 36,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: AppColors.hotPink.withOpacity(0.12),
                  ),
                  child: const Icon(Icons.stop_rounded,
                      color: AppColors.hotPink, size: 20),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            _transcript.isEmpty
                ? 'Di algo...'
                : _transcript,
            style: GoogleFonts.dmSans(
              fontSize: 16,
              color: _transcript.isEmpty ? t.textDim : t.textPrimary,
              fontStyle: _transcript.isEmpty
                  ? FontStyle.italic
                  : FontStyle.normal,
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }

  Widget _bottomPill({
    required int idx,
    required IconData icon,
    required String label,
    required VoidCallback onTap,
    required RfTheme t,
    bool showWaveform = false,
    bool expanded = false,
    bool compact = false,
  }) {
    final isActive = _activeBottomIdx == idx;
    final isListeningExpanded = showWaveform && _isListening;
    final showLabel = !compact || isActive;

    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 250),
        height: 58,
        padding: EdgeInsets.symmetric(
            horizontal: isListeningExpanded ? 14 : (showLabel ? 18 : 14)),
        decoration: BoxDecoration(
          color: t.isDark ? t.card : Colors.white,
          borderRadius: BorderRadius.circular(29),
          border: Border.all(
            color: isActive ? AppColors.hotPink.withOpacity(0.3) : t.borderFaint,
          ),
        ),
        child: isListeningExpanded
            ? Row(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize:
                    expanded ? MainAxisSize.max : MainAxisSize.min,
                children: [
                  Text('Escuchando',
                      style: GoogleFonts.dmSans(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.hotPink,
                      )),
                  const SizedBox(width: 8),
                  ...List.generate(7, (i) {
                    final h = 6.0 +
                        (i == 3 ? 18 : i % 2 == 0 ? 12 : 7);
                    return Padding(
                      padding:
                          const EdgeInsets.symmetric(horizontal: 1.5),
                      child: Container(
                        width: 3, height: h,
                        decoration: BoxDecoration(
                          color: AppColors.hotPink.withOpacity(
                              0.25 +
                                  (i == 3 ? 0.45 : i % 2 == 0 ? 0.2 : 0)),
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    );
                  }),
                  const SizedBox(width: 8),
                  Container(
                    width: 36, height: 36,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: AppColors.hotPink.withOpacity(0.12),
                    ),
                    child: const Icon(Icons.mic_rounded,
                        color: AppColors.hotPink, size: 20),
                  ),
                ],
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize:
                    expanded ? MainAxisSize.max : MainAxisSize.min,
                children: [
                  Icon(icon, color: t.textPrimary, size: 22),
                  if (showLabel) ...[
                    const SizedBox(width: 8),
                    Text(label,
                        style: GoogleFonts.dmSans(
                          fontSize: 15,
                          fontWeight: FontWeight.w700,
                          color: t.textPrimary,
                        )),
                  ],
                ],
              ),
      ),
    );
  }
}
