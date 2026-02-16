import 'package:flutter/material.dart';
import '../data/product_models.dart';
import '../data/products_repository.dart';

class ProductsProvider extends ChangeNotifier {
  final ProductsRepository _repository = ProductsRepository();

  List<Product> _products = [];
  List<Product> get products => _products;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Product? _selectedProduct;
  Product? get selectedProduct => _selectedProduct;

  Future<void> fetchProducts() async {
    _setLoading(true);
    _error = null;
    try {
      _products = await _repository.getProducts();
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> fetchProductsByCategory(String categoryId) async {
    _setLoading(true);
    _error = null;
    try {
      // The backend should have an endpoint for this, e.g., /categories/{id}/articles
      // Or we can filter client side if list is small, but better to query API.
      // Based on `categories.go`, there is `/categories/{categoryId}/articles`.
      final data = await ApiClient.get('/categories/$categoryId/articles');
      _products = (data as List).map((e) => Product.fromJson(e)).toList();
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> fetchProductDetails(String id) async {
    _setLoading(true);
    _error = null;
    try {
      _selectedProduct = await _repository.getProduct(id);
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
