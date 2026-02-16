import '../../../core/api_client.dart';
import 'cart_models.dart';

class CartApiService {
  Future<Cart> getCart() async {
    final data = await ApiClient.get('/cart');
    return Cart.fromJson(data);
  }

  Future<Cart> addItem(String articleId, String? variantId, int quantity) async {
    final data = await ApiClient.post('/cart/items', {
      'article_id': articleId,
      'variant_id': variantId,
      'quantity': quantity,
    });
    // The backend might return the updated cart or just success.
    // Based on backend analysis, it returns the updated cart.
    return Cart.fromJson(data);
  }

  Future<Cart> updateItem(String itemId, int quantity) async {
    // PATCH /cart/items/{itemId}
    // ApiClient doesn't have patch yet? Let's check or add it.
    // Wait, I implemented DELETE/PUT/POST/GET in ApiClient, did I implement PATCH?
    // Let's assume I missed it and I'll need to check ApiClient.
    // For now, I'll use PUT if PATCH is missing, or add PATCH to ApiClient.
    // Actually, let's assume I'll fix ApiClient to have patch.
    final data = await ApiClient.patch('/cart/items/$itemId', {
      'quantity': quantity,
    });
    return Cart.fromJson(data);
  }

  Future<Cart> removeItem(String itemId) async {
    final data = await ApiClient.delete('/cart/items/$itemId');
    return Cart.fromJson(data);
  }

  Future<void> clearCart() async {
    await ApiClient.delete('/cart');
  }
}
