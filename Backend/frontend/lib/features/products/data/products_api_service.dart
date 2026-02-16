import '../../../core/api_client.dart';
import 'product_models.dart';

class ProductsApiService {
  Future<List<Product>> getProducts() async {
    final data = await ApiClient.get('/articles');
    return (data as List).map((e) => Product.fromJson(e)).toList();
  }

  Future<Product> getProduct(String id) async {
    final data = await ApiClient.get('/articles/$id');
    return Product.fromJson(data);
  }
}
