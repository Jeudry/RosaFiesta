import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import '../../../../core/api_client/api_client.dart';

class AuthProvider extends ChangeNotifier {
  bool _loading = false;
  String? _error;
  bool _isAuthenticated = false;
  Map<String, dynamic>? _user;

  bool get loading => _loading;
  String? get error => _error;
  bool get isAuthenticated => _isAuthenticated;
  Map<String, dynamic>? get user => _user;

  Future<bool> login(String email, String password) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      final response = await apiClient.login(email, password);
      final data = response.data['data'];
      await apiClient.saveToken(data['token']);
      _user = data['user'];
      _isAuthenticated = true;
      _loading = false;
      notifyListeners();
      return true;
    } on DioException catch (e) {
      _loading = false;
      if (e.response?.statusCode == 403) {
        _error = 'Acceso denegado. No tienes permisos de administrador.';
      } else if (e.response?.statusCode == 401) {
        _error = 'Email o contraseña incorrectos.';
      } else {
        _error = 'Error de conexión. Intenta de nuevo.';
      }
      notifyListeners();
      return false;
    } catch (e) {
      _loading = false;
      _error = 'Error inesperado.';
      notifyListeners();
      return false;
    }
  }

  Future<void> checkAuth() async {
    await apiClient.loadToken();
    if (apiClient.token != null) {
      // Token exists, try to validate it
      try {
        final response = await apiClient.getAdminProfile();
        _user = response.data['data'];
        _isAuthenticated = true;
      } catch (e) {
        await apiClient.clearToken();
        _isAuthenticated = false;
      }
    }
    notifyListeners();
  }

  Future<void> logout() async {
    await apiClient.clearToken();
    _isAuthenticated = false;
    _user = null;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
