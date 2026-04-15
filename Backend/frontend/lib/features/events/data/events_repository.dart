import '../../../core/api_client.dart';
import 'event_model.dart';
import 'event_debrief_model.dart';
import 'event_photo_model.dart';
import 'event_review.dart';

class EventsRepository {
  Future<EventDebrief> getEventDebrief(String id) async {
    final response = await ApiClient.get('/events/$id/debrief');
    return EventDebrief.fromJson(response);
  }

  Future<List<Event>> getEvents() async {
    final response = await ApiClient.get('/events');
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

  Future<Event> payEvent(String id, String paymentMethod, {String? phone, bool isDeposit = false}) async {
    final response = await ApiClient.post('/events/$id/pay', {
      'payment_method': paymentMethod,
      if (phone != null && phone.isNotEmpty) 'phone': phone,
      'is_deposit': isDeposit,
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

  Future<List<EventItem>> getEventItems(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/items');
    final List<dynamic> data = response;
    return data.map((json) => EventItem.fromJson(json)).toList();
  }

  Future<EventItem> addEventItem(String eventId, Map<String, dynamic> itemData) async {
    final response = await ApiClient.post('/events/$eventId/items', itemData);
    return EventItem.fromJson(response);
  }

  Future<void> removeEventItem(String eventId, String itemId) async {
    await ApiClient.delete('/events/$eventId/items/$itemId');
  }

  Future<List<EventPhoto>> getEventPhotos(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/photos');
    final List<dynamic> data = response;
    return data.map((json) => EventPhoto.fromJson(json)).toList();
  }

  Future<EventReview> createEventReview(String eventId, int rating, String comment, {List<String>? photoURLs}) async {
    final response = await ApiClient.post('/events/$eventId/reviews', {
      'rating': rating,
      'comment': comment,
      if (photoURLs != null) 'photoURLs': photoURLs,
    });
    return EventReview.fromJson(response);
  }

  Future<List<EventReview>> getEventReviews(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/reviews');
    final List<dynamic> data = response;
    return data.map((json) => EventReview.fromJson(json)).toList();
  }

  Future<List<String>> getEventColors(String eventId) async {
    final response = await ApiClient.get('/events/$eventId/colors');
    final List<dynamic> data = response;
    return data.map((json) => json as String).toList();
  }

  Future<List<String>> setEventColors(String eventId, List<String> colors) async {
    final response = await ApiClient.put('/events/$eventId/colors', {'colors': colors});
    final List<dynamic> data = response;
    return data.map((json) => json as String).toList();
  }
}