/// API 端点路径定义
class ApiPaths {
  // ============ 认证模块 ============
  static const String authLogin = '/auth/login';
  static const String authLogout = '/auth/logout';
  static const String authRefresh = '/auth/refresh';
  static const String authRegister = '/auth/register';
  static const String authForgotPassword = '/auth/forgot-password';
  static const String authResetPassword = '/auth/reset-password';
  static const String authMe = '/auth/me';

  // ============ OAuth 模块 ============
  static const String oauthGiteeConfig = '/oauth/gitee/config';
  static const String oauthGiteeCallback = '/oauth/gitee/callback';

  // ============ ZKP 认证模块 ============
  static const String zkpInit = '/zkp/init';
  static const String zkpPublicKey = '/zkp/public-key';
  static const String zkpVerify = '/zkp/verify';
  static const String zkpStatus = '/zkp/status';

  // ============ DID 模块 ============
  static const String didCreate = '/did/create';
  static const String didMy = '/did/my';
  static const String didByDid = '/did';
  static const String didVerify = '/did/verify';
  static const String didRevoke = '/did/revoke';
  static const String didCheck = '/did/check';

  // ============ VC 模块 ============
  static const String vcIssue = '/vc/issue';
  static const String vcVerify = '/vc/verify';
  static const String vcMy = '/vc/my';
  static const String vcList = '/vc/list';
  static const String vcRevoke = '/vc/revoke';
  static const String vcTypes = '/vc/types';

  // ============ 支付模块 ============
  static const String paymentWallet = '/payment/wallet';
  static const String paymentCreate = '/payment/create';
  static const String paymentExecute = '/payment/execute';
  static const String paymentHistory = '/payment/history';
  static const String paymentRate = '/payment/rate';

  // ============ KYC 模块 ============
  static const String kycSubmit = '/kyc/submit';
  static const String kycStatus = '/kyc/status';
  static const String kycDetail = '/kyc/detail';

  // ============ 用户模块 ============
  static const String userList = '/user/list';
  static const String userDetail = '/user';
  static const String userCreate = '/user';
  static const String userUpdate = '/user';
  static const String userDelete = '/user';
  static const String userStatus = '/user/status';
  static const String userResetPassword = '/user/reset-password';
}