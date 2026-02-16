import 'package:flutter/material.dart';
import '../data/auth_repository.dart';
import '../data/models.dart';
import '../../../core/utils/error_translator.dart';

class AuthProvider extends ChangeNotifier {
  final AuthRepository _repository;

  AuthProvider({AuthRepository? repository}) 
      : _repository = repository ?? AuthRepository();
  
  bool _isLoading = false;
  bool get isLoading => _isLoading;

  User? _user;
  User? get user => _user;

  String? _error;
  String? get error => _error;

  bool get isAuthenticated => _user != null;

  Future<void> login(String email, String password) async {
    _setLoading(true);
    _error = null;
    try {
      final response = await _repository.login(email, password);
      // TODO: Fetch user profile with the token
      // For now, we just know we are authenticated
      _user = User(id: response.userId, email: email);
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
