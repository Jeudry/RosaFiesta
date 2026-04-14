import 'product_models.dart';
import 'products_api_service.dart';

class ProductsRepository {
  final ProductsApiService _apiService = ProductsApiService();

  Future<List<Product>> getProducts({
    int limit = 20,
    int offset = 0,
    String? search,
    String? categoryId,
    bool availableOnly = false,
    String? sort,
  }) async {
    return await _apiService.getProducts(
      limit: limit,
      offset: offset,
      search: search,
      categoryId: categoryId,
      availableOnly: availableOnly,
      sort: sort,
    );
  }

  Future<Product> getProduct(String id) async {
    return await _apiService.getProduct(id);
  }

  Future<List<Product>> getProductsByCategory(String categoryId) async {
    return await _apiService.getProductsByCategory(categoryId);
  }
}