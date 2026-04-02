import 'package:flutter/material.dart';

/// OAuth 回调页面
class OAuthCallbackPage extends StatefulWidget {
  final String code;

  const OAuthCallbackPage({super.key, required this.code});

  @override
  State<OAuthCallbackPage> createState() => _OAuthCallbackPageState();
}

class _OAuthCallbackPageState extends State<OAuthCallbackPage> {
  @override
  void initState() {
    super.initState();
    _handleCallback();
  }

  Future<void> _handleCallback() async {
    // TODO: 处理 OAuth 回调
    // 调用后端 API 换取 Token
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text('正在处理登录...'),
          ],
        ),
      ),
    );
  }
}