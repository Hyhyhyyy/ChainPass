import 'package:flutter/material.dart';

/// DID 管理页面
class DIDManagePage extends StatelessWidget {
  const DIDManagePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('DID 身份管理')),
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
                    '什么是 DID？',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'DID (Decentralized Identifier) 是去中心化身份标识符，遵循 W3C 标准。'
                    '它允许用户自主控制自己的身份，无需依赖第三方机构。',
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () {
              // TODO: 创建 DID
            },
            icon: const Icon(Icons.add),
            label: const Text('创建 DID'),
          ),
          const SizedBox(height: 16),
          const Center(
            child: Text(
              '您还没有创建 DID',
              style: TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {},
        child: const Icon(Icons.qr_code_scanner),
      ),
    );
  }
}