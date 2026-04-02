import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../core/theme/text_styles.dart';

/// 空状态组件
class CpEmpty extends StatelessWidget {
  final String? title;
  final String? message;
  final IconData icon;
  final Widget? action;

  const CpEmpty({
    super.key,
    this.title,
    this.message,
    this.icon = Icons.inbox_outlined,
    this.action,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 64,
              color: AppColors.textHint,
            ),
            const SizedBox(height: 16),
            if (title != null)
              Text(
                title!,
                style: TextStyles.titleMedium.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            if (message != null)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  message!,
                  style: TextStyles.bodyMedium.copyWith(
                    color: AppColors.textHint,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
            if (action != null) Padding(
              padding: const EdgeInsets.only(top: 24),
              child: action!,
            ),
          ],
        ),
      ),
    );
  }
}

/// 无数据状态
class NoDataEmpty extends StatelessWidget {
  final String? message;
  final VoidCallback? onRefresh;

  const NoDataEmpty({
    super.key,
    this.message,
    this.onRefresh,
  });

  @override
  Widget build(BuildContext context) {
    return CpEmpty(
      title: '暂无数据',
      message: message ?? '当前没有相关数据',
      icon: Icons.folder_open_outlined,
      action: onRefresh != null
          ? ElevatedButton.icon(
              onPressed: onRefresh,
              icon: const Icon(Icons.refresh),
              label: const Text('刷新'),
            )
          : null,
    );
  }
}

/// 无网络状态
class NoNetworkEmpty extends StatelessWidget {
  final VoidCallback? onRetry;

  const NoNetworkEmpty({super.key, this.onRetry});

  @override
  Widget build(BuildContext context) {
    return CpEmpty(
      title: '网络连接失败',
      message: '请检查网络连接后重试',
      icon: Icons.wifi_off_outlined,
      action: onRetry != null
          ? ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
            )
          : null,
    );
  }
}