import 'package:frontend/core/api_client.dart';
import 'bundle_model.dart';

class BundlesApiService {
  Future<List<Bundle>> getBundles() async {
    final data = await ApiClient.get('/bundles');
    return (data as List).map((e) => Bundle.fromJson(e)).toList();
  }

  Future<Bundle> getBundle(String id) async {
    final data = await ApiClient.get('/bundles/$id');
    return Bundle.fromJson(data);
  }

  Future<List<Bundle>> getBundlesByCategory(String categoryId) async {
    final data = await ApiClient.get('/bundles/category/$categoryId');
    return (data as List).map((e) => Bundle.fromJson(e)).toList();
  }
}
