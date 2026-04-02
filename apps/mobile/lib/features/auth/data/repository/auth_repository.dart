import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_response.dart';
import '../../../core/storage/secure_storage.dart';
import '../../../core/constants/api_paths.dart';
import '../../../shared/providers/app_provider.dart';

/// 登录请求
class LoginRequest {
  final String username;
  final String password;

  LoginRequest({required this.username, required this.password});

  Map<String, dynamic> toJson() => {'username': username, 'password': password};
}

/// 登录响应
class LoginResponse {
  final String accessToken;
  final String refreshToken;

  LoginResponse({required this.accessToken, required this.refreshToken});

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
    );
  }
}

/// 认证仓库
class AuthRepository {
  final ApiClient _api;
  final SecureStorageService _storage;
  final Ref _ref;

  AuthRepository(this._api, this._storage, this._ref);

  /// 账密登录
  Future<bool> login(String username, String password) async {
    try {
      final response = await _api.post<LoginResponse>(
        ApiPaths.authLogin,
        data: LoginRequest(username: username, password: password).toJson(),
        fromJson: LoginResponse.fromJson,
      );

      if (response.isSuccess && response.data != null) {
        await _saveTokens(response.data!);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  /// 刷新 Token
  Future<bool> refreshToken(String refreshToken) async {
    try {
      final response = await _api.post<LoginResponse>(
        ApiPaths.authRefresh,
        data: {'refreshToken': refreshToken},
        fromJson: LoginResponse.fromJson,
      );

      if (response.isSuccess && response.data != null) {
        await _saveTokens(response.data!);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  /// 登出
  Future<void> logout() async {
    try {
      await _api.post<void>(ApiPaths.authLogout);
    } catch (e) {
      // 忽略错误
    } finally {
      await _storage.clearAuth();
      _ref.read(appProvider.notifier).logout();
    }
  }

  /// 保存认证信息
  Future<void> _saveTokens(LoginResponse response) async {
    await _storage.saveAccessToken(response.accessToken);
    await _storage.saveRefreshToken(response.refreshToken);
    await _storage.saveTokenExpiresAt(
      DateTime.now().millisecondsSinceEpoch + 15 * 60 * 1000,
    );
  }
}

/// 认证仓库 Provider
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final api = ref.watch(apiClientProvider);
  final storage = ref.watch(secureStorageProvider);
  return AuthRepository(api, storage, ref);
});