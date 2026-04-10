import '../../../core/api_client.dart';
import '../../events/data/event_model.dart';
import 'active_event_response.dart';

/// Talks to the backend `/events/active` endpoints. The draft event is
/// the user's "working basket" — the catalog quick-add writes here.
class ActiveEventApiService {
  /// Fetches the current draft event, creating one on demand if none exists.
  /// Returns both the event and its items so the caller doesn't have to
  /// issue two requests.
  Future<ActiveEventResponse> getActive() async {
    final data = await ApiClient.get('/events/active');
    return ActiveEventResponse.fromJson(data as Map<String, dynamic>);
  }

  /// Adds an item to the draft event. Backend upserts: calling this twice
  /// with the same (article, variant) increments the existing line.
  /// Returns the freshly-inserted/updated line.
  Future<EventItem> addItem({
    required String eventId,
    required String articleId,
    String? variantId,
    required int quantity,
    double? priceSnapshot,
  }) async {
    final data = await ApiClient.post('/events/$eventId/items', {
      'article_id': articleId,
      if (variantId != null) 'variant_id': variantId,
      'quantity': quantity,
      if (priceSnapshot != null) 'price_snapshot': priceSnapshot,
    });
    return EventItem.fromJson(data as Map<String, dynamic>);
  }

  /// Sets the absolute quantity for an item in the active draft.
  /// Passing 0 (or anything <= 0) deletes the line on the backend.
  Future<void> updateItemQuantity({
    required String itemId,
    required int quantity,
  }) async {
    await ApiClient.patch('/events/active/items/$itemId', {
      'quantity': quantity,
    });
  }

  /// Removes an item from the draft. This is a 204-returning endpoint
  /// that the store layer exposes via the generic event `/items` route.
  Future<void> removeItem({
    required String eventId,
    required String itemId,
  }) async {
    await ApiClient.delete('/events/$eventId/items/$itemId');
  }
}
