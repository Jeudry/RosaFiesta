import '../../../core/api_client.dart';
import 'models.dart';

class AuthApiService {
  Future<AuthResponse> login(String email, String password) async {
    final data = await ApiClient.post('/authentication/token', {
      'email': email,
      'password': password,
    });
    return AuthResponse.fromJson(data);
  }

  Future<void> register(String username, String email, String password) async {
    await ApiClient.post('/authentication/register', {
      'username': username,
      'email': email,
      'password': password,
    });
  }

  Future<AuthResponse> refresh(String refreshToken) async {
    final data = await ApiClient.post('/authentication/refresh', {
      'refreshToken': refreshToken,
    });
    return AuthResponse.fromJson(data);
  }
}
