import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../providers/clients_provider.dart';

class ClientDetailScreen extends StatefulWidget {
  final String clientId;

  const ClientDetailScreen({super.key, required this.clientId});

  @override
  State<ClientDetailScreen> createState() => _ClientDetailScreenState();
}

class _ClientDetailScreenState extends State<ClientDetailScreen> {
  Map<String, dynamic>? _client;
  bool _loading = true;
  bool _editing = false;
  bool _saving = false;

  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();
  final _noteController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadClient();
  }

  Future<void> _loadClient() async {
    setState(() => _loading = true);
    final client = await context.read<ClientsProvider>().getClient(widget.clientId);
    if (mounted) {
      setState(() {
        _client = client;
        _nameController.text = client?['name'] ?? '';
        _emailController.text = client?['email'] ?? '';
        _phoneController.text = client?['phone'] ?? '';
        _noteController.text = client?['admin_note'] ?? '';
        _loading = false;
      });
    }
  }

  Future<void> _saveChanges() async {
    setState(() => _saving = true);
    final success = await context.read<ClientsProvider>().updateClient(widget.clientId, {
      'name': _nameController.text,
      'email': _emailController.text,
      'phone': _phoneController.text,
    });
    if (success) {
      await context.read<ClientsProvider>().addNote(widget.clientId, _noteController.text);
    }
    if (mounted) {
      setState(() {
        _saving = false;
        _editing = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Cliente',
      showBack: true,
      actions: [
        if (!_editing)
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: () => setState(() => _editing = true),
          )
        else
          TextButton(
            onPressed: _saving ? null : _saveChanges,
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
                  // Header
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Container(
                            width: 64,
                            height: 64,
                            decoration: BoxDecoration(
                              color: _client?['is_lead'] == true
                                  ? AppColors.warning.withValues(alpha: 0.1)
                                  : AppColors.primary.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(32),
                            ),
                            child: Icon(
                              _client?['is_lead'] == true ? Icons.person_outline : Icons.person,
                              size: 32,
                              color: _client?['is_lead'] == true ? AppColors.warning : AppColors.primary,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Text(_client?['name'] ?? '', style: GoogleFonts.outfit(fontSize: 20, fontWeight: FontWeight.w700)),
                                    if (_client?['is_lead'] == true) ...[
                                      const SizedBox(width: 8),
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                        decoration: BoxDecoration(
                                          color: AppColors.warning.withValues(alpha: 0.1),
                                          borderRadius: BorderRadius.circular(6),
                                        ),
                                        child: Text('Lead', style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.warning)),
                                      ),
                                    ],
                                  ],
                                ),
                                if (_client?['is_active'] == false)
                                  Container(
                                    margin: const EdgeInsets.only(top: 4),
                                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                    decoration: BoxDecoration(
                                      color: AppColors.error.withValues(alpha: 0.1),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Text('Bloqueado', style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.error)),
                                  ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Contact info
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Información de Contacto', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          if (_editing) ...[
                            AdminTextField(label: 'Nombre', controller: _nameController),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Email', controller: _emailController, keyboardType: TextInputType.emailAddress),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Teléfono', controller: _phoneController, keyboardType: TextInputType.phone),
                          ] else ...[
                            _InfoTile(icon: Icons.email, label: 'Email', value: _client?['email'] ?? 'No definido'),
                            _InfoTile(icon: Icons.phone, label: 'Teléfono', value: _client?['phone'] ?? 'No definido'),
                            _InfoTile(icon: Icons.calendar_today, label: 'Cliente desde', value: _client?['created_at']?.toString().split('T').first ?? ''),
                          ],
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Stats
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Estadísticas', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(
                                child: _StatTile(
                                  label: 'Eventos',
                                  value: '${_client?['events_count'] ?? 0}',
                                  icon: Icons.event,
                                  color: AppColors.primary,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: _StatTile(
                                  label: 'Total gastado',
                                  value: 'RD\$${_client?['total_spent'] ?? 0}',
                                  icon: Icons.attach_money,
                                  color: AppColors.success,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Admin note
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Nota Interna', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          if (_editing)
                            AdminTextField(label: '', controller: _noteController, lines: 4, hint: 'Agregar nota sobre el cliente...')
                          else
                            Text(
                              _client?['admin_note'] ?? 'Sin notas',
                              style: GoogleFonts.dmSans(
                                color: _client?['admin_note'] == null ? AppColors.textMuted : null,
                              ),
                            ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Events history
                  if (_client?['events'] != null && (_client!['events'] as List).isNotEmpty) ...[
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Historial de Eventos', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 12),
                            ...((_client!['events'] as List).map((event) => ListTile(
                                  contentPadding: EdgeInsets.zero,
                                  leading: Container(
                                    width: 40,
                                    height: 40,
                                    decoration: BoxDecoration(
                                      color: AppColors.primary.withValues(alpha: 0.1),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: const Icon(Icons.event, color: AppColors.primary, size: 20),
                                  ),
                                  title: Text(event['event_type'] ?? ''),
                                  subtitle: Text(event['date'] ?? ''),
                                  trailing: Text('RD\$${event['total'] ?? 0}'),
                                  onTap: () => Navigator.pushNamed(context, '/events/${event['id']}'),
                                ))),
                          ],
                        ),
                      ),
                    ),
                  ],

                  // Actions
                  const SizedBox(height: 16),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Acciones', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          ListTile(
                            contentPadding: EdgeInsets.zero,
                            leading: Icon(
                              _client?['is_active'] == false ? Icons.lock_open : Icons.block,
                              color: _client?['is_active'] == false ? AppColors.success : AppColors.error,
                            ),
                            title: Text(_client?['is_active'] == false ? 'Desbloquear usuario' : 'Bloquear usuario'),
                            onTap: () async {
                              final confirmed = await showDialog<bool>(
                                context: context,
                                builder: (ctx) => AlertDialog(
                                  title: Text(_client?['is_active'] == false ? 'Desbloquear' : 'Bloquear'),
                                  content: Text(
                                    _client?['is_active'] == false
                                        ? '¿Desbloquear a este usuario?'
                                        : '¿Bloquear a este usuario? No podrá iniciar sesión.',
                                  ),
                                  actions: [
                                    TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                    ElevatedButton(
                                      onPressed: () => Navigator.pop(ctx, true),
                                      style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
                                      child: const Text('Confirmar'),
                                    ),
                                  ],
                                ),
                              );
                              if (confirmed == true) {
                                await context.read<ClientsProvider>().blockClient(
                                      widget.clientId,
                                      _client?['is_active'] != false,
                                    );
                                await _loadClient();
                              }
                            },
                          ),
                          const Divider(),
                          ListTile(
                            contentPadding: EdgeInsets.zero,
                            leading: const Icon(Icons.logout, color: AppColors.error),
                            title: const Text('Forzar cierre de sesión'),
                            subtitle: Text('Invalidar todos los tokens del usuario', style: GoogleFonts.dmSans(fontSize: 12, color: Colors.grey)),
                            onTap: () async {
                              final confirmed = await showDialog<bool>(
                                context: context,
                                builder: (ctx) => AlertDialog(
                                  title: const Text('Forzar cierre de sesión'),
                                  content: const Text('¿Invalidar todos los tokens de este usuario? Deberá iniciar sesión de nuevo.'),
                                  actions: [
                                    TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                    ElevatedButton(
                                      onPressed: () => Navigator.pop(ctx, true),
                                      style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
                                      child: const Text('Forzar'),
                                    ),
                                  ],
                                ),
                              );
                              if (confirmed == true) {
                                final ok = await context.read<ClientsProvider>().forceLogout(widget.clientId);
                                if (context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(content: Text(ok ? 'Sesión invalidada' : 'Error al invalidar sesión')),
                                  );
                                }
                              }
                            },
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _InfoTile({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 18, color: AppColors.textMuted),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted)),
              Text(value, style: GoogleFonts.dmSans()),
            ],
          ),
        ],
      ),
    );
  }
}

class _StatTile extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color color;

  const _StatTile({required this.label, required this.value, required this.icon, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(height: 8),
          Text(value, style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w700)),
          Text(label, style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
        ],
      ),
    );
  }
}
