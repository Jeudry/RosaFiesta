import 'package:flutter/material.dart';
import '../data/company_reviews_repository.dart';

class CompanyReview {
  final String id;
  final String userName;
  final String? avatar;
  final int rating;
  final String comment;
  final String source;
  final DateTime created;

  CompanyReview({
    required this.id,
    required this.userName,
    this.avatar,
    required this.rating,
    required this.comment,
    required this.source,
    required this.created,
  });

  factory CompanyReview.fromJson(Map<String, dynamic> json) {
    return CompanyReview(
      id: json['id'] as String,
      userName: (json['user']?['user_name'] ?? 'Usuario') as String,
      avatar: json['user']?['avatar'] as String?,
      rating: json['rating'] as int,
      comment: json['comment'] as String,
      source: json['source'] as String? ?? 'direct',
      created: DateTime.tryParse(json['created'] ?? '') ?? DateTime.now(),
    );
  }

  int get daysAgo => DateTime.now().difference(created).inDays;
}

class CompanyReviewsProvider extends ChangeNotifier {
  final CompanyReviewsRepository _repository;

  CompanyReviewsProvider({CompanyReviewsRepository? repository})
      : _repository = repository ?? CompanyReviewsRepository();

  List<CompanyReview> _reviews = [];
  List<CompanyReview> get reviews => _reviews;

  double _averageRating = 0;
  double get averageRating => _averageRating;

  int _reviewCount = 0;
  int get reviewCount => _reviewCount;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Future<void> fetchReviews() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final data = await _repository.getReviews();
      _reviews = data.map((json) => CompanyReview.fromJson(json)).toList();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchSummary() async {
    try {
      final summary = await _repository.getSummary();
      _averageRating = (summary['avg'] as num?)?.toDouble() ?? 0;
      _reviewCount = (summary['count'] as num?)?.toInt() ?? 0;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
    }
  }

  Future<bool> addReview(int rating, String comment, {String source = 'direct'}) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final data = await _repository.createReview(
        rating: rating,
        comment: comment,
        source: source,
      );
      _reviews.insert(0, CompanyReview.fromJson(data));
      _reviewCount++;
      // Recalculate average
      double total = 0;
      for (var r in _reviews) {
        total += r.rating;
      }
      _averageRating = total / _reviews.length;
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> fetchAll() async {
    await Future.wait([fetchReviews(), fetchSummary()]);
  }
}