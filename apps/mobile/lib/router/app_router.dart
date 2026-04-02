import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'routes.dart';
import '../features/auth/presentation/pages/login_page.dart';
import '../features/auth/presentation/pages/register_page.dart';
import '../features/auth/presentation/pages/zkp_verify_page.dart';
import '../features/auth/presentation/pages/oauth_callback_page.dart';
import '../features/home/presentation/pages/home_page.dart';
import '../features/home/presentation/pages/dashboard_page.dart';
import '../features/did/presentation/pages/did_manage_page.dart';
import '../features/did/presentation/pages/did_detail_page.dart';
import '../features/vc/presentation/pages/vc_list_page.dart';
import '../features/vc/presentation/pages/vc_detail_page.dart';
import '../features/payment/presentation/pages/wallet_page.dart';
import '../features/payment/presentation/pages/transfer_page.dart';
import '../features/payment/presentation/pages/history_page.dart';
import '../features/kyc/presentation/pages/kyc_apply_page.dart';
import '../features/kyc/presentation/pages/kyc_status_page.dart';
import '../features/user/presentation/pages/profile_page.dart';
import '../features/user/presentation/pages/settings_page.dart';
import '../shared/widgets/navigation/scaffold_wrapper.dart';
import '../shared/providers/app_provider.dart';

/// 路由配置
final routerProvider = Provider<GoRouter>((ref) {
  final appState = ref.watch(appProvider);

  return GoRouter(
    initialLocation: Routes.login,
    debugLogDiagnostics: true,

    // 全局重定向（认证守卫）
    redirect: (context, state) {
      final isAuthenticated = appState.isLoggedIn;
      final currentPath = state.matchedLocation;

      // 白名单路由直接放行
      if (Routes.authWhitelist.contains(currentPath)) {
        // 已登录用户访问登录页，重定向到首页
        if (currentPath == Routes.login && isAuthenticated) {
          return Routes.home;
        }
        return null;
      }

      // 未登录用户访问需要认证的页面，重定向到登录页
      if (!isAuthenticated) {
        return '${Routes.login}?redirect=${Uri.encodeComponent(currentPath)}';
      }

      return null;
    },

    // 路由配置
    routes: [
      // ============ 认证模块 ============
      GoRoute(
        path: Routes.auth,
        redirect: (_, __) => Routes.login,
      ),
      GoRoute(
        path: Routes.login,
        builder: (context, state) {
          final redirect = state.uri.queryParameters['redirect'];
          return LoginPage(redirectPath: redirect);
        },
      ),
      GoRoute(
        path: Routes.register,
        builder: (_, __) => const RegisterPage(),
      ),
      GoRoute(
        path: Routes.zkpVerify,
        builder: (_, __) => const ZKPVerifyPage(),
      ),
      GoRoute(
        path: Routes.oauthCallback,
        builder: (context, state) {
          final code = state.uri.queryParameters['code'];
          return OAuthCallbackPage(code: code ?? '');
        },
      ),

      // ============ 主应用（带底部导航） ============
      ShellRoute(
        builder: (_, __, child) => ScaffoldWrapper(child: child),
        routes: [
          // 首页/仪表盘
          GoRoute(
            path: Routes.home,
            builder: (_, __) => const HomePage(),
          ),
          GoRoute(
            path: Routes.dashboard,
            builder: (_, __) => const DashboardPage(),
          ),

          // 身份管理
          GoRoute(
            path: Routes.didManage,
            builder: (_, __) => const DIDManagePage(),
          ),
          GoRoute(
            path: Routes.didDetail,
            builder: (context, state) {
              final did = state.uri.queryParameters['did'] ?? '';
              return DIDDetailPage(did: did);
            },
          ),
          GoRoute(
            path: Routes.vcList,
            builder: (_, __) => const VCListPage(),
          ),
          GoRoute(
            path: Routes.vcDetail,
            builder: (context, state) {
              final vcId = state.uri.queryParameters['vcId'] ?? '';
              return VCDetailPage(vcId: vcId);
            },
          ),

          // 支付中心
          GoRoute(
            path: Routes.wallet,
            builder: (_, __) => const WalletPage(),
          ),
          GoRoute(
            path: Routes.transfer,
            builder: (_, __) => const TransferPage(),
          ),
          GoRoute(
            path: Routes.history,
            builder: (_, __) => const HistoryPage(),
          ),

          // 合规中心
          GoRoute(
            path: Routes.kycApply,
            builder: (_, __) => const KYCApplyPage(),
          ),
          GoRoute(
            path: Routes.kycStatus,
            builder: (_, __) => const KYCStatusPage(),
          ),

          // 用户中心
          GoRoute(
            path: Routes.profile,
            builder: (_, __) => const ProfilePage(),
          ),
          GoRoute(
            path: Routes.settings,
            builder: (_, __) => const SettingsPage(),
          ),
        ],
      ),

      // ============ 错误页面 ============
      GoRoute(
        path: Routes.forbidden,
        builder: (_, __) => const Scaffold(
          body: Center(child: Text('无权访问')),
        ),
      ),
      GoRoute(
        path: Routes.notFound,
        builder: (_, __) => const Scaffold(
          body: Center(child: Text('页面不存在')),
        ),
      ),
    ],

    // 错误处理
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 16),
            Text('路由错误: ${state.error}'),
            ElevatedButton(
              onPressed: () => context.go(Routes.home),
              child: const Text('返回首页'),
            ),
          ],
        ),
      ),
    ),
  );
});