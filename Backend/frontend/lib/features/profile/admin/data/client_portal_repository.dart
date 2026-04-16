import 'package:frontend/core/api_client.dart';

class ClientPortalRepository {
  Future<Map<String, dynamic>> getDashboard() async {
    return await ApiClient.get('/client-portal/dashboard');
  }

  Future<Map<String, dynamic>> getEventDetail(String eventId) async {
    return await ApiClient.get('/client-portal/events/$eventId');
  }

  Future<Map<String, dynamic>> getEventDocuments(String eventId) async {
    return await ApiClient.get('/client-portal/events/$eventId/documents');
  }

  Future<List<dynamic>> getEventPayments(String eventId) async {
    final data = await ApiClient.get('/client-portal/events/$eventId/payments');
    return data as List<dynamic>;
  }

  Future<List<dynamic>> getNotifications() async {
    final data = await ApiClient.get('/client-portal/notifications');
    return data as List<dynamic>;
  }
}
