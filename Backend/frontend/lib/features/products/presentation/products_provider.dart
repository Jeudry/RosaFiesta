import 'package:flutter/material.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../data/product_models.dart';
import '../data/products_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/product_cache_service.dart';

class ProductsProvider extends ChangeNotifier {
  final ProductsRepository _repository;

  ProductsProvider({ProductsRepository? repository})
      : _repository = repository ?? ProductsRepository();

  List<Product> _products = [];
  List<Product> get products => _products;

  String? _searchQuery;
  String? get searchQuery => _searchQuery;
  bool _isSearching = false;
  bool get isSearching => _isSearching;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  bool _isLoadingMore = false;
  bool get isLoadingMore => _isLoadingMore;

  bool _hasMore = true;
  bool get hasMore => _hasMore;

  bool _isOffline = false;
  bool get isOffline => _isOffline;

  static const int _pageSize = 20;
  int _offset = 0;

  String? _error;
  String? get error => _error;

  Product? _selectedProduct;
  Product? get selectedProduct => _selectedProduct;

  /// First page load (or pull-to-refresh when [refresh] is true).
  /// On first call, seeds from cache instantly for offline support.
  Future<void> fetchProducts({bool refresh = false}) async {
    if (_isLoading) return;
    if (refresh) {
      _offset = 0;
      _hasMore = true;
      _products = [];
    }

    // Seed from cache immediately if available
    if (_products.isEmpty) {
      final cached = await ProductCacheService.getCachedProducts();
      if (cached != null && cached.isNotEmpty) {
        _products = cached;
        _offset = cached.length;
        _hasMore = cached.length == _pageSize;
        notifyListeners();
      }
    }

    _setLoading(true);
    _error = null;
    try {
      final hasConnectivity = await _checkConnectivity();
      _isOffline = !hasConnectivity;

      if (hasConnectivity) {
        final page = await _repository.getProducts(
          limit: _pageSize,
          offset: 0,
        );
        _products = page;
        _offset = page.length;
        _hasMore = page.length == _pageSize;
        // Update cache
        await ProductCacheService.cacheProducts(page);
      }
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      // On network error, try to load from cache if still empty
      if (_products.isEmpty) {
        final cached = await ProductCacheService.getCachedProducts();
        if (cached != null && cached.isNotEmpty) {
          _products = cached;
          _offset = cached.length;
          _isOffline = true;
        }
      }
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

  Future<bool> _checkConnectivity() async {
    final result = await Connectivity().checkConnectivity();
    return result.isNotEmpty && !result.contains(ConnectivityResult.none);
  }

  void search(String query) {
    _searchQuery = query.trim();
    notifyListeners();
  }

  void clearSearch() {
    _searchQuery = null;
    notifyListeners();
  }

  List<Product> get displayProducts {
    if (_searchQuery == null || _searchQuery!.isEmpty) return _products;
    final q = _searchQuery!.toLowerCase();
    return _products.where((p) {
      final name = p.nameTemplate.toLowerCase();
      final desc = (p.descriptionTemplate ?? '').toLowerCase();
      return name.contains(q) || desc.contains(q);
    }).toList();
  }
}
