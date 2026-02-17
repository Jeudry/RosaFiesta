import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/auth/presentation/auth_provider.dart';
import 'package:frontend/features/auth/data/auth_repository.dart';
import 'package:frontend/features/auth/data/models.dart';
import 'package:frontend/core/services/firebase_service.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

void main() {
  late AuthProvider authProvider;
  late MockAuthRepository mockAuthRepository;
  late MockFirebaseService mockFirebaseService;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    mockFirebaseService = MockFirebaseService();
    authProvider = AuthProvider(
      repository: mockAuthRepository,
      firebaseService: mockFirebaseService,
    );
    // Default stub for FCM token sync
    when(() => mockFirebaseService.getToken()).thenAnswer((_) async => null);
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
      expect(authProvider.error, contains('Credenciales inválidas. Por favor verifique su correo y contraseña.'));
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

    test('login syncs FCM token if available', () async {
      // Arrange
      final mockFirebaseService = MockFirebaseService();
      final authProviderWithFirebase = AuthProvider(
        repository: mockAuthRepository,
        firebaseService: mockFirebaseService,
      );

      when(() => mockAuthRepository.login(testEmail, testPassword))
          .thenAnswer((_) async => authResponse);
      when(() => mockFirebaseService.getToken())
          .thenAnswer((_) async => 'mock-fcm-token');
      when(() => mockAuthRepository.updateFCMToken('mock-fcm-token'))
          .thenAnswer((_) async => {});

      // Act
      await authProviderWithFirebase.login(testEmail, testPassword);

      // Assert
      verify(() => mockFirebaseService.getToken()).called(1);
      verify(() => mockAuthRepository.updateFCMToken('mock-fcm-token')).called(1);
    });
  });
}

class MockFirebaseService extends Mock implements FirebaseService {}
