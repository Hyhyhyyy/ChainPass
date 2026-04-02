import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../constants/app_constants.dart';

/// 安全存储服务
/// 使用 Flutter Secure Storage 存储敏感数据
class SecureStorageService {
  final FlutterSecureStorage _storage;

  SecureStorageService() : _storage = const FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true,
    ),
    iOptions: IOSOptions(
      accessibility: KeyChainAccessibility.first_unlock_this_device,
    ),
  );

  // ============ Token 管理 ============

  /// 保存 AccessToken
  Future<void> saveAccessToken(String token) async {
    await _storage.write(key: AppConstants.accessTokenKey, value: token);
  }

  /// 获取 AccessToken
  Future<String?> getAccessToken() async {
    return await _storage.read(key: AppConstants.accessTokenKey);
  }

  /// 保存 RefreshToken
  Future<void> saveRefreshToken(String token) async {
    await _storage.write(key: AppConstants.refreshTokenKey, value: token);
  }

  /// 获取 RefreshToken
  Future<String?> getRefreshToken() async {
    return await _storage.read(key: AppConstants.refreshTokenKey);
  }

  /// 保存 Token 过期时间
  Future<void> saveTokenExpiresAt(int expiresAt) async {
    await _storage.write(
      key: AppConstants.tokenExpiresAtKey,
      value: expiresAt.toString(),
    );
  }

  /// 获取 Token 过期时间
  Future<int?> getTokenExpiresAt() async {
    final value = await _storage.read(key: AppConstants.tokenExpiresAtKey);
    return value != null ? int.tryParse(value) : null;
  }

  /// 检查 Token 是否有效
  Future<bool> isTokenValid() async {
    final token = await getAccessToken();
    if (token == null) return false;

    final expiresAt = await getTokenExpiresAt();
    if (expiresAt == null) return false;

    return expiresAt > DateTime.now().millisecondsSinceEpoch;
  }

  // ============ 用户信息存储 ============

  /// 保存用户 ID
  Future<void> saveUserId(int userId) async {
    await _storage.write(key: AppConstants.userIdKey, value: userId.toString());
  }

  /// 获取用户 ID
  Future<int?> getUserId() async {
    final value = await _storage.read(key: AppConstants.userIdKey);
    return value != null ? int.tryParse(value) : null;
  }

  /// 保存用户名
  Future<void> saveUsername(String username) async {
    await _storage.write(key: AppConstants.usernameKey, value: username);
  }

  /// 获取用户名
  Future<String?> getUsername() async {
    return await _storage.read(key: AppConstants.usernameKey);
  }

  /// 保存昵称
  Future<void> saveNickname(String nickname) async {
    await _storage.write(key: AppConstants.nicknameKey, value: nickname);
  }

  /// 获取昵称
  Future<String?> getNickname() async {
    return await _storage.read(key: AppConstants.nicknameKey);
  }

  /// 保存邮箱
  Future<void> saveEmail(String email) async {
    await _storage.write(key: AppConstants.emailKey, value: email);
  }

  /// 获取邮箱
  Future<String?> getEmail() async {
    return await _storage.read(key: AppConstants.emailKey);
  }

  /// 保存头像
  Future<void> saveAvatar(String avatar) async {
    await _storage.write(key: AppConstants.avatarKey, value: avatar);
  }

  /// 获取头像
  Future<String?> getAvatar() async {
    return await _storage.read(key: AppConstants.avatarKey);
  }

  /// 保存 Gitee ID
  Future<void> saveGiteeId(String giteeId) async {
    await _storage.write(key: 'gitee_id', value: giteeId);
  }

  /// 获取 Gitee ID
  Future<String?> getGiteeId() async {
    return await _storage.read(key: 'gitee_id');
  }

  /// 保存角色列表
  Future<void> saveRoles(List<String> roles) async {
    await _storage.write(key: AppConstants.rolesKey, value: roles.join(','));
  }

  /// 获取角色列表
  Future<List<String>> getRoles() async {
    final value = await _storage.read(key: AppConstants.rolesKey);
    if (value == null || value.isEmpty) return [];
    return value.split(',');
  }

  /// 保存权限列表
  Future<void> savePermissions(List<String> permissions) async {
    await _storage.write(key: AppConstants.permissionsKey, value: permissions.join(','));
  }

  /// 获取权限列表
  Future<List<String>> getPermissions() async {
    final value = await _storage.read(key: AppConstants.permissionsKey);
    if (value == null || value.isEmpty) return [];
    return value.split(',');
  }

  // ============ DID 私钥存储 ============

  /// 保存 DID 私钥
  Future<void> saveDidPrivateKey(String did, String privateKey) async {
    await _storage.write(
      key: '${AppConstants.didPrivateKeyPrefix}${did}',
      value: privateKey,
    );
  }

  /// 获取 DID 私钥
  Future<String?> getDidPrivateKey(String did) async {
    return await _storage.read(
      key: '${AppConstants.didPrivateKeyPrefix}${did}',
    );
  }

  /// 删除 DID 私钥
  Future<void> deleteDidPrivateKey(String did) async {
    await _storage.delete(
      key: '${AppConstants.didPrivateKeyPrefix}${did}',
    );
  }

  // ============ 清理操作 ============

  /// 清除认证相关信息
  Future<void> clearAuth() async {
    await _storage.deleteAll();
  }

  /// 清除所有数据
  Future<void> clearAll() async {
    await _storage.deleteAll();
  }
}

/// 安全存储 Provider
final secureStorageProvider = Provider<SecureStorageService>((ref) {
  return SecureStorageService();
});