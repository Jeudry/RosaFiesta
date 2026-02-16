import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/auth/presentation/auth_provider.dart';
import 'package:frontend/features/auth/data/auth_repository.dart';
import 'package:frontend/features/auth/data/models.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

void main() {
  late AuthProvider authProvider;
  late MockAuthRepository mockAuthRepository;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    authProvider = AuthProvider(repository: mockAuthRepository);
  });

  group('AuthProvider Tests', () {
    const testEmail = 'test@example.com';
    const testPassword = 'password123';
    const userId = 'user-123';
    
    final authResponse = AuthResponse(
      accessToken: 'token',
      userId: userId,
      accessTokenExpirationTimestamp: 1234567890,
      refreshToken: 'refresh_token',
    );

    test('Initial state should be unauthenticated', () {
      expect(authProvider.isAuthenticated, false);
      expect(authProvider.user, null);
      expect(authProvider.isLoading, false);
      expect(authProvider.error, null);
    });

    test('login success updates user and notifies listeners', () async {
      // Arrange
      when(() => mockAuthRepository.login(testEmail, testPassword))
          .thenAnswer((_) async => authResponse);

      // Act
      await authProvider.login(testEmail, testPassword);

      // Assert
      expect(authProvider.isAuthenticated, true);
      expect(authProvider.user?.id, userId);
      expect(authProvider.user?.email, testEmail);
      expect(authProvider.isLoading, false);
      expect(authProvider.error, null);
      verify(() => mockAuthRepository.login(testEmail, testPassword)).called(1);
    });

    test('login failure sets error message', () async {
      // Arrange
      const errorMessage = 'Invalid credentials';
      when(() => mockAuthRepository.login(testEmail, testPassword))
          .thenThrow(Exception(errorMessage));

      // Act
      await authProvider.login(testEmail, testPassword);

      // Assert
      expect(authProvider.isAuthenticated, false);
      expect(authProvider.user, null);
      expect(authProvider.isLoading, false);
      expect(authProvider.error, contains(errorMessage));
    });

    test('logout clears user state', () async {
      // Arrange
      when(() => mockAuthRepository.login(testEmail, testPassword))
          .thenAnswer((_) async => authResponse);
      when(() => mockAuthRepository.logout()).thenAnswer((_) async => {});

      // Login first
      await authProvider.login(testEmail, testPassword);
      expect(authProvider.isAuthenticated, true);

      // Act
      await authProvider.logout();

      // Assert
      expect(authProvider.isAuthenticated, false);
      expect(authProvider.user, null);
      verify(() => mockAuthRepository.logout()).called(1);
    });
  });
}
