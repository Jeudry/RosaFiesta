import '../../../core/api_client.dart';
import '../../products/data/product_models.dart';

class FavoritesApiService {
  Future<List<Product>> list() async {
    final data = await ApiClient.get('/favorites');
    return (data as List).map((e) => Product.fromJson(e)).toList();
  }

  Future<void> add(String articleId) async {
    await ApiClient.post('/favorites/$articleId', {});
  }

  Future<void> remove(String articleId) async {
    await ApiClient.delete('/favorites/$articleId');
  }
}
