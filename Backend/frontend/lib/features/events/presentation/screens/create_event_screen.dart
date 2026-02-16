import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../events_provider.dart';

class CreateEventScreen extends StatefulWidget {
  const CreateEventScreen({super.key});

  @override
  State<CreateEventScreen> createState() => _CreateEventScreenState();
}

class _CreateEventScreenState extends State<CreateEventScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  final _budgetController = TextEditingController();
  final _guestCountController = TextEditingController();
  DateTime _selectedDate = DateTime.now();

  @override
  void dispose() {
    _nameController.dispose();
    _locationController.dispose();
    _budgetController.dispose();
    _guestCountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nuevo Evento')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: ListView(
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Nombre del Evento'),
                validator: (value) => value!.isEmpty ? 'Campo requerido' : null,
              ),
              const SizedBox(height: 16),
              ListTile(
                title: Text('Fecha: ${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}'),
                trailing: const Icon(Icons.calendar_today),
                onTap: () async {
                  final picked = await showDatePicker(
                    context: context,
                    initialDate: _selectedDate,
                    firstDate: DateTime.now(),
                    lastDate: DateTime(2030),
                  );
                  if (picked != null) {
                    setState(() => _selectedDate = picked);
                  }
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _locationController,
                decoration: const InputDecoration(labelText: 'Ubicación'),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _budgetController,
                decoration: const InputDecoration(labelText: 'Presupuesto Estimado'),
                keyboardType: TextInputType.number,
                validator: (value) {
                    if (value == null || value.isEmpty) return null;
                    if (double.tryParse(value) == null) return 'Debe ser un número válido';
                    return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _guestCountController,
                decoration: const InputDecoration(labelText: 'Cantidad de Invitados'),
                keyboardType: TextInputType.number,
                 validator: (value) {
                    if (value == null || value.isEmpty) return null;
                    if (int.tryParse(value) == null) return 'Debe ser un número entero';
                    return null;
                },
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _submit,
                child: const Text('Crear Evento'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (_formKey.currentState!.validate()) {
      final name = _nameController.text;
      final location = _locationController.text;
      final budget = double.tryParse(_budgetController.text) ?? 0.0;
      final guestCount = int.tryParse(_guestCountController.text) ?? 0;

      final success = await context.read<EventsProvider>().createEvent(
            name,
            _selectedDate,
            location,
            budget,
            guestCount,
          );

      if (success && mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Evento creado exitosamente')),
        );
      } else if (mounted) {
         ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Error al crear evento')),
        );
      }
    }
  }
}
