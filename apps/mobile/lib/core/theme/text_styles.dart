import 'package:flutter/material.dart';

/// 文字样式定义
class TextStyles {
  // ============ 基础样式 ============
  static TextStyle get base => const TextStyle(
        fontFamily: 'Roboto',
        fontWeight: FontWeight.normal,
      );

  // ============ 标题样式 ============
  static TextStyle get titleLarge => base.copyWith(
        fontSize: 22,
        fontWeight: FontWeight.bold,
        letterSpacing: 0,
      );

  static TextStyle get titleMedium => base.copyWith(
        fontSize: 18,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.15,
      );

  static TextStyle get titleSmall => base.copyWith(
        fontSize: 14,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
      );

  // ============ 正文样式 ============
  static TextStyle get bodyLarge => base.copyWith(
        fontSize: 16,
        fontWeight: FontWeight.normal,
        letterSpacing: 0.5,
      );

  static TextStyle get bodyMedium => base.copyWith(
        fontSize: 14,
        fontWeight: FontWeight.normal,
        letterSpacing: 0.25,
      );

  static TextStyle get bodySmall => base.copyWith(
        fontSize: 12,
        fontWeight: FontWeight.normal,
        letterSpacing: 0.4,
      );

  // ============ 标签样式 ============
  static TextStyle get labelLarge => base.copyWith(
        fontSize: 14,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
      );

  static TextStyle get labelMedium => base.copyWith(
        fontSize: 12,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
      );

  static TextStyle get labelSmall => base.copyWith(
        fontSize: 11,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
      );

  // ============ 特殊样式 ============
  /// 数字/金额样式
  static TextStyle get amount => base.copyWith(
        fontSize: 24,
        fontWeight: FontWeight.bold,
        letterSpacing: 0,
      );

  /// DID 地址样式（小号等宽）
  static TextStyle get didAddress => const TextStyle(
        fontFamily: 'RobotoMono',
        fontSize: 12,
        fontWeight: FontWeight.normal,
        letterSpacing: 0,
      );

  /// 超链接样式
  static TextStyle get link => base.copyWith(
        fontSize: 14,
        fontWeight: FontWeight.w500,
        color: const Color(0xFF1E88E5),
        decoration: TextDecoration.underline,
      );

  /// 提示文字样式
  static TextStyle get hint => base.copyWith(
        fontSize: 14,
        fontWeight: FontWeight.normal,
        color: const Color(0xFF9E9E9E),
      );

  /// 错误文字样式
  static TextStyle get error => base.copyWith(
        fontSize: 12,
        fontWeight: FontWeight.normal,
        color: const Color(0xFFE53935),
      );

  /// 状态标签样式
  static TextStyle get statusTag => base.copyWith(
        fontSize: 12,
        fontWeight: FontWeight.w500,
      );
}