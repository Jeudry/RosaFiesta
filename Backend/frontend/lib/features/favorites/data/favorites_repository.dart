import '../../products/data/product_models.dart';
import 'favorites_api_service.dart';

class FavoritesRepository {
  final FavoritesApiService _apiService = FavoritesApiService();

  Future<List<Product>> list() => _apiService.list();
  Future<void> add(String articleId) => _apiService.add(articleId);
  Future<void> remove(String articleId) => _apiService.remove(articleId);
}
