import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';
import '../providers/quotes_provider.dart';

class CreateQuoteScreen extends StatefulWidget {
  const CreateQuoteScreen({super.key});

  @override
  State<CreateQuoteScreen> createState() => _CreateQuoteScreenState();
}

class _CreateQuoteScreenState extends State<CreateQuoteScreen> {
  final _formKey = GlobalKey<FormState>();
  final _clientNameController = TextEditingController();
  final _clientPhoneController = TextEditingController();
  final _clientEmailController = TextEditingController();
  final _dateController = TextEditingController();
  final _addressController = TextEditingController();
  final _notesController = TextEditingController();

  String _eventType = 'Boda';
  String _quoteMode = 'manual'; // 'manual' or 'ai'
  final List<Map<String, dynamic>> _selectedItems = [];
  bool _isLead = false;
  bool _saving = false;
  bool _searching = false;
  List<dynamic> _clientSearchResults = [];
  Map<String, dynamic>? _selectedClient;

  final _eventTypes = ['Boda', 'Cumpleaños', 'Baby Shower', 'Graduación', 'Corporativo', 'Quinceañera', 'Otro'];

  int get _subtotal => _selectedItems.fold<int>(0, (sum, item) => sum + ((item['price'] ?? 0) * (item['quantity'] ?? 1) as int));

  Future<void> _searchClients(String query) async {
    if (query.length < 2) {
      setState(() => _clientSearchResults = []);
      return;
    }
    setState(() => _searching = true);
    try {
      final response = await apiClient.searchClients(query);
      _clientSearchResults = response.data['data'] ?? [];
    } catch (e) {
      _clientSearchResults = [];
    }
    setState(() => _searching = false);
  }

  Future<void> _saveQuote() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedItems.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Agrega al menos un artículo')),
      );
      return;
    }

    setState(() => _saving = true);

    final quoteId = await context.read<QuotesProvider>().createQuote({
      'client_name': _clientNameController.text,
      'client_phone': _clientPhoneController.text,
      'client_email': _clientEmailController.text,
      'date': _dateController.text,
      'address': _addressController.text,
      'notes': _notesController.text,
      'event_type': _eventType,
      'items': _selectedItems,
      'total': _subtotal,
      'is_lead': _isLead,
    });

    if (mounted) {
      setState(() => _saving = false);
      if (quoteId != null) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cotización creada')),
        );
      }
    }
  }

  void _addItem() async {
    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => _ItemPickerSheet(
        onSelect: (item) {
          Navigator.pop(ctx, item);
        },
      ),
    );
    if (result != null) {
      setState(() {
        _selectedItems.add(result);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Nueva Cotización',
      showBack: true,
      actions: [
        TextButton(
          onPressed: _saving ? null : _saveQuote,
          child: _saving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Crear'),
        ),
      ],
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Client selection
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text('Cliente', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const Spacer(),
                          Row(
                            children: [
                              Checkbox(
                                value: _isLead,
                                onChanged: (v) => setState(() => _isLead = v ?? false),
                              ),
                              Text('Lead (sin registro)', style: GoogleFonts.dmSans(fontSize: 12)),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      if (!_isLead) ...[
                        AdminSearchField(
                          hint: 'Buscar cliente por nombre, email o teléfono...',
                          onChanged: _searchClients,
                        ),
                        if (_searching)
                          const Padding(
                            padding: EdgeInsets.all(8),
                            child: CircularProgressIndicator(),
                          ),
                        if (_clientSearchResults.isNotEmpty)
                          Container(
                            constraints: const BoxConstraints(maxHeight: 200),
                            child: ListView.builder(
                              shrinkWrap: true,
                              itemCount: _clientSearchResults.length,
                              itemBuilder: (ctx, i) {
                                final client = _clientSearchResults[i];
                                return ListTile(
                                  title: Text(client['name'] ?? ''),
                                  subtitle: Text(client['email'] ?? ''),
                                  trailing: Text(client['phone'] ?? ''),
                                  onTap: () {
                                    setState(() {
                                      _selectedClient = client;
                                      _clientNameController.text = client['name'] ?? '';
                                      _clientEmailController.text = client['email'] ?? '';
                                      _clientPhoneController.text = client['phone'] ?? '';
                                      _clientSearchResults = [];
                                    });
                                  },
                                );
                              },
                            ),
                          ),
                        if (_selectedClient != null)
                          Container(
                            margin: const EdgeInsets.only(top: 8),
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: AppColors.success.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Row(
                              children: [
                                const Icon(Icons.check_circle, color: AppColors.success, size: 18),
                                const SizedBox(width: 8),
                                Text('Cliente seleccionado: ${_selectedClient!['name']}'),
                                const Spacer(),
                                IconButton(
                                  icon: const Icon(Icons.close, size: 18),
                                  onPressed: () => setState(() => _selectedClient = null),
                                ),
                              ],
                            ),
                          ),
                        const SizedBox(height: 12),
                      ],
                      AdminTextField(
                        label: 'Nombre',
                        controller: _clientNameController,
                        validator: (v) => v == null || v.isEmpty ? 'Requerido' : null,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Teléfono',
                        controller: _clientPhoneController,
                        keyboardType: TextInputType.phone,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Email',
                        controller: _clientEmailController,
                        keyboardType: TextInputType.emailAddress,
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Event details
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Detalles del Evento', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 16),
                      Text('Tipo', style: GoogleFonts.dmSans(fontSize: 13, fontWeight: FontWeight.w500)),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: _eventTypes
                            .map((t) => ChoiceChip(label: Text(t), selected: _eventType == t, onSelected: (_) => setState(() => _eventType = t)))
                            .toList(),
                      ),
                      const SizedBox(height: 16),
                      AdminTextField(label: 'Fecha', controller: _dateController, hint: 'YYYY-MM-DD'),
                      const SizedBox(height: 12),
                      AdminTextField(label: 'Dirección', controller: _addressController),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Quote mode
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Agregar Artículos', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: _addItem,
                              icon: const Icon(Icons.add, size: 18),
                              label: const Text('Manual'),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: () => setState(() => _quoteMode = 'ai'),
                              icon: const Icon(Icons.smart_toy, size: 18),
                              label: const Text('IA'),
                              style: OutlinedButton.styleFrom(
                                backgroundColor: _quoteMode == 'ai' ? AppColors.primary.withValues(alpha: 0.1) : null,
                              ),
                            ),
                          ),
                        ],
                      ),
                      if (_selectedItems.isNotEmpty) ...[
                        const SizedBox(height: 16),
                        ...List.generate(_selectedItems.length, (i) {
                          final item = _selectedItems[i];
                          return ListTile(
                            contentPadding: EdgeInsets.zero,
                            leading: Container(
                              width: 40,
                              height: 40,
                              decoration: BoxDecoration(
                                color: AppColors.primary.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: const Icon(Icons.inventory_2, color: AppColors.primary, size: 20),
                            ),
                            title: Text(item['name'] ?? ''),
                            subtitle: Text('RD\$${item['price']} x ${item['quantity']}'),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text('RD\$${(item['price'] * item['quantity'])}', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                                IconButton(
                                  icon: const Icon(Icons.remove_circle_outline, size: 18, color: AppColors.error),
                                  onPressed: () => setState(() => _selectedItems.removeAt(i)),
                                ),
                              ],
                            ),
                          );
                        }),
                        const Divider(),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Subtotal', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                            Text('RD\$$_subtotal', style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w700, color: AppColors.primary)),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Notes
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: AdminTextField(label: 'Notas', controller: _notesController, lines: 3),
                ),
              ),
              const SizedBox(height: 24),

              SizedBox(
                width: double.infinity,
                child: AdminButton(label: 'Crear Cotización', onTap: _saveQuote, loading: _saving),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ItemPickerSheet extends StatefulWidget {
  final Function(Map<String, dynamic>) onSelect;

  const _ItemPickerSheet({required this.onSelect});

  @override
  State<_ItemPickerSheet> createState() => _ItemPickerSheetState();
}

class _ItemPickerSheetState extends State<_ItemPickerSheet> {
  final _searchController = TextEditingController();
  List<dynamic> _products = [];
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _searchProducts('');
  }

  Future<void> _searchProducts(String query) async {
    setState(() => _loading = true);
    try {
      final response = await apiClient.searchProducts(query);
      _products = response.data['data'] ?? [];
    } catch (e) {
      _products = [];
    }
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      expand: false,
      builder: (ctx, scrollController) => Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(color: AppColors.textMuted, borderRadius: BorderRadius.circular(2)),
                ),
                const SizedBox(height: 16),
                Text('Seleccionar Artículo', style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600)),
                const SizedBox(height: 12),
                AdminSearchField(
                  controller: _searchController,
                  hint: 'Buscar artículos...',
                  onChanged: _searchProducts,
                ),
              ],
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    controller: scrollController,
                    itemCount: _products.length,
                    itemBuilder: (ctx, i) {
                      final product = _products[i];
                      return ListTile(
                        leading: Container(
                          width: 48,
                          height: 48,
                          decoration: BoxDecoration(
                            color: AppColors.primary.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Icon(Icons.inventory_2, color: AppColors.primary),
                        ),
                        title: Text(product['name'] ?? ''),
                        subtitle: Text('RD\$${product['rental_price'] ?? 0}'),
                        onTap: () {
                          widget.onSelect({
                            'article_id': product['id'],
                            'name': product['name'],
                            'price': product['rental_price'] ?? 0,
                            'quantity': 1,
                          });
                        },
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
