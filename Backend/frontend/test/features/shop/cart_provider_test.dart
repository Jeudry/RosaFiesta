import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/shop/presentation/cart_provider.dart';
import 'package:frontend/features/shop/data/cart_repository.dart';
import 'package:frontend/features/shop/data/cart_models.dart';

class MockCartRepository extends Mock implements CartRepository {}

void main() {
  late CartProvider cartProvider;
  late MockCartRepository mockCartRepository;

  setUp(() {
    mockCartRepository = MockCartRepository();
    cartProvider = CartProvider(repository: mockCartRepository);
  });

  group('CartProvider Tests', () {
    test('Initial state should be null', () {
      expect(cartProvider.cart, null);
      expect(cartProvider.isLoading, false);
      expect(cartProvider.error, null);
    });

    test('addItem success updates cart', () async {
      // Arrange
      final cart = Cart(
        id: 'cart-1', 
        userId: 'user-1',
        items: [], 
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );
      when(() => mockCartRepository.addItem('item-1', null, 1))
          .thenAnswer((_) async => cart);

      // Act
      await cartProvider.addItem('item-1', null, 1);

      // Assert
      expect(cartProvider.cart, cart);
      expect(cartProvider.isLoading, false);
      expect(cartProvider.error, null);
      verify(() => mockCartRepository.addItem('item-1', null, 1)).called(1);
    });

    test('addItem failure sets error', () async {
      // Arrange
      const errorMessage = 'Out of stock';
      when(() => mockCartRepository.addItem('item-1', null, 1))
          .thenThrow(Exception(errorMessage));

      // Act
      await cartProvider.addItem('item-1', null, 1);

      // Assert
      expect(cartProvider.cart, null);
      expect(cartProvider.isLoading, false);
      expect(cartProvider.error, contains('Producto agotado.'));
    });

    // Add more tests for removeItem, updateItem, clearCart, etc.
  });
}
