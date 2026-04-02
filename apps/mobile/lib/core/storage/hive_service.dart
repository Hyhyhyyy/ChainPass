import 'package:hive_flutter/hive_flutter.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Hive 本地数据库服务
/// 用于存储非敏感数据
class HiveService {
  late final Box<dynamic> _cacheBox;
  late final Box<dynamic> _settingsBox;

  static const String cacheBoxName = 'cache';
  static const String settingsBoxName = 'settings';

  /// 初始化 Hive
  Future<void> init() async {
    await Hive.initFlutter();

    // 打开盒子
    _cacheBox = await Hive.openBox(cacheBoxName);
    _settingsBox = await Hive.openBox(settingsBoxName);
  }

  // ============ 缓存操作 ============

  /// 保存缓存数据
  Future<void> putCache(String key, dynamic value, {Duration? expiry}) async {
    await _cacheBox.put(key, {
      'value': value,
      'timestamp': DateTime.now().millisecondsSinceEpoch,
      'expiry': expiry?.inMilliseconds,
    });
  }

  /// 获取缓存数据
  T? getCache<T>(String key) {
    final data = _cacheBox.get(key) as Map<dynamic, dynamic>?;

    if (data == null) return null;

    // 检查是否过期
    final timestamp = data['timestamp'] as int?;
    final expiry = data['expiry'] as int?;

    if (timestamp != null && expiry != null) {
      final now = DateTime.now().millisecondsSinceEpoch;
      if (now > timestamp + expiry) {
        _cacheBox.delete(key);
        return null;
      }
    }

    return data['value'] as T?;
  }

  /// 删除缓存数据
  Future<void> deleteCache(String key) async {
    await _cacheBox.delete(key);
  }

  /// 清空所有缓存
  Future<void> clearCache() async {
    await _cacheBox.clear();
  }

  // ============ 设置操作 ============

  /// 保存设置
  Future<void> putSetting(String key, dynamic value) async {
    await _settingsBox.put(key, value);
  }

  /// 获取设置
  T? getSetting<T>(String key, T defaultValue) {
    final value = _settingsBox.get(key);
    return value != null ? value as T : defaultValue;
  }

  /// 删除设置
  Future<void> deleteSetting(String key) async {
    await _settingsBox.delete(key);
  }

  /// 清空所有设置
  Future<void> clearSettings() async {
    await _settingsBox.clear();
  }

  // ============ 常用设置键 ============

  static const String themeModeKey = 'theme_mode';
  static const String localeKey = 'locale';
  static const String lastLoginTimeKey = 'last_login_time';
  static const String rememberPasswordKey = 'remember_password';
}

/// Hive 服务 Provider
final hiveServiceProvider = Provider<HiveService>((ref) {
  return HiveService();
});