import 'package:flutter/material.dart';
import '../data/product_models.dart';
import '../data/reviews_repository.dart';
import '../data/products_api_service.dart';
import '../../../core/utils/error_translator.dart';

class ReviewsProvider extends ChangeNotifier {
  final ReviewsRepository _repository;

  ReviewsProvider({ReviewsRepository? repository})
      : _repository = repository ?? ReviewsRepository(ProductsApiService());

  List<Review> _reviews = [];
  List<Review> get reviews => _reviews;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Future<void> fetchReviews(String articleId) async {
    _setLoading(true);
    _error = null;
    try {
      _reviews = await _repository.getReviews(articleId);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<void> addReview(String articleId, int rating, String comment) async {
    _setLoading(true);
    _error = null;
    try {
      final newReview = await _repository.createReview(articleId, rating, comment);
      _reviews.insert(0, newReview);
      notifyListeners();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
