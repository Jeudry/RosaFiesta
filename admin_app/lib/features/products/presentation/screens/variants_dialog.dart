import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';

class VariantsDialog extends StatefulWidget {
  final String articleId;

  const VariantsDialog({super.key, required this.articleId});

  @override
  State<VariantsDialog> createState() => _VariantsDialogState();
}

class _VariantsDialogState extends State<VariantsDialog> {
  List<dynamic> _variants = [];
  bool _loading = true;
  final _apiClient = ApiClient();

  @override
  void initState() {
    super.initState();
    _loadVariants();
  }

  Future<void> _loadVariants() async {
    setState(() => _loading = true);
    try {
      final resp = await _apiClient.getArticleVariants(widget.articleId);
      final data = resp.data['data'] as List? ?? [];
      if (mounted) setState(() { _variants = data; _loading = false; });
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _addVariant() async {
    final nameCtrl = TextEditingController();
    final skuCtrl = TextEditingController();
    final priceCtrl = TextEditingController();
    final stockCtrl = TextEditingController(text: '1');
    bool isActive = true;

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          title: Text('Nueva Variante', style: GoogleFonts.outfit()),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(controller: nameCtrl, decoration: const InputDecoration(labelText: 'Nombre (ej: Grande)')),
                const SizedBox(height: 8),
                TextField(controller: skuCtrl, decoration: const InputDecoration(labelText: 'SKU')),
                const SizedBox(height: 8),
                TextField(controller: priceCtrl, decoration: const InputDecoration(labelText: 'Precio alquiler', prefixText: 'RD\$ '), keyboardType: TextInputType.number),
                const SizedBox(height: 8),
                TextField(controller: stockCtrl, decoration: const InputDecoration(labelText: 'Stock'), keyboardType: TextInputType.number),
                const SizedBox(height: 8),
                SwitchListTile(
                  title: const Text('Activo'),
                  value: isActive,
                  onChanged: (v) => setDialogState(() => isActive = v),
                  contentPadding: EdgeInsets.zero,
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
            ElevatedButton(
              onPressed: () => Navigator.pop(ctx, {
                'name': nameCtrl.text,
                'sku': skuCtrl.text,
                'rental_price': double.tryParse(priceCtrl.text) ?? 0,
                'stock': int.tryParse(stockCtrl.text) ?? 1,
                'is_active': isActive,
              }),
              child: const Text('Crear'),
            ),
          ],
        ),
      ),
    );

    if (result != null && mounted) {
      await _apiClient.createArticleVariant(widget.articleId, result);
      _loadVariants();
    }
  }

  Future<void> _deleteVariant(String variantId) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Eliminar Variante'),
        content: const Text('¿Eliminar esta variante?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
          ElevatedButton(style: ElevatedButton.styleFrom(backgroundColor: AppColors.error), onPressed: () => Navigator.pop(ctx, true), child: const Text('Eliminar')),
        ],
      ),
    );
    if (confirmed == true && mounted) {
      await _apiClient.deleteArticleVariant(variantId);
      _loadVariants();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        width: 600,
        height: 500,
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Variantes del Producto', style: GoogleFonts.outfit(fontSize: 20, fontWeight: FontWeight.w600)),
                IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.pop(context)),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(child: Text('SKU', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, fontSize: 12))),
                Expanded(child: Text('Nombre', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, fontSize: 12))),
                Expanded(child: Text('Precio', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, fontSize: 12))),
                Expanded(child: Text('Stock', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, fontSize: 12))),
                SizedBox(width: 80),
              ],
            ),
            const Divider(),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator())
                  : _variants.isEmpty
                      ? Center(child: Text('Sin variantes', style: GoogleFonts.dmSans(color: Colors.grey)))
                      : ListView.builder(
                          itemCount: _variants.length,
                          itemBuilder: (ctx, i) {
                            final v = _variants[i];
                            return Padding(
                              padding: const EdgeInsets.symmetric(vertical: 6),
                              child: Row(
                                children: [
                                  Expanded(child: Text(v['sku'] ?? '-', style: GoogleFonts.dmSans(fontSize: 13))),
                                  Expanded(child: Text(v['name'] ?? '-', style: GoogleFonts.dmSans(fontSize: 13))),
                                  Expanded(child: Text('RD\$${v['rental_price'] ?? 0}', style: GoogleFonts.dmSans(fontSize: 13))),
                                  Expanded(child: Text('${v['stock'] ?? 0}', style: GoogleFonts.dmSans(fontSize: 13))),
                                  SizedBox(
                                    width: 80,
                                    child: Row(
                                      children: [
                                        Container(
                                          width: 8, height: 8,
                                          decoration: BoxDecoration(
                                            shape: BoxShape.circle,
                                            color: v['is_active'] == true ? Colors.green : Colors.grey,
                                          ),
                                        ),
                                        const Spacer(),
                                        IconButton(
                                          icon: const Icon(Icons.delete_outline, size: 18),
                                          color: AppColors.error,
                                          onPressed: () => _deleteVariant(v['id']),
                                        ),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                            );
                          },
                        ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: AdminButton(label: '+ Agregar Variante', onTap: _addVariant),
            ),
          ],
        ),
      ),
    );
  }
}
