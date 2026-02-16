import '../../../core/api_client.dart';
import 'supplier_model.dart';

class SuppliersRepository {
  Future<List<Supplier>> getSuppliers() async {
    final response = await ApiClient.get('/v1/suppliers');
    final List<dynamic> data = response;
    return data.map((json) => Supplier.fromJson(json)).toList();
  }

  Future<Supplier> addSupplier(Supplier supplier) async {
    final response = await ApiClient.post('/v1/suppliers', supplier.toJson());
    return Supplier.fromJson(response);
  }

  Future<Supplier> updateSupplier(String id, Map<String, dynamic> updates) async {
    final response = await ApiClient.patch('/v1/suppliers/$id', updates);
    return Supplier.fromJson(response);
  }

  Future<void> deleteSupplier(String id) async {
    await ApiClient.delete('/v1/suppliers/$id');
  }
}
