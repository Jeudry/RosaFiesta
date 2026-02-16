class ApiException implements Exception {
  final String message;
  final int? statusCode;
  final String? code;

  ApiException({
    required this.message,
    this.statusCode,
    this.code,
  });

  @override
  String toString() => 'ApiException(statusCode: $statusCode, message: $message, code: $code)';
}

class NetworkException extends ApiException {
  NetworkException({super.message = 'Network error occurred', super.statusCode});
}

class UnauthorizedException extends ApiException {
  UnauthorizedException({super.message = 'Unauthorized access', super.statusCode = 401});
}

class ServerException extends ApiException {
  ServerException({super.message = 'Server error occurred', super.statusCode = 500});
}

class BadRequestException extends ApiException {
  BadRequestException({required super.message, super.statusCode = 400});
}

class NotFoundException extends ApiException {
  NotFoundException({super.message = 'Resource not found', super.statusCode = 404});
}
