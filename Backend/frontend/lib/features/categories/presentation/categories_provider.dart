import 'package:flutter/material.dart';
import '../data/category_models.dart';
import '../data/categories_repository.dart';
import '../../../core/utils/error_translator.dart';

class CategoriesProvider extends ChangeNotifier {
  final CategoriesRepository _repository;

  CategoriesProvider({CategoriesRepository? repository}) 
      : _repository = repository ?? CategoriesRepository();

  List<Category> _categories = [];
  List<Category> get categories => _categories;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Future<void> fetchCategories() async {
    _setLoading(true);
    _error = null;
    try {
      _categories = await _repository.getCategories();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
