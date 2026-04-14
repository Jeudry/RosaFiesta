import 'bundles_api_service.dart';
import 'bundle_model.dart';

class BundlesRepository {
  final BundlesApiService _apiService = BundlesApiService();

  Future<List<Bundle>> getBundles() async {
    return await _apiService.getBundles();
  }

  Future<Bundle> getBundle(String id) async {
    return await _apiService.getBundle(id);
  }

  Future<List<Bundle>> getBundlesByCategory(String categoryId) async {
    return await _apiService.getBundlesByCategory(categoryId);
  }
}
