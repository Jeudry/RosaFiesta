import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../data/supplier_model.dart';
import 'suppliers_provider.dart';

class SupplierListScreen extends StatefulWidget {
  const SupplierListScreen({super.key});

  @override
  State<SupplierListScreen> createState() => _SupplierListScreenState();
}

class _SupplierListScreenState extends State<SupplierListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SuppliersProvider>().fetchSuppliers();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis Proveedores'),
      ),
      body: Consumer<SuppliersProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading && provider.suppliers.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null && provider.suppliers.isEmpty) {
            return Center(child: Text('Error: ${provider.error}'));
          }

          if (provider.suppliers.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.contact_phone_outlined, size: 64, color: Colors.grey),
                  const SizedBox(height: 16),
                  const Text('No tienes proveedores guardados'),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () => _showAddSupplierDialog(context),
                    child: const Text('Agregar Proveedor'),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: provider.suppliers.length,
            itemBuilder: (context, index) {
              final supplier = provider.suppliers[index];
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: ListTile(
                  leading: const CircleAvatar(
                    child: Icon(Icons.business),
                  ),
                  title: Text(supplier.name),
                  subtitle: Text(supplier.contactName ?? 'Sin contacto'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showSupplierDetails(context, supplier),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddSupplierDialog(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  void _showAddSupplierDialog(BuildContext context, {Supplier? supplier}) {
    showDialog(
      context: context,
      builder: (context) => AddSupplierDialog(supplier: supplier),
    );
  }

  void _showSupplierDetails(BuildContext context, Supplier supplier) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SupplierDetailsSheet(supplier: supplier),
    );
  }
}

class AddSupplierDialog extends StatefulWidget {
  final Supplier? supplier;
  const AddSupplierDialog({super.key, this.supplier});

  @override
  State<AddSupplierDialog> createState() => _AddSupplierDialogState();
}

class _AddSupplierDialogState extends State<AddSupplierDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _contactController;
  late TextEditingController _emailController;
  late TextEditingController _phoneController;
  late TextEditingController _websiteController;
  late TextEditingController _notesController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.supplier?.name ?? '');
    _contactController = TextEditingController(text: widget.supplier?.contactName ?? '');
    _emailController = TextEditingController(text: widget.supplier?.email ?? '');
    _phoneController = TextEditingController(text: widget.supplier?.phone ?? '');
    _websiteController = TextEditingController(text: widget.supplier?.website ?? '');
    _notesController = TextEditingController(text: widget.supplier?.notes ?? '');
  }

  @override
  void dispose() {
    _nameController.dispose();
    _contactController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _websiteController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.supplier != null;

    return AlertDialog(
      title: Text(isEditing ? 'Editar Proveedor' : 'Nuevo Proveedor'),
      content: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Nombre de la Empresa *'),
                validator: (v) => v == null || v.isEmpty ? 'Requerido' : null,
              ),
              TextFormField(
                controller: _contactController,
                decoration: const InputDecoration(labelText: 'Persona de Contacto'),
              ),
              TextFormField(
                controller: _phoneController,
                decoration: const InputDecoration(labelText: 'Teléfono'),
                keyboardType: TextInputType.phone,
              ),
              TextFormField(
                controller: _emailController,
                decoration: const InputDecoration(labelText: 'Email'),
                keyboardType: TextInputType.emailAddress,
              ),
              TextFormField(
                controller: _websiteController,
                decoration: const InputDecoration(labelText: 'Sitio Web'),
                keyboardType: TextInputType.url,
              ),
              TextFormField(
                controller: _notesController,
                decoration: const InputDecoration(labelText: 'Notas'),
                maxLines: 3,
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancelar'),
        ),
        ElevatedButton(
          onPressed: () async {
            if (_formKey.currentState!.validate()) {
              final provider = context.read<SuppliersProvider>();
              bool success;

              if (isEditing) {
                success = await provider.updateSupplier(widget.supplier!.id, {
                  'name': _nameController.text,
                  'contact_name': _contactController.text,
                  'phone': _phoneController.text,
                  'email': _emailController.text,
                  'website': _websiteController.text,
                  'notes': _notesController.text,
                });
              } else {
                final newSupplier = Supplier(
                  id: '', // Will be set by backend
                  userId: '',
                  name: _nameController.text,
                  contactName: _contactController.text,
                  phone: _phoneController.text,
                  email: _emailController.text,
                  website: _websiteController.text,
                  notes: _notesController.text,
                  createdAt: DateTime.now(),
                  updatedAt: DateTime.now(),
                );
                success = await provider.addSupplier(newSupplier);
              }

              if (success) {
                if (mounted) Navigator.pop(context);
              }
            }
          },
          child: Text(isEditing ? 'Guardar' : 'Crear'),
        ),
      ],
    );
  }
}

class SupplierDetailsSheet extends StatelessWidget {
  final Supplier supplier;
  const SupplierDetailsSheet({super.key, required this.supplier});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(
                  supplier.name,
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.edit),
                onPressed: () {
                  Navigator.pop(context);
                  showDialog(
                    context: context,
                    builder: (context) => AddSupplierDialog(supplier: supplier),
                  );
                },
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (supplier.contactName != null && supplier.contactName!.isNotEmpty)
            _buildInfoRow(Icons.person, 'Contacto', supplier.contactName!),
          if (supplier.phone != null && supplier.phone!.isNotEmpty)
            _buildInfoRow(Icons.phone, 'Teléfono', supplier.phone!, onTap: () => _launchURL('tel:${supplier.phone}')),
          if (supplier.email != null && supplier.email!.isNotEmpty)
            _buildInfoRow(Icons.email, 'Email', supplier.email!, onTap: () => _launchURL('mailto:${supplier.email}')),
          if (supplier.website != null && supplier.website!.isNotEmpty)
            _buildInfoRow(Icons.language, 'Web', supplier.website!, onTap: () => _launchURL(supplier.website!)),
          if (supplier.notes != null && supplier.notes!.isNotEmpty) ...[
            const SizedBox(height: 16),
            const Text('Notas', style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 4),
            Text(supplier.notes!),
          ],
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () => _confirmDelete(context),
              icon: const Icon(Icons.delete, color: Colors.red),
              label: const Text('Eliminar Proveedor', style: TextStyle(color: Colors.red)),
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value, {VoidCallback? onTap}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: InkWell(
        onTap: onTap,
        child: Row(
          children: [
            Icon(icon, color: Colors.blue),
            const SizedBox(width: 16),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
                Text(value, style: TextStyle(fontSize: 16, color: onTap != null ? Colors.blue : null)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _launchURL(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }

  void _confirmDelete(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('¿Eliminar proveedor?'),
        content: const Text('Esta acción no se puede deshacer.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          TextButton(
            onPressed: () async {
              final success = await Provider.of<SuppliersProvider>(context, listen: false).deleteSupplier(supplier.id);
              if (success) {
                if (context.mounted) {
                  Navigator.pop(context); // Close dialog
                  Navigator.pop(context); // Close sheet
                }
              }
            },
            child: const Text('Eliminar', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}
