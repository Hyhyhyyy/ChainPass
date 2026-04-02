/// 通用 API 响应结构
class ApiResponse<T> {
  final int code;
  final String msg;
  final T? data;

  ApiResponse({
    required this.code,
    this.msg = '',
    this.data,
  });

  bool get isSuccess => code == 200;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>)? fromJsonT,
  ) {
    return ApiResponse(
      code: json['code'] as int? ?? 200,
      msg: json['msg'] as String? ?? '',
      data: json['data'] != null && fromJsonT != null
          ? fromJsonT(json['data'] as Map<String, dynamic>)
          : json['data'] as T?,
    );
  }
}

/// 分页响应
class PageResponse<T> {
  final List<T> list;
  final int total;
  final int page;
  final int pageSize;

  PageResponse({
    required this.list,
    required this.total,
    required this.page,
    required this.pageSize,
  });

  factory PageResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return PageResponse(
      list: (json['list'] as List)
          .map((e) => fromJsonT(e as Map<String, dynamic>))
          .toList(),
      total: json['total'] as int,
      page: json['page'] as int,
      pageSize: json['pageSize'] as int,
    );
  }
}

/// 登录响应
class LoginResponse {
  final String accessToken;
  final String refreshToken;
  final int? userId;
  final String? username;
  final String? nickname;
  final String? avatar;
  final String? giteeId;

  LoginResponse({
    required this.accessToken,
    required this.refreshToken,
    this.userId,
    this.username,
    this.nickname,
    this.avatar,
    this.giteeId,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      userId: json['userId'] as int?,
      username: json['username'] as String?,
      nickname: json['nickname'] as String?,
      avatar: json['avatar'] as String?,
      giteeId: json['giteeId'] as String?,
    );
  }
}

/// 用户信息
class UserInfo {
  final int id;
  final String username;
  final String nickname;
  final String email;
  final String phone;
  final String avatar;
  final int status;
  final List<String> roles;
  final List<String> permissions;
  final DateTime createdAt;
  final DateTime updatedAt;

  UserInfo({
    required this.id,
    required this.username,
    required this.nickname,
    required this.email,
    required this.phone,
    required this.avatar,
    required this.status,
    required this.roles,
    required this.permissions,
    required this.createdAt,
    required this.updatedAt,
  });

  factory UserInfo.fromJson(Map<String, dynamic> json) {
    return UserInfo(
      id: json['id'] as int,
      username: json['username'] as String,
      nickname: json['nickname'] as String? ?? '',
      email: json['email'] as String? ?? '',
      phone: json['phone'] as String? ?? '',
      avatar: json['avatar'] as String? ?? '',
      status: json['status'] as int? ?? 1,
      roles: (json['roles'] as List?)?.map((e) => e as String).toList() ?? [],
      permissions: (json['permissions'] as List?)?.map((e) => e as String).toList() ?? [],
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
    );
  }
}

/// 二维码会话
class QRSession {
  final String sessionId;
  final String type;
  final String qrContent;
  final String status;
  final int createdAt;
  final int expiresAt;
  final int? confirmedBy;
  final dynamic data;

  QRSession({
    required this.sessionId,
    required this.type,
    required this.qrContent,
    required this.status,
    required this.createdAt,
    required this.expiresAt,
    this.confirmedBy,
    this.data,
  });

  factory QRSession.fromJson(Map<String, dynamic> json) {
    return QRSession(
      sessionId: json['sessionId'] as String,
      type: json['type'] as String,
      qrContent: json['qrContent'] as String,
      status: json['status'] as String,
      createdAt: json['createdAt'] as int,
      expiresAt: json['expiresAt'] as int,
      confirmedBy: json['confirmedBy'] as int?,
      data: json['data'],
    );
  }

  bool get isExpired => DateTime.now().millisecondsSinceEpoch > expiresAt;
  bool get isPending => status == 'PENDING';
  bool get isScanned => status == 'SCANNED';
  bool get isConfirmed => status == 'CONFIRMED';
}

/// DID 文档
class DIDDocument {
  final List<String> context;
  final String id;
  final List<VerificationMethod> verificationMethod;
  final List<String> authentication;
  final DateTime created;

  DIDDocument({
    required this.context,
    required this.id,
    required this.verificationMethod,
    required this.authentication,
    required this.created,
  });

  factory DIDDocument.fromJson(Map<String, dynamic> json) {
    return DIDDocument(
      context: (json['@context'] as List).map((e) => e as String).toList(),
      id: json['id'] as String,
      verificationMethod: (json['verificationMethod'] as List)
          .map((e) => VerificationMethod.fromJson(e as Map<String, dynamic>))
          .toList(),
      authentication: (json['authentication'] as List)
          .map((e) => e as String)
          .toList(),
      created: DateTime.parse(json['created'] as String),
    );
  }
}

/// 验证方法
class VerificationMethod {
  final String id;
  final String type;
  final String controller;
  final String publicKeyBase64;

  VerificationMethod({
    required this.id,
    required this.type,
    required this.controller,
    required this.publicKeyBase64,
  });

  factory VerificationMethod.fromJson(Map<String, dynamic> json) {
    return VerificationMethod(
      id: json['id'] as String,
      type: json['type'] as String,
      controller: json['controller'] as String,
      publicKeyBase64: json['publicKeyBase64'] as String,
    );
  }
}

/// 钱包信息
class Wallet {
  final int id;
  final String did;
  final String address;
  final double balanceCny;
  final double balanceUsd;
  final double balanceEth;
  final double totalBalanceCny;
  final String status;

  Wallet({
    required this.id,
    required this.did,
    required this.address,
    required this.balanceCny,
    required this.balanceUsd,
    required this.balanceEth,
    required this.totalBalanceCny,
    required this.status,
  });

  factory Wallet.fromJson(Map<String, dynamic> json) {
    return Wallet(
      id: json['id'] as int,
      did: json['did'] as String,
      address: json['address'] as String,
      balanceCny: (json['balanceCny'] as num).toDouble(),
      balanceUsd: (json['balanceUsd'] as num).toDouble(),
      balanceEth: (json['balanceEth'] as num).toDouble(),
      totalBalanceCny: (json['totalBalanceCny'] as num).toDouble(),
      status: json['status'] as String,
    );
  }
}

/// KYC 状态
class KYCStatus {
  final bool verified;
  final int kycLevel;
  final String status;
  final String message;

  KYCStatus({
    required this.verified,
    required this.kycLevel,
    required this.status,
    required this.message,
  });

  factory KYCStatus.fromJson(Map<String, dynamic> json) {
    return KYCStatus(
      verified: json['verified'] as bool,
      kycLevel: json['kycLevel'] as int,
      status: json['status'] as String,
      message: json['message'] as String,
    );
  }
}