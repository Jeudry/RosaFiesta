import 'package:flutter/foundation.dart';
import '../../products/data/product_models.dart';
import '../../../core/services/hive_service.dart';
import 'favorites_api_service.dart';

class FavoritesRepository {
  final FavoritesApiService _apiService = FavoritesApiService();

  Future<List<Product>> list() => _apiService.list();
  Future<void> add(String articleId) => _apiService.add(articleId);
  Future<void> remove(String articleId) => _apiService.remove(articleId);

  /// Saves a favorite article ID to local Hive storage (works without login).
  Future<void> addFavoriteLocal(String articleId) async {
    final box = HiveService.localFavoritesBox;
    await box.put(articleId, articleId);
  }

  /// Removes a favorite article ID from local Hive storage.
  Future<void> removeFavoriteLocal(String articleId) async {
    final box = HiveService.localFavoritesBox;
    await box.delete(articleId);
  }

  /// Returns all locally-stored favorite article IDs as a Set.
  Set<String> getFavoriteIdsLocal() {
    final box = HiveService.localFavoritesBox;
    return box.values.cast<String>().toSet();
  }

  /// Syncs all local favorites to the server. Clears local Hive storage after
  /// successful sync. Failures are ignored silently.
  Future<void> syncLocalFavoritesToServer() async {
    final localIds = getFavoriteIdsLocal();
    if (localIds.isEmpty) return;

    final results = await Future.wait(
      localIds.map((id) async {
        try {
          await _apiService.add(id);
          return true;
        } catch (e) {
          debugPrint('syncLocalFavoritesToServer: failed to add $id: $e');
          return false;
        }
      }),
    );

    // Only clear local favorites that were successfully synced
    final syncedIds = <String>[];
    for (var i = 0; i < results.length; i++) {
      if (results[i]) syncedIds.add(localIds.elementAt(i));
    }

    final box = HiveService.localFavoritesBox;
    for (final id in syncedIds) {
      await box.delete(id);
    }
  }
}
