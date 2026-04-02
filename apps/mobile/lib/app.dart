import 'package:flutter/material.dart';

/// 应用配置常量
class AppConfig {
  /// 应用名称
  static const String appName = 'ChainPass';

  /// 应用版本
  static const String version = '1.0.0';

  /// API 基础地址
  static const String apiBaseUrl = 'http://localhost:8080';

  /// 生产环境 API 地址
  static const String apiBaseUrlProd = 'https://api.chainpass.io';

  /// 是否为生产环境
  static bool isProduction = false;

  /// 获取当前 API 地址
  static String get currentApiBaseUrl =>
      isProduction ? apiBaseUrlProd : apiBaseUrl;

  /// Token 有效期（毫秒）
  static const int tokenExpiresIn = 15 * 60 * 1000; // 15分钟

  /// RefreshToken 有效期（毫秒）
  static const int refreshTokenExpiresIn = 7 * 24 * 60 * 60 * 1000; // 7天

  /// OAuth 回调 Scheme
  static const String oauthCallbackScheme = 'chainpass';

  /// OAuth 回调路径
  static const String oauthCallbackPath = '/oauth/callback';
}