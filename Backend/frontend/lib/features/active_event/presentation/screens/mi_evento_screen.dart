import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../../events/data/event_model.dart';
import '../active_event_provider.dart';

/// "Mi Evento" — replaces the cart screen.
///
/// Renders the user's draft event as a list of items grouped by category
/// (store-bucket pattern from e-commerce), with collapsible sections,
/// product cards, quantity steppers, and a sticky "Ver detalle del evento"
/// button at the bottom.
class MiEventoScreen extends StatefulWidget {
  const MiEventoScreen({super.key});

  @override
  State<MiEventoScreen> createState() => _MiEventoScreenState();
}

class _MiEventoScreenState extends State<MiEventoScreen> {
  /// Tracks which category sections are expanded.
  final Map<String, bool> _expanded = {};

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ActiveEventProvider>().fetch(force: true);
    });
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final provider = context.watch<ActiveEventProvider>();

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            'Mi evento',
            style: GoogleFonts.outfit(
              fontSize: 22,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [AppColors.violet, AppColors.hotPink],
                    ),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    '${provider.itemCount} ${provider.itemCount == 1 ? "artículo" : "artículos"}',
                    style: GoogleFonts.dmSans(
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      body: _buildBody(provider, t),
    );
  }

  Widget _buildBody(ActiveEventProvider provider, RfTheme t) {
    if (provider.isLoading && provider.event == null) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.error != null && provider.items.isEmpty) {
      return _ErrorState(message: provider.error!, t: t);
    }

    if (provider.items.isEmpty) {
      return _EmptyState(t: t);
    }

    // Group items by category.
    final buckets = _groupByCategory(provider.items);

    return Column(
      children: [
        if (provider.event?.date == null) _DateMissingBanner(t: t),
        Expanded(
          child: RefreshIndicator(
            onRefresh: () => provider.fetch(force: true),
            child: ListView.separated(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 120),
              itemCount: buckets.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final bucket = buckets[index];
                return _CategorySection(
                  category: bucket.category,
                  items: bucket.items,
                  isExpanded: _expanded[bucket.category.id] ?? true,
                  onToggle: () => setState(() {
                    _expanded[bucket.category.id] =
                        !(_expanded[bucket.category.id] ?? true);
                  }),
                  provider: provider,
                  t: t,
                );
              },
            ),
          ),
        ),
        _StickyFooter(provider: provider, t: t),
      ],
    );
  }

  List<_CategoryBucket> _groupByCategory(List<EventItem> items) {
    final Map<String, _CategoryBucket> map = {};
    for (final item in items) {
      final cat = _resolveCategory(item);
      final bucket = map.putIfAbsent(cat.id, () => _CategoryBucket(category: cat));
      bucket.items.add(item);
    }
    return map.values.toList();
  }

  _CategoryInfo _resolveCategory(EventItem item) {
    final catId = item.article?.categoryId ?? '';
    return _categoryInfoMap[catId] ?? _categoryInfoMap['']!;
  }

  static final Map<String, _CategoryInfo> _categoryInfoMap = {
    '': _CategoryInfo('General', Icons.category_rounded, [
      AppColors.violet,
      AppColors.hotPink,
    ]),
    'mesas': _CategoryInfo('Mesas', Icons.table_restaurant_rounded, [
      AppColors.teal,
      AppColors.sky,
    ]),
    'sillas': _CategoryInfo('Sillas', Icons.chair_rounded, [
      AppColors.amber,
      Color(0xFFFF8C00),
    ]),
    'decoracion': _CategoryInfo('Decoración', Icons.palette_rounded, [
      AppColors.hotPink,
      AppColors.coral,
    ]),
    'flores': _CategoryInfo('Florería', Icons.local_florist_rounded, [
      Color(0xFFE91E63),
      Color(0xFFFF5722),
    ]),
    'iluminacion': _CategoryInfo('Iluminación', Icons.lightbulb_rounded, [
      AppColors.amber,
      AppColors.violet,
    ]),
    'textil': _CategoryInfo('Textil y Mantelería', Icons.bed_rounded, [
      AppColors.coral,
      AppColors.violet,
    ]),
    'cristaleria': _CategoryInfo('Cristalería', Icons.wine_bar_rounded, [
      AppColors.teal,
      Color(0xFF00BCD4),
    ]),
    'comida': _CategoryInfo('Dulces y Comida', Icons.cake_rounded, [
      AppColors.hotPink,
      Color(0xFFFF6D00),
    ]),
    'servicios': _CategoryInfo('Servicios', Icons.room_service_rounded, [
      AppColors.violet,
      AppColors.sky,
    ]),
  };
}

class _CategoryInfo {
  final String name;
  final IconData icon;
  final List<Color> colors;
  const _CategoryInfo(this.name, this.icon, this.colors);

  String get id => name;
}

class _CategoryBucket {
  final _CategoryInfo category;
  final List<EventItem> items = [];
  _CategoryBucket({required this.category});
}

// ── Category section (collapsible store-bucket) ───────────────────────────

class _CategorySection extends StatelessWidget {
  final _CategoryInfo category;
  final List<EventItem> items;
  final bool isExpanded;
  final VoidCallback onToggle;
  final ActiveEventProvider provider;
  final RfTheme t;

  const _CategorySection({
    required this.category,
    required this.items,
    required this.isExpanded,
    required this.onToggle,
    required this.provider,
    required this.t,
  });

  double get _sectionTotal =>
      items.fold(0.0, (sum, item) => sum + item.lineTotal);

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Category header (collapsible)
        GestureDetector(
          onTap: onToggle,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            decoration: BoxDecoration(
              color: t.isDark ? t.card : Colors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: t.borderFaint),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.04),
                  blurRadius: 8,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Row(
              children: [
                // Gradient icon circle
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: category.colors,
                    ),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(category.icon, color: Colors.white, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        category.name,
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: t.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '${items.length} ${items.length == 1 ? "artículo" : "artículos"}',
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          color: t.textMuted,
                        ),
                      ),
                    ],
                  ),
                ),
                // Section subtotal
                ShaderMask(
                  shaderCallback: (b) => LinearGradient(
                    colors: category.colors,
                  ).createShader(b),
                  child: Text(
                    'RD\$ ${_sectionTotal.toStringAsFixed(0)}',
                    style: GoogleFonts.outfit(
                      fontSize: 15,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                AnimatedRotation(
                  turns: isExpanded ? 0.5 : 0.0,
                  duration: const Duration(milliseconds: 200),
                  child: Icon(
                    Icons.keyboard_arrow_down_rounded,
                    color: t.textDim,
                    size: 22,
                  ),
                ),
              ],
            ),
          ),
        ),
        // Collapsible item list
        AnimatedCrossFade(
          firstChild: const SizedBox.shrink(),
          secondChild: Padding(
            padding: const EdgeInsets.only(top: 8),
            child: Column(
              children: items.map((item) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: _CartItemTile(
                    item: item,
                    categoryColors: category.colors,
                    t: t,
                    onIncrement: () => provider.updateQuantity(
                      item.id,
                      item.quantity + 1,
                    ),
                    onDecrement: () => provider.updateQuantity(
                      item.id,
                      item.quantity - 1,
                    ),
                    onRemove: () => provider.removeItem(item.id),
                  ),
                );
              }).toList(),
            ),
          ),
          crossFadeState: isExpanded
              ? CrossFadeState.showSecond
              : CrossFadeState.showFirst,
          duration: const Duration(milliseconds: 200),
        ),
      ],
    );
  }
}

// ── Cart item tile (grocery-store style) ──────────────────────────────────

class _CartItemTile extends StatelessWidget {
  final EventItem item;
  final List<Color> categoryColors;
  final RfTheme t;
  final VoidCallback onIncrement;
  final VoidCallback onDecrement;
  final VoidCallback onRemove;

  const _CartItemTile({
    required this.item,
    required this.categoryColors,
    required this.t,
    required this.onIncrement,
    required this.onDecrement,
    required this.onRemove,
  });

  @override
  Widget build(BuildContext context) {
    final imageUrl = item.variant?.imageUrl;
    final articleName = item.article?.nameTemplate ?? 'Artículo';
    final variantName = item.variant?.name;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          // Product image
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SizedBox(
              width: 68,
              height: 68,
              child: imageUrl != null && imageUrl.isNotEmpty
                  ? Image.network(
                      imageUrl,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: categoryColors.first.withValues(alpha: 0.1),
                        child: Icon(
                          Icons.image_not_supported_rounded,
                          color: categoryColors.first,
                        ),
                      ),
                    )
                  : Container(
                      color: categoryColors.first.withValues(alpha: 0.1),
                      child: Icon(
                        Icons.image_not_supported_rounded,
                        color: categoryColors.first,
                      ),
                    ),
            ),
          ),
          const SizedBox(width: 12),
          // Info
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  articleName,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                  ),
                ),
                if (variantName != null && variantName.isNotEmpty) ...[
                  const SizedBox(height: 2),
                  Text(
                    variantName,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.dmSans(
                      fontSize: 12,
                      color: t.textMuted,
                    ),
                  ),
                ],
                const SizedBox(height: 6),
                // Price
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    ShaderMask(
                      shaderCallback: (b) => LinearGradient(
                        colors: categoryColors,
                      ).createShader(b),
                      child: Text(
                        'RD\$ ${item.lineTotal.toStringAsFixed(0)}',
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    const SizedBox(width: 4),
                    Padding(
                      padding: const EdgeInsets.only(bottom: 1),
                      child: Text(
                        '(${item.unitPrice.toStringAsFixed(0)} c/u)',
                        style: GoogleFonts.dmSans(
                          fontSize: 10,
                          color: t.textDim,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          // Quantity stepper
          Column(
            children: [
              _CompactStepper(
                quantity: item.quantity,
                onIncrement: onIncrement,
                onDecrement: onDecrement,
                colors: categoryColors,
                t: t,
              ),
              const SizedBox(height: 6),
              GestureDetector(
                onTap: onRemove,
                child: Text(
                  'Quitar',
                  style: GoogleFonts.dmSans(
                    fontSize: 11,
                    color: AppColors.coral,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _CompactStepper extends StatelessWidget {
  final int quantity;
  final VoidCallback onIncrement;
  final VoidCallback onDecrement;
  final List<Color> colors;
  final RfTheme t;

  const _CompactStepper({
    required this.quantity,
    required this.onIncrement,
    required this.onDecrement,
    required this.colors,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withValues(alpha: 0.06)
            : Colors.black.withValues(alpha: 0.05),
        borderRadius: BorderRadius.circular(22),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _StepBtn(
            icon: Icons.remove_rounded,
            onTap: onDecrement,
            t: t,
            iconSize: 14,
          ),
          SizedBox(
            width: 28,
            child: Text(
              '$quantity',
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
          ),
          _StepBtn(
            icon: Icons.add_rounded,
            onTap: onIncrement,
            t: t,
            iconSize: 14,
            color: colors.first,
          ),
        ],
      ),
    );
  }
}

class _StepBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  final RfTheme t;
  final double iconSize;
  final Color? color;

  const _StepBtn({
    required this.icon,
    required this.onTap,
    required this.t,
    this.iconSize = 16,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(22),
      child: Container(
        padding: const EdgeInsets.all(8),
        child: Icon(
          icon,
          size: iconSize,
          color: color ?? t.textPrimary,
        ),
      ),
    );
  }
}

// ── Sticky footer ──────────────────────────────────────────────────────────

class _StickyFooter extends StatelessWidget {
  final ActiveEventProvider provider;
  final RfTheme t;

  const _StickyFooter({required this.provider, required this.t});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.fromLTRB(
        20,
        14,
        20,
        14 + MediaQuery.of(context).padding.bottom,
      ),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 20,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Summary row
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.violet, AppColors.hotPink],
                  ),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '${provider.lineCount} ${provider.lineCount == 1 ? "categoría" : "categorías"}',
                  style: GoogleFonts.dmSans(
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                '${provider.itemCount} ${provider.itemCount == 1 ? "artículo" : "artículos"}',
                style: GoogleFonts.dmSans(
                  fontSize: 12,
                  color: t.textMuted,
                ),
              ),
              const Spacer(),
              ShaderMask(
                shaderCallback: (b) => AppColors.titleGradient.createShader(b),
                child: Text(
                  'RD\$ ${provider.subtotal.toStringAsFixed(0)}',
                  style: GoogleFonts.outfit(
                    fontSize: 24,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // CTA Button
          GestureDetector(
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text(
                    'Ver detalle del evento: próximamente conectado al flujo de eventos',
                  ),
                ),
              );
            },
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(vertical: 16),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                borderRadius: BorderRadius.circular(28),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.hotPink.withValues(alpha: 0.3),
                    blurRadius: 16,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.visibility_rounded,
                    color: Colors.white,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Ver detalle del evento',
                    style: GoogleFonts.outfit(
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Empty / error / banner states ──────────────────────────────────────────

class _EmptyState extends StatelessWidget {
  final RfTheme t;
  const _EmptyState({required this.t});

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
                    AppColors.violet.withValues(alpha: 0.18),
                    AppColors.hotPink.withValues(alpha: 0.18),
                  ],
                ),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.celebration_rounded,
                color: AppColors.hotPink,
                size: 56,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Tu evento está vacío',
              textAlign: TextAlign.center,
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Explora el catálogo y agrega los artículos que necesitas para tu celebración.',
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

class _ErrorState extends StatelessWidget {
  final String message;
  final RfTheme t;
  const _ErrorState({required this.message, required this.t});

  @override
  Widget build(BuildContext context) {
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
              message,
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(fontSize: 14, color: t.textMuted),
            ),
            const SizedBox(height: 16),
            RfLuxeButton(
              label: 'Reintentar',
              onTap: () => context
                  .read<ActiveEventProvider>()
                  .fetch(force: true),
            ),
          ],
        ),
      ),
    );
  }
}

class _DateMissingBanner extends StatelessWidget {
  final RfTheme t;
  const _DateMissingBanner({required this.t});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.amber.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.amber.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          const Icon(Icons.calendar_today_rounded,
              color: AppColors.amber, size: 20),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              'Aún no has elegido una fecha. La disponibilidad real se valida cuando confirmas tu evento.',
              style: GoogleFonts.dmSans(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: t.textPrimary,
                height: 1.35,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Totals bar ─────────────────────────────────────────────────────────────

class _TotalsBar extends StatelessWidget {
  final ActiveEventProvider provider;
  final RfTheme t;
  const _TotalsBar({required this.provider, required this.t});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.fromLTRB(
        20,
        16,
        20,
        16 + MediaQuery.of(context).padding.bottom,
      ),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.06),
            blurRadius: 16,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Text(
                'Estimado',
                style: GoogleFonts.dmSans(
                  fontSize: 13,
                  color: t.textMuted,
                ),
              ),
              const Spacer(),
              ShaderMask(
                shaderCallback: (b) => const LinearGradient(colors: [
                  AppColors.violet,
                  AppColors.hotPink,
                ]).createShader(b),
                child: Text(
                  'RD\$ ${provider.subtotal.toStringAsFixed(0)}',
                  style: GoogleFonts.outfit(
                    fontSize: 26,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            '${provider.itemCount} ${provider.itemCount == 1 ? "artículo" : "artículos"} · ${provider.lineCount} ${provider.lineCount == 1 ? "línea" : "líneas"}',
            style: GoogleFonts.dmSans(
              fontSize: 12,
              color: t.textDim,
            ),
          ),
          const SizedBox(height: 16),
          RfLuxeButton(
            label: 'Solicitar cotización',
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text(
                      'Solicitar cotización: próximamente conectado al flujo de eventos'),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
