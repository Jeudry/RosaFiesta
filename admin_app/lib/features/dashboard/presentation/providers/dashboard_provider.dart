import 'package:flutter/foundation.dart';
import '../../../../core/api_client/api_client.dart';

class DashboardProvider extends ChangeNotifier {
  bool _loading = false;
  Map<String, dynamic> _stats = {};
  List<dynamic> _todayEvents = [];
  List<dynamic> _weekEvents = [];
  List<dynamic> _alerts = [];
  List<dynamic> _recentActivity = [];

  bool get loading => _loading;
  Map<String, dynamic> get stats => _stats;
  List<dynamic> get todayEvents => _todayEvents;
  List<dynamic> get weekEvents => _weekEvents;
  List<dynamic> get alerts => _alerts;
  List<dynamic> get recentActivity => _recentActivity;

  Future<void> loadDashboard() async {
    _loading = true;
    notifyListeners();

    try {
      final responses = await Future.wait([
        apiClient.getEventsStats(),
        apiClient.getEventsToday(),
        apiClient.getEventsThisWeek(),
      ]);

      _stats = responses[0].data['data'] ?? {};
      _todayEvents = responses[1].data['data'] ?? [];
      _weekEvents = responses[2].data['data'] ?? [];

      // Build alerts from stats
      _alerts = [];
      if (_stats['overdue_payments'] != null && _stats['overdue_payments'] > 0) {
        _alerts.add({'type': 'payment', 'title': 'Pagos vencidos', 'count': _stats['overdue_payments']});
      }
      if (_stats['tomorrow_unconfirmed'] != null && _stats['tomorrow_unconfirmed'] > 0) {
        _alerts.add({'type': 'confirm', 'title': 'Eventos mañana sin confirmar', 'count': _stats['tomorrow_unconfirmed']});
      }
      if (_stats['low_stock_items'] != null && _stats['low_stock_items'] > 0) {
        _alerts.add({'type': 'stock', 'title': 'Stock bajo', 'count': _stats['low_stock_items']});
      }

      _loading = false;
      notifyListeners();
    } catch (e) {
      _loading = false;
      notifyListeners();
    }
  }

  int get todayCount => _todayEvents.length;
  int get weekCount => _weekEvents.length;
  int get monthRevenue => _stats['month_revenue'] ?? 0;
  int get pendingQuotes => _stats['pending_quotes'] ?? 0;
}
