import 'categories_api_service.dart';
import 'category_models.dart';

class CategoriesRepository {
  final CategoriesApiService _apiService = CategoriesApiService();

  Future<List<Category>> getCategories() async {
    return await _apiService.getCategories();
  }
}
