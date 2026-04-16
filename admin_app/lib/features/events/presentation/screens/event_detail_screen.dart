import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';
import '../providers/events_provider.dart';

class EventDetailScreen extends StatefulWidget {
  final String eventId;

  const EventDetailScreen({super.key, required this.eventId});

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  Map<String, dynamic>? _event;
  bool _loading = true;
  bool _saving = false;
  bool _editing = false;

  final _dateController = TextEditingController();
  final _timeController = TextEditingController();
  final _addressController = TextEditingController();
  final _notesController = TextEditingController();
  final _internalNotesController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadEvent();
  }

  Future<void> _loadEvent() async {
    setState(() => _loading = true);
    final event = await context.read<EventsProvider>().getEvent(widget.eventId);
    if (mounted) {
      setState(() {
        _event = event;
        _dateController.text = event?['date'] ?? '';
        _timeController.text = event?['time'] ?? '';
        _addressController.text = event?['address'] ?? '';
        _notesController.text = event?['notes'] ?? '';
        _internalNotesController.text = event?['internal_notes'] ?? '';
        _loading = false;
      });
    }
  }

  Future<void> _saveChanges() async {
    setState(() => _saving = true);
    final success = await context.read<EventsProvider>().updateEvent(widget.eventId, {
      'date': _dateController.text,
      'time': _timeController.text,
      'address': _addressController.text,
      'notes': _notesController.text,
      'internal_notes': _internalNotesController.text,
    });
    if (mounted) {
      setState(() {
        _saving = false;
        _editing = false;
      });
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cambios guardados')),
        );
      }
    }
  }

  Future<void> _changeStatus(String status) async {
    final success = await context.read<EventsProvider>().updateEvent(widget.eventId, {'status': status});
    if (success) {
      await _loadEvent();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return AdminScaffold(
      title: 'Detalle del Evento',
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
                  // Header card
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(
                                  _event?['client_name'] ?? 'Evento',
                                  style: GoogleFonts.outfit(fontSize: 22, fontWeight: FontWeight.w700),
                                ),
                              ),
                              _buildStatusBadge(_event?['status']),
                            ],
                          ),
                          if (_event?['event_type'] != null) ...[
                            const SizedBox(height: 8),
                            Text(
                              _event?['event_type'] ?? '',
                              style: GoogleFonts.dmSans(color: AppColors.textMuted),
                            ),
                          ],
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Icon(Icons.calendar_today, size: 18, color: AppColors.textMuted),
                              const SizedBox(width: 8),
                              Text(_event?['date'] ?? '', style: GoogleFonts.dmSans()),
                              if (_event?['time'] != null) ...[
                                const SizedBox(width: 16),
                                Icon(Icons.access_time, size: 18, color: AppColors.textMuted),
                                const SizedBox(width: 8),
                                Text(_event?['time'] ?? '', style: GoogleFonts.dmSans()),
                              ],
                            ],
                          ),
                          const SizedBox(height: 8),
                          Row(
                            children: [
                              Icon(Icons.location_on, size: 18, color: AppColors.textMuted),
                              const SizedBox(width: 8),
                              Expanded(child: Text(_event?['address'] ?? 'Sin dirección', style: GoogleFonts.dmSans())),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Editable fields
                  if (_editing) ...[
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Editar Información', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 16),
                            AdminTextField(label: 'Fecha', controller: _dateController, hint: 'YYYY-MM-DD'),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Hora', controller: _timeController, hint: 'HH:MM'),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Dirección', controller: _addressController),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Notas del cliente', controller: _notesController, lines: 3),
                            const SizedBox(height: 12),
                            AdminTextField(label: 'Notas internas', controller: _internalNotesController, lines: 3),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // Status management
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Estado', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: [
                              _StatusButton(label: 'Borrador', status: 'draft', current: _event?['status']),
                              _StatusButton(label: 'Pendiente', status: 'pending', current: _event?['status']),
                              _StatusButton(label: 'Confirmado', status: 'confirmed', current: _event?['status']),
                              _StatusButton(label: 'Pagado', status: 'paid', current: _event?['status']),
                              _StatusButton(label: 'Completado', status: 'completed', current: _event?['status']),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Financials
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Información de Pago', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          _InfoRow(label: 'Total', value: 'RD\$${_event?['total'] ?? 0}'),
                          _InfoRow(label: 'Pendiente', value: 'RD\$${_event?['pending_amount'] ?? 0}', color: AppColors.warning),
                          _InfoRow(label: 'Método', value: _event?['payment_method'] ?? 'No definido'),
                          _InfoRow(label: 'Status pago', value: _event?['payment_status'] ?? 'No pagado'),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Items
                  if (_event?['items'] != null && (_event!['items'] as List).isNotEmpty) ...[
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text('Items del Evento', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                                TextButton.icon(
                                  onPressed: () {},
                                  icon: const Icon(Icons.add, size: 18),
                                  label: const Text('Agregar'),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            ...((_event!['items'] as List).map((item) => ListTile(
                                  contentPadding: EdgeInsets.zero,
                                  title: Text(item['name'] ?? ''),
                                  subtitle: Text('Cantidad: ${item['quantity']}'),
                                  trailing: Text('RD\$${item['price'] ?? 0}'),
                                ))),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // Actions
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Acciones', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          _ActionTile(
                            icon: Icons.picture_as_pdf,
                            label: 'Generar/Descargar Contrato PDF',
                            onTap: () {},
                          ),
                          _ActionTile(
                            icon: Icons.photo_library,
                            label: 'Subir Fotos al Evento',
                            onTap: () {},
                          ),
                          _ActionTile(
                            icon: Icons.people,
                            label: 'Ver Lista de Invitados',
                            onTap: () {},
                          ),
                          _ActionTile(
                            icon: Icons.send,
                            label: 'Enviar Cotización por WhatsApp',
                            onTap: () {},
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

  Widget _buildStatusBadge(String? status) {
    switch (status) {
      case 'pending':
        return StatusBadge.pending();
      case 'pending_quote':
        return StatusBadge(label: 'Cotización', color: AppColors.violet);
      case 'confirmed':
        return StatusBadge.confirmed();
      case 'paid':
        return StatusBadge.paid();
      case 'completed':
        return StatusBadge.completed();
      default:
        return StatusBadge.draft();
    }
  }
}

class _StatusButton extends StatelessWidget {
  final String label;
  final String status;
  final String current;

  const _StatusButton({required this.label, required this.status, required this.current});

  @override
  Widget build(BuildContext context) {
    final isSelected = status == current;
    return OutlinedButton(
      onPressed: () {
        context.read<EventsProvider>().updateEvent(
              (context.findAncestorStateOfType<_EventDetailScreenState>() as dynamic).widget.eventId,
              {'status': status},
            );
      },
      style: OutlinedButton.styleFrom(
        backgroundColor: isSelected ? AppColors.primary.withValues(alpha: 0.1) : null,
        side: BorderSide(color: isSelected ? AppColors.primary : AppColors.borderDark),
      ),
      child: Text(label),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;
  final Color? color;

  const _InfoRow({required this.label, required this.value, this.color});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: GoogleFonts.dmSans(color: AppColors.textMuted)),
          Text(value, style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, color: color)),
        ],
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionTile({required this.icon, required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Icon(icon, color: AppColors.primary),
      title: Text(label),
      trailing: const Icon(Icons.chevron_right, size: 18),
      onTap: onTap,
    );
  }
}
