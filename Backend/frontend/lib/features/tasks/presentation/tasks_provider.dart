import 'package:flutter/foundation.dart';
import '../data/task_model.dart';
import '../data/tasks_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/notification_service.dart';

class EventTasksProvider with ChangeNotifier {
  final EventTasksRepository _repository;
  final NotificationService _notificationService = NotificationService();
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
    notifyListeners();

    try {
      _tasks = await _repository.getTasks(eventId);
      _syncNotifications();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> addTask(String eventId, EventTask task) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _repository.addTask(eventId, task);
      await fetchTasks(eventId);
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> toggleTask(String taskId, String eventId, bool isCompleted) async {
    try {
      await _repository.updateTask(taskId, {'is_completed': isCompleted});
      await fetchTasks(eventId);
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteTask(String taskId, String eventId) async {
    try {
      await _repository.deleteTask(taskId);
      await fetchTasks(eventId);
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
