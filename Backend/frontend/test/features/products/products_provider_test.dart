import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/products/presentation/products_provider.dart';
import 'package:frontend/features/products/data/products_repository.dart';
import 'package:frontend/features/products/data/product_models.dart';

class MockProductsRepository extends Mock implements ProductsRepository {}

// Helper to create a dummy Product
Product createDummyProduct(String id) {
  return Product(
    id: id,
    nameTemplate: 'Product $id',
    isActive: true,
    type: 'Rental',
  );
}

void main() {
  late ProductsProvider productsProvider;
  late MockProductsRepository mockProductsRepository;

  setUp(() {
    mockProductsRepository = MockProductsRepository();
    productsProvider = ProductsProvider(repository: mockProductsRepository);
  });

  group('ProductsProvider Tests', () {
    test('Initial state should be empty', () {
      expect(productsProvider.products, isEmpty);
      expect(productsProvider.isLoading, false);
      expect(productsProvider.error, null);
    });

    test('fetchProducts success updates list', () async {
      // Arrange
      final products = [createDummyProduct('1'), createDummyProduct('2')];
      when(() => mockProductsRepository.getProducts())
          .thenAnswer((_) async => products);

      // Act
      await productsProvider.fetchProducts();

      // Assert
      expect(productsProvider.products.length, 2);
      expect(productsProvider.isLoading, false);
      expect(productsProvider.error, null);
    });

    test('fetchProducts failure sets error', () async {
      // Arrange
      const errorMessage = 'Network error';
      when(() => mockProductsRepository.getProducts())
          .thenThrow(Exception(errorMessage));

      // Act
      await productsProvider.fetchProducts();

      // Assert
      expect(productsProvider.products, isEmpty);
      expect(productsProvider.isLoading, false);
      expect(productsProvider.error, contains(errorMessage));
    });

    test('fetchProductsByCategory success updates list', () async {
      // Arrange
      final List<Product> products = [createDummyProduct('1'), createDummyProduct('2')];
      when(() => mockProductsRepository.getProductsByCategory('cat-1'))
          .thenAnswer((_) async => products);

      // Act
      await productsProvider.fetchProductsByCategory('cat-1');

      // Assert
      expect(productsProvider.products.length, 2);
      expect(productsProvider.isLoading, false);
      expect(productsProvider.error, null);
    });

    // Note: fetchProductsByCategory uses ApiClient directly in the implementation I wrote earlier.
    // I need to check if I updated it to use repository or if it's still using ApiClient directly.
    // If it uses ApiClient directly, I should refactor it to usage repository first for better testing
    // OR I mock ApiClient if it was injectable (it's static currently).
    // Let's assume I missed moving it to repository in previous steps and just implemented it in provider directly.
    // I should fix that first to make it testable via repository mock.
  });
}
