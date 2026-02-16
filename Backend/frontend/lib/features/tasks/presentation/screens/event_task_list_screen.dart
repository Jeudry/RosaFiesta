import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/task_model.dart';
import 'tasks_provider.dart';

class EventTaskListScreen extends StatefulWidget {
  final String eventId;

  const EventTaskListScreen({super.key, required this.eventId});

  @override
  State<EventTaskListScreen> createState() => _EventTaskListScreenState();
}

class _EventTaskListScreenState extends State<EventTaskListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<EventTasksProvider>().fetchTasks(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tareas del Evento'),
      ),
      body: Column(
        children: [
          _buildProgressHeader(),
          Expanded(
            child: Consumer<EventTasksProvider>(
              builder: (context, provider, child) {
                if (provider.isLoading && provider.tasks.isEmpty) {
                  return const Center(child: CircularProgressIndicator());
                }

                if (provider.error != null && provider.tasks.isEmpty) {
                  return Center(child: Text(provider.error!));
                }

                if (provider.tasks.isEmpty) {
                  return const Center(child: Text('No hay tareas pendientes.'));
                }

                return ListView.builder(
                  itemCount: provider.tasks.length,
                  itemBuilder: (context, index) {
                    final task = provider.tasks[index];
                    return ListTile(
                      leading: Checkbox(
                        value: task.isCompleted,
                        onChanged: (value) {
                          provider.toggleTask(task.id, widget.eventId, value ?? false);
                        },
                      ),
                      title: Text(
                        task.title,
                        style: TextStyle(
                          decoration: task.isCompleted ? TextDecoration.lineThrough : null,
                          color: task.isCompleted ? Colors.grey : null,
                        ),
                      ),
                      subtitle: task.dueDate != null 
                        ? Text('Vence: ${task.dueDate!.day}/${task.dueDate!.month}/${task.dueDate!.year}') 
                        : null,
                      trailing: IconButton(
                        icon: const Icon(Icons.delete, color: Colors.red),
                        onPressed: () => provider.deleteTask(task.id, widget.eventId),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddTaskDialog(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildProgressHeader() {
    return Consumer<EventTasksProvider>(
      builder: (context, provider, child) {
        if (provider.tasks.isEmpty) return const SizedBox.shrink();
        
        return Container(
          padding: const EdgeInsets.all(16),
          color: Colors.blue.withOpacity(0.1),
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Progreso', style: TextStyle(fontWeight: FontWeight.bold)),
                  Text('${provider.completedCount} / ${provider.tasks.length} tareas completadas'),
                ],
              ),
              const SizedBox(height: 8),
              LinearProgressIndicator(
                value: provider.progress,
                backgroundColor: Colors.grey[300],
                valueColor: const AlwaysStoppedAnimation<Color>(Colors.blue),
              ),
            ],
          ),
        );
      },
    );
  }

  void _showAddTaskDialog(BuildContext context) {
    final titleController = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Nueva Tarea'),
        content: TextField(
          controller: titleController,
          decoration: const InputDecoration(labelText: '¿Qué hay que hacer?'),
          autofocus: true,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () {
              if (titleController.text.isNotEmpty) {
                final newTask = EventTask(
                  id: '',
                  eventId: widget.eventId,
                  title: titleController.text,
                  isCompleted: false,
                  createdAt: DateTime.now(),
                  updatedAt: DateTime.now(),
                );
                context.read<EventTasksProvider>().addTask(widget.eventId, newTask).then((success) {
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
