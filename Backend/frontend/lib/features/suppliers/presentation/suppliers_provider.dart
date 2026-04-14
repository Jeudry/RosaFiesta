import 'package:flutter/foundation.dart';
import '../data/supplier_model.dart';
import '../data/suppliers_repository.dart';

class SuppliersProvider extends ChangeNotifier {
  final SuppliersRepository _repository;

  SuppliersProvider({SuppliersRepository? repository})
      : _repository = repository ?? SuppliersRepository();

  List<Supplier> _suppliers = [];
  List<Supplier> get suppliers => _suppliers;

  bool _loading = false;
  bool get loading => _loading;

  String? _error;
  String? get error => _error;

  Future<void> loadSuppliers() async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _suppliers = await _repository.getSuppliers();
    } catch (e) {
      _error = e.toString();
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<bool> addSupplier({
    required String name,
    String? contactName,
    String? email,
    String? phone,
    String? website,
    String? notes,
  }) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      final supplier = await _repository.createSupplier({
        'name': name,
        if (contactName != null) 'contact_name': contactName,
        if (email != null) 'email': email,
        if (phone != null) 'phone': phone,
        if (website != null) 'website': website,
        if (notes != null) 'notes': notes,
      });
      _suppliers = [..._suppliers, supplier];
      _loading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _loading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateSupplier(
    String id, {
    String? name,
    String? contactName,
    String? email,
    String? phone,
    String? website,
    String? notes,
  }) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      final updated = await _repository.updateSupplier(id, {
        if (name != null) 'name': name,
        if (contactName != null) 'contact_name': contactName,
        if (email != null) 'email': email,
        if (phone != null) 'phone': phone,
        if (website != null) 'website': website,
        if (notes != null) 'notes': notes,
      });
      _suppliers = _suppliers.map((s) => s.id == id ? updated : s).toList();
      _loading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _loading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteSupplier(String id) async {
    try {
      await _repository.deleteSupplier(id);
      _suppliers = _suppliers.where((s) => s.id != id).toList();
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }
}
