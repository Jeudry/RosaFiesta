import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/guest_model.dart';
import '../guests_provider.dart';

class GuestListScreen extends StatefulWidget {
  final String eventId;

  const GuestListScreen({super.key, required this.eventId});

  @override
  State<GuestListScreen> createState() => _GuestListScreenState();
}

class _GuestListScreenState extends State<GuestListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<GuestsProvider>().fetchGuests(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Invitados')),
      body: Consumer<GuestsProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading && provider.guests.isEmpty) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null && provider.guests.isEmpty) {
            return Center(child: Text(provider.error!));
          }

          if (provider.guests.isEmpty) {
            return const Center(child: Text('No hay invitados registrados'));
          }

          return ListView.builder(
            itemCount: provider.guests.length,
            itemBuilder: (context, index) {
              final guest = provider.guests[index];
              return ListTile(
                title: Text(guest.name),
                subtitle: Text('Estado: ${guest.rsvpStatus}'),
                leading: CircleAvatar(
                  backgroundColor: _getStatusColor(guest.rsvpStatus),
                  child: const Icon(Icons.person, color: Colors.white),
                ),
                trailing: PopupMenuButton<String>(
                  onSelected: (value) {
                    if (value == 'delete') {
                      provider.deleteGuest(guest.id, widget.eventId);
                    } else {
                      provider.updateRSVP(guest.id, widget.eventId, value);
                    }
                  },
                  itemBuilder: (context) => [
                    const PopupMenuItem(value: 'confirmed', child: Text('Confirmar')),
                    const PopupMenuItem(value: 'declined', child: Text('Rechazar')),
                    const PopupMenuItem(value: 'pending', child: Text('Pendiente')),
                    const PopupMenuItem(value: 'delete', child: Text('Eliminar', style: TextStyle(color: Colors.red))),
                  ],
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddGuestDialog(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'confirmed':
        return Colors.green;
      case 'declined':
        return Colors.red;
      default:
        return Colors.orange;
    }
  }

  void _showAddGuestDialog(BuildContext context) {
    final nameController = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Nuevo Invitado'),
        content: TextField(
          controller: nameController,
          decoration: const InputDecoration(labelText: 'Nombre'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () {
              if (nameController.text.isNotEmpty) {
                final newGuest = Guest(
                  id: '',
                  eventId: widget.eventId,
                  name: nameController.text,
                  rsvpStatus: 'pending',
                  plusOne: false,
                  createdAt: DateTime.now(),
                  updatedAt: DateTime.now(),
                );
                context.read<GuestsProvider>().addGuest(widget.eventId, newGuest).then((success) {
                  if (success) Navigator.pop(context);
                });
              }
            },
            child: const Text('Agregar'),
          ),
        ],
      ),
    );
  }
}
