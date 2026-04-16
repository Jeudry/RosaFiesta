import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../providers/events_provider.dart';

class CreateEventScreen extends StatefulWidget {
  const CreateEventScreen({super.key});

  @override
  State<CreateEventScreen> createState() => _CreateEventScreenState();
}

class _CreateEventScreenState extends State<CreateEventScreen> {
  final _formKey = GlobalKey<FormState>();
  final _clientNameController = TextEditingController();
  final _clientEmailController = TextEditingController();
  final _clientPhoneController = TextEditingController();
  final _dateController = TextEditingController();
  final _timeController = TextEditingController();
  final _addressController = TextEditingController();
  final _notesController = TextEditingController();
  String _eventType = 'Boda';
  bool _saving = false;

  final _eventTypes = ['Boda', 'Cumpleaños', 'Baby Shower', 'Graduación', 'Corporativo', 'Quinceañera', 'Otro'];

  @override
  void dispose() {
    _clientNameController.dispose();
    _clientEmailController.dispose();
    _clientPhoneController.dispose();
    _dateController.dispose();
    _timeController.dispose();
    _addressController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);

    final eventId = await context.read<EventsProvider>().createEvent({
      'client_name': _clientNameController.text,
      'client_email': _clientEmailController.text,
      'client_phone': _clientPhoneController.text,
      'date': _dateController.text,
      'time': _timeController.text,
      'address': _addressController.text,
      'notes': _notesController.text,
      'event_type': _eventType,
      'status': 'draft',
    });

    if (mounted) {
      setState(() => _saving = false);
      if (eventId != null) {
        Navigator.pushReplacementNamed(context, '/events/$eventId');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Nuevo Evento',
      showBack: true,
      actions: [
        TextButton(
          onPressed: _saving ? null : _save,
          child: _saving
              ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
              : const Text('Crear'),
        ),
      ],
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Cliente', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 16),
                      AdminTextField(
                        label: 'Nombre del cliente',
                        controller: _clientNameController,
                        validator: (v) => v == null || v.isEmpty ? 'Requerido' : null,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Email',
                        controller: _clientEmailController,
                        keyboardType: TextInputType.emailAddress,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Teléfono',
                        controller: _clientPhoneController,
                        keyboardType: TextInputType.phone,
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
                      Text('Detalles del Evento', style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 16),
                      Text('Tipo de evento', style: GoogleFonts.dmSans(fontSize: 13, fontWeight: FontWeight.w500)),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: _eventTypes
                            .map((t) => ChoiceChip(
                                  label: Text(t),
                                  selected: _eventType == t,
                                  onSelected: (_) => setState(() => _eventType = t),
                                ))
                            .toList(),
                      ),
                      const SizedBox(height: 16),
                      AdminTextField(
                        label: 'Fecha',
                        controller: _dateController,
                        hint: 'YYYY-MM-DD',
                        validator: (v) => v == null || v.isEmpty ? 'Requerido' : null,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Hora',
                        controller: _timeController,
                        hint: 'HH:MM',
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Dirección',
                        controller: _addressController,
                      ),
                      const SizedBox(height: 12),
                      AdminTextField(
                        label: 'Notas',
                        controller: _notesController,
                        lines: 3,
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: AdminButton(
                  label: 'Crear Evento',
                  onTap: _save,
                  loading: _saving,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
