import 'package:frontend/core/api_client.dart';
import 'stats_model.dart';

class StatsRepository {
  Future<AdminStats> getStats() async {
    final response = await ApiClient.get('/admin/stats');
    return AdminStats.fromJson(response);
  }
}
