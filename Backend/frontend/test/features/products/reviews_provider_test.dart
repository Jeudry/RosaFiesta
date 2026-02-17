import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/products/presentation/reviews_provider.dart';
import 'package:frontend/features/products/data/reviews_repository.dart';
import 'package:frontend/features/products/data/product_models.dart';

class MockReviewsRepository extends Mock implements ReviewsRepository {}

void main() {
  late ReviewsProvider reviewsProvider;
  late MockReviewsRepository mockReviewsRepository;

  setUp(() {
    mockReviewsRepository = MockReviewsRepository();
    reviewsProvider = ReviewsProvider(repository: mockReviewsRepository);
  });

  Review createDummyReview(String id) {
    return Review(
      id: id,
      userId: 'user-$id',
      articleId: 'article-1',
      rating: 5,
      comment: 'Comment $id',
      created: DateTime.now(),
    );
  }

  group('ReviewsProvider Tests', () {
    test('Initial state should be empty', () {
      expect(reviewsProvider.reviews, isEmpty);
      expect(reviewsProvider.isLoading, false);
      expect(reviewsProvider.error, null);
    });

    test('fetchReviews success updates list', () async {
      // Arrange
      final reviews = [createDummyReview('1'), createDummyReview('2')];
      when(() => mockReviewsRepository.getReviews('article-1'))
          .thenAnswer((_) async => reviews);

      // Act
      await reviewsProvider.fetchReviews('article-1');

      // Assert
      expect(reviewsProvider.reviews.length, 2);
      expect(reviewsProvider.isLoading, false);
      expect(reviewsProvider.error, null);
    });

    test('addReview success inserts at top', () async {
      // Arrange
      final newReview = createDummyReview('new');
      when(() => mockReviewsRepository.createReview('article-1', 5, 'New!'))
          .thenAnswer((_) async => newReview);

      // Act
      await reviewsProvider.addReview('article-1', 5, 'New!');

      // Assert
      expect(reviewsProvider.reviews.first.id, 'new');
      expect(reviewsProvider.reviews.length, 1);
      verify(() => mockReviewsRepository.createReview('article-1', 5, 'New!')).called(1);
    });

    test('fetchReviews failure sets error', () async {
      // Arrange
      when(() => mockReviewsRepository.getReviews('article-1'))
          .thenThrow(Exception('API Error'));

      // Act
      await reviewsProvider.fetchReviews('article-1');

      // Assert
      expect(reviewsProvider.error, isNotNull);
      expect(reviewsProvider.isLoading, false);
    });
  });
}
