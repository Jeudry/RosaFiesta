import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../suppliers_provider.dart';
import '../../data/supplier_model.dart';
import 'supplier_form_screen.dart';

class SuppliersListScreen extends StatelessWidget {
  const SuppliersListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.base,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: t.textPrimary),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          'Proveedores',
          style: GoogleFonts.outfit(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.add_rounded, color: AppColors.hotPink),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(
                builder: (_) => const SupplierFormScreen(),
              ),
            ),
          ),
        ],
      ),
      body: Consumer<SuppliersProvider>(
        builder: (context, provider, _) {
          if (provider.loading && provider.suppliers.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.suppliers.isEmpty) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.inventory_2_outlined, color: t.textDim, size: 64),
                  const SizedBox(height: 16),
                  Text(
                    'Sin proveedores aún',
                    style: GoogleFonts.dmSans(
                      fontSize: 16,
                      color: t.textMuted,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextButton.icon(
                    onPressed: () => Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => const SupplierFormScreen(),
                      ),
                    ),
                    icon: const Icon(Icons.add),
                    label: const Text('Añadir proveedor'),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: provider.loadSuppliers,
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: provider.suppliers.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, i) => _SupplierCard(
                supplier: provider.suppliers[i],
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => SupplierFormScreen(
                      supplier: provider.suppliers[i],
                    ),
                  ),
                ),
                onDelete: () => _confirmDelete(context, provider, provider.suppliers[i]),
              ),
            ),
          );
        },
      ),
    );
  }

  void _confirmDelete(BuildContext context, SuppliersProvider provider, Supplier supplier) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Eliminar proveedor'),
        content: Text('¿Eliminar "${supplier.name}"? Esta acción no se puede deshacer.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              provider.deleteSupplier(supplier.id);
              Navigator.of(ctx).pop();
            },
            child: Text('Eliminar', style: TextStyle(color: AppColors.coral)),
          ),
        ],
      ),
    );
  }
}

class _SupplierCard extends StatelessWidget {
  final Supplier supplier;
  final VoidCallback onTap;
  final VoidCallback onDelete;

  const _SupplierCard({
    required this.supplier,
    required this.onTap,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: t.isDark ? t.card.withValues(alpha: 0.8) : Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: t.borderFaint),
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Center(
                child: Text(
                  supplier.name.isNotEmpty ? supplier.name[0].toUpperCase() : '?',
                  style: GoogleFonts.outfit(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    supplier.name,
                    style: GoogleFonts.dmSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: t.textPrimary,
                    ),
                  ),
                  if (supplier.contactName.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(
                      supplier.contactName,
                      style: GoogleFonts.dmSans(
                        fontSize: 13,
                        color: t.textMuted,
                      ),
                    ),
                  ],
                  if (supplier.phone.isNotEmpty || supplier.email.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(
                      supplier.phone.isNotEmpty ? supplier.phone : supplier.email,
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        color: t.textDim,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            IconButton(
              icon: Icon(Icons.delete_outline_rounded, color: AppColors.coral.withValues(alpha: 0.7)),
              onPressed: onDelete,
            ),
          ],
        ),
      ),
    );
  }
}
