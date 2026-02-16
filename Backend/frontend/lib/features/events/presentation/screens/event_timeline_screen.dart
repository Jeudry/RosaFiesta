import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../timeline_provider.dart';
import '../../data/timeline_model.dart';

class EventTimelineScreen extends StatefulWidget {
  final String eventId;

  const EventTimelineScreen({super.key, required this.eventId});

  @override
  State<EventTimelineScreen> createState() => _EventTimelineScreenState();
}

class _EventTimelineScreenState extends State<EventTimelineScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TimelineProvider>().fetchTimeline(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cronograma del Evento'),
      ),
      body: Consumer<TimelineProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(child: Text(provider.error!));
          }

          if (provider.items.isEmpty) {
            return const Center(
              child: Text('No hay actividades programadas aún.'),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: provider.items.length,
            itemBuilder: (context, index) {
              final item = provider.items[index];
              final isLast = index == provider.items.length - 1;

              return IntrinsicHeight(
                child: Row(
                  children: [
                    Column(
                      children: [
                        Container(
                          width: 12,
                          height: 12,
                          decoration: const BoxDecoration(
                            color: Colors.blue,
                            shape: BoxShape.circle,
                          ),
                        ),
                        if (!isLast)
                          Expanded(
                            child: Container(
                              width: 2,
                              color: Colors.blue.withOpacity(0.3),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 24.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '${_formatTime(item.startTime)} - ${_formatTime(item.endTime)}',
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                color: Colors.blue,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              item.title,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            if (item.description.isNotEmpty) ...[
                              const SizedBox(height: 4),
                              Text(
                                item.description,
                                style: TextStyle(color: Colors.grey[600]),
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.edit_outlined, size: 20),
                      onPressed: () => _showAddEditDialog(item),
                    ),
                    IconButton(
                      icon: const Icon(Icons.delete_outline, size: 20, color: Colors.red),
                      onPressed: () => provider.deleteItem(item.id),
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddEditDialog(null),
        child: const Icon(Icons.add),
      ),
    );
  }

  String _formatTime(DateTime time) {
    final hour = time.hour.toString().padLeft(2, '0');
    final minute = time.minute.toString().padLeft(2, '0');
    return '$hour:$minute';
  }

  void _showAddEditDialog(TimelineItem? item) {
    final titleController = TextEditingController(text: item?.title);
    final descController = TextEditingController(text: item?.description);
    TimeOfDay startTime = TimeOfDay.fromDateTime(item?.startTime ?? DateTime.now());
    TimeOfDay endTime = TimeOfDay.fromDateTime(item?.endTime ?? DateTime.now().add(const Duration(hours: 1)));

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: Text(item == null ? 'Agregar Actividad' : 'Editar Actividad'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: titleController,
                  decoration: const InputDecoration(labelText: 'Título'),
                ),
                TextField(
                  controller: descController,
                  decoration: const InputDecoration(labelText: 'Descripción'),
                ),
                const SizedBox(height: 16),
                ListTile(
                  title: const Text('Inicio'),
                  trailing: Text(startTime.format(context)),
                  onTap: () async {
                    final picked = await showTimePicker(context: context, initialTime: startTime);
                    if (picked != null) setState(() => startTime = picked);
                  },
                ),
                ListTile(
                  title: const Text('Fin'),
                  trailing: Text(endTime.format(context)),
                  onTap: () async {
                    final picked = await showTimePicker(context: context, initialTime: endTime);
                    if (picked != null) setState(() => endTime = picked);
                  },
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
            ElevatedButton(
              onPressed: () {
                final now = DateTime.now();
                final start = DateTime(now.year, now.month, now.day, startTime.hour, startTime.minute);
                final end = DateTime(now.year, now.month, now.day, endTime.hour, endTime.minute);
                
                if (item == null) {
                  context.read<TimelineProvider>().addItem(
                    widget.eventId,
                    titleController.text,
                    descController.text,
                    start,
                    end,
                  );
                } else {
                  context.read<TimelineProvider>().updateItem(
                    item.id,
                    titleController.text,
                    descController.text,
                    start,
                    end,
                  );
                }
                Navigator.pop(context);
              },
              child: const Text('Guardar'),
            ),
          ],
        ),
      ),
    );
  }
}
