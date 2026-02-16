import '../../../../core/api_client.dart';
import 'timeline_model.dart';

class TimelineRepository {
  final ApiClient apiClient;

  TimelineRepository(this.apiClient);

  Future<List<TimelineItem>> getTimeline(String eventId) async {
    final response = await apiClient.get('/events/$eventId/timeline');
    return (response['data'] as List)
        .map((json) => TimelineItem.fromJson(json))
        .toList();
  }

  Future<TimelineItem> createItem(String eventId, Map<String, dynamic> data) async {
    final response = await apiClient.post('/events/$eventId/timeline', data: data);
    return TimelineItem.fromJson(response['data']);
  }

  Future<TimelineItem> updateItem(String itemId, Map<String, dynamic> data) async {
    final response = await apiClient.put('/timeline/$itemId', data: data);
    return TimelineItem.fromJson(response['data']);
  }

  Future<void> deleteItem(String itemId) async {
    await apiClient.delete('/timeline/$itemId');
  }
}
