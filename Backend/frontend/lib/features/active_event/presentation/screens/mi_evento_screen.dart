import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../../events/data/event_model.dart';
import '../active_event_provider.dart';

/// "Mi Evento" — replaces the cart screen.
///
/// Renders the user's draft event as a list of items + a totals section
/// at the bottom. The CTA at the bottom is "Solicitar cotización" (not
/// "Pay") because RosaFiesta is rental-first and the user is building an
/// event, not buying a product.
class MiEventoScreen extends StatefulWidget {
  const MiEventoScreen({super.key});

  @override
  State<MiEventoScreen> createState() => _MiEventoScreenState();
}

class _MiEventoScreenState extends State<MiEventoScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      // Always refresh on entry so the screen reflects the latest server state.
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

    return Column(
      children: [
        if (provider.event?.date == null)
          _DateMissingBanner(t: t),
        Expanded(
          child: RefreshIndicator(
            onRefresh: () => provider.fetch(force: true),
            child: ListView.separated(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
              itemCount: provider.items.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final item = provider.items[index];
                return _LineTile(
                  item: item,
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
                );
              },
            ),
          ),
        ),
        _TotalsBar(provider: provider, t: t),
      ],
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
                    AppColors.violet.withOpacity(0.18),
                    AppColors.hotPink.withOpacity(0.18),
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
        color: AppColors.amber.withOpacity(0.12),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.amber.withOpacity(0.3)),
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

// ── Line tile ──────────────────────────────────────────────────────────────

class _LineTile extends StatelessWidget {
  final EventItem item;
  final RfTheme t;
  final VoidCallback onIncrement;
  final VoidCallback onDecrement;
  final VoidCallback onRemove;

  const _LineTile({
    required this.item,
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
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SizedBox(
              width: 72,
              height: 72,
              child: imageUrl != null && imageUrl.isNotEmpty
                  ? Image.network(
                      imageUrl,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: AppColors.hotPink.withOpacity(0.08),
                        child: const Icon(Icons.image_not_supported_rounded,
                            color: AppColors.hotPink),
                      ),
                    )
                  : Container(
                      color: AppColors.hotPink.withOpacity(0.08),
                      child: const Icon(Icons.image_not_supported_rounded,
                          color: AppColors.hotPink),
                    ),
            ),
          ),
          const SizedBox(width: 12),
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
                const SizedBox(height: 8),
                ShaderMask(
                  shaderCallback: (b) => const LinearGradient(colors: [
                    AppColors.violet,
                    AppColors.hotPink,
                  ]).createShader(b),
                  child: Text(
                    'RD\$ ${item.lineTotal.toStringAsFixed(0)}',
                    style: GoogleFonts.outfit(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          _QuantityStepper(
            quantity: item.quantity,
            onIncrement: onIncrement,
            onDecrement: onDecrement,
            t: t,
          ),
          IconButton(
            icon: Icon(Icons.close_rounded, color: t.textDim, size: 20),
            onPressed: onRemove,
            tooltip: 'Quitar',
            padding: EdgeInsets.zero,
            constraints: const BoxConstraints(
              minWidth: 30,
              minHeight: 30,
            ),
          ),
        ],
      ),
    );
  }
}

class _QuantityStepper extends StatelessWidget {
  final int quantity;
  final VoidCallback onIncrement;
  final VoidCallback onDecrement;
  final RfTheme t;

  const _QuantityStepper({
    required this.quantity,
    required this.onIncrement,
    required this.onDecrement,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.04)
            : Colors.black.withOpacity(0.04),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _StepBtn(
            icon: Icons.remove_rounded,
            onTap: onDecrement,
            t: t,
          ),
          SizedBox(
            width: 24,
            child: Text(
              '$quantity',
              textAlign: TextAlign.center,
              style: GoogleFonts.dmSans(
                fontSize: 14,
                fontWeight: FontWeight.w700,
                color: t.textPrimary,
              ),
            ),
          ),
          _StepBtn(
            icon: Icons.add_rounded,
            onTap: onIncrement,
            t: t,
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
  const _StepBtn({required this.icon, required this.onTap, required this.t});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Icon(icon, size: 16, color: t.textPrimary),
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
            color: Colors.black.withOpacity(0.06),
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
