import 'package:flutter/foundation.dart';
import '../data/supplier_model.dart';
import '../data/suppliers_repository.dart';
import '../../../core/utils/error_translator.dart';

class SuppliersProvider with ChangeNotifier {
  final SuppliersRepository _repository;
  List<Supplier> _suppliers = [];
  bool _isLoading = false;
  String? _error;

  SuppliersProvider(this._repository);

  List<Supplier> get suppliers => _suppliers;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchSuppliers() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _suppliers = await _repository.getSuppliers();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> addSupplier(Supplier supplier) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _repository.addSupplier(supplier);
      await fetchSuppliers();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateSupplier(String id, Map<String, dynamic> updates) async {
    try {
      await _repository.updateSupplier(id, updates);
      await fetchSuppliers();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteSupplier(String id) async {
    try {
      await _repository.deleteSupplier(id);
      await fetchSuppliers();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }
}
