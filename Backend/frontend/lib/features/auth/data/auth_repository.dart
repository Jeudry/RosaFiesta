import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'auth_api_service.dart';
import 'models.dart';

class AuthRepository {
  final AuthApiService _apiService = AuthApiService();
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<AuthResponse> login(String email, String password) async {
    final response = await _apiService.login(email, password);
    await _saveTokens(response.accessToken, response.refreshToken);
    return response;
  }

  Future<void> register(String username, String email, String password) async {
    await _apiService.register(username, email, password);
  }

  Future<void> logout() async {
    await _storage.deleteAll();
  }

  Future<String?> getAccessToken() async {
    return await _storage.read(key: 'access_token');
  }

  Future<String?> getRefreshToken() async {
    return await _storage.read(key: 'refresh_token');
  }

  Future<void> _saveTokens(String access, String refresh) async {
    await _storage.write(key: 'access_token', value: access);
    await _storage.write(key: 'refresh_token', value: refresh);
  }
}
