import 'package:flutter/material.dart';
import '../data/user_models.dart';
import '../data/profile_repository.dart';

class ProfileProvider extends ChangeNotifier {
  final ProfileRepository _repository = ProfileRepository();

  UserProfile? _userProfile;
  UserProfile? get userProfile => _userProfile;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Future<void> fetchProfile(String userId) async {
    _setLoading(true);
    _error = null;
    try {
      _userProfile = await _repository.getUserProfile(userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  void clearProfile() {
    _userProfile = null;
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
