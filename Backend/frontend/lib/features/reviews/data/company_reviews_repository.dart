import 'reviews_api_service.dart';

class CompanyReviewsRepository {
  final ReviewsApiService _apiService;

  CompanyReviewsRepository({ReviewsApiService? apiService})
      : _apiService = apiService ?? ReviewsApiService();

  Future<List<Map<String, dynamic>>> getReviews() {
    return _apiService.getCompanyReviews();
  }

  Future<Map<String, dynamic>> getSummary() {
    return _apiService.getCompanyReviewsSummary();
  }

  Future<Map<String, dynamic>> createReview({
    required int rating,
    required String comment,
    String source = 'direct',
  }) {
    return _apiService.createCompanyReview(
      rating: rating,
      comment: comment,
      source: source,
    );
  }
}
