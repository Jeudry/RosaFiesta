import '../../../core/api_client.dart';
import 'supplier_model.dart';

class SuppliersRepository {
  Future<List<Supplier>> getSuppliers() async {
    final response = await ApiClient.get('/suppliers');
    final List<dynamic> data = response;
    return data.map((json) => Supplier.fromJson(json)).toList();
  }

  Future<Supplier> getSupplier(String id) async {
    final response = await ApiClient.get('/suppliers/$id');
    return Supplier.fromJson(response);
  }

  Future<Supplier> createSupplier(Map<String, dynamic> data) async {
    final response = await ApiClient.post('/suppliers', data);
    return Supplier.fromJson(response);
  }

  Future<Supplier> updateSupplier(String id, Map<String, dynamic> data) async {
    final response = await ApiClient.patch('/suppliers/$id', data);
    return Supplier.fromJson(response);
  }

  Future<void> deleteSupplier(String id) async {
    await ApiClient.delete('/suppliers/$id');
  }
}
