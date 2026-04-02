/// 统一 API 响应结构
class ApiResponse<T> {
  /// 响应码（200 表示成功）
  final int code;

  /// 响应消息
  final String msg;

  /// 响应数据
  final T? data;

  ApiResponse({
    required this.code,
    this.msg = '',
    this.data,
  });

  /// 是否成功
  bool get isSuccess => code == 200;

  /// 是否失败
  bool get isFailure => !isSuccess;

  /// 工厂方法：从 JSON 解析
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

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'code': code,
      'msg': msg,
      'data': data,
    };
  }

  @override
  String toString() {
    return 'ApiResponse(code: $code, msg: $msg, data: $data)';
  }
}