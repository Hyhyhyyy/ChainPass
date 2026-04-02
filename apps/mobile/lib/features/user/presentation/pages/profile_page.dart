import 'package:flutter/material.dart';

/// 用户资料页面
class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('个人中心')),
      body: ListView(
        children: [
          // 用户信息卡片
          Container(
            padding: const EdgeInsets.all(24),
            child: Row(
              children: [
                CircleAvatar(
                  radius: 40,
                  backgroundColor: const Color(0xFF1E88E5),
                  child: const Text(
                    'U',
                    style: TextStyle(fontSize: 32, color: Colors.white),
                  ),
                ),
                const SizedBox(width: 16),
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '用户名',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 4),
                    Text(
                      'user@example.com',
                      style: TextStyle(color: Colors.grey),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const Divider(),
          // 菜单项
          _MenuTile(
            icon: Icons.person_outline,
            title: '个人资料',
            onTap: () {},
          ),
          _MenuTile(
            icon: Icons.security_outlined,
            title: '安全设置',
            onTap: () {},
          ),
          _MenuTile(
            icon: Icons.devices_outlined,
            title: '登录设备',
            onTap: () {},
          ),
          _MenuTile(
            icon: Icons.history_outlined,
            title: '操作日志',
            onTap: () {},
          ),
          const Divider(),
          _MenuTile(
            icon: Icons.settings_outlined,
            title: '设置',
            onTap: () {},
          ),
          _MenuTile(
            icon: Icons.help_outline,
            title: '帮助与反馈',
            onTap: () {},
          ),
          _MenuTile(
            icon: Icons.info_outline,
            title: '关于',
            onTap: () {},
          ),
          const Divider(),
          _MenuTile(
            icon: Icons.logout,
            title: '退出登录',
            iconColor: Colors.red,
            onTap: () {
              _showLogoutDialog(context);
            },
          ),
        ],
      ),
    );
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              // TODO: 执行登出
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
            ),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}

class _MenuTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color? iconColor;
  final VoidCallback? onTap;

  const _MenuTile({
    required this.icon,
    required this.title,
    this.iconColor,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: iconColor ?? const Color(0xFF1E88E5)),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }
}