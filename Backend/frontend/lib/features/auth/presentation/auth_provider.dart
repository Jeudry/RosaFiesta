import 'package:flutter/material.dart';
import '../data/auth_repository.dart';
import '../data/models.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/firebase_service.dart';
import '../../favorites/presentation/favorites_provider.dart';

class AuthProvider extends ChangeNotifier {
  final AuthRepository _repository;
  final FirebaseService _firebaseService;
  FavoritesProvider? _favoritesProvider;

  AuthProvider({AuthRepository? repository, FirebaseService? firebaseService})
      : _repository = repository ?? AuthRepository(),
        _firebaseService = firebaseService ?? FirebaseService();

  /// Registers the [FavoritesProvider] instance. Called once from [main.dart]
  /// after both providers are created.
  void registerFavoritesProvider(FavoritesProvider fp) {
    _favoritesProvider = fp;
  }

  /// Notifies [FavoritesProvider] of auth state changes.
  void _notifyFavorites(bool isLoggedIn) {
    _favoritesProvider?.setLoggedIn(isLoggedIn);
  }

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  User? _user;
  User? get user => _user;

  List<PendingEvent> _pendingEvents = [];
  List<PendingEvent> get pendingEvents => _pendingEvents;

  String? _error;
  String? get error => _error;

  bool _initialized = false;
  bool get initialized => _initialized;

  bool get isAuthenticated => _user != null;

  /// Try to restore a previous session from secure storage.
  Future<void> tryRestoreSession() async {
    final token = await _repository.getAccessToken();
    final userId = await _repository.getUserId();
    if (token != null && token.isNotEmpty && userId != null && userId.isNotEmpty) {
      // Token exists - restore session with proper user ID
      _user = User(id: userId, email: '');
      _notifyFavorites(true);
    }
    _initialized = true;
    notifyListeners();
  }

  Future<void> login(String email, String password) async {
    _setLoading(true);
    _error = null;
    try {
      final response = await _repository.login(email, password);
      _user = User(id: response.userId, email: email);
      _pendingEvents = response.pendingEvents;

      // Phase 20: Sync FCM Token
      try {
        String? fcmToken = await _firebaseService.getToken();
        if (fcmToken != null) {
          await _repository.updateFCMToken(fcmToken);
        }
      } catch (e) {
        debugPrint("Error updating FCM token: $e");
      }

      // Sync local favorites to server now that we're logged in
      _notifyFavorites(true);
      await _favoritesProvider?.syncOnLogin();

      notifyListeners();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<void> register(String username, String email, String password) async {
    _setLoading(true);
    _error = null;
    try {
      await _repository.register(username, email, password);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    _user = null;
    _favoritesProvider?.clear();
    _notifyFavorites(false);
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
