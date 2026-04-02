/// 路由路径常量
class Routes {
  // ============ 认证模块 ============
  static const String auth = '/auth';
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String forgotPassword = '/auth/forgot-password';
  static const String zkpVerify = '/auth/zkp-verify';
  static const String oauthCallback = '/auth/oauth/callback';

  // ============ 主页 ============
  static const String home = '/home';
  static const String dashboard = '/home/dashboard';

  // ============ 身份管理 ============
  static const String identity = '/identity';
  static const String didManage = '/identity/did';
  static const String didDetail = '/identity/did/detail';
  static const String vcList = '/identity/vc';
  static const String vcDetail = '/identity/vc/detail';

  // ============ 支付中心 ============
  static const String payment = '/payment';
  static const String wallet = '/payment/wallet';
  static const String transfer = '/payment/transfer';
  static const String history = '/payment/history';

  // ============ 合规中心 ============
  static const String compliance = '/compliance';
  static const String kycApply = '/compliance/kyc';
  static const String kycStatus = '/compliance/kyc/status';

  // ============ 用户中心 ============
  static const String user = '/user';
  static const String profile = '/user/profile';
  static const String security = '/user/security';
  static const String devices = '/user/devices';
  static const String settings = '/user/settings';

  // ============ 错误页面 ============
  static const String forbidden = '/403';
  static const String notFound = '/404';

  // ============ 白名单路由（无需登录） ============
  static const List<String> authWhitelist = [
    login,
    register,
    forgotPassword,
    zkpVerify,
    oauthCallback,
    forbidden,
    notFound,
  ];
}