import '../../../core/api_client.dart';
import 'task_model.dart';

class EventTasksRepository {
  Future<List<EventTask>> getTasks(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/tasks');
    final List<dynamic> data = response;
    return data.map((json) => EventTask.fromJson(json)).toList();
  }

  Future<EventTask> addTask(String eventId, EventTask task) async {
    final response = await ApiClient.post('/events/$eventId/tasks', task.toJson());
    return EventTask.fromJson(response);
  }

  Future<EventTask> updateTask(String taskId, Map<String, dynamic> updates) async {
    final response = await ApiClient.put('/tasks/$taskId', updates);
    return EventTask.fromJson(response);
  }

  Future<void> deleteTask(String taskId) async {
    await ApiClient.delete('/tasks/$taskId');
  }
}
