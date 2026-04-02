import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/storage/secure_storage.dart';
import '../../core/storage/hive_service.dart';
import '../../core/constants/app_constants.dart';

/// 应用状态
class AppState {
  /// 是否已登录
  final bool isLoggedIn;

  /// 用户 ID
  final int? userId;

  /// 用户名
  final String? username;

  /// 昵称
  final String? nickname;

  /// 邮箱
  final String? email;

  /// 头像
  final String? avatar;

  /// 角色
  final List<String> roles;

  /// 权限
  final List<String> permissions;

  /// 主题模式
  final ThemeMode themeMode;

  /// 语言
  final Locale locale;

  const AppState({
    this.isLoggedIn = false,
    this.userId,
    this.username,
    this.nickname,
    this.email,
    this.avatar,
    this.roles = [],
    this.permissions = [],
    this.themeMode = ThemeMode.system,
    this.locale = const Locale('zh', 'CN'),
  });

  /// 复制并修改
  AppState copyWith({
    bool? isLoggedIn,
    int? userId,
    String? username,
    String? nickname,
    String? email,
    String? avatar,
    List<String>? roles,
    List<String>? permissions,
    ThemeMode? themeMode,
    Locale? locale,
  }) {
    return AppState(
      isLoggedIn: isLoggedIn ?? this.isLoggedIn,
      userId: userId ?? this.userId,
      username: username ?? this.username,
      nickname: nickname ?? this.nickname,
      email: email ?? this.email,
      avatar: avatar ?? this.avatar,
      roles: roles ?? this.roles,
      permissions: permissions ?? this.permissions,
      themeMode: themeMode ?? this.themeMode,
      locale: locale ?? this.locale,
    );
  }
}

/// 应用状态 Provider
final appProvider = StateNotifierProvider<AppNotifier, AppState>((ref) {
  final storage = ref.watch(secureStorageProvider);
  final hive = ref.watch(hiveServiceProvider);
  return AppNotifier(storage, hive);
});

/// 应用状态管理器
class AppNotifier extends StateNotifier<AppState> {
  final SecureStorageService _storage;
  final HiveService _hive;

  AppNotifier(this._storage, this._hive) : super(const AppState()) {
    _loadState();
  }

  /// 从存储加载状态
  Future<void> _loadState() async {
    final isLoggedIn = await _storage.isTokenValid();
    final userId = await _storage.getUserId();
    final username = await _storage.getUsername();
    final nickname = await _storage.getNickname();
    final email = await _storage.getEmail();
    final avatar = await _storage.getAvatar();
    final roles = await _storage.getRoles();
    final permissions = await _storage.getPermissions();

    final themeModeIndex = _hive.getSetting<int>(HiveService.themeModeKey, 0);
    final themeMode = ThemeMode.values[themeModeIndex];

    final localeCode = _hive.getSetting<String>(HiveService.localeKey, 'zh_CN');
    final locale = _parseLocale(localeCode);

    state = AppState(
      isLoggedIn: isLoggedIn,
      userId: userId,
      username: username,
      nickname: nickname,
      email: email,
      avatar: avatar,
      roles: roles,
      permissions: permissions,
      themeMode: themeMode,
      locale: locale,
    );
  }

  /// 设置登录状态
  Future<void> setLoggedIn({
    required int userId,
    required String username,
    String? nickname,
    String? email,
    String? avatar,
    List<String>? roles,
    List<String>? permissions,
  }) async {
    await _storage.saveUserId(userId);
    await _storage.saveUsername(username);
    if (nickname != null) await _storage.saveNickname(nickname);
    if (email != null) await _storage.saveEmail(email);
    if (avatar != null) await _storage.saveAvatar(avatar);
    if (roles != null) await _storage.saveRoles(roles);
    if (permissions != null) await _storage.savePermissions(permissions);

    state = state.copyWith(
      isLoggedIn: true,
      userId: userId,
      username: username,
      nickname: nickname,
      email: email,
      avatar: avatar,
      roles: roles ?? [],
      permissions: permissions ?? [],
    );
  }

  /// 清除登录状态
  Future<void> logout() async {
    await _storage.clearAuth();
    state = const AppState(isLoggedIn: false);
  }

  /// 设置主题模式
  Future<void> setThemeMode(ThemeMode mode) async {
    await _hive.putSetting(HiveService.themeModeKey, mode.index);
    state = state.copyWith(themeMode: mode);
  }

  /// 设置语言
  Future<void> setLocale(Locale locale) async {
    final localeCode = '${locale.languageCode}_${locale.countryCode ?? ''}';
    await _hive.putSetting(HiveService.localeKey, localeCode);
    state = state.copyWith(locale: locale);
  }

  /// 检查是否有权限
  bool hasPermission(String permission) {
    return state.permissions.contains(permission) ||
        state.permissions.contains('*:*:*');
  }

  /// 检查是否有角色
  bool hasRole(String role) {
    return state.roles.contains(role) || state.roles.contains('admin');
  }

  Locale _parseLocale(String code) {
    final parts = code.split('_');
    if (parts.length >= 2) {
      return Locale(parts[0], parts[1]);
    }
    return Locale(parts[0]);
  }
}