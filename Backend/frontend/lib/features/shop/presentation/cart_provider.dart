import 'package:flutter/material.dart';
import '../data/cart_models.dart';
import '../data/cart_repository.dart';

class CartProvider extends ChangeNotifier {
  final CartRepository _repository = CartRepository();

  Cart? _cart;
  Cart? get cart => _cart;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  int get itemCount => _cart?.items.fold(0, (sum, item) => sum + item.quantity) ?? 0;
  double get total => _cart?.items.fold(0.0, (sum, item) => sum + item.totalPrice) ?? 0.0;

  Future<void> fetchCart() async {
    _setLoading(true);
    _error = null;
    try {
      _cart = await _repository.getCart();
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> addItem(String articleId, String? variantId, int quantity) async {
    // Optimistic UI update could be done here, but for simplicity
    // we'll wait for server response which returns the updated cart.
    _setLoading(true);
    _error = null;
    try {
      _cart = await _repository.addItem(articleId, variantId, quantity);
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> updateItem(String itemId, int quantity) async {
    _setLoading(true);
    _error = null;
    try {
      _cart = await _repository.updateItem(itemId, quantity);
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> removeItem(String itemId) async {
    _setLoading(true);
    _error = null;
    try {
      _cart = await _repository.removeItem(itemId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> clearCart() async {
    _setLoading(true);
    _error = null;
    try {
      await _repository.clearCart();
      _cart = null; // Or fetch empty cart?
      await fetchCart(); // Re-fetch to get clean state
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
