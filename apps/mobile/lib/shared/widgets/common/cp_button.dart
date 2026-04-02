import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../core/theme/text_styles.dart';

/// 自定义按钮组件
enum CpButtonType { primary, secondary, outline, text, danger }

class CpButton extends StatelessWidget {
  final String text;
  final CpButtonType type;
  final VoidCallback? onPressed;
  final bool isLoading;
  final bool isDisabled;
  final IconData? icon;
  final double? width;
  final double height;
  final EdgeInsetsGeometry? padding;

  const CpButton({
    super.key,
    required this.text,
    this.type = CpButtonType.primary,
    this.onPressed,
    this.isLoading = false,
    this.isDisabled = false,
    this.icon,
    this.width,
    this.height = 48,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    final effectiveOnPressed = isDisabled || isLoading ? null : onPressed;

    Widget buttonChild = Row(
      mainAxisSize: MainAxisSize.min,
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        if (isLoading)
          const SizedBox(
            width: 20,
            height: 20,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
            ),
          )
        else if (icon != null)
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: Icon(icon, size: 18),
          ),
        if (!isLoading)
          Text(
            text,
            style: TextStyles.labelLarge.copyWith(color: _getTextColor()),
          ),
      ],
    );

    final buttonStyle = _getButtonStyle();

    Widget button;
    switch (type) {
      case CpButtonType.outline:
        button = OutlinedButton(
          onPressed: effectiveOnPressed,
          style: buttonStyle,
          child: buttonChild,
        );
        break;
      case CpButtonType.text:
        button = TextButton(
          onPressed: effectiveOnPressed,
          style: buttonStyle,
          child: buttonChild,
        );
        break;
      default:
        button = ElevatedButton(
          onPressed: effectiveOnPressed,
          style: buttonStyle,
          child: buttonChild,
        );
    }

    if (width != null) {
      button = SizedBox(width: width, height: height, child: button);
    } else {
      button = SizedBox(
        height: height,
        child: button,
      );
    }

    return button;
  }

  ButtonStyle _getButtonStyle() {
    switch (type) {
      case CpButtonType.primary:
        return ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          disabledBackgroundColor: AppColors.primary.withOpacity(0.5),
          disabledForegroundColor: Colors.white.withOpacity(0.7),
          padding: padding ?? const EdgeInsets.symmetric(horizontal: 24),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        );
      case CpButtonType.secondary:
        return ElevatedButton.styleFrom(
          backgroundColor: AppColors.textSecondary,
          foregroundColor: Colors.white,
          padding: padding ?? const EdgeInsets.symmetric(horizontal: 24),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        );
      case CpButtonType.outline:
        return OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          side: const BorderSide(color: AppColors.primary),
          padding: padding ?? const EdgeInsets.symmetric(horizontal: 24),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        );
      case CpButtonType.text:
        return TextButton.styleFrom(
          foregroundColor: AppColors.primary,
          padding: padding ?? const EdgeInsets.symmetric(horizontal: 16),
        );
      case CpButtonType.danger:
        return ElevatedButton.styleFrom(
          backgroundColor: AppColors.error,
          foregroundColor: Colors.white,
          padding: padding ?? const EdgeInsets.symmetric(horizontal: 24),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        );
    }
  }

  Color _getTextColor() {
    if (isDisabled) {
      return Colors.white.withOpacity(0.7);
    }
    switch (type) {
      case CpButtonType.primary:
      case CpButtonType.secondary:
      case CpButtonType.danger:
        return Colors.white;
      case CpButtonType.outline:
      case CpButtonType.text:
        return AppColors.primary;
    }
  }
}