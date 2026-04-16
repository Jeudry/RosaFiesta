import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../providers/products_provider.dart';
import 'variants_dialog.dart';

class ProductEditScreen extends StatefulWidget {
  final String? productId;

  const ProductEditScreen({super.key, this.productId});

  @override
  State<ProductEditScreen> createState() => _ProductEditScreenState();
}

class _ProductEditScreenState extends State<ProductEditScreen> {
  Map<String, dynamic>? _product;
  bool _loading = true;
  bool _saving = false;
  bool _isNew = true;

  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _rentalPriceController = TextEditingController();
  final _salePriceController = TextEditingController();
  final _stockController = TextEditingController();
  final _lowStockController = TextEditingController();
  String _type = 'Rental';
  String? _categoryId;
  bool _isActive = true;

  @override
  void initState() {
    super.initState();
    _isNew = widget.productId == null;
    if (_isNew) {
      _loading = false;
      _lowStockController.text = '5';
    } else {
      _loadProduct();
    }
  }

  Future<void> _loadProduct() async {
    final product = await context.read<ProductsProvider>().getProduct(widget.productId!);
    if (mounted && product != null) {
      setState(() {
        _product = product;
        _nameController.text = product['name'] ?? '';
        _descriptionController.text = product['description'] ?? '';
        _rentalPriceController.text = product['rental_price']?.toString() ?? '';
        _salePriceController.text = product['sale_price']?.toString() ?? '';
        _stockController.text = product['stock_quantity']?.toString() ?? '';
        _lowStockController.text = product['low_stock_threshold']?.toString() ?? '5';
        _type = product['type'] ?? 'Rental';
        _categoryId = product['category_id'];
        _isActive = product['is_active'] != false;
        _loading = false;
      });
    }
  }

  Future<void> _save() async {
    setState(() => _saving = true);

    final data = {
      'name': _nameController.text,
      'description': _descriptionController.text,
      'rental_price': int.tryParse(_rentalPriceController.text) ?? 0,
      'sale_price': int.tryParse(_salePriceController.text) ?? 0,
      'stock_quantity': int.tryParse(_stockController.text) ?? 0,
      'low_stock_threshold': int.tryParse(_lowStockController.text) ?? 5,
      'type': _type,
      'category_id': _categoryId,
      'is_active': _isActive,
    };

    bool success;
    if (_isNew) {
      final id = await context.read<ProductsProvider>().createProduct(data);
      success = id != null;
    } else {
      success = await context.read<ProductsProvider>().updateProduct(widget.productId!, data);
    }

    if (mounted) {
      setState(() => _saving = false);
      if (success) {
        Navigator.pop(context);
      }
    }
  }

  Future<void> _toggleActive() async {
    final newState = !_isActive;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(newState ? 'Activar Producto' : 'Desactivar Producto'),
        content: Text(newState
            ? '¿Activar este producto? Estará visible en el catálogo.'
            : '¿Desactivar este producto? No estará visible en el catálogo.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: Text(newState ? 'Activar' : 'Desactivar'),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await context.read<ProductsProvider>().toggleProduct(widget.productId!, newState);
      setState(() => _isActive = newState);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: _isNew ? 'Nuevo Producto' : 'Editar Producto',
      showBack: true,
      actions: [
        if (!_isNew)
          IconButton(
            icon: Icon(_isActive ? Icons.visibility_off : Icons.visibility),
            onPressed: _toggleActive,
            tooltip: _isActive ? 'Desactivar' : 'Activar',
          ),
        TextButton(
          onPressed: _saving ? null : _save,
          child: _saving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Guardar'),
        ),
      ],
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Información Básica', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          AdminTextField(label: 'Nombre', controller: _nameController),
                          const SizedBox(height: 12),
                          AdminTextField(label: 'Descripción', controller: _descriptionController, lines: 3),
                          const SizedBox(height: 12),
                          Text('Tipo', style: GoogleFonts.dmSans(fontSize: 13, fontWeight: FontWeight.w500)),
                          const SizedBox(height: 8),
                          Row(
                            children: [
                              ChoiceChip(
                                label: const Text('Alquiler'),
                                selected: _type == 'Rental',
                                onSelected: (_) => setState(() => _type = 'Rental'),
                              ),
                              const SizedBox(width: 8),
                              ChoiceChip(
                                label: const Text('Venta'),
                                selected: _type == 'Sale',
                                onSelected: (_) => setState(() => _type = 'Sale'),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Precios', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          AdminTextField(
                            label: 'Precio de alquiler (RD\$)',
                            controller: _rentalPriceController,
                            keyboardType: TextInputType.number,
                          ),
                          const SizedBox(height: 12),
                          AdminTextField(
                            label: 'Precio de venta (RD\$)',
                            controller: _salePriceController,
                            keyboardType: TextInputType.number,
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Inventario', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          AdminTextField(
                            label: 'Cantidad en stock',
                            controller: _stockController,
                            keyboardType: TextInputType.number,
                          ),
                          const SizedBox(height: 12),
                          AdminTextField(
                            label: 'Umbral de stock bajo',
                            controller: _lowStockController,
                            keyboardType: TextInputType.number,
                            hint: 'Cantidad mínima para mostrar alerta',
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  if (!_isNew)
                    SizedBox(
                      width: double.infinity,
                      child: OutlinedButton.icon(
                        onPressed: () {
                          showDialog(context: context, builder: (_) => VariantsDialog(articleId: widget.productId!));
                        },
                        icon: const Icon(Icons.layers),
                        label: const Text('Gestionar Variantes'),
                      ),
                    ),

                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    child: AdminButton(label: _isNew ? 'Crear Producto' : 'Guardar Cambios', onTap: _save, loading: _saving),
                  ),

                  if (!_isNew) ...[
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: OutlinedButton(
                        onPressed: () async {
                          final confirmed = await showDialog<bool>(
                            context: context,
                            builder: (ctx) => AlertDialog(
                              title: const Text('Eliminar Producto'),
                              content: const Text('¿Eliminar este producto permanentemente?'),
                              actions: [
                                TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                ElevatedButton(
                                  onPressed: () => Navigator.pop(ctx, true),
                                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
                                  child: const Text('Eliminar'),
                                ),
                              ],
                            ),
                          );
                          if (confirmed == true) {
                            await context.read<ProductsProvider>().deleteProduct(widget.productId!);
                            if (context.mounted) Navigator.pop(context);
                          }
                        },
                        style: OutlinedButton.styleFrom(foregroundColor: AppColors.error, side: const BorderSide(color: AppColors.error)),
                        child: const Text('Eliminar Producto'),
                      ),
                    ),
                  ],
                ],
              ),
            ),
    );
  }
}
