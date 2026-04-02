import 'package:flutter/material.dart';

/// 设置页面
class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('设置')),
      body: ListView(
        children: [
          _SettingsSection(title: '外观'),
          _SettingsTile(
            icon: Icons.palette_outlined,
            title: '主题',
            subtitle: '跟随系统',
            onTap: () {
              _showThemeDialog(context);
            },
          ),
          _SettingsTile(
            icon: Icons.language_outlined,
            title: '语言',
            subtitle: '简体中文',
            onTap: () {
              _showLanguageDialog(context);
            },
          ),
          _SettingsSection(title: '安全'),
          _SettingsTile(
            icon: Icons.fingerprint,
            title: '生物识别',
            trailing: Switch(value: false, onChanged: (_) {}),
          ),
          _SettingsTile(
            icon: Icons.lock_outline,
            title: '修改密码',
            onTap: () {},
          ),
          _SettingsSection(title: '通知'),
          _SettingsTile(
            icon: Icons.notifications_outlined,
            title: '推送通知',
            trailing: Switch(value: true, onChanged: (_) {}),
          ),
          _SettingsSection(title: '其他'),
          _SettingsTile(
            icon: Icons.cleaning_services_outlined,
            title: '清除缓存',
            subtitle: '0 MB',
            onTap: () {},
          ),
          _SettingsTile(
            icon: Icons.info_outline,
            title: '关于',
            subtitle: 'v1.0.0',
            onTap: () {},
          ),
        ],
      ),
    );
  }

  void _showThemeDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('选择主题'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            RadioListTile<String>(
              title: const Text('浅色'),
              value: 'light',
              groupValue: 'system',
              onChanged: (_) => Navigator.of(ctx).pop(),
            ),
            RadioListTile<String>(
              title: const Text('暗色'),
              value: 'dark',
              groupValue: 'system',
              onChanged: (_) => Navigator.of(ctx).pop(),
            ),
            RadioListTile<String>(
              title: const Text('跟随系统'),
              value: 'system',
              groupValue: 'system',
              onChanged: (_) => Navigator.of(ctx).pop(),
            ),
          ],
        ),
      ),
    );
  }

  void _showLanguageDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('选择语言'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            RadioListTile<String>(
              title: const Text('简体中文'),
              value: 'zh_CN',
              groupValue: 'zh_CN',
              onChanged: (_) => Navigator.of(ctx).pop(),
            ),
            RadioListTile<String>(
              title: const Text('English'),
              value: 'en_US',
              groupValue: 'zh_CN',
              onChanged: (_) => Navigator.of(ctx).pop(),
            ),
          ],
        ),
      ),
    );
  }
}

class _SettingsSection extends StatelessWidget {
  final String title;

  const _SettingsSection({required this.title});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.grey[600],
        ),
      ),
    );
  }
}

class _SettingsTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? trailing;
  final VoidCallback? onTap;

  const _SettingsTile({
    required this.icon,
    required this.title,
    this.subtitle,
    this.trailing,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: const Color(0xFF1E88E5)),
      title: Text(title),
      subtitle: subtitle != null ? Text(subtitle!) : null,
      trailing: trailing ?? const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }
}