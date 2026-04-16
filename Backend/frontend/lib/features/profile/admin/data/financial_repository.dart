import 'package:frontend/core/api_client.dart';

class FinancialRepository {
  Future<List<dynamic>> getCategories() async {
    final data = await ApiClient.get('/financial/categories');
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createCategory(Map<String, dynamic> body) async {
    return await ApiClient.post('/financial/categories', body);
  }

  Future<List<dynamic>> getRecords({
    required String startDate,
    required String endDate,
    String? type,
    String? categoryId,
  }) async {
    String path = '/financial/records?start_date=$startDate&end_date=$endDate';
    if (type != null) path += '&type=$type';
    if (categoryId != null) path += '&category_id=$categoryId';
    final data = await ApiClient.get(path);
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createRecord(Map<String, dynamic> body) async {
    return await ApiClient.post('/financial/records', body);
  }

  Future<void> reconcileRecord(String recordId) async {
    await ApiClient.post('/financial/records/$recordId/reconcile', {});
  }

  Future<Map<String, dynamic>> getSummary({
    String? startDate,
    String? endDate,
  }) async {
    String path = '/financial/summary';
    if (startDate != null && endDate != null) {
      path += '?start_date=$startDate&end_date=$endDate';
    }
    return await ApiClient.get(path);
  }

  Future<List<dynamic>> getIncomeByCategory({
    String? startDate,
    String? endDate,
  }) async {
    String path = '/financial/income-by-category';
    if (startDate != null && endDate != null) {
      path += '?start_date=$startDate&end_date=$endDate';
    }
    final data = await ApiClient.get(path);
    return data as List<dynamic>;
  }

  Future<List<dynamic>> getExpensesByCategory({
    String? startDate,
    String? endDate,
  }) async {
    String path = '/financial/expenses-by-category';
    if (startDate != null && endDate != null) {
      path += '?start_date=$startDate&end_date=$endDate';
    }
    final data = await ApiClient.get(path);
    return data as List<dynamic>;
  }

  Future<List<dynamic>> getInvoices({String? clientId, String? status}) async {
    String path = '/financial/invoices';
    if (clientId != null || status != null) {
      path += '?';
      if (clientId != null) path += 'client_id=$clientId';
      if (status != null) path += '&status=$status';
    }
    final data = await ApiClient.get(path);
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createInvoice(Map<String, dynamic> body) async {
    return await ApiClient.post('/financial/invoices', body);
  }

  Future<List<dynamic>> getVendors() async {
    final data = await ApiClient.get('/financial/vendors');
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createVendor(Map<String, dynamic> body) async {
    return await ApiClient.post('/financial/vendors', body);
  }

  Future<List<dynamic>> getVendorPayments(String vendorId) async {
    final data = await ApiClient.get('/financial/vendors/$vendorId/payments');
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createVendorPayment(
    String vendorId,
    Map<String, dynamic> body,
  ) async {
    return await ApiClient.post('/financial/vendors/$vendorId/payments', body);
  }
}

class InsuranceRepository {
  Future<List<dynamic>> getArticleInsurance(String articleId) async {
    final data = await ApiClient.get(
      '/financial/insurance/articles?article_id=$articleId',
    );
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createArticleInsurance(
    Map<String, dynamic> body,
  ) async {
    return await ApiClient.post('/financial/insurance/articles', body);
  }

  Future<List<dynamic>> getEventInsurance(String eventId) async {
    final data = await ApiClient.get(
      '/financial/insurance/event?event_id=$eventId',
    );
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createEventInsurance(
    String eventId,
    Map<String, dynamic> body,
  ) async {
    return await ApiClient.post(
      '/financial/insurance/event?event_id=$eventId',
      body,
    );
  }

  Future<List<dynamic>> getClaims({String? status}) async {
    String path = '/financial/insurance/claims';
    if (status != null) path += '?status=$status';
    final data = await ApiClient.get(path);
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> createClaim(Map<String, dynamic> body) async {
    return await ApiClient.post('/financial/insurance/claims', body);
  }

  Future<void> updateClaimStatus(
    String claimId,
    Map<String, dynamic> body,
  ) async {
    await ApiClient.patch('/financial/insurance/claims/$claimId/status', body);
  }

  Future<List<dynamic>> getAllArticleInsurance() async {
    final data = await ApiClient.get('/financial/insurance/all');
    return data as List<dynamic>;
  }
}

class AuditRepository {
  Future<List<dynamic>> getClientAuditLog(String userId) async {
    final data = await ApiClient.get('/financial/audit/client/$userId');
    return data as List<dynamic>;
  }

  Future<Map<String, dynamic>> getAllAuditLogs({
    String? userId,
    String? action,
    String? entityType,
    String? startDate,
    String? endDate,
  }) async {
    String path = '/financial/audit?';
    final params = <String>[];
    if (userId != null) params.add('user_id=$userId');
    if (action != null) params.add('action=$action');
    if (entityType != null) params.add('entity_type=$entityType');
    if (startDate != null) params.add('start_date=$startDate');
    if (endDate != null) params.add('end_date=$endDate');
    path += params.join('&');
    return await ApiClient.get(path);
  }
}
