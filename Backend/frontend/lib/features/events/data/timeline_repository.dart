import '../../../../core/api_client.dart';
import 'timeline_model.dart';

class TimelineRepository {
  TimelineRepository();

  Future<List<TimelineItem>> getTimeline(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/timeline');
    final List<dynamic> data = response;
    return data
        .map((json) => TimelineItem.fromJson(json))
        .toList();
  }

  Future<TimelineItem> createItem(String eventId, Map<String, dynamic> data) async {
    final response = await ApiClient.post('/events/$eventId/timeline', data);
    return TimelineItem.fromJson(response);
  }

  Future<TimelineItem> updateItem(String itemId, Map<String, dynamic> data) async {
    final response = await ApiClient.put('/timeline/$itemId', data);
    return TimelineItem.fromJson(response);
  }

  Future<void> deleteItem(String itemId) async {
    await ApiClient.delete('/timeline/$itemId');
  }
}
