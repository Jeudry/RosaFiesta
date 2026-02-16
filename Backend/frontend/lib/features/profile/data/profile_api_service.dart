import '../../../core/api_client.dart';
import 'user_models.dart';

class ProfileApiService {
  Future<UserProfile> getUserProfile(String userId) async {
    final data = await ApiClient.get('/users/$userId');
    return UserProfile.fromJson(data);
  }
}
