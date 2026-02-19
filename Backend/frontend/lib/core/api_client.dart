import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:frontend/core/config/env_config.dart';
import 'package:frontend/core/errors/api_exception.dart';

class ApiClient {
  static final Dio _dio = Dio(
    BaseOptions(
      baseUrl: EnvConfig.apiUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
      },
    ),
  );

  static const _storage = FlutterSecureStorage();

  static void init() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'access_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (DioException e, handler) async {
        if (e.response?.statusCode == 401) {
          // TODO: Implement token refresh logic here
          // For now, just pass the error through
        }
        return handler.next(e);
      },
    ));
  }

  static Future<dynamic> post(String path, Map<String, dynamic> body) async {
    try {
      final response = await _dio.post(path, data: body);
      if (response.data == null) return null;
      return response.data['data'];
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  static Future<dynamic> get(String path) async {
    try {
      final response = await _dio.get(path);
      if (response.data == null) return null;
      return response.data['data'];
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  static Future<dynamic> put(String path, Map<String, dynamic> body) async {
    try {
      final response = await _dio.put(path, data: body);
      if (response.data == null) return null;
      return response.data['data'];
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }
  
  static Future<dynamic> patch(String path, Map<String, dynamic> body) async {
    try {
      final response = await _dio.patch(path, data: body);
      if (response.data == null) return null;
      return response.data['data'];
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  static Future<dynamic> delete(String path) async {
    try {
      final response = await _dio.delete(path);
      if (response.statusCode == 204 || response.data == null) {
        return null;
      }
      return response.data['data'];
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  static ApiException _handleDioError(DioException e) {
    print('ApiClient Error: ${e.message}');
    if (e.response != null) {
      final statusCode = e.response!.statusCode;
      final data = e.response!.data;
      final message = data['error'] ?? 'Unknown error occurred';

      switch (statusCode) {
        case 400:
          return BadRequestException(message: message);
        case 401:
          return UnauthorizedException();
        case 404:
          return NotFoundException(message: message);
        case 500:
          return ServerException();
        default:
          return ApiException(
            message: message,
            statusCode: statusCode,
          );
      }
    }
    return NetworkException(message: e.message ?? 'Network error');
  }
}
