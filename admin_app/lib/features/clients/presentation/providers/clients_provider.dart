import 'package:flutter/foundation.dart';
import '../../../../core/api_client/api_client.dart';

class ClientsProvider extends ChangeNotifier {
  bool _loading = false;
  List<dynamic> _clients = [];
  String? _searchQuery;
  int _page = 1;
  bool _hasMore = true;

  bool get loading => _loading;
  List<dynamic> get clients => _clients;
  bool get hasMore => _hasMore;

  Future<void> loadClients({bool refresh = false}) async {
    if (_loading) return;
    if (refresh) {
      _page = 1;
      _clients = [];
      _hasMore = true;
    }
    if (!_hasMore) return;

    _loading = true;
    notifyListeners();

    try {
      final response = await apiClient.getClients(page: _page, search: _searchQuery);
      final data = response.data['data'] ?? [];
      if (refresh) {
        _clients = data;
      } else {
        _clients.addAll(data);
      }
      _hasMore = data.length >= 20;
      _page++;
    } catch (e) {
      // Handle error
    }

    _loading = false;
    notifyListeners();
  }

  Future<void> search(String query) async {
    _searchQuery = query.isEmpty ? null : query;
    _page = 1;
    _clients = [];
    _hasMore = true;
    loadClients(refresh: true);
  }

  Future<Map<String, dynamic>?> getClient(String id) async {
    try {
      final response = await apiClient.getClient(id);
      return response.data['data'];
    } catch (e) {
      return null;
    }
  }

  Future<bool> updateClient(String id, Map<String, dynamic> data) async {
    try {
      await apiClient.updateClient(id, data);
      final idx = _clients.indexWhere((c) => c['id'] == id);
      if (idx != -1) {
        _clients[idx] = {..._clients[idx], ...data};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> blockClient(String id, bool block) async {
    try {
      await apiClient.blockClient(id);
      final idx = _clients.indexWhere((c) => c['id'] == id);
      if (idx != -1) {
        _clients[idx] = {..._clients[idx], 'is_active': !block};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> forceLogout(String id) async {
    try {
      await apiClient.forceLogout(id);
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> addNote(String id, String note) async {
    try {
      await apiClient.updateClient(id, {'admin_note': note});
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<String?> createLead(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.createLead(data);
      final lead = response.data['data'];
      _clients.insert(0, lead);
      notifyListeners();
      return lead['id'];
    } catch (e) {
      return null;
    }
  }
}
