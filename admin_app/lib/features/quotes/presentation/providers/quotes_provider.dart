import 'package:flutter/foundation.dart';
import '../../../../core/api_client/api_client.dart';

class QuotesProvider extends ChangeNotifier {
  bool _loading = false;
  List<dynamic> _quotes = [];
  String? _statusFilter;
  int _page = 1;
  bool _hasMore = true;

  bool get loading => _loading;
  List<dynamic> get quotes => _quotes;
  String? get statusFilter => _statusFilter;
  bool get hasMore => _hasMore;

  Future<void> loadQuotes({bool refresh = false, String? status}) async {
    if (_loading) return;
    if (refresh) {
      _page = 1;
      _quotes = [];
      _hasMore = true;
    }
    if (!_hasMore) return;

    _loading = true;
    notifyListeners();

    try {
      final response = await apiClient.getQuotes(status: status ?? _statusFilter);
      final data = response.data['data'] ?? [];
      if (refresh) {
        _quotes = data;
      } else {
        _quotes.addAll(data);
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
    loadQuotes(refresh: true);
  }

  Future<String?> createQuote(Map<String, dynamic> data) async {
    try {
      final response = await apiClient.createQuote(data);
      final quote = response.data['data'];
      _quotes.insert(0, quote);
      notifyListeners();
      return quote['id'];
    } catch (e) {
      return null;
    }
  }

  Future<bool> adjustQuote(String eventId, Map<String, dynamic> data) async {
    try {
      await apiClient.adjustQuote(eventId, data);
      final idx = _quotes.indexWhere((q) => q['id'] == eventId);
      if (idx != -1) {
        _quotes[idx] = {..._quotes[idx], ...data, 'status': 'adjusted'};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> sendQuote(String eventId) async {
    try {
      await apiClient.sendQuote(eventId);
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> approveQuote(String eventId) async {
    try {
      await apiClient.approveQuote(eventId);
      final idx = _quotes.indexWhere((q) => q['id'] == eventId);
      if (idx != -1) {
        _quotes[idx] = {..._quotes[idx], 'status': 'paid'};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<bool> rejectQuote(String eventId) async {
    try {
      await apiClient.rejectQuote(eventId);
      final idx = _quotes.indexWhere((q) => q['id'] == eventId);
      if (idx != -1) {
        _quotes[idx] = {..._quotes[idx], 'status': 'rejected'};
        notifyListeners();
      }
      return true;
    } catch (e) {
      return false;
    }
  }
}
