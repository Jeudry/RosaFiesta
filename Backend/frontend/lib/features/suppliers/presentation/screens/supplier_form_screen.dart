import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../suppliers_provider.dart';
import '../../data/supplier_model.dart';

class SupplierFormScreen extends StatefulWidget {
  final Supplier? supplier;

  const SupplierFormScreen({super.key, this.supplier});

  @override
  State<SupplierFormScreen> createState() => _SupplierFormScreenState();
}

class _SupplierFormScreenState extends State<SupplierFormScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _nameController;
  late final TextEditingController _contactNameController;
  late final TextEditingController _emailController;
  late final TextEditingController _phoneController;
  late final TextEditingController _websiteController;
  late final TextEditingController _notesController;
  bool _saving = false;

  bool get isEditing => widget.supplier != null;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.supplier?.name ?? '');
    _contactNameController = TextEditingController(text: widget.supplier?.contactName ?? '');
    _emailController = TextEditingController(text: widget.supplier?.email ?? '');
    _phoneController = TextEditingController(text: widget.supplier?.phone ?? '');
    _websiteController = TextEditingController(text: widget.supplier?.website ?? '');
    _notesController = TextEditingController(text: widget.supplier?.notes ?? '');
  }

  @override
  void dispose() {
    _nameController.dispose();
    _contactNameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _websiteController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);

    final provider = context.read<SuppliersProvider>();
    bool success;

    if (isEditing) {
      success = await provider.updateSupplier(
        widget.supplier!.id,
        name: _nameController.text,
        contactName: _contactNameController.text,
        email: _emailController.text,
        phone: _phoneController.text,
        website: _websiteController.text,
        notes: _notesController.text,
      );
    } else {
      success = await provider.addSupplier(
        name: _nameController.text,
        contactName: _contactNameController.text,
        email: _emailController.text,
        phone: _phoneController.text,
        website: _websiteController.text,
        notes: _notesController.text,
      );
    }

    if (!mounted) return;
    setState(() => _saving = false);

    if (success) {
      Navigator.of(context).pop();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(provider.error ?? 'Error al guardar'),
          backgroundColor: AppColors.coral,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.base,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.close_rounded, color: t.textPrimary),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          isEditing ? 'Editar proveedor' : 'Nuevo proveedor',
          style: GoogleFonts.outfit(
            fontSize: 18,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        actions: [
          TextButton(
            onPressed: _saving ? null : _save,
            child: _saving
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : Text(
                    'Guardar',
                    style: GoogleFonts.dmSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: AppColors.hotPink,
                    ),
                  ),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            _buildField(
              t: t,
              label: 'Nombre de la empresa *',
              controller: _nameController,
              validator: (v) => v == null || v.isEmpty ? 'Requerido' : null,
            ),
            const SizedBox(height: 16),
            _buildField(
              t: t,
              label: 'Persona de contacto',
              controller: _contactNameController,
            ),
            const SizedBox(height: 16),
            _buildField(
              t: t,
              label: 'Correo electrónico',
              controller: _emailController,
              keyboardType: TextInputType.emailAddress,
            ),
            const SizedBox(height: 16),
            _buildField(
              t: t,
              label: 'Teléfono',
              controller: _phoneController,
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 16),
            _buildField(
              t: t,
              label: 'Sitio web',
              controller: _websiteController,
              keyboardType: TextInputType.url,
            ),
            const SizedBox(height: 16),
            _buildField(
              t: t,
              label: 'Notas',
              controller: _notesController,
              maxLines: 4,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildField({
    required RfTheme t,
    required String label,
    required TextEditingController controller,
    String? Function(String?)? validator,
    TextInputType? keyboardType,
    int maxLines = 1,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.dmSans(
            fontSize: 13,
            fontWeight: FontWeight.w600,
            color: t.textMuted,
          ),
        ),
        const SizedBox(height: 6),
        TextFormField(
          controller: controller,
          validator: validator,
          keyboardType: keyboardType,
          maxLines: maxLines,
          style: GoogleFonts.dmSans(
            fontSize: 15,
            color: t.textPrimary,
          ),
          decoration: InputDecoration(
            filled: true,
            fillColor: t.isDark ? t.card.withValues(alpha: 0.5) : const Color(0xFFF8F9FA),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: t.borderFaint),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: t.borderFaint),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(color: AppColors.hotPink, width: 1.5),
            ),
            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          ),
        ),
      ],
    );
  }
}
