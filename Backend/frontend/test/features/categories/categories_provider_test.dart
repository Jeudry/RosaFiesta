import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/categories/presentation/categories_provider.dart';
import 'package:frontend/features/categories/data/categories_repository.dart';
import 'package:frontend/features/categories/data/category_models.dart';

class MockCategoriesRepository extends Mock implements CategoriesRepository {}

void main() {
  late CategoriesProvider categoriesProvider;
  late MockCategoriesRepository mockCategoriesRepository;

  setUp(() {
    mockCategoriesRepository = MockCategoriesRepository();
    categoriesProvider = CategoriesProvider(repository: mockCategoriesRepository);
  });

  group('CategoriesProvider Tests', () {
    test('Initial state should be empty', () {
      expect(categoriesProvider.categories, isEmpty);
      expect(categoriesProvider.isLoading, false);
      expect(categoriesProvider.error, null);
    });

    test('fetchCategories success updates list', () async {
      // Arrange
      final List<Category> categories = [
        Category(id: '1', name: 'Cat 1', description: 'Desc 1', imageUrl: 'img1'),
        Category(id: '2', name: 'Cat 2', description: 'Desc 2', imageUrl: 'img2'),
      ];
      when(() => mockCategoriesRepository.getCategories())
          .thenAnswer((_) async => categories);

      // Act
      await categoriesProvider.fetchCategories();

      // Assert
      expect(categoriesProvider.categories.length, 2);
      expect(categoriesProvider.isLoading, false);
      expect(categoriesProvider.error, null);
    });

    test('fetchCategories failure sets error', () async {
      // Arrange
      const errorMessage = 'Failed to load';
      when(() => mockCategoriesRepository.getCategories())
          .thenThrow(Exception(errorMessage));

      // Act
      await categoriesProvider.fetchCategories();

      // Assert
      expect(categoriesProvider.categories, isEmpty);
      expect(categoriesProvider.isLoading, false);
      expect(categoriesProvider.error, contains(errorMessage));
    });
  });
}
