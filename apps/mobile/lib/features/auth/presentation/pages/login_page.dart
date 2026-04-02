import 'package:flutter/material.dart';
import '../../../shared/widgets/common/cp_button.dart';
import '../../../shared/widgets/common/cp_input.dart';
import '../../../shared/widgets/common/cp_card.dart';
import '../../../l10n/app_localizations.dart';

/// 登录页面
class LoginPage extends StatefulWidget {
  final String? redirectPath;

  const LoginPage({super.key, this.redirectPath});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    setState(() => _isLoading = true);
    try {
      // TODO: 实现登录逻辑
      await Future.delayed(const Duration(seconds: 1));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('登录成功')),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 60),
              // Logo
              const Icon(
                Icons.lock_person_outlined,
                size: 80,
                color: Color(0xFF1E88E5),
              ),
              const SizedBox(height: 24),
              // 标题
              Text(
                l10n.appName,
                style: const TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              Text(
                '区块链数字身份验证系统',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey[600],
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 48),
              // 登录表单
              CpInput(
                label: l10n.username,
                hint: l10n.pleaseEnterUsername,
                controller: _usernameController,
                prefixIcon: const Icon(Icons.person_outline),
              ),
              const SizedBox(height: 16),
              CpInput(
                label: l10n.password,
                hint: l10n.pleaseEnterPassword,
                controller: _passwordController,
                obscureText: true,
                prefixIcon: const Icon(Icons.lock_outline),
              ),
              const SizedBox(height: 24),
              // 登录按钮
              CpButton(
                text: l10n.login,
                isLoading: _isLoading,
                onPressed: _handleLogin,
              ),
              const SizedBox(height: 16),
              // 其他登录方式
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  TextButton(
                    onPressed: () {
                      // TODO: 跳转注册页
                    },
                    child: Text(l10n.register),
                  ),
                  TextButton(
                    onPressed: () {
                      // TODO: 跳转忘记密码
                    },
                    child: Text(l10n.forgotPassword),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              // 分割线
              const Row(
                children: [
                  Expanded(child: Divider()),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: Text('其他登录方式'),
                  ),
                  Expanded(child: Divider()),
                ],
              ),
              const SizedBox(height: 24),
              // OAuth 登录
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _OAuthButton(
                    icon: Icons.code,
                    label: 'Gitee',
                    onTap: () {
                      // TODO: Gitee 登录
                    },
                  ),
                  const SizedBox(width: 24),
                  _OAuthButton(
                    icon: Icons.fingerprint,
                    label: 'ZKP',
                    onTap: () {
                      // TODO: ZKP 登录
                    },
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _OAuthButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback? onTap;

  const _OAuthButton({
    required this.icon,
    required this.label,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey[300]!),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Icon(icon, size: 32, color: const Color(0xFF1E88E5)),
            const SizedBox(height: 8),
            Text(label, style: const TextStyle(fontSize: 12)),
          ],
        ),
      ),
    );
  }
}