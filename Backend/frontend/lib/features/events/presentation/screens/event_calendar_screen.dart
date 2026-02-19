import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:table_calendar/table_calendar.dart';
import '../../data/event_model.dart';
import '../events_provider.dart';
import '../../../tasks/presentation/tasks_provider.dart';
import 'event_detail_screen.dart';

class EventCalendarScreen extends StatefulWidget {
  const EventCalendarScreen({super.key});

  @override
  State<EventCalendarScreen> createState() => _EventCalendarScreenState();
}

class _EventCalendarScreenState extends State<EventCalendarScreen> {
  CalendarFormat _calendarFormat = CalendarFormat.month;
  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<EventsProvider>().fetchEvents();
      // Optional: fetch tasks if we want to show deadlines
      // context.read<EventTasksProvider>().fetchTasks(); 
    });
  }

  List<dynamic> _getEventsForDay(DateTime day) {
    final events = context.read<EventsProvider>().events;
    final tasks = context.read<EventTasksProvider>().tasks;

    final List<dynamic> dayItems = [];

    // Filter events for this day
    dayItems.addAll(events.where((event) => isSameDay(event.date, day)));

    // Filter tasks with deadlines for this day
    dayItems.addAll(tasks.where((task) => 
        task.dueDate != null && isSameDay(task.dueDate!, day)));

    return dayItems;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Calendario de Eventos'),
      ),
      body: Column(
        children: [
          TableCalendar(
            firstDay: DateTime.utc(2020, 1, 1),
            lastDay: DateTime.utc(2030, 12, 31),
            focusedDay: _focusedDay,
            calendarFormat: _calendarFormat,
            selectedDayPredicate: (day) {
              return isSameDay(_selectedDay, day);
            },
            onDaySelected: (selectedDay, focusedDay) {
              if (!isSameDay(_selectedDay, selectedDay)) {
                setState(() {
                  _selectedDay = selectedDay;
                  _focusedDay = focusedDay;
                });
              }
            },
            onFormatChanged: (format) {
              if (_calendarFormat != format) {
                setState(() {
                  _calendarFormat = format;
                });
              }
            },
            onPageChanged: (focusedDay) {
              _focusedDay = focusedDay;
            },
            eventLoader: _getEventsForDay,
            calendarStyle: const CalendarStyle(
              markersMaxCount: 3,
              markerDecoration: BoxDecoration(
                color: Colors.blue,
                shape: BoxShape.circle,
              ),
              todayDecoration: BoxDecoration(
                color: Colors.blueAccent,
                shape: BoxShape.circle,
              ),
              selectedDecoration: BoxDecoration(
                color: Colors.blue,
                shape: BoxShape.circle,
              ),
            ),
          ),
          const SizedBox(height: 8.0),
          Expanded(
            child: _buildDayItemList(),
          ),
        ],
      ),
    );
  }

  Widget _buildDayItemList() {
    final items = _getEventsForDay(_selectedDay!);

    if (items.isEmpty) {
      return const Center(
        child: Text('No hay eventos ni tareas para este día.'),
      );
    }

    return ListView.builder(
      itemCount: items.length,
      itemBuilder: (context, index) {
        final item = items[index];
        if (item is Event) {
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
            child: ListTile(
              leading: const Icon(Icons.event, color: Colors.blue),
              title: Text(item.name),
              subtitle: Text('Evento - ${item.status}'),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => EventDetailScreen(eventId: item.id),
                  ),
                );
              },
            ),
          );
        } else {
          // It's a task
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
            child: ListTile(
              leading: const Icon(Icons.task, color: Colors.orange),
              title: Text(item.title),
              subtitle: const Text('Fecha límite de tarea'),
              onTap: () {
                // Future: Navigate to task details or event details containing the task
              },
            ),
          );
        }
      },
    );
  }
}
