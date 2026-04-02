import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../core/theme/text_styles.dart';
import '../../core/constants/status_constants.dart';

/// 状态标签组件
class StatusTag extends StatelessWidget {
  final String status;
  final Color? customColor;
  final String? customText;
  final double fontSize;
  final EdgeInsetsGeometry? padding;

  const StatusTag({
    super.key,
    required this.status,
    this.customColor,
    this.customText,
    this.fontSize = 12,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    final (color, text) = _getStatusConfig();

    return Container(
      padding: padding ?? const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        customText ?? text,
        style: TextStyles.statusTag.copyWith(
          color: color,
          fontSize: fontSize,
        ),
      ),
    );
  }

  (Color, String) _getStatusConfig() {
    if (customColor != null) {
      return (customColor!, customText ?? status);
    }

    // DID 状态
    if (status == StatusConstants.didActive) {
      return (AppColors.statusActive, '有效');
    }
    if (status == StatusConstants.didRevoked) {
      return (AppColors.statusRevoked, '已吊销');
    }
    if (status == StatusConstants.didExpired) {
      return (AppColors.statusExpired, '已过期');
    }

    // VC 状态
    if (status == StatusConstants.vcValid) {
      return (AppColors.statusActive, '有效');
    }
    if (status == StatusConstants.vcRevoked) {
      return (AppColors.statusRevoked, '已吊销');
    }
    if (status == StatusConstants.vcExpired) {
      return (AppColors.statusExpired, '已过期');
    }

    // 支付状态
    if (status == StatusConstants.paymentPending) {
      return (AppColors.statusPending, '待支付');
    }
    if (status == StatusConstants.paymentProcessing) {
      return (AppColors.info, '处理中');
    }
    if (status == StatusConstants.paymentSuccess) {
      return (AppColors.statusActive, '已完成');
    }
    if (status == StatusConstants.paymentFailed) {
      return (AppColors.statusRevoked, '失败');
    }

    // KYC 状态
    if (status == StatusConstants.kycPending) {
      return (AppColors.statusPending, '待审核');
    }
    if (status == StatusConstants.kycApproved) {
      return (AppColors.statusActive, '已通过');
    }
    if (status == StatusConstants.kycRejected) {
      return (AppColors.statusRevoked, '已拒绝');
    }

    // 默认
    return (AppColors.textSecondary, status);
  }
}

/// 验证状态标签
class VerifiedTag extends StatelessWidget {
  final bool isVerified;

  const VerifiedTag({super.key, required this.isVerified});

  @override
  Widget build(BuildContext context) {
    return StatusTag(
      status: '',
      customColor: isVerified ? AppColors.success : AppColors.warning,
      customText: isVerified ? '已验证' : '未验证',
    );
  }
}