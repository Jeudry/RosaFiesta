import 'package:flutter/foundation.dart';
import '../data/task_model.dart';
import '../data/tasks_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/notification_service.dart';
import '../../../core/services/hive_service.dart';
import '../../../core/services/sync_service.dart';
import '../../../core/models/sync_action.dart';

class EventTasksProvider with ChangeNotifier {
  final EventTasksRepository _repository;
  final NotificationService _notificationService = NotificationService();
  final SyncService _syncService = SyncService();
  
  List<EventTask> _tasks = [];
  bool _isLoading = false;
  String? _error;

  EventTasksProvider(this._repository);

  List<EventTask> get tasks => _tasks;
  bool get isLoading => _isLoading;
  String? get error => _error;

  int get completedCount => _tasks.where((t) => t.isCompleted).length;
  double get progress => _tasks.isEmpty ? 0 : completedCount / _tasks.length;

  Future<void> fetchTasks(String eventId) async {
    _isLoading = true;
    _error = null;
    
    // Initial load from Hive (Optimistic/Offline)
    _loadFromHive(eventId);
    notifyListeners();

    try {
      final fetchedTasks = await _repository.getTasks(eventId);
      _tasks = fetchedTasks;
      await _saveToHive(eventId, fetchedTasks);
      _syncNotifications();
    } catch (e) {
      // If API fails, we already have Hive data loaded
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void _loadFromHive(String eventId) {
    _tasks = HiveService.tasksBox.values
        .where((t) => t.eventId == eventId)
        .toList();
  }

  Future<void> _saveToHive(String eventId, List<EventTask> tasks) async {
    // Clear old tasks for this event
    final keysToDelete = HiveService.tasksBox.keys
        .where((key) => HiveService.tasksBox.get(key)?.eventId == eventId);
    await HiveService.tasksBox.deleteAll(keysToDelete);
    
    // Add new ones
    for (var task in tasks) {
      await HiveService.tasksBox.put(task.id, task);
    }
  }

  Future<bool> toggleTask(String taskId, String eventId, bool isCompleted) async {
    try {
      // Optimistic local update
      final taskIndex = _tasks.indexWhere((t) => t.id == taskId);
      if (taskIndex != -1) {
        final task = _tasks[taskIndex];
        final updatedTask = EventTask(
          id: task.id,
          eventId: task.eventId,
          title: task.title,
          description: task.description,
          isCompleted: isCompleted,
          dueDate: task.dueDate,
          createdAt: task.createdAt,
          updatedAt: DateTime.now(),
        );
        _tasks[taskIndex] = updatedTask;
        await HiveService.tasksBox.put(taskId, updatedTask);
        notifyListeners();
      }

      await _syncService.addAction(
        entityType: 'task',
        entityId: taskId,
        operation: SyncOperation.update,
        payload: {'is_completed': isCompleted},
      );
      
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  Future<bool> addTask(String eventId, EventTask task) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _syncService.addAction(
        entityType: 'task',
        entityId: task.id,
        operation: SyncOperation.create,
        payload: task.toJson(),
      );
      
      _tasks.add(task);
      await HiveService.tasksBox.put(task.id, task);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteTask(String taskId, String eventId) async {
    try {
      _tasks.removeWhere((t) => t.id == taskId);
      await HiveService.tasksBox.delete(taskId);
      notifyListeners();

      await _syncService.addAction(
        entityType: 'task',
        entityId: taskId,
        operation: SyncOperation.delete,
        payload: {},
      );
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  void _syncNotifications() {
    for (var task in _tasks) {
      if (!task.isCompleted && task.dueDate != null) {
        _notificationService.scheduleNotification(
          id: task.id.hashCode,
          title: 'Recordatorio de Tarea',
          body: 'La tarea "${task.title}" vence pronto.',
          scheduledDate: task.dueDate!,
        );
      } else {
        _notificationService.cancelNotification(task.id.hashCode);
      }
    }
  }
}
