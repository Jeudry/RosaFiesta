import 'package:flutter/material.dart';
import '../data/product_models.dart';
import '../data/products_repository.dart';
import '../../../core/utils/error_translator.dart';

class ProductsProvider extends ChangeNotifier {
  final ProductsRepository _repository;

  ProductsProvider({ProductsRepository? repository}) 
      : _repository = repository ?? ProductsRepository();

  List<Product> _products = [];
  List<Product> get products => _products;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  bool _isLoadingMore = false;
  bool get isLoadingMore => _isLoadingMore;

  bool _hasMore = true;
  bool get hasMore => _hasMore;

  static const int _pageSize = 20;
  int _offset = 0;

  String? _error;
  String? get error => _error;

  Product? _selectedProduct;
  Product? get selectedProduct => _selectedProduct;

  /// First page load (or pull-to-refresh when [refresh] is true).
  Future<void> fetchProducts({bool refresh = false}) async {
    if (_isLoading) return;
    if (refresh) {
      _offset = 0;
      _hasMore = true;
      _products = [];
    }
    _setLoading(true);
    _error = null;
    try {
      final page = await _repository.getProducts(
        limit: _pageSize,
        offset: 0,
      );
      _products = page;
      _offset = page.length;
      _hasMore = page.length == _pageSize;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  /// Fetch the next page and append. No-op if already loading, no more pages,
  /// or an initial load is still in progress.
  Future<void> fetchMoreProducts() async {
    if (_isLoading || _isLoadingMore || !_hasMore) return;
    _isLoadingMore = true;
    notifyListeners();
    try {
      final page = await _repository.getProducts(
        limit: _pageSize,
        offset: _offset,
      );
      _products = [..._products, ...page];
      _offset += page.length;
      _hasMore = page.length == _pageSize;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _isLoadingMore = false;
      notifyListeners();
    }
  }

  Future<void> fetchProductsByCategory(String categoryId) async {
    _setLoading(true);
    _error = null;
    try {
      // The backend should have an endpoint for this, e.g., /categories/{id}/articles
      // Or we can filter client side if list is small, but better to query API.
      // Based on `categories.go`, there is `/categories/{categoryId}/articles`.
      _products = await _repository.getProductsByCategory(categoryId);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
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
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
