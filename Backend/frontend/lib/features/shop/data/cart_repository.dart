import 'cart_api_service.dart';
import 'cart_models.dart';

class CartRepository {
  final CartApiService _apiService = CartApiService();

  Future<Cart> getCart() async {
    return await _apiService.getCart();
  }

  Future<Cart> addItem(String articleId, String? variantId, int quantity) async {
    return await _apiService.addItem(articleId, variantId, quantity);
  }

  Future<Cart> updateItem(String itemId, int quantity) async {
    return await _apiService.updateItem(itemId, quantity);
  }

  Future<Cart> removeItem(String itemId) async {
    return await _apiService.removeItem(itemId);
  }

  Future<void> clearCart() async {
    await _apiService.clearCart();
  }
}
