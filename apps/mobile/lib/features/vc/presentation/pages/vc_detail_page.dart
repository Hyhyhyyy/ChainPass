import 'package:flutter/material.dart';

/// VC 凭证详情页面
class VCDetailPage extends StatelessWidget {
  final String vcId;

  const VCDetailPage({super.key, required this.vcId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('凭证详情')),
      body: Center(child: Text('凭证详情页面 - $vcId')),
    );
  }
}