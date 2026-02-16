import 'profile_api_service.dart';
import 'user_models.dart';

class ProfileRepository {
  final ProfileApiService _apiService = ProfileApiService();

  Future<UserProfile> getUserProfile(String userId) async {
    return await _apiService.getUserProfile(userId);
  }
}
