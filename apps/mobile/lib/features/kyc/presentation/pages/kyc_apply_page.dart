import 'package:flutter/material.dart';

/// KYC 认证申请页面
class KYCApplyPage extends StatelessWidget {
  const KYCApplyPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('KYC 认证')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '身份认证',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '完成 KYC 认证后，您将获得可验证凭证，可用于跨境支付等场景。',
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          _KYCLevelCard(
            level: 1,
            title: '基础认证',
            description: '姓名、国籍、证件信息',
            status: '未认证',
            onTap: () {},
          ),
          _KYCLevelCard(
            level: 2,
            title: '中级认证',
            description: '增加人脸识别、地址验证',
            status: '未认证',
            enabled: false,
            onTap: () {},
          ),
          _KYCLevelCard(
            level: 3,
            title: '高级认证',
            description: '完整企业级认证',
            status: '未认证',
            enabled: false,
            onTap: () {},
          ),
        ],
      ),
    );
  }
}

class _KYCLevelCard extends StatelessWidget {
  final int level;
  final String title;
  final String description;
  final String status;
  final bool enabled;
  final VoidCallback? onTap;

  const _KYCLevelCard({
    required this.level,
    required this.title,
    required this.description,
    required this.status,
    this.enabled = true,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: enabled
                ? const Color(0xFF1E88E5).withOpacity(0.1)
                : Colors.grey[200],
            borderRadius: BorderRadius.circular(24),
          ),
          child: Center(
            child: Text(
              'L$level',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: enabled ? const Color(0xFF1E88E5) : Colors.grey,
              ),
            ),
          ),
        ),
        title: Text(title),
        subtitle: Text(description),
        trailing: Text(
          status,
          style: TextStyle(
            color: status == '已认证' ? Colors.green : Colors.orange,
          ),
        ),
        onTap: enabled ? onTap : null,
      ),
    );
  }
}