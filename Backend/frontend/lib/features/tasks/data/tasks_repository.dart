import '../../../core/api/api_client.dart';
import 'task_model.dart';

class EventTasksRepository {
  Future<List<EventTask>> getTasks(String eventId) async {
    final response = await ApiClient.get('/v1/events/$eventId/tasks');
    final List<dynamic> data = response;
    return data.map((json) => EventTask.fromJson(json)).toList();
  }

  Future<EventTask> addTask(String eventId, EventTask task) async {
    final response = await ApiClient.post('/v1/events/$eventId/tasks', task.toJson());
    return EventTask.fromJson(response);
  }

  Future<EventTask> updateTask(String taskId, Map<String, dynamic> updates) async {
    final response = await ApiClient.put('/v1/tasks/$taskId', updates);
    return EventTask.fromJson(response);
  }

  Future<void> deleteTask(String taskId) async {
    await ApiClient.delete('/v1/tasks/$taskId');
  }
}
