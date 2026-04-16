import 'package:flutter/foundation.dart';
import '../../../../core/api_client/api_client.dart';

class ProductsProvider extends ChangeNotifier {
  bool _loading = false;
  List<dynamic> _products = [];
  String? _searchQuery;
  String? _categoryId;
  int _page = 1;
  bool _hasMore = true;

  bool get loading => _loading;
  List<dynamic> get products => _products;
  bool get hasMore => _hasMore;

  Future<void> loadProducts({bool refresh = false}) async {
    if (_loading) return;
    if (refresh) {
      _page = 1;
      _products = [];
      _hasMore = true;
    }
    if (!_hasMore) return;

    _loading = true;
    notifyListeners();

    try {
      final response = await apiClient.getProducts(
        page: _page,
        search: _searchQuery,
        categoryId: _categoryId,
      );
      final data = response.data['data'] ?? [];
      if (refresh) {
        _products = data;
      } else {
        _products.addAll(data);
      }
      _hasMore = data.length >= 20;
      _page++;
    } catch (e) {
      // Handle error
    }

    _loading = false;
    notifyListeners();
  }

  Future<void> search(String query) async {
    _searchQuery = query.isEmpty ? null : query;
    _page = 1;
    _products = [];
    _hasMore = true;
    loadProducts(refresh: true);
  }

  void filterByCategory(String? categoryId) {
    _categoryId = categoryId;
    loadProducts(refresh: true);
  }

  Future<Map<String, dynamic>?> getProduct(String id) async {
    try {
      final response = await apiClient.getProduct(id);
      return response.data['data'];
    } catch (e) {
      return null;
    }
  }

  Future<String?> createProduct(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.createProduct(data);
      final product = response.data['data'];
      _products.insert(0, product);
      notifyListeners();
      return product['id'];
    } catch (e) {
      return null;
    }
  }

  Future<bool> updateProduct(String id, Map<String, dynamic> data) async {
    try {
      await apiClient.updateProduct(id, data);
      final idx = _products.indexWhere((p) => p['id'] == id);
      if (idx != -1) {
        _products[idx] = {..._products[idx], ...data};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> toggleProduct(String id, bool active) async {
    try {
      await apiClient.toggleProduct(id, active);
      final idx = _products.indexWhere((p) => p['id'] == id);
      if (idx != -1) {
        _products[idx] = {..._products[idx], 'is_active': active};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> deleteProduct(String id) async {
    try {
      await apiClient.deleteProduct(id);
      _products.removeWhere((p) => p['id'] == id);
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    }
  }
}
