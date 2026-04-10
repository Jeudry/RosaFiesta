import 'product_models.dart';
import 'products_api_service.dart';

class ProductsRepository {
  final ProductsApiService _apiService = ProductsApiService();

  Future<List<Product>> getProducts({int limit = 20, int offset = 0}) async {
    return await _apiService.getProducts(limit: limit, offset: offset);
  }

  Future<Product> getProduct(String id) async {
    return await _apiService.getProduct(id);
  }

  Future<List<Product>> getProductsByCategory(String categoryId) async {
    return await _apiService.getProductsByCategory(categoryId);
  }
}
