import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class CategoriesScreen extends StatefulWidget {
  const CategoriesScreen({super.key});

  @override
  State<CategoriesScreen> createState() => _CategoriesScreenState();
}

class _CategoriesScreenState extends State<CategoriesScreen> {
  List<dynamic> _categories = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.getCategories();
      _categories = response.data['data'] ?? [];
    } catch (e) {
      _categories = [];
    }
    setState(() => _loading = false);
  }

  Future<void> _createCategory() async {
    final nameController = TextEditingController();
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Nueva Categoría'),
        content: AdminTextField(label: 'Nombre', controller: nameController),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, {'name': nameController.text}),
            child: const Text('Crear'),
          ),
        ],
      ),
    );

    if (result != null && result['name']!.isNotEmpty) {
      await apiClient.createCategory(result);
      await _loadCategories();
    }
  }

  Future<void> _editCategory(String id, String currentName) async {
    final nameController = TextEditingController(text: currentName);
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Editar Categoría'),
        content: AdminTextField(label: 'Nombre', controller: nameController),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, {'name': nameController.text}),
            child: const Text('Guardar'),
          ),
        ],
      ),
    );

    if (result != null && result['name']!.isNotEmpty) {
      await apiClient.updateCategory(id, result);
      await _loadCategories();
    }
  }

  Future<void> _deleteCategory(String id) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Eliminar Categoría'),
        content: const Text('¿Eliminar esta categoría?'),
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
      await apiClient.deleteCategory(id);
      await _loadCategories();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Categorías',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _categories.isEmpty
              ? EmptyState(
                  icon: Icons.category_outlined,
                  title: 'No hay categorías',
                  action: AdminButton(label: 'Crear Categoría', onTap: _createCategory),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _categories.length,
                  itemBuilder: (ctx, i) {
                    final category = _categories[i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: Container(
                          width: 40,
                          height: 40,
                          decoration: BoxDecoration(
                            color: AppColors.primary.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Icon(Icons.category, color: AppColors.primary, size: 20),
                        ),
                        title: Text(category['name'] ?? ''),
                        trailing: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: const Icon(Icons.edit, size: 18),
                              onPressed: () => _editCategory(category['id'], category['name'] ?? ''),
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete, size: 18, color: AppColors.error),
                              onPressed: () => _deleteCategory(category['id']),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createCategory,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}
