import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class ConfigScreen extends StatefulWidget {
  const ConfigScreen({super.key});

  @override
  State<ConfigScreen> createState() => _ConfigScreenState();
}

class _ConfigScreenState extends State<ConfigScreen> {
  Map<String, dynamic>? _profile;
  List<dynamic>? _deliveryZones;
  List<dynamic>? _paymentMethods;
  bool _loading = true;

  final _nameController = TextEditingController();
  final _emailController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final responses = await Future.wait([
        apiClient.getAdminProfile(),
        apiClient.getDeliveryZones(),
        apiClient.getPaymentMethods(),
      ]);
      _profile = responses[0].data['data'];
      _deliveryZones = responses[1].data['data'] ?? [];
      _paymentMethods = responses[2].data['data'] ?? [];
      _nameController.text = _profile?['name'] ?? '';
      _emailController.text = _profile?['email'] ?? '';
    } catch (e) {
      // Handle error
    }
    setState(() => _loading = false);
  }

  Future<void> _saveProfile() async {
    await apiClient.updateAdminProfile({
      'name': _nameController.text,
      'email': _emailController.text,
    });
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Perfil actualizado')));
    }
  }

  Future<void> _changePassword() async {
    final currentController = TextEditingController();
    final newController = TextEditingController();

    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Cambiar Contraseña'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AdminTextField(label: 'Contraseña actual', controller: currentController, obscure: true),
            const SizedBox(height: 12),
            AdminTextField(label: 'Nueva contraseña', controller: newController, obscure: true),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () async {
              try {
                await apiClient.changePassword(currentController.text, newController.text);
                if (ctx.mounted) {
                  Navigator.pop(ctx, true);
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Contraseña cambiada')));
                }
              } catch (e) {
                ScaffoldMessenger.of(ctx).showSnackBar(const SnackBar(content: Text('Error al cambiar contraseña')));
              }
            },
            child: const Text('Cambiar'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Configuración',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Profile
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Mi Cuenta', style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          AdminTextField(label: 'Nombre', controller: _nameController),
                          const SizedBox(height: 12),
                          AdminTextField(label: 'Email', controller: _emailController, keyboardType: TextInputType.emailAddress),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(
                                child: OutlinedButton(
                                  onPressed: _saveProfile,
                                  child: const Text('Guardar'),
                                ),
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: ElevatedButton(
                                  onPressed: _changePassword,
                                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.textSecondary),
                                  child: const Text('Cambiar Contraseña'),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Delivery zones
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('Zonas de Delivery', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                              IconButton(
                                icon: const Icon(Icons.add),
                                onPressed: _addDeliveryZone,
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),
                          if (_deliveryZones != null)
                            ..._deliveryZones!.map((zone) => ListTile(
                                  contentPadding: EdgeInsets.zero,
                                  title: Text(zone['name'] ?? ''),
                                  subtitle: Text('Radio: ${zone['radius_km']}km - RD\$${zone['fee']}'),
                                  trailing: IconButton(
                                    icon: const Icon(Icons.edit, size: 18),
                                    onPressed: () => _editDeliveryZone(zone),
                                  ),
                                )),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Payment methods
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Métodos de Pago', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          if (_paymentMethods != null)
                            ..._paymentMethods!.map((method) => SwitchListTile(
                                  contentPadding: EdgeInsets.zero,
                                  title: Text(method['name'] ?? ''),
                                  value: method['enabled'] == true,
                                  onChanged: (v) async {
                                    await apiClient.updatePaymentMethods([
                                      {...method, 'enabled': v}
                                    ]);
                                    _loadData();
                                  },
                                )),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Work hours
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Horarios de Trabajo', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          _InfoRow(label: 'Montaje', value: '2 horas antes del evento'),
                          _InfoRow(label: 'Desmontaje', value: '1 hora después del evento'),
                          _InfoRow(label: 'Delivery', value: '2 horas antes'),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }

  void _addDeliveryZone() {
    final nameController = TextEditingController();
    final radiusController = TextEditingController();
    final feeController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Nueva Zona'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AdminTextField(label: 'Nombre', controller: nameController),
            const SizedBox(height: 12),
            AdminTextField(label: 'Radio (km)', controller: radiusController, keyboardType: TextInputType.number),
            const SizedBox(height: 12),
            AdminTextField(label: 'Tarifa (RD\$)', controller: feeController, keyboardType: TextInputType.number),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () async {
              final List<Map<String, dynamic>> zones = [...(_deliveryZones ?? []), {
                'name': nameController.text,
                'radius_km': int.tryParse(radiusController.text) ?? 0,
                'fee': int.tryParse(feeController.text) ?? 0,
              }];
              await apiClient.updateDeliveryZones(zones);
              if (ctx.mounted) {
                Navigator.pop(ctx);
                _loadData();
              }
            },
            child: const Text('Crear'),
          ),
        ],
      ),
    );
  }

  void _editDeliveryZone(Map<String, dynamic> zone) {
    // Similar to add but pre-filled
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;

  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: GoogleFonts.dmSans(color: AppColors.textMuted)),
          Text(value, style: GoogleFonts.dmSans()),
        ],
      ),
    );
  }
}
