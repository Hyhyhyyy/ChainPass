import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/api_client.dart';
import '../../../shared/providers/app_provider.dart';

/// 二维码确认页面
/// 从深度链接进入，用于确认登录或其他操作
class QRConfirmPage extends ConsumerStatefulWidget {
  final String sessionId;
  final String? operationType;

  const QRConfirmPage({
    super.key,
    required this.sessionId,
    this.operationType,
  });

  @override
  ConsumerState<QRConfirmPage> createState() => _QRConfirmPageState();
}

class _QRConfirmPageState extends ConsumerState<QRConfirmPage> {
  bool _isLoading = false;
  bool _isConfirmed = false;

  Future<void> _handleConfirm() async {
    setState(() => _isLoading = true);

    try {
      final api = ref.read(apiClientProvider);
      final userId = ref.read(appProvider).userId;

      // 扫描
      await api.post<void>(
        '/qr/scan/${widget.sessionId}',
        fromJson: (_) => null,
      );

      // 确认
      final response = await api.post<Map<String, dynamic>>(
        '/qr/confirm/${widget.sessionId}',
        data: {'operationType': widget.operationType ?? 'LOGIN'},
        fromJson: (json) => json,
      );

      if (response.isSuccess) {
        setState(() => _isConfirmed = true);

        // 2秒后关闭页面
        Future.delayed(const Duration(seconds: 2), () {
          if (mounted) Navigator.of(context).pop(true);
        });
      } else {
        _showError(response.msg);
      }
    } catch (e) {
      _showError('确认失败: $e');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.red),
    );
  }

  @override
  Widget build(BuildContext context) {
    final operationTitle = _getOperationTitle();

    return Scaffold(
      appBar: AppBar(
        title: Text(operationTitle),
      ),
      body: Center(
        child: _isConfirmed
            ? _buildSuccessView()
            : _buildConfirmView(operationTitle),
      ),
    );
  }

  String _getOperationTitle() {
    switch (widget.operationType) {
      case 'LOGIN':
        return '确认登录';
      case 'PAYMENT_CONFIRM':
        return '确认支付';
      case 'DID_REVOKE':
        return '确认吊销';
      default:
        return '确认操作';
    }
  }

  Widget _buildConfirmView(String title) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.devices,
            size: 80,
            color: Color(0xFF1E88E5),
          ),
          const SizedBox(height: 24),
          Text(
            title,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            _getConfirmMessage(),
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 16,
              color: Colors.grey,
            ),
          ),
          const SizedBox(height: 32),
          SizedBox(
            width: double.infinity,
            height: 50,
            child: ElevatedButton(
              onPressed: _isLoading ? null : _handleConfirm,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF1E88E5),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: _isLoading
                  ? const CircularProgressIndicator(color: Colors.white)
                  : const Text(
                      '确认',
                      style: TextStyle(fontSize: 16),
                    ),
            ),
          ),
          const SizedBox(height: 16),
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('取消'),
          ),
        ],
      ),
    );
  }

  String _getConfirmMessage() {
    switch (widget.operationType) {
      case 'LOGIN':
        return '是否确认在另一台设备上登录 ChainPass？';
      case 'PAYMENT_CONFIRM':
        return '是否确认此支付操作？请核实支付信息。';
      case 'DID_REVOKE':
        return '是否确认吊销此 DID？此操作不可撤销。';
      default:
        return '是否确认执行此操作？';
    }
  }

  Widget _buildSuccessView() {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 80,
          height: 80,
          decoration: BoxDecoration(
            color: Colors.green.withOpacity(0.1),
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.check,
            size: 48,
            color: Colors.green,
          ),
        ),
        const SizedBox(height: 24),
        const Text(
          '操作成功',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 8),
        const Text(
          '请在另一台设备上继续操作',
          style: TextStyle(color: Colors.grey),
        ),
      ],
    );
  }
}