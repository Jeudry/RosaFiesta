import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

import '../../../active_event/presentation/active_event_provider.dart';
import '../../data/product_models.dart';

/// Modal bottom sheet that lets the user pick a quantity and add the
/// product to their active (draft) event.
///
/// Designed for narrow viewports — the product detail screen's bottom bar
/// no longer carries a quantity stepper because it didn't fit on small
/// screens. The full-width CTA there opens this sheet instead.
///
/// Usage:
/// ```dart
/// AddToEventSheet.show(
///   context: context,
///   product: product,
///   variant: selectedVariant,
/// );
/// ```
class AddToEventSheet extends StatefulWidget {
  final Product product;
  final ProductVariant? variant;

  const AddToEventSheet({
    super.key,
    required this.product,
    required this.variant,
  });

  /// Convenience launcher. Opens the sheet, returns once it closes.
  static Future<void> show({
    required BuildContext context,
    required Product product,
    required ProductVariant? variant,
  }) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black.withOpacity(0.55),
      builder: (_) => AddToEventSheet(
        product: product,
        variant: variant,
      ),
    );
  }

  @override
  State<AddToEventSheet> createState() => _AddToEventSheetState();
}

class _AddToEventSheetState extends State<AddToEventSheet> {
  int _qty = 1;
  bool _submitting = false;

  void _inc() => setState(() => _qty++);
  void _dec() {
    if (_qty > 1) setState(() => _qty--);
  }

  Future<void> _submit() async {
    if (widget.variant == null || _submitting) return;
    setState(() => _submitting = true);

    final messenger = ScaffoldMessenger.of(context);
    final navigator = Navigator.of(context);
    final provider = context.read<ActiveEventProvider>();
    final productName = widget.product.nameTemplate;

    try {
      await provider.addItem(
        widget.product,
        variant: widget.variant,
        quantity: _qty,
      );
      navigator.pop();
      messenger.showSnackBar(SnackBar(
        content: Row(children: [
          const Icon(Icons.celebration_rounded,
              color: Colors.white, size: 18),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              '$_qty × $productName en tu evento',
              style: GoogleFonts.dmSans(fontWeight: FontWeight.w600),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ]),
        backgroundColor: AppColors.teal,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12)),
      ));
    } catch (e) {
      if (!mounted) return;
      setState(() => _submitting = false);
      messenger.showSnackBar(SnackBar(
        content: Text('Error: $e'),
        backgroundColor: AppColors.coral,
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final variant = widget.variant;
    final unitPrice = variant?.rentalPrice ?? 0;
    final total = unitPrice * _qty;
    final imageUrl = variant?.imageUrl;
    final variantLabel = variant?.name;

    return Container(
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        boxShadow: [
          BoxShadow(
            color: AppColors.hotPink.withOpacity(0.08),
            blurRadius: 32,
            offset: const Offset(0, -8),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: EdgeInsets.fromLTRB(
            24, 12, 24,
            20 + MediaQuery.of(context).viewInsets.bottom,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Drag handle
              Center(
                child: Container(
                  width: 44, height: 4,
                  decoration: BoxDecoration(
                    color: t.textDim.withOpacity(0.3),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 18),
              // Product header card
              _ProductHeader(
                imageUrl: imageUrl,
                name: widget.product.nameTemplate,
                variantLabel: variantLabel,
                unitPrice: unitPrice,
                t: t,
              ),
              const SizedBox(height: 24),
              Row(
                children: [
                  Text(
                    'Cantidad',
                    style: GoogleFonts.dmSans(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: t.textMuted,
                      letterSpacing: 0.4,
                    ),
                  ),
                  const Spacer(),
                  // Compact pill-shaped quantity stepper
                  _QtyPicker(
                    quantity: _qty,
                    onIncrement: _inc,
                    onDecrement: _dec,
                    t: t,
                  ),
                ],
              ),
              const SizedBox(height: 22),
              // Live total — gradient
              _TotalRow(total: total, t: t),
              const SizedBox(height: 22),
              // Primary CTA
              RfLuxeButton(
                label: _submitting ? 'Agregando…' : 'Agregar a mi evento',
                onTap: _submitting ? () {} : _submit,
                loading: _submitting,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Product header ─────────────────────────────────────────────────────────

class _ProductHeader extends StatelessWidget {
  final String? imageUrl;
  final String name;
  final String? variantLabel;
  final double unitPrice;
  final RfTheme t;

  const _ProductHeader({
    required this.imageUrl,
    required this.name,
    required this.variantLabel,
    required this.unitPrice,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.04)
            : Colors.black.withOpacity(0.025),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(14),
            child: SizedBox(
              width: 60, height: 60,
              child: imageUrl != null && imageUrl!.isNotEmpty
                  ? Image.network(
                      imageUrl!,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: AppColors.hotPink.withOpacity(0.08),
                        child: const Icon(
                          Icons.image_not_supported_rounded,
                          color: AppColors.hotPink,
                        ),
                      ),
                    )
                  : Container(
                      color: AppColors.hotPink.withOpacity(0.08),
                      child: const Icon(
                        Icons.image_not_supported_rounded,
                        color: AppColors.hotPink,
                      ),
                    ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: GoogleFonts.outfit(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                    color: t.textPrimary,
                    height: 1.2,
                  ),
                ),
                if (variantLabel != null && variantLabel!.isNotEmpty) ...[
                  const SizedBox(height: 2),
                  Text(
                    variantLabel!,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.dmSans(
                      fontSize: 12,
                      color: t.textMuted,
                    ),
                  ),
                ],
                const SizedBox(height: 4),
                Text(
                  'RD\$ ${unitPrice.toStringAsFixed(0)} c/u',
                  style: GoogleFonts.dmSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: t.textDim,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ── Quantity picker ────────────────────────────────────────────────────────

/// Compact pill-shaped quantity stepper. Sits inline next to the
/// "Cantidad" label so it doesn't dominate the modal vertically.
class _QtyPicker extends StatelessWidget {
  final int quantity;
  final VoidCallback onIncrement;
  final VoidCallback onDecrement;
  final RfTheme t;

  const _QtyPicker({
    required this.quantity,
    required this.onIncrement,
    required this.onDecrement,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    final canDecrement = quantity > 1;
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.05)
            : Colors.black.withOpacity(0.04),
        borderRadius: BorderRadius.circular(40),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _QtyCircleBtn(
            icon: Icons.remove_rounded,
            enabled: canDecrement,
            onTap: onDecrement,
            t: t,
          ),
          SizedBox(
            width: 56,
            child: Text(
              '$quantity',
              textAlign: TextAlign.center,
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: t.textPrimary,
                height: 1,
              ),
            ),
          ),
          _QtyCircleBtn(
            icon: Icons.add_rounded,
            enabled: true,
            onTap: onIncrement,
            t: t,
          ),
        ],
      ),
    );
  }
}

class _QtyCircleBtn extends StatelessWidget {
  final IconData icon;
  final bool enabled;
  final VoidCallback onTap;
  final RfTheme t;

  const _QtyCircleBtn({
    required this.icon,
    required this.enabled,
    required this.onTap,
    required this.t,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: enabled ? onTap : null,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: enabled
              ? const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.violet, AppColors.hotPink],
                )
              : null,
          color: enabled
              ? null
              : (t.isDark
                  ? Colors.white.withOpacity(0.04)
                  : Colors.black.withOpacity(0.04)),
          boxShadow: enabled
              ? [
                  BoxShadow(
                    color: AppColors.hotPink.withOpacity(0.28),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ]
              : null,
        ),
        child: Icon(
          icon,
          color: enabled
              ? Colors.white
              : t.textDim.withOpacity(0.5),
          size: 18,
        ),
      ),
    );
  }
}

// ── Total row ──────────────────────────────────────────────────────────────

class _TotalRow extends StatelessWidget {
  final double total;
  final RfTheme t;
  const _TotalRow({required this.total, required this.t});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Text(
          'Total',
          style: GoogleFonts.dmSans(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: t.textMuted,
          ),
        ),
        ShaderMask(
          shaderCallback: (b) => const LinearGradient(colors: [
            AppColors.violet,
            AppColors.hotPink,
          ]).createShader(b),
          child: Text(
            'RD\$ ${total.toStringAsFixed(0)}',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w800,
              color: Colors.white,
              height: 1,
            ),
          ),
        ),
      ],
    );
  }
}
