/// 应用常量定义
class AppConstants {
  /// 本地存储键名
  static const String accessTokenKey = 'access_token';
  static const String refreshTokenKey = 'refresh_token';
  static const String tokenExpiresAtKey = 'token_expires_at';
  static const String refreshTokenExpiresAtKey = 'refresh_token_expires_at';
  static const String userIdKey = 'user_id';
  static const String usernameKey = 'username';
  static const String nicknameKey = 'nickname';
  static const String emailKey = 'email';
  static const String avatarKey = 'avatar';
  static const String rolesKey = 'roles';
  static const String permissionsKey = 'permissions';
  static const String didPrivateKeyPrefix = 'did_private_key_';

  /// 默认超时时间（毫秒）
  static const int defaultTimeout = 15000;

  /// 分页默认大小
  static const int defaultPageSize = 20;

  /// 正则表达式
  static const String emailRegex =
      r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$';
  static const String phoneRegex = r'^1[3-9]\d{9}$';
  static const String usernameRegex = r'^[a-zA-Z0-9_]{3,20}$';
}

/// 状态常量
class StatusConstants {
  /// DID 状态
  static const String didActive = 'ACTIVE';
  static const String didRevoked = 'REVOKED';
  static const String didExpired = 'EXPIRED';

  /// VC 状态
  static const String vcValid = 'VALID';
  static const String vcRevoked = 'REVOKED';
  static const String vcExpired = 'EXPIRED';

  /// 支付状态
  static const String paymentPending = 'PENDING';
  static const String paymentProcessing = 'PROCESSING';
  static const String paymentSuccess = 'SUCCESS';
  static const String paymentFailed = 'FAILED';

  /// KYC 状态
  static const String kycPending = 'PENDING';
  static const String kycApproved = 'APPROVED';
  static const String kycRejected = 'REJECTED';
}