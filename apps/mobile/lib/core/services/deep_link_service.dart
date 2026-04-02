import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:uni_links/uni_links.dart';
import 'dart:async';

/// 深度链接服务
/// 处理 chainpass:// 和 Universal Links
class DeepLinkService {
  final Ref _ref;
  StreamSubscription<Uri?>? _linkSubscription;

  DeepLinkService(this._ref);

  /// 初始化深度链接监听
  Future<void> init() async {
    // 处理冷启动时的链接
    try {
      final initialUri = await getInitialUri();
      if (initialUri != null) {
        _handleUri(initialUri);
      }
    } catch (e) {
      debugPrint('获取初始链接失败: $e');
    }

    // 监听运行时的链接
    _linkSubscription = uriLinkStream.listen(
      (Uri? uri) {
        if (uri != null) {
          _handleUri(uri);
        }
      },
      onError: (err) {
        debugPrint('深度链接错误: $err');
      },
    );
  }

  /// 处理深度链接 URI
  void _handleUri(Uri uri) {
    debugPrint('收到深度链接: $uri');

    final path = uri.path;
    final params = uri.queryParameters;

    switch (path) {
      case '/auth/confirm':
        _handleAuthConfirm(params);
        break;
      case '/did/detail':
        _handleDIDDetail(params);
        break;
      case '/vc/detail':
        _handleVCDetail(params);
        break;
      case '/payment/confirm':
        _handlePaymentConfirm(params);
        break;
      default:
        debugPrint('未知的深度链接路径: $path');
    }
  }

  /// 处理登录确认
  void _handleAuthConfirm(Map<String, String> params) {
    final sessionId = params['sessionId'];
    if (sessionId != null) {
      // 跳转到确认页面
      _ref.read(routerProvider).go('/auth/qr-confirm?sessionId=$sessionId');
    }
  }

  /// 处理 DID 详情
  void _handleDIDDetail(Map<String, String> params) {
    final did = params['did'];
    if (did != null) {
      _ref.read(routerProvider).go('/identity/did/detail?did=$did');
    }
  }

  /// 处理 VC 详情
  void _handleVCDetail(Map<String, String> params) {
    final vcId = params['vcId'];
    if (vcId != null) {
      _ref.read(routerProvider).go('/identity/vc/detail?vcId=$vcId');
    }
  }

  /// 处理支付确认
  void _handlePaymentConfirm(Map<String, String> params) {
    final orderNo = params['orderNo'];
    if (orderNo != null) {
      _ref.read(routerProvider).go('/payment/confirm?orderNo=$orderNo');
    }
  }

  /// 销毁监听
  void dispose() {
    _linkSubscription?.cancel();
  }
}

/// 深度链接服务 Provider
final deepLinkServiceProvider = Provider<DeepLinkService>((ref) {
  final service = DeepLinkService(ref);
  ref.onDispose(() => service.dispose());
  return service;
});

/// 支持的深度链接路径
class DeepLinkPaths {
  /// 登录确认
  static const String authConfirm = '/auth/confirm';

  /// DID 详情
  static const String didDetail = '/did/detail';

  /// VC 详情
  static const String vcDetail = '/vc/detail';

  /// 支付确认
  static const String paymentConfirm = '/payment/confirm';
}

/// 深度链接 URL 生成工具
class DeepLinkBuilder {
  static const String scheme = 'chainpass';
  static const String host = 'app.chainpass.io';

  /// 生成登录确认链接
  static String authConfirm(String sessionId) {
    return '$scheme://auth/confirm?sessionId=$sessionId';
  }

  /// 生成 DID 详情链接
  static String didDetail(String did) {
    return '$scheme://did/detail?did=${Uri.encodeComponent(did)}';
  }

  /// 生成 VC 详情链接
  static String vcDetail(String vcId) {
    return '$scheme://vc/detail?vcId=$vcId';
  }

  /// 生成支付确认链接
  static String paymentConfirm(String orderNo) {
    return '$scheme://payment/confirm?orderNo=$orderNo';
  }
}