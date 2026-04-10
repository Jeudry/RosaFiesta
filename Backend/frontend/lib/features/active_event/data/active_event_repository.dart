import '../../events/data/event_model.dart';
import 'active_event_api_service.dart';
import 'active_event_response.dart';

class ActiveEventRepository {
  final ActiveEventApiService _apiService;

  ActiveEventRepository({ActiveEventApiService? apiService})
      : _apiService = apiService ?? ActiveEventApiService();

  Future<ActiveEventResponse> getActive() => _apiService.getActive();

  Future<EventItem> addItem({
    required String eventId,
    required String articleId,
    String? variantId,
    required int quantity,
    double? priceSnapshot,
  }) =>
      _apiService.addItem(
        eventId: eventId,
        articleId: articleId,
        variantId: variantId,
        quantity: quantity,
        priceSnapshot: priceSnapshot,
      );

  Future<void> updateItemQuantity({
    required String itemId,
    required int quantity,
  }) =>
      _apiService.updateItemQuantity(itemId: itemId, quantity: quantity);

  Future<void> removeItem({
    required String eventId,
    required String itemId,
  }) =>
      _apiService.removeItem(eventId: eventId, itemId: itemId);
}
