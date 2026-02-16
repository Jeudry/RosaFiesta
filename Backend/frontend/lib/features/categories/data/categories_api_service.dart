import '../../../core/api_client.dart';
import 'category_models.dart';

class CategoriesApiService {
  Future<List<Category>> getCategories() async {
    final data = await ApiClient.get('/categories');
    return (data as List).map((e) => Category.fromJson(e)).toList();
  }
}
