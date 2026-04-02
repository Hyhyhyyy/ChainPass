/// API 异常类型
enum ApiExceptionType {
  network,
  badRequest,
  unauthorized,
  forbidden,
  notFound,
  serverError,
  cancelled,
  unknown,
}

/// API 异常
class ApiException implements Exception {
  final ApiExceptionType type;
  final String message;
  final int? statusCode;

  ApiException({
    required this.type,
    required this.message,
    this.statusCode,
  });

  /// 网络异常
  factory ApiException.network(String message) {
    return ApiException(type: ApiExceptionType.network, message: message);
  }

  /// 请求参数错误
  factory ApiException.badRequest(String message) {
    return ApiException(
      type: ApiExceptionType.badRequest,
      message: message,
      statusCode: 400,
    );
  }

  /// 未授权
  factory ApiException.unauthorized(String message) {
    return ApiException(
      type: ApiExceptionType.unauthorized,
      message: message,
      statusCode: 401,
    );
  }

  /// 禁止访问
  factory ApiException.forbidden(String message) {
    return ApiException(
      type: ApiExceptionType.forbidden,
      message: message,
      statusCode: 403,
    );
  }

  /// 资源不存在
  factory ApiException.notFound(String message) {
    return ApiException(
      type: ApiExceptionType.notFound,
      message: message,
      statusCode: 404,
    );
  }

  /// 服务器错误
  factory ApiException.serverError(String message) {
    return ApiException(
      type: ApiExceptionType.serverError,
      message: message,
      statusCode: 500,
    );
  }

  /// 请求取消
  factory ApiException.cancelled(String message) {
    return ApiException(type: ApiExceptionType.cancelled, message: message);
  }

  /// 未知错误
  factory ApiException.unknown(String message) {
    return ApiException(type: ApiExceptionType.unknown, message: message);
  }

  /// 是否为网络错误
  bool get isNetworkError => type == ApiExceptionType.network;

  /// 是否为未授权错误
  bool get isUnauthorized => type == ApiExceptionType.unauthorized;

  /// 是否需要重新登录
  bool get needRelogin =>
      type == ApiExceptionType.unauthorized || statusCode == 401;

  @override
  String toString() {
    return 'ApiException(type: $type, message: $message, statusCode: $statusCode)';
  }
}