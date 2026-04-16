import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ApiClient {
  static const String baseUrl = 'http://localhost:3000/v1';

  late final Dio _dio;
  String? _token;

  ApiClient() {
    _dio = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        if (_token != null) {
          options.headers['Authorization'] = 'Bearer $_token';
        }
        return handler.next(options);
      },
      onError: (error, handler) {
        if (error.response?.statusCode == 401) {
          _token = null;
          SharedPreferences.getInstance().then((prefs) => prefs.remove('admin_token'));
        }
        return handler.next(error);
      },
    ));
  }

  void setToken(String? token) {
    _token = token;
  }

  String? get token => _token;

  Future<void> loadToken() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('admin_token');
  }

  Future<void> saveToken(String token) async {
    _token = token;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('admin_token', token);
  }

  Future<void> clearToken() async {
    _token = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('admin_token');
  }

  // Auth
  Future<Response> login(String email, String password) async {
    return _dio.post('/authentication/login', data: {'email': email, 'password': password});
  }

  Future<Response> registerAdmin(String name, String email, String password) async {
    return _dio.post('/authentication/register', data: {
      'name': name,
      'email': email,
      'password': password,
    });
  }

  // Events
  Future<Response> getEvents({String? status, int page = 1, int limit = 20}) async {
    return _dio.get('/events', queryParameters: {
      if (status != null) 'status': status,
      'page': page,
      'limit': limit,
    });
  }

  Future<Response> getEvent(String id) async {
    return _dio.get('/events/$id');
  }

  Future<Response> createEvent(Map<String, dynamic> data) async {
    return _dio.post('/events', data: data);
  }

  Future<Response> deleteEvent(String id) async {
    return _dio.delete('/events/$id');
  }

  Future<Response> updateEvent(String id, Map<String, dynamic> data) async {
    return _dio.patch('/events/$id', data: data);
  }

  Future<Response> getEventsToday() async {
    return _dio.get('/events/stats/today');
  }

  Future<Response> getEventsThisWeek() async {
    return _dio.get('/events/stats/week');
  }

  Future<Response> getEventsStats() async {
    return _dio.get('/admin/events/stats');
  }

  Future<Response> getAuditLogs({int page = 1, int limit = 20, String? adminId, String? actionType}) async {
    return _dio.get('/admin/events/audit', queryParameters: {
      'page': page,
      'limit': limit,
      if (adminId != null) 'admin_id': adminId,
      if (actionType != null) 'action_type': actionType,
    });
  }

  // Quotes
  Future<Response> getQuotes({String? status}) async {
    return _dio.get('/events', queryParameters: {
      if (status != null) 'status': status,
      'quote_only': true,
    });
  }

  Future<Response> createQuote(Map<String, dynamic> data) async {
    return _dio.post('/admin/quotes', data: data);
  }

  Future<Response> adjustQuote(String eventId, Map<String, dynamic> data) async {
    return _dio.patch('/admin/events/$eventId/adjust', data: data);
  }

  Future<Response> sendQuote(String eventId) async {
    return _dio.post('/admin/events/$eventId/send-quote');
  }

  Future<Response> approveQuote(String eventId) async {
    return _dio.post('/events/$eventId/approve');
  }

  Future<Response> rejectQuote(String eventId) async {
    return _dio.post('/events/$eventId/reject');
  }

  // Clients
  Future<Response> getClients({int page = 1, int limit = 20, String? search}) async {
    return _dio.get('/users', queryParameters: {
      'page': page,
      'limit': limit,
      if (search != null) 'search': search,
    });
  }

  Future<Response> getClient(String id) async {
    return _dio.get('/users/$id');
  }

  Future<Response> updateClient(String id, Map<String, dynamic> data) async {
    return _dio.patch('/users/$id', data: data);
  }

  Future<Response> blockClient(String id) async {
    return _dio.post('/admin/users/$id/block');
  }

  Future<Response> createLead(Map<String, dynamic> data) async {
    return _dio.post('/admin/users/lead', data: data);
  }

  // Products
  Future<Response> getProducts({int page = 1, int limit = 20, String? search, String? categoryId}) async {
    return _dio.get('/articles', queryParameters: {
      'page': page,
      'limit': limit,
      if (search != null) 'search': search,
      if (categoryId != null) 'category_id': categoryId,
    });
  }

  Future<Response> getProduct(String id) async {
    return _dio.get('/articles/$id');
  }

  Future<Response> createProduct(Map<String, dynamic> data) async {
    return _dio.post('/admin/articles', data: data);
  }

  Future<Response> updateProduct(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/articles/$id', data: data);
  }

  Future<Response> deleteProduct(String id) async {
    return _dio.delete('/admin/articles/$id');
  }

  Future<Response> toggleProduct(String id, bool active) async {
    return _dio.patch('/admin/articles/$id', data: {'is_active': active});
  }

  Future<Response> bulkDeactivateProducts(List<String> ids, bool active) async {
    return _dio.post('/admin/articles/bulk-deactivate', data: {
      'article_ids': ids,
      'active': active,
    });
  }

  // Article Variants
  Future<Response> getArticleVariants(String articleId) async {
    return _dio.get('/admin/articles/$articleId/variants');
  }

  Future<Response> createArticleVariant(String articleId, Map<String, dynamic> data) async {
    return _dio.post('/admin/articles/$articleId/variants', data: data);
  }

  Future<Response> updateArticleVariant(String variantId, Map<String, dynamic> data) async {
    return _dio.patch('/admin/variants/$variantId', data: data);
  }

  Future<Response> deleteArticleVariant(String variantId) async {
    return _dio.delete('/admin/variants/$variantId');
  }

  // Bundle Items
  Future<Response> addBundleItem(String bundleId, Map<String, dynamic> data) async {
    return _dio.post('/admin/bundles/$bundleId/items', data: data);
  }

  Future<Response> removeBundleItem(String bundleId, String articleId) async {
    return _dio.delete('/admin/bundles/$bundleId/items/$articleId');
  }

  // Quote History
  Future<Response> getQuoteHistory({String? eventId, int page = 1, int limit = 20}) async {
    return _dio.get('/admin/quotes/history', queryParameters: {
      'page': page,
      'limit': limit,
      if (eventId != null) 'event_id': eventId,
    });
  }

  // Force Logout
  Future<Response> forceLogout(String userId) async {
    return _dio.post('/admin/users/$userId/force-logout');
  }

  // Categories
  Future<Response> getCategories() async {
    return _dio.get('/categories');
  }

  Future<Response> createCategory(Map<String, dynamic> data) async {
    return _dio.post('/admin/categories', data: data);
  }

  Future<Response> updateCategory(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/categories/$id', data: data);
  }

  Future<Response> deleteCategory(String id) async {
    return _dio.delete('/admin/categories/$id');
  }

  // Bundles
  Future<Response> getBundles() async {
    return _dio.get('/bundles');
  }

  Future<Response> getBundle(String bundleId) async {
    return _dio.get('/bundles/$bundleId');
  }

  Future<Response> createBundle(Map<String, dynamic> data) async {
    return _dio.post('/admin/bundles', data: data);
  }

  Future<Response> updateBundle(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/bundles/$id', data: data);
  }

  // Event Types
  Future<Response> getEventTypes() async {
    return _dio.get('/admin/event-types');
  }

  Future<Response> createEventType(Map<String, dynamic> data) async {
    return _dio.post('/admin/event-types', data: data);
  }

  Future<Response> updateEventType(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/event-types/$id', data: data);
  }

  Future<Response> deleteEventType(String id) async {
    return _dio.delete('/admin/event-types/$id');
  }

  // Maintenance Logs
  Future<Response> getMaintenanceLogs({String? status, String? type}) async {
    return _dio.get('/admin/maintenance', queryParameters: {
      if (status != null) 'status': status,
      if (type != null) 'type': type,
    });
  }

  Future<Response> createMaintenanceLog(Map<String, dynamic> data) async {
    return _dio.post('/admin/maintenance', data: data);
  }

  Future<Response> updateMaintenanceLog(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/maintenance/$id', data: data);
  }

  Future<Response> getMaintenanceOverdue() async {
    return _dio.get('/admin/maintenance/overdue');
  }

  Future<Response> getArticleMaintenance(String articleId) async {
    return _dio.get('/admin/articles/$articleId/maintenance');
  }

  // Recurring Events
  Future<Response> getRecurringEvents() async {
    return _dio.get('/admin/recurring');
  }

  Future<Response> createRecurringEvent(Map<String, dynamic> data) async {
    return _dio.post('/admin/recurring', data: data);
  }

  Future<Response> updateRecurringEvent(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/recurring/$id', data: data);
  }

  Future<Response> deleteRecurringEvent(String id) async {
    return _dio.delete('/admin/recurring/$id');
  }

  Future<Response> generateRecurringEvent(String id) async {
    return _dio.post('/admin/recurring/$id/generate');
  }

  // AI Config
  Future<Response> getAIConfig() async {
    return _dio.get('/admin/ai/config');
  }

  Future<Response> updateAIConfig(Map<String, dynamic> data) async {
    return _dio.patch('/admin/ai/config', data: data);
  }

  Future<Response> getAIHistory({int page = 1, int limit = 20}) async {
    return _dio.get('/admin/ai/history', queryParameters: {'page': page, 'limit': limit});
  }

  // Notifications
  Future<Response> getEmailTemplates() async {
    return _dio.get('/admin/notifications/email-templates');
  }

  Future<Response> updateEmailTemplate(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/notifications/email-templates/$id', data: data);
  }

  Future<Response> getWhatsAppTemplates() async {
    return _dio.get('/admin/notifications/whatsapp-templates');
  }

  Future<Response> updateWhatsAppTemplate(String id, Map<String, dynamic> data) async {
    return _dio.patch('/admin/notifications/whatsapp-templates/$id', data: data);
  }

  Future<Response> sendTestEmail(String templateId) async {
    return _dio.post('/admin/notifications/test-email', data: {'template_id': templateId});
  }

  Future<Response> getNotificationTriggers() async {
    return _dio.get('/admin/notifications/triggers');
  }

  Future<Response> updateNotificationTrigger(String id, bool enabled) async {
    return _dio.patch('/admin/notifications/triggers/$id', data: {'enabled': enabled});
  }

  // Analytics
  Future<Response> getMonthlyStats() async {
    return _dio.get('/admin/analytics/monthly');
  }

  Future<Response> getRevenueChart({int months = 12}) async {
    return _dio.get('/admin/analytics/revenue', queryParameters: {'months': months});
  }

  Future<Response> getTopProducts({int limit = 10}) async {
    return _dio.get('/admin/analytics/top-products', queryParameters: {'limit': limit});
  }

  Future<Response> getConversionRate() async {
    return _dio.get('/admin/analytics/conversion-rate');
  }

  Future<Response> getPendingPayments() async {
    return _dio.get('/admin/analytics/pending-payments');
  }

  Future<Response> exportCSV(String type) async {
    return _dio.get('/admin/analytics/export/$type', options: Options(responseType: ResponseType.plain));
  }

  Future<Response> getReportPDF() async {
    return _dio.get('/admin/analytics/report', options: Options(responseType: ResponseType.bytes));
  }

  // Config
  Future<Response> getDeliveryZones() async {
    return _dio.get('/admin/config/delivery-zones');
  }

  Future<Response> updateDeliveryZones(List<Map<String, dynamic>> zones) async {
    return _dio.patch('/admin/config/delivery-zones', data: {'zones': zones});
  }

  Future<Response> getPaymentMethods() async {
    return _dio.get('/admin/config/payment-methods');
  }

  Future<Response> updatePaymentMethods(List<Map<String, dynamic>> methods) async {
    return _dio.patch('/admin/config/payment-methods', data: {'methods': methods});
  }

  Future<Response> getAdminProfile() async {
    return _dio.get('/admin/profile');
  }

  Future<Response> updateAdminProfile(Map<String, dynamic> data) async {
    return _dio.patch('/admin/profile', data: data);
  }

  Future<Response> changePassword(String currentPassword, String newPassword) async {
    return _dio.post('/admin/profile/change-password', data: {
      'current_password': currentPassword,
      'new_password': newPassword,
    });
  }

  // Search
  Future<Response> searchClients(String query) async {
    return _dio.get('/users/search', queryParameters: {'q': query});
  }

  Future<Response> searchProducts(String query) async {
    return _dio.get('/articles/search', queryParameters: {'q': query});
  }
}

final apiClient = ApiClient();
