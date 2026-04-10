import 'package:flutter/material.dart';
import '../data/auth_repository.dart';
import '../data/models.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/firebase_service.dart';

class AuthProvider extends ChangeNotifier {
  final AuthRepository _repository;
  final FirebaseService _firebaseService;

  AuthProvider({AuthRepository? repository, FirebaseService? firebaseService}) 
      : _repository = repository ?? AuthRepository(),
        _firebaseService = firebaseService ?? FirebaseService();
  
  bool _isLoading = false;
  bool get isLoading => _isLoading;

  User? _user;
  User? get user => _user;

  String? _error;
  String? get error => _error;

  bool _initialized = false;
  bool get initialized => _initialized;

  bool get isAuthenticated => _user != null;

  /// Try to restore a previous session from secure storage.
  Future<void> tryRestoreSession() async {
    final token = await _repository.getAccessToken();
    if (token != null && token.isNotEmpty) {
      // Token exists - user was logged in before
      _user = User(id: '', email: '');
    }
    _initialized = true;
    notifyListeners();
  }

  Future<void> login(String email, String password) async {
    _setLoading(true);
    _error = null;
    try {
      final response = await _repository.login(email, password);
      // TODO: Fetch user profile with the token
      // For now, we just know we are authenticated
      _user = User(id: response.userId, email: email);

      // Phase 20: Sync FCM Token
      try {
        String? fcmToken = await _firebaseService.getToken();
        if (fcmToken != null) {
          await _repository.updateFCMToken(fcmToken);
        }
      } catch (e) {
        debugPrint("Error updating FCM token: $e");
      }

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
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
