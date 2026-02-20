import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/app_theme.dart';
import 'package:frontend/features/tasks/presentation/tasks_provider.dart';
import 'package:frontend/features/events/presentation/timeline_provider.dart';
import 'package:frontend/features/tasks/data/task_model.dart';
import 'package:frontend/features/events/data/timeline_model.dart';
import 'package:intl/intl.dart';

class ExecutionItem {
  final String id;
  final String title;
  final String? description;
  final DateTime? time;
  final bool isCompleted;
  final EventTask? taskRef;
  final TimelineItem? timelineRef;

  ExecutionItem({
    required this.id,
    required this.title,
    this.description,
    this.time,
    this.isCompleted = false,
    this.taskRef,
    this.timelineRef,
  });
}

class EventExecutionScreen extends StatefulWidget {
  final String eventId;

  const EventExecutionScreen({super.key, required this.eventId});

  @override
  State<EventExecutionScreen> createState() => _EventExecutionScreenState();
}

class _EventExecutionScreenState extends State<EventExecutionScreen> {
  bool _isLoading = true;
  final Set<String> _completedTimelineItems = {};

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    await Future.wait([
      context.read<EventTasksProvider>().fetchTasks(widget.eventId),
      context.read<TimelineProvider>().fetchTimeline(widget.eventId),
    ]);
    if (mounted) {
      setState(() => _isLoading = false);
    }
  }

  List<ExecutionItem> _buildUnifiedList(List<EventTask> tasks, List<TimelineItem> timeline) {
    List<ExecutionItem> items = [];

    for (var task in tasks) {
      items.add(ExecutionItem(
        id: task.id,
        title: task.title,
        description: task.description,
        time: task.dueDate,
        isCompleted: task.isCompleted,
        taskRef: task,
      ));
    }

    for (var tl in timeline) {
      items.add(ExecutionItem(
        id: tl.id,
        title: tl.title,
        description: tl.description,
        time: tl.startTime,
        isCompleted: _completedTimelineItems.contains(tl.id),
        timelineRef: tl,
      ));
    }

    // Sort by time, if no time push to the bottom
    items.sort((a, b) {
      if (a.time == null && b.time == null) return 0;
      if (a.time == null) return 1;
      if (b.time == null) return -1;
      return a.time!.compareTo(b.time!);
    });

    return items;
  }

  void _toggleItemStatus(ExecutionItem item, bool? newValue) async {
    final status = newValue ?? false;
    if (item.taskRef != null) {
      // It's a task, sync to backend
      final provider = context.read<EventTasksProvider>();
      await provider.toggleTask(item.taskRef!.id, widget.eventId, status);
    } else if (item.timelineRef != null) {
      // It's a timeline item, save locally
      setState(() {
        if (status) {
          _completedTimelineItems.add(item.id);
        } else {
          _completedTimelineItems.remove(item.id);
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text('Modo Ejecución'),
        backgroundColor: Colors.black87,
        foregroundColor: Colors.white,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Consumer2<EventTasksProvider, TimelineProvider>(
              builder: (context, tasksProvider, timelineProvider, child) {
                final items = _buildUnifiedList(tasksProvider.tasks, timelineProvider.items);
                
                if (items.isEmpty) {
                  return const Center(child: Text('No hay actividades para el día del evento'));
                }

                int completedCount = items.where((i) => i.isCompleted).length;
                double progress = items.isNotEmpty ? completedCount / items.length : 0;

                return Column(
                  children: [
                    // Sticky Header with Progress
                    Container(
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        boxShadow: [
                          BoxShadow(color: Colors.black12, blurRadius: 4, offset: Offset(0, 2))
                        ],
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text('Progreso del Evento', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
                              Text('${(progress * 100).toInt()}%', style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18, color: AppColors.primary)),
                            ],
                          ),
                          const SizedBox(height: 12),
                          LinearProgressIndicator(
                            value: progress,
                            backgroundColor: Colors.grey[200],
                            color: AppColors.primary,
                            minHeight: 10,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          const SizedBox(height: 8),
                          Text('$completedCount de ${items.length} tareas completadas', style: const TextStyle(color: Colors.grey)),
                        ],
                      ),
                    ),
                    
                    // Checklist List
                    Expanded(
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: items.length,
                        itemBuilder: (context, index) {
                          final item = items[index];
                          // Group by hour headers could be nice, but checking if time changed
                          
                          return Container(
                            margin: const EdgeInsets.only(bottom: 12),
                            decoration: BoxDecoration(
                              color: item.isCompleted ? Colors.grey[100] : Colors.white,
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(
                                color: item.isCompleted ? Colors.green.withOpacity(0.5) : Colors.transparent,
                                width: 2,
                              ),
                              boxShadow: item.isCompleted ? [] : AppDecorations.softShadow,
                            ),
                            child: CheckboxListTile(
                              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                              value: item.isCompleted,
                              onChanged: (val) => _toggleItemStatus(item, val),
                              activeColor: Colors.green,
                              checkColor: Colors.white,
                              controlAffinity: ListTileControlAffinity.leading, // Checkbox on the left
                              title: Text(
                                item.title,
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 18,
                                  decoration: item.isCompleted ? TextDecoration.lineThrough : null,
                                  color: item.isCompleted ? Colors.grey : Colors.black87,
                                ),
                              ),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (item.description != null && item.description!.isNotEmpty)
                                    Padding(
                                      padding: const EdgeInsets.only(top: 4, bottom: 8),
                                      child: Text(
                                        item.description!,
                                        style: TextStyle(
                                          color: item.isCompleted ? Colors.grey : Colors.black54,
                                        ),
                                      ),
                                    ),
                                  Row(
                                    children: [
                                      Icon(
                                        item.taskRef != null ? Icons.check_circle_outline : Icons.schedule,
                                        size: 16,
                                        color: item.isCompleted ? Colors.grey : AppColors.primary,
                                      ),
                                      const SizedBox(width: 6),
                                      Text(
                                        item.time != null 
                                            ? DateFormat('HH:mm a').format(item.time!)
                                            : 'Flexible',
                                        style: TextStyle(
                                          fontWeight: FontWeight.bold,
                                          color: item.isCompleted ? Colors.grey : AppColors.primary,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                        decoration: BoxDecoration(
                                          color: item.isCompleted ? Colors.grey[300] : Colors.grey[200],
                                          borderRadius: BorderRadius.circular(4),
                                        ),
                                        child: Text(
                                          item.taskRef != null ? 'Tarea' : 'Cronograma',
                                          style: const TextStyle(fontSize: 10),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                );
              },
            ),
    );
  }
}
