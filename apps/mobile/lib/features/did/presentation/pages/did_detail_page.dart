import 'package:flutter/material.dart';

/// DID 详情页面
class DIDDetailPage extends StatelessWidget {
  final String did;

  const DIDDetailPage({super.key, required this.did});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('DID 详情')),
      body: Center(child: Text('DID 详情页面 - $did')),
    );
  }
}