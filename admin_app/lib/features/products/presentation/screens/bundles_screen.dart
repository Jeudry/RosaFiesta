import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class BundlesScreen extends StatefulWidget {
  const BundlesScreen({super.key});

  @override
  State<BundlesScreen> createState() => _BundlesScreenState();
}

class _BundlesScreenState extends State<BundlesScreen> {
  List<dynamic> _bundles = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadBundles();
  }

  Future<void> _loadBundles() async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.getBundles();
      _bundles = response.data['data'] ?? [];
    } catch (e) {
      _bundles = [];
    }
    setState(() => _loading = false);
  }

  Future<void> _createBundle() async {
    final nameController = TextEditingController();
    final descController = TextEditingController();
    final priceController = TextEditingController();
    final discountController = TextEditingController(text: '10');

    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Nuevo Bundle'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              AdminTextField(label: 'Nombre', controller: nameController),
              const SizedBox(height: 12),
              AdminTextField(label: 'Descripción', controller: descController, lines: 2),
              const SizedBox(height: 12),
              AdminTextField(label: 'Precio total (RD\$)', controller: priceController, keyboardType: TextInputType.number),
              const SizedBox(height: 12),
              AdminTextField(label: 'Descuento (%)', controller: discountController, keyboardType: TextInputType.number),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, {
              'name': nameController.text,
              'description': descController.text,
              'total_price': int.tryParse(priceController.text) ?? 0,
              'discount_percent': int.tryParse(discountController.text) ?? 10,
            }),
            child: const Text('Crear'),
          ),
        ],
      ),
    );

    if (result != null) {
      await apiClient.createBundle(result);
      await _loadBundles();
    }
  }

  Future<void> _showBundleItems(Map<String, dynamic> bundle) async {
    final bundleId = bundle['id'];
    final bundleData = await apiClient.getBundle(bundleId);
    final bundleObj = bundleData.data['data'] ?? bundleData.data;
    final bundleItems = (bundleObj['items'] as List?) ?? [];
    final currentItems = bundleItems.map((e) => e['article_id'] as String).toSet();
    final articlesResp = await apiClient.getProducts(page: 1, limit: 100);
    final availableArticles = (articlesResp.data['data'] as List?) ?? [];

    final selected = <String>{...currentItems};

    if (!mounted) return;
    final result = await showDialog<Set<String>>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          title: Text('Items: ${bundle['name']}'),
          content: SizedBox(
            width: double.maxFinite,
            height: 400,
            child: ListView.builder(
              itemCount: availableArticles.length,
              itemBuilder: (ctx, i) {
                final article = availableArticles[i];
                final articleId = article['id'] as String;
                final isSelected = selected.contains(articleId);
                return CheckboxListTile(
                  title: Text(article['name'] ?? ''),
                  subtitle: Text('RD\$${article['rental_price'] ?? 0}'),
                  value: isSelected,
                  onChanged: (val) {
                    setDialogState(() {
                      if (val == true) {
                        selected.add(articleId);
                      } else {
                        selected.remove(articleId);
                      }
                    });
                  },
                );
              },
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
            ElevatedButton(
              onPressed: () => Navigator.pop(ctx, selected),
              child: const Text('Guardar'),
            ),
          ],
        ),
      ),
    );

    if (result != null) {
      // Remove items that were deselected
      for (final articleId in currentItems) {
        if (!result.contains(articleId)) {
          await apiClient.removeBundleItem(bundleId, articleId);
        }
      }
      // Add newly selected items
      for (final articleId in result) {
        if (!currentItems.contains(articleId)) {
          await apiClient.addBundleItem(bundleId, {'article_id': articleId, 'quantity': 1});
        }
      }
      await _loadBundles();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Bundles',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _bundles.isEmpty
              ? EmptyState(
                  icon: Icons.card_giftcard_outlined,
                  title: 'No hay bundles',
                  action: AdminButton(label: 'Crear Bundle', onTap: _createBundle),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _bundles.length,
                  itemBuilder: (ctx, i) {
                    final bundle = _bundles[i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: InkWell(
                        onTap: () => _showBundleItems(bundle),
                        borderRadius: BorderRadius.circular(12),
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    width: 48,
                                    height: 48,
                                    decoration: BoxDecoration(
                                      color: AppColors.violet.withValues(alpha: 0.1),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: const Icon(Icons.card_giftcard, color: AppColors.violet),
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(bundle['name'] ?? '', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600)),
                                        Text(
                                          bundle['description'] ?? '',
                                          style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                          maxLines: 2,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 12),
                              Row(
                                children: [
                                  if (bundle['discount_percent'] != null)
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                      decoration: BoxDecoration(
                                        color: AppColors.success.withValues(alpha: 0.1),
                                        borderRadius: BorderRadius.circular(4),
                                      ),
                                      child: Text(
                                        '-${bundle['discount_percent']}%',
                                        style: GoogleFonts.dmSans(fontSize: 12, fontWeight: FontWeight.w600, color: AppColors.success),
                                      ),
                                    ),
                                  const SizedBox(width: 8),
                                  Text(
                                    'RD\$${bundle['total_price'] ?? 0}',
                                    style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700, color: AppColors.primary),
                                  ),
                                  const Spacer(),
                                  Text(
                                    '${bundle['items_count'] ?? 0} items',
                                    style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createBundle,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}
