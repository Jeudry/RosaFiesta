import 'dart:convert';
import 'package:flutter/foundation.dart';
import '../../products/data/product_models.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/hive_service.dart';
import '../data/favorites_repository.dart';

/// Manages user favorites, supporting both logged-in (server-backed) and
/// logged-out (local Hive-backed) modes.
///
/// When logged in: favorites are fetched from and synced to the server.
/// When logged out: favorites are stored locally in Hive and synced to the
/// server on the next login.
class FavoritesProvider extends ChangeNotifier {
  final FavoritesRepository _repository;

  FavoritesProvider({FavoritesRepository? repository})
      : _repository = repository ?? FavoritesRepository();

  List<Product> _favorites = [];
  List<Product> get favorites => _favorites;

  /// All known favorite IDs — union of server IDs (when logged in) and
  /// local IDs (always). Used by [isFavorite] to determine heart state.
  final Set<String> _favoriteIds = {};
  Set<String> get favoriteIds => _favoriteIds;

  /// Local-only favorite IDs, stored in Hive. Used to persist favorites
  /// across sessions when not logged in.
  Set<String> _localFavoriteIds = {};
  bool get hasLocalFavorites => _localFavoriteIds.isNotEmpty;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  /// Whether the user is currently authenticated.
  bool _isLoggedIn = false;
  bool get isLoggedIn => _isLoggedIn;

  /// Updates the login state. Called by [AuthProvider] on login/logout.
  void setLoggedIn(bool value) {
    if (_isLoggedIn == value) return;
    _isLoggedIn = value;
    if (value) {
      // Coming back online — load server favorites
      fetchFavorites();
    } else {
      // Going offline — load local favorites from Hive
      loadLocalFavorites();
    }
  }

  bool isFavorite(String articleId) => _favoriteIds.contains(articleId);

  /// Loads favorites from the server (logged-in path).
  Future<void> fetchFavorites() async {
    if (!_isLoggedIn) return;
    _setLoading(true);
    _error = null;
    try {
      _favorites = await _repository.list();
      _favoriteIds
        ..clear()
        ..addAll(_favorites.map((p) => p.id));
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  /// Loads locally-stored favorite IDs from Hive (offline path).
  void loadLocalFavorites() {
    _localFavoriteIds = _repository.getFavoriteIdsLocal();
    _favoriteIds
      ..clear()
      ..addAll(_localFavoriteIds);
    // Load cached local products for display
    _favorites = _getLocalFavoriteProducts();
    notifyListeners();
  }

  /// Retrieves full Product objects for locally-stored favorite IDs from
  /// the Hive cache. Products are stored as JSON strings.
  List<Product> _getLocalFavoriteProducts() {
    final box = HiveService.localFavoritesBox;
    final products = <Product>[];
    for (final id in _localFavoriteIds) {
      final jsonStr = box.get('product_$id');
      if (jsonStr != null) {
        try {
          final decoded = jsonDecode(jsonStr) as Map<String, dynamic>;
          products.add(Product.fromJson(decoded));
        } catch (_) {
          // Corrupt entry — skip
        }
      }
    }
    return products;
  }

  /// Toggles favorite state for the given product.
  ///
  /// When logged in: calls the server API (add or remove).
  /// When logged out: saves to Hive locally; full product is stored so it
  /// can be displayed without a network request.
  Future<void> toggle(Product product) async {
    final wasFavorite = _favoriteIds.contains(product.id);

    if (wasFavorite) {
      await _removeFavorite(product.id);
    } else {
      await _addFavorite(product);
    }
  }

  Future<void> _addFavorite(Product product) async {
    _favoriteIds.add(product.id);
    if (_isLoggedIn) {
      _favorites = [product, ..._favorites];
      notifyListeners();
      try {
        await _repository.add(product.id);
      } catch (e) {
        _favoriteIds.remove(product.id);
        _favorites.removeWhere((p) => p.id == product.id);
        _error = ErrorTranslator.translate(e.toString());
        notifyListeners();
      }
    } else {
      _localFavoriteIds.add(product.id);
      // Store product JSON in Hive for offline display
      final box = HiveService.localFavoritesBox;
      await box.put(product.id, product.id);
      await box.put('product_${product.id}', jsonEncode(product.toJson()));
      _favorites = [product, ..._favorites];
      notifyListeners();
    }
  }

  Future<void> _removeFavorite(String articleId) async {
    _favoriteIds.remove(articleId);
    _favorites.removeWhere((p) => p.id == articleId);
    notifyListeners();

    if (_isLoggedIn) {
      try {
        await _repository.remove(articleId);
      } catch (e) {
        // Rollback
        _favoriteIds.add(articleId);
        // Re-fetch to get the product back
        final rollback = _favorites.firstWhere((p) => p.id == articleId, orElse: () => _favorites.first);
        _favorites = [rollback, ..._favorites];
        _error = ErrorTranslator.translate(e.toString());
        notifyListeners();
      }
    } else {
      _localFavoriteIds.remove(articleId);
      final box = HiveService.localFavoritesBox;
      await box.delete(articleId);
      await box.delete('product_$articleId');
    }
  }

  /// Syncs locally-stored favorites to the server. Called automatically
  /// after a successful login. Clears Hive local favorites after sync.
  Future<void> syncOnLogin() async {
    if (!_isLoggedIn || _localFavoriteIds.isEmpty) return;
    await _repository.syncLocalFavoritesToServer();
    _localFavoriteIds.clear();
    // Re-fetch server favorites so the union is correct
    await fetchFavorites();
  }

  /// Clears in-memory state. Called after logout. Local Hive favorites are
  /// preserved so they can be restored on the next login.
  void clear() {
    _favorites = [];
    _favoriteIds.clear();
    _error = null;
    _isLoggedIn = false;
    // _localFavoriteIds is intentionally kept — Hive is the source of truth
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}