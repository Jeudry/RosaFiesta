import 'product_models.dart';
import 'products_api_service.dart';

class ReviewsRepository {
  final ProductsApiService _apiService;

  ReviewsRepository(this._apiService);

  Future<List<Review>> getReviews(String articleId) {
    return _apiService.getReviews(articleId);
  }

  Future<Review> createReview(String articleId, int rating, String comment) {
    return _apiService.createReview(articleId, rating, comment);
  }
}
