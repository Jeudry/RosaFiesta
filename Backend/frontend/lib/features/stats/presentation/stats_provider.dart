import 'package:flutter/material.dart';
import 'package:frontend/features/admin/data/stats_model.dart';
import 'package:frontend/features/admin/data/stats_repository.dart';

class StatsProvider extends ChangeNotifier {
  final StatsRepository _repository = StatsRepository();

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  AdminStats? _stats;
  AdminStats? get stats => _stats;

  Future<void> fetchStats() async {
    _isLoading = true;
    _error = null;
    notifyListeners();
    try {
      _stats = await _repository.getStats();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
