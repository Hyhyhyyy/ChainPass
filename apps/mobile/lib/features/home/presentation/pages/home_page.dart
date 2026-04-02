import 'package:flutter/material.dart';

/// 首页
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('ChainPass'),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_outlined),
            onPressed: () {},
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildWelcomeCard(),
          const SizedBox(height: 16),
          _buildQuickActions(context),
          const SizedBox(height: 24),
          _buildStatusSection(),
        ],
      ),
    );
  }

  Widget _buildWelcomeCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1E88E5), Color(0xFF64B5F6)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '欢迎使用 ChainPass',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '安全、可信的区块链数字身份',
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '快捷功能',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            _QuickActionCard(
              icon: Icons.badge_outlined,
              label: 'DID 管理',
              color: const Color(0xFF4CAF50),
              onTap: () {
                // TODO: 跳转 DID 管理页
              },
            ),
            const SizedBox(width: 12),
            _QuickActionCard(
              icon: Icons.description_outlined,
              label: '我的凭证',
              color: const Color(0xFFFF9800),
              onTap: () {
                // TODO: 跳转凭证页
              },
            ),
            const SizedBox(width: 12),
            _QuickActionCard(
              icon: Icons.wallet_outlined,
              label: '钱包',
              color: const Color(0xFFE53935),
              onTap: () {
                // TODO: 跳转钱包页
              },
            ),
            const SizedBox(width: 12),
            _QuickActionCard(
              icon: Icons.verified_user_outlined,
              label: 'KYC',
              color: const Color(0xFF9C27B0),
              onTap: () {
                // TODO: 跳转 KYC 页
              },
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildStatusSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '状态概览',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        _buildStatusItem(Icons.badge, 'DID 身份', '未创建', false),
        _buildStatusItem(Icons.description, '可验证凭证', '0 个', false),
        _buildStatusItem(Icons.verified_user, 'KYC 认证', '未认证', false),
        _buildStatusItem(Icons.wallet, '钱包余额', '¥ 0.00', true),
      ],
    );
  }

  Widget _buildStatusItem(IconData icon, String title, String value, bool isPositive) {
    return ListTile(
      leading: Icon(icon, color: const Color(0xFF1E88E5)),
      title: Text(title),
      trailing: Text(
        value,
        style: TextStyle(
          color: isPositive ? const Color(0xFF4CAF50) : Colors.grey[600],
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback? onTap;

  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.color,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            children: [
              Icon(icon, color: color, size: 28),
              const SizedBox(height: 8),
              Text(
                label,
                style: TextStyle(
                  fontSize: 12,
                  color: color,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}