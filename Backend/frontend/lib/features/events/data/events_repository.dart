import '../../../core/api_client.dart';
import 'event_model.dart';

class EventsRepository {
  Future<List<Event>> getEvents() async {
    final response = await ApiClient.get('/events');
    // Assuming backend returns a list directly or wrapped in data which ApiClient handles?
    // Based on previous patterns (products), ApiClient likely returns dynamic which we cast.
    // If ApiClient returns the data directly:
    final List<dynamic> data = response; 
    return data.map((json) => Event.fromJson(json)).toList();
  }

  Future<Event> getEvent(String id) async {
    final response = await ApiClient.get('/events/$id');
    return Event.fromJson(response);
  }

  Future<Event> createEvent(Map<String, dynamic> eventData) async {
    final response = await ApiClient.post('/events', eventData);
    return Event.fromJson(response);
  }

  Future<Event> updateEvent(String id, Map<String, dynamic> eventData) async {
    final response = await ApiClient.put('/events/$id', eventData);
    return Event.fromJson(response);
  }

  Future<void> deleteEvent(String id) async {
    await ApiClient.delete('/events/$id');
  }

  Future<Event> adjustQuote(String id, double additionalCosts, String adminNotes) async {
    final response = await ApiClient.patch('/events/$id/adjust', {
      'additional_costs': additionalCosts,
      'admin_notes': adminNotes,
    });
    return Event.fromJson(response);
  }

  Future<Event> requestQuote(String id) async {
    return await updateEvent(id, {'status': 'requested'});
  }

  Future<Event> confirmQuote(String id) async {
    return await updateEvent(id, {'status': 'confirmed'});
  }

  Future<Event> payEvent(String id, String paymentMethod) async {
    final response = await ApiClient.post('/events/$id/pay', {
      'payment_method': paymentMethod,
    });
    return Event.fromJson(response);
  }

  Future<List<dynamic>> getMessages(String eventId) async {
    return await ApiClient.get('/events/$eventId/messages');
  }

  Future<dynamic> sendMessage(String eventId, String content) async {
    return await ApiClient.post('/events/$eventId/messages', {
      'content': content,
    });
  }
}
