import 'package:flutter/foundation.dart';
import '../../../../core/api_client/api_client.dart';

class EventsProvider extends ChangeNotifier {
  bool _loading = false;
  List<dynamic> _events = [];
  String? _statusFilter;
  String? _searchQuery;
  int _page = 1;
  bool _hasMore = true;

  bool get loading => _loading;
  List<dynamic> get events => _events;
  String? get statusFilter => _statusFilter;
  bool get hasMore => _hasMore;

  Future<void> loadEvents({bool refresh = false}) async {
    if (_loading) return;
    if (refresh) {
      _page = 1;
      _events = [];
      _hasMore = true;
    }
    if (!_hasMore) return;

    _loading = true;
    notifyListeners();

    try {
      final response = await apiClient.getEvents(
        status: _statusFilter,
        page: _page,
      );
      final data = response.data['data'] ?? [];
      if (refresh) {
        _events = data;
      } else {
        _events.addAll(data);
      }
      _hasMore = data.length >= 20;
      _page++;
    } catch (e) {
      // Handle error
    }

    _loading = false;
    notifyListeners();
  }

  void setStatusFilter(String? status) {
    _statusFilter = status;
    loadEvents(refresh: true);
  }

  Future<void> search(String query) async {
    _searchQuery = query.isEmpty ? null : query;
    _page = 1;
    _events = [];
    _hasMore = true;
    loadEvents(refresh: true);
  }

  Future<Map<String, dynamic>?> getEvent(String id) async {
    try {
      final response = await apiClient.getEvent(id);
      return response.data['data'];
    } catch (e) {
      return null;
    }
  }

  Future<bool> updateEvent(String id, Map<String, dynamic> data) async {
    try {
      await apiClient.updateEvent(id, data);
      // Refresh list
      final idx = _events.indexWhere((e) => e['id'] == id);
      if (idx != -1) {
        _events[idx] = {..._events[idx], ...data};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> deleteEvent(String id) async {
    try {
      await apiClient.deleteEvent(id);
      _events.removeWhere((e) => e['id'] == id);
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<String?> createEvent(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.createEvent(data);
      final event = response.data['data'];
      _events.insert(0, event);
      notifyListeners();
      return event['id'];
    } catch (e) {
      return null;
    }
  }
}
