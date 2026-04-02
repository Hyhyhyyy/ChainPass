import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';

/// 加载指示器
class CpLoading extends StatelessWidget {
  final double size;
  final double strokeWidth;
  final Color? color;
  final String? message;

  const CpLoading({
    super.key,
    this.size = 40,
    this.strokeWidth = 3,
    this.color,
    this.message,
  });

  @override
  Widget build(BuildContext context) {
    Widget loading = CircularProgressIndicator(
      strokeWidth: strokeWidth,
      valueColor: AlwaysStoppedAnimation<Color>(
        color ?? AppColors.primary,
      ),
    );

    if (size != 40) {
      loading = SizedBox(width: size, height: size, child: loading);
    }

    if (message != null) {
      loading = Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          loading,
          const SizedBox(height: 16),
          Text(
            message!,
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      );
    }

    return loading;
  }
}

/// 全屏加载遮罩
class CpLoadingOverlay extends StatelessWidget {
  final bool isLoading;
  final String? message;
  final Widget child;

  const CpLoadingOverlay({
    super.key,
    required this.isLoading,
    this.message,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        child,
        if (isLoading)
          Positioned.fill(
            child: Container(
              color: Colors.black.withOpacity(0.3),
              child: Center(
                child: CpLoading(message: message),
              ),
            ),
          ),
      ],
    );
  }
}

/// 加载状态页面
class CpLoadingPage extends StatelessWidget {
  final String? message;

  const CpLoadingPage({super.key, this.message});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: CpLoading(message: message ?? '加载中...'),
      ),
    );
  }
}