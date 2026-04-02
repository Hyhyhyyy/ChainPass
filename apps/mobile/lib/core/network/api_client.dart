import 'package:dio/dio.dart';
import '../constants/app_constants.dart';
import '../../app.dart';

/// API 客户端配置
class ApiClient {
  late final Dio _dio;

  ApiClient() {
    _dio = Dio(
      BaseOptions(
        baseUrl: AppConfig.currentApiBaseUrl,
        connectTimeout: const Duration(milliseconds: AppConstants.defaultTimeout),
        receiveTimeout: const Duration(milliseconds: AppConstants.defaultTimeout),
        sendTimeout: const Duration(milliseconds: AppConstants.defaultTimeout),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    // 配置拦截器
    _dio.interceptors.addAll([
      LogInterceptor(
        request: true,
        requestHeader: true,
        requestBody: true,
        responseHeader: true,
        responseBody: true,
        error: true,
        logPrint: (obj) => debugPrint(obj.toString()),
      ),
    ]);
  }

  /// 添加拦截器
  void addInterceptor(Interceptor interceptor) {
    _dio.interceptors.add(interceptor);
  }

  /// GET 请求
  Future<ApiResponse<T>> get<T>(
    String path,
    {
      Map<String, dynamic>? queryParameters,
      Options? options,
      T Function(Map<String, dynamic>)? fromJson,
    }
  ) async {
    try {
      final response = await _dio.get(path, queryParameters: queryParameters, options: options);
      return _handleResponse<T>(response, fromJson);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// POST 请求
  Future<ApiResponse<T>> post<T>(
    String path,
    {
      dynamic data,
      Map<String, dynamic>? queryParameters,
      Options? options,
      T Function(Map<String, dynamic>)? fromJson,
    }
  ) async {
    try {
      final response = await _dio.post(path, data: data, queryParameters: queryParameters, options: options);
      return _handleResponse<T>(response, fromJson);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// PUT 请求
  Future<ApiResponse<T>> put<T>(
    String path,
    {
      dynamic data,
      Map<String, dynamic>? queryParameters,
      Options? options,
      T Function(Map<String, dynamic>)? fromJson,
    }
  ) async {
    try {
      final response = await _dio.put(path, data: data, queryParameters: queryParameters, options: options);
      return _handleResponse<T>(response, fromJson);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// DELETE 请求
  Future<ApiResponse<T>> delete<T>(
    String path,
    {
      dynamic data,
      Map<String, dynamic>? queryParameters,
      Options? options,
      T Function(Map<String, dynamic>)? fromJson,
    }
  ) async {
    try {
      final response = await _dio.delete(path, data: data, queryParameters: queryParameters, options: options);
      return _handleResponse<T>(response, fromJson);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// 处理响应
  ApiResponse<T> _handleResponse<T>(Response response, T Function(Map<String, dynamic>)? fromJson) {
    final data = response.data as Map<String, dynamic>;
    final code = data['code'] as int? ?? 200;
    final msg = data['msg'] as String? ?? '';

    if (fromJson != null && data['data'] != null) {
      final parsedData = fromJson(data['data'] as Map<String, dynamic>);
      return ApiResponse<T>(code: code, msg: msg, data: parsedData);
    }

    return ApiResponse<T>(code: code, msg: msg, data: data['data'] as T?);
  }

  /// 处理错误
  ApiException _handleError(DioException error) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return ApiException.network('网络连接超时');
      case DioExceptionType.connectionError:
        return ApiException.network('网络连接异常');
      case DioExceptionType.badResponse:
        final statusCode = error.response?.statusCode;
        final data = error.response?.data as Map<String, dynamic>?;
        final msg = data?['msg'] as String? ?? '请求失败';
        switch (statusCode) {
          case 400:
            return ApiException.badRequest(msg);
          case 401:
            return ApiException.unauthorized('登录已过期');
          case 403:
            return ApiException.forbidden('无权访问');
          case 404:
            return ApiException.notFound('请求的资源不存在');
          case 500:
            return ApiException.serverError('服务器内部错误');
          default:
            return ApiException.unknown(msg);
        }
      case DioExceptionType.cancel:
        return ApiException.cancelled('请求已取消');
      default:
        return ApiException.unknown('未知错误');
    }
  }
}

/// API 客户端 Provider
final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient();
});