import 'package:flutter/material.dart';

/// ZKP 验证登录页面
class ZKPVerifyPage extends StatefulWidget {
  const ZKPVerifyPage({super.key});

  @override
  State<ZKPVerifyPage> createState() => _ZKPVerifyPageState();
}

class _ZKPVerifyPageState extends State<ZKPVerifyPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ZKP 身份认证')),
      body: const Center(child: Text('ZKP 验证页面 - 待实现')),
    );
  }
}