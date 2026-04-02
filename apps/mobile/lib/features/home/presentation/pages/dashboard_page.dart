import 'package:flutter/material.dart';

/// 仪表盘页面
class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('仪表盘')),
      body: const Center(child: Text('仪表盘页面 - 待实现')),
    );
  }
}