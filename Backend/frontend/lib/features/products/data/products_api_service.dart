import '../../../core/api_client.dart';
import 'product_models.dart';

class ProductsApiService {
  Future<List<Product>> getProducts({
    int limit = 20,
    int offset = 0,
    String? search,
    String? categoryId,
    bool availableOnly = false,
    String? sort,
  }) async {
    final params = <String, String>{};
    params['limit'] = limit.toString();
    params['offset'] = offset.toString();
    if (search != null && search.isNotEmpty) params['search'] = search;
    if (categoryId != null && categoryId.isNotEmpty) params['category_id'] = categoryId;
    if (availableOnly) params['available_only'] = 'true';
    if (sort != null && sort.isNotEmpty) params['sort'] = sort;

    final queryString = params.entries.map((e) => '${e.key}=${Uri.encodeComponent(e.value)}').join('&');
    final data = await ApiClient.get('/articles?$queryString');
    return (data as List).map((e) => Product.fromJson(e)).toList();
  }

  Future<Product> getProduct(String id) async {
    final data = await ApiClient.get('/articles/$id');
    return Product.fromJson(data);
  }

  Future<List<Product>> getProductsByCategory(String categoryId) async {
    final data = await ApiClient.get('/categories/$categoryId/articles');
    return (data as List).map((e) => Product.fromJson(e)).toList();
  }

  Future<List<Review>> getReviews(String articleId) async {
    final data = await ApiClient.get('/articles/$articleId/reviews');
    return (data as List).map((e) => Review.fromJson(e)).toList();
  }

  Future<Review> createReview(
      String articleId, int rating, String comment) async {
    final data = await ApiClient.post('/articles/$articleId/reviews', {
      'rating': rating,
      'comment': comment,
    });
    return Review.fromJson(data);
  }
}