import 'package:frontend/core/api_client.dart';

class ReviewsApiService {
  Future<List<Map<String, dynamic>>> getArticleReviews(String articleId) async {
    final data = await ApiClient.get('/articles/$articleId/reviews');
    return (data as List).cast<Map<String, dynamic>>();
  }

  Future<Map<String, dynamic>> createReview({
    required String articleId,
    required int rating,
    required String comment,
  }) async {
    final data = await ApiClient.post(
      '/articles/$articleId/reviews',
      {'rating': rating, 'comment': comment},
    );
    return data as Map<String, dynamic>;
  }

  Future<List<Map<String, dynamic>>> getCompanyReviews() async {
    final data = await ApiClient.get('/company/reviews');
    return (data as List).cast<Map<String, dynamic>>();
  }

  Future<Map<String, dynamic>> getCompanyReviewsSummary() async {
    final data = await ApiClient.get('/company/reviews/summary');
    return data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> createCompanyReview({
    required int rating,
    required String comment,
    String source = 'direct',
  }) async {
    final data = await ApiClient.post(
      '/company/reviews',
      {'rating': rating, 'comment': comment, 'source': source},
    );
    return data as Map<String, dynamic>;
  }
}
