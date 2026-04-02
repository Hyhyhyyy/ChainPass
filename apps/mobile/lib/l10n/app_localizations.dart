import 'package:flutter/material.dart';

/// 应用本地化
class AppLocalizations {
  final Locale locale;

  AppLocalizations(this.locale);

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      AppLocalizationsDelegate();

  // ============ 通用 ============
  String get appName => 'ChainPass';
  String get loading => '加载中...';
  String get success => '成功';
  String get error => '错误';
  String get confirm => '确认';
  String get cancel => '取消';
  String get save => '保存';
  String get delete => '删除';
  String get edit => '编辑';
  String get refresh => '刷新';
  String get retry => '重试';
  String get submit => '提交';
  String get back => '返回';
  String get next => '下一步';
  String get done => '完成';
  String get search => '搜索';
  String get noData => '暂无数据';
  String get networkError => '网络连接失败';

  // ============ 认证 ============
  String get login => '登录';
  String get logout => '登出';
  String get register => '注册';
  String get forgotPassword => '忘记密码';
  String get username => '用户名';
  String get password => '密码';
  String get confirmPassword => '确认密码';
  String get email => '邮箱';
  String get phone => '手机号';
  String get nickname => '昵称';
  String get loginSuccess => '登录成功';
  String get loginFailed => '登录失败';
  String get registerSuccess => '注册成功';
  String get pleaseEnterUsername => '请输入用户名';
  String get pleaseEnterPassword => '请输入密码';
  String get passwordNotMatch => '密码不一致';

  // ============ 身份管理 ============
  String get identity => '身份管理';
  String get did => 'DID';
  String get didManage => 'DID 管理';
  String get didCreate => '创建 DID';
  String get didDetail => 'DID 详情';
  String get didAddress => 'DID 地址';
  String get didPublicKey => '公钥';
  String get didStatus => '状态';
  String get didCreateTime => '创建时间';
  String get didExpireTime => '过期时间';
  String get didVerify => '验证签名';
  String get didRevoke => '吊销 DID';
  String get didRevokeConfirm => '确认吊销此 DID？';

  String get vc => '可验证凭证';
  String get vcList => '凭证列表';
  String get vcDetail => '凭证详情';
  String get vcType => '凭证类型';
  String get vcIssuer => '签发者';
  String get vcHolder => '持有者';
  String get vcIssueTime => '签发时间';
  String get vcExpireTime => '过期时间';
  String get vcVerify => '验证凭证';
  String get vcRevoke => '吊销凭证';

  // ============ 支付 ============
  String get payment => '支付';
  String get wallet => '钱包';
  String get transfer => '转账';
  String get history => '交易记录';
  String get balance => '余额';
  String get amount => '金额';
  String get currency => '币种';
  String get exchangeRate => '汇率';
  String get payee => '收款方';
  String get payer => '付款方';
  String get orderNo => '订单号';
  String get paymentStatus => '支付状态';
  String get paymentSuccess => '支付成功';
  String get paymentFailed => '支付失败';
  String get cny => '人民币';
  String get usd => '美元';
  String get eth => '以太坊';

  // ============ KYC ============
  String get kyc => 'KYC 认证';
  String get kycApply => '申请认证';
  String get kycStatus => '认证状态';
  String get kycLevel => '认证等级';
  String get fullName => '姓名';
  String get nationality => '国籍';
  String get idType => '证件类型';
  String get idNumber => '证件号码';
  String get idCard => '身份证';
  String get passport => '护照';
  String get idDocument => '证件照片';
  String get facePhoto => '人脸照片';
  String get kycPending => '待审核';
  String get kycApproved => '已通过';
  String get kycRejected => '已拒绝';

  // ============ 用户中心 ============
  String get profile => '个人资料';
  String get security => '安全设置';
  String get settings => '设置';
  String get devices => '登录设备';
  String get changePassword => '修改密码';
  String get theme => '主题';
  String get language => '语言';
  String get themeLight => '浅色';
  String get themeDark => '暗色';
  String get themeSystem => '跟随系统';
  String get about => '关于';
  String get version => '版本';
}

/// 本地化 Delegate
class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const AppLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) {
    return ['zh', 'en'].contains(locale.languageCode);
  }

  @override
  Future<AppLocalizations> load(Locale locale) async {
    return AppLocalizations(locale);
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) {
    return false;
  }
}