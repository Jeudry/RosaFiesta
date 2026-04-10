import 'package:flutter/material.dart';

import '../../products/data/product_models.dart';
import '../../../core/utils/error_translator.dart';
import '../data/favorites_repository.dart';

class FavoritesProvider extends ChangeNotifier {
  final FavoritesRepository _repository;

  FavoritesProvider({FavoritesRepository? repository})
      : _repository = repository ?? FavoritesRepository();

  List<Product> _favorites = [];
  List<Product> get favorites => _favorites;

  final Set<String> _favoriteIds = {};
  Set<String> get favoriteIds => _favoriteIds;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  bool isFavorite(String articleId) => _favoriteIds.contains(articleId);

  Future<void> fetchFavorites() async {
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

  /// Toggles favorite state for the given product. Optimistic: updates local
  /// state immediately and rolls back on failure.
  Future<void> toggle(Product product) async {
    final wasFavorite = _favoriteIds.contains(product.id);

    if (wasFavorite) {
      _favoriteIds.remove(product.id);
      _favorites.removeWhere((p) => p.id == product.id);
    } else {
      _favoriteIds.add(product.id);
      _favorites = [product, ..._favorites];
    }
    notifyListeners();

    try {
      if (wasFavorite) {
        await _repository.remove(product.id);
      } else {
        await _repository.add(product.id);
      }
    } catch (e) {
      // Rollback on failure
      if (wasFavorite) {
        _favoriteIds.add(product.id);
        _favorites = [product, ..._favorites];
      } else {
        _favoriteIds.remove(product.id);
        _favorites.removeWhere((p) => p.id == product.id);
      }
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
    }
  }

  /// Clears local state. Use after logout.
  void clear() {
    _favorites = [];
    _favoriteIds.clear();
    _error = null;
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
