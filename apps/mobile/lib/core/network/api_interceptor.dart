import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../storage/secure_storage.dart';
import '../../features/auth/data/repository/auth_repository.dart';
import '../../router/app_router.dart';
import 'api_client.dart';

/// 认证拦截器
/// 自动添加 Token 到请求头，处理 Token 过期刷新
class AuthInterceptor extends Interceptor {
  final SecureStorageService _storage;
  final Ref _ref;

  AuthInterceptor(this._storage, this._ref);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    // 获取 AccessToken
    final token = await _storage.getAccessToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }

    // 特殊处理 OAuth 接口
    if (options.path.contains('/oauth/gitee/config')) {
      final giteeId = await _storage.getGiteeId();
      if (giteeId != null) {
        options.headers['X-Gitee-Id'] = giteeId;
      }
    }

    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // 处理 401 错误（Token 过期）
    if (err.response?.statusCode == 401) {
      final refreshToken = await _storage.getRefreshToken();
      if (refreshToken != null) {
        // 尝试刷新 Token
        try {
          final authRepo = _ref.read(authRepositoryProvider);
          final success = await authRepo.refreshToken(refreshToken);

          if (success) {
            // Token 刷新成功，重试原请求
            final newToken = await _storage.getAccessToken();
            err.requestOptions.headers['Authorization'] = 'Bearer $newToken';

            // 创建新的请求并执行
            final dio = _ref.read(apiClientProvider)._dio;
            final response = await dio.fetch(err.requestOptions);
            return handler.resolve(response);
          }
        } catch (e) {
          // Token 刷新失败
        }
      }

      // 清除登录状态
      await _storage.clearAuth();

      // 跳转登录页
      _ref.read(routerProvider).go('/auth/login');
    }

    handler.next(err);
  }
}

/// 认证拦截器 Provider
final authInterceptorProvider = Provider<AuthInterceptor>((ref) {
  final storage = ref.watch(secureStorageProvider);
  return AuthInterceptor(storage, ref);
});