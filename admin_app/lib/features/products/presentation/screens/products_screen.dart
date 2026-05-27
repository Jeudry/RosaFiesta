import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../providers/products_provider.dart';

class ProductsScreen extends StatefulWidget {
  const ProductsScreen({super.key});

  @override
  State<ProductsScreen> createState() => _ProductsScreenState();
}

class _ProductsScreenState extends State<ProductsScreen> {
  final _searchController = TextEditingController();
  final _apiClient = ApiClient();
  final Set<String> _selected = {};
  bool _selectMode = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProductsProvider>().loadProducts(refresh: true);
    });
  }

  void _toggleSelect(String id) {
    setState(() {
      if (_selected.contains(id)) {
        _selected.remove(id);
      } else {
        _selected.add(id);
      }
      if (_selected.isEmpty) _selectMode = false;
    });
  }

  Future<void> _bulkDeactivate() async {
    if (_selected.isEmpty) return;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Desactivar productos'),
        content: Text('¿Desactivar ${_selected.length} productos seleccionados?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
          ElevatedButton(style: ElevatedButton.styleFrom(backgroundColor: AppColors.error), onPressed: () => Navigator.pop(ctx, true), child: const Text('Desactivar')),
        ],
      ),
    );
    if (confirmed == true) {
      await _apiClient.bulkDeactivateProducts(_selected.toList(), false);
      if (mounted) {
        setState(() { _selected.clear(); _selectMode = false; });
        context.read<ProductsProvider>().loadProducts(refresh: true);
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Productos desactivados')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ProductsProvider>();

    return AdminScaffold(
      title: _selectMode ? '${_selected.length} seleccionados' : 'Productos',
      actions: [
        if (_selectMode && _selected.isNotEmpty)
          TextButton.icon(
            icon: const Icon(Icons.toggle_off),
            label: const Text('Desactivar'),
            onPressed: _bulkDeactivate,
          ),
        IconButton(
          icon: Icon(_selectMode ? Icons.close : Icons.checklist),
          onPressed: () => setState(() { _selectMode = !_selectMode; if (!_selectMode) _selected.clear(); }),
          tooltip: _selectMode ? 'Cancelar' : 'Seleccionar',
        ),
      ],
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            child: AdminSearchField(
              controller: _searchController,
              hint: 'Buscar productos...',
              onChanged: (q) => provider.search(q),
            ),
          ),
          Expanded(
            child: provider.products.isEmpty && !provider.loading
                ? EmptyState(
                    icon: Icons.inventory_2_outlined,
                    title: 'No hay productos',
                    action: AdminButton(
                      label: 'Agregar Producto',
                      onTap: () => Navigator.pushNamed(context, '/products/new'),
                    ),
                  )
                : RefreshIndicator(
                    onRefresh: () => provider.loadProducts(refresh: true),
                    child: GridView.builder(
                      padding: const EdgeInsets.all(16),
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        childAspectRatio: 0.75,
                        crossAxisSpacing: 12,
                        mainAxisSpacing: 12,
                      ),
                      itemCount: provider.products.length,
                      itemBuilder: (ctx, i) => _ProductCard(
                        product: provider.products[i],
                        selectable: _selectMode,
                        selected: _selected.contains(provider.products[i]['id']),
                        onSelect: () => _toggleSelect(provider.products[i]['id']),
                      ),
                    ),
                  ),
          ),
        ],
      ),
      floatingActionButton: _selectMode
          ? null
          : FloatingActionButton(
              onPressed: () => Navigator.pushNamed(context, '/products/new'),
              backgroundColor: AppColors.primary,
              child: const Icon(Icons.add),
            ),
    );
  }
}

class _ProductCard extends StatelessWidget {
  final Map<String, dynamic> product;
  final bool selectable;
  final bool selected;
  final VoidCallback onSelect;

  const _ProductCard({
    required this.product,
    this.selectable = false,
    this.selected = false,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    final isActive = product['is_active'] != false;
    final stockVal = product['stock_quantity'] ?? 0;
    final isLowStock = stockVal <= (product['low_stock_threshold'] ?? 5);

    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).cardTheme.color ?? Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(
          color: AppColors.primary.withValues(alpha: 0.14),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(18),
        child: InkWell(
          onTap: selectable
              ? onSelect
              : () => Navigator.pushNamed(context, '/products/${product['id']}'),
          child: Stack(
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Stack(
                      fit: StackFit.expand,
                      children: [
                        Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                              colors: [
                                AppColors.primary.withValues(alpha: 0.08),
                                AppColors.violet.withValues(alpha: 0.14),
                              ],
                            ),
                          ),
                          child: product['image_url'] != null && product['image_url'] != ''
                              ? Image.network(product['image_url'], fit: BoxFit.cover)
                              : Center(
                                  child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Container(
                                        padding: const EdgeInsets.all(12),
                                        decoration: BoxDecoration(
                                          color: AppColors.primary.withValues(alpha: 0.12),
                                          shape: BoxShape.circle,
                                        ),
                                        child: const Icon(
                                          Icons.inventory_2_outlined,
                                          size: 32,
                                          color: AppColors.primary,
                                        ),
                                      ),
                                      const SizedBox(height: 6),
                                      Text(
                                        'SIN IMAGEN',
                                        style: GoogleFonts.dmSans(
                                          fontSize: 9,
                                          fontWeight: FontWeight.w800,
                                          letterSpacing: 0.8,
                                          color: AppColors.primary.withValues(alpha: 0.6),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                        ),
                        if (!isActive)
                          Container(
                            color: Colors.black.withValues(alpha: 0.55),
                            child: const Center(
                              child: Text(
                                'INACTIVO',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w900,
                                  fontSize: 11,
                                  letterSpacing: 1.2,
                                ),
                              ),
                            ),
                          ),
                        if (isLowStock && isActive)
                          Positioned(
                            top: 8,
                            right: 8,
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppColors.error.withValues(alpha: 0.9),
                                borderRadius: BorderRadius.circular(20),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withValues(alpha: 0.1),
                                    blurRadius: 4,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: Text(
                                'AGOTADO',
                                style: GoogleFonts.dmSans(
                                  fontSize: 9,
                                  fontWeight: FontWeight.w900,
                                  letterSpacing: 0.8,
                                  color: Colors.white,
                                ),
                              ),
                            ),
                          ),
                        if (product['type'] == 'Sale')
                          Positioned(
                            top: 8,
                            left: 8,
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppColors.teal.withValues(alpha: 0.9),
                                borderRadius: BorderRadius.circular(20),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withValues(alpha: 0.1),
                                    blurRadius: 4,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: Text(
                                'VENTA',
                                style: GoogleFonts.dmSans(
                                  fontSize: 9,
                                  fontWeight: FontWeight.w900,
                                  letterSpacing: 0.8,
                                  color: Colors.white,
                                ),
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(10),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          product['name'] ?? 'Sin Nombre',
                          style: GoogleFonts.dmSans(
                            fontSize: 13, 
                            fontWeight: FontWeight.w800,
                            color: const Color(0xFF2C1A4D),
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            Text(
                              'RD\$${product['rental_price'] ?? 0}',
                              style: GoogleFonts.outfit(
                                fontSize: 15, 
                                fontWeight: FontWeight.w900, 
                                color: AppColors.primary,
                              ),
                            ),
                            const Spacer(),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: (isLowStock ? AppColors.error : AppColors.teal).withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    isLowStock ? Icons.warning_amber_rounded : Icons.check_circle_outline,
                                    size: 11,
                                    color: isLowStock ? AppColors.error : AppColors.teal,
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    isLowStock ? 'Min: $stockVal' : 'Stock: $stockVal',
                                    style: GoogleFonts.dmSans(
                                      fontSize: 10,
                                      fontWeight: FontWeight.w800,
                                      color: isLowStock ? AppColors.error : AppColors.teal,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              if (selectable)
                Positioned(
                  top: 8,
                  left: 8,
                  child: Container(
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      color: selected ? AppColors.primary : Colors.white,
                      shape: BoxShape.circle,
                      border: Border.all(color: selected ? AppColors.primary : Colors.grey, width: 2),
                    ),
                    child: selected
                        ? const Icon(Icons.check, size: 14, color: Colors.white)
                        : null,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
