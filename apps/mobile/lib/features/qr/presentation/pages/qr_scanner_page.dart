import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_response.dart';
import '../../../shared/providers/app_provider.dart';

/// 二维码扫描页面
class QRScannerPage extends StatefulWidget {
  const QRScannerPage({super.key});

  @override
  State<QRScannerPage> createState() => _QRScannerPageState();
}

class _QRScannerPageState extends State<QRScannerPage> {
  final MobileScannerController _controller = MobileScannerController();
  bool _isProcessing = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onDetect(BarcodeCapture capture) {
    if (_isProcessing) return;

    final barcodes = capture.barcodes;
    if (barcodes.isEmpty) return;

    final barcode = barcodes.first;
    final rawValue = barcode.rawValue;
    if (rawValue == null) return;

    _handleQRCode(rawValue);
  }

  Future<void> _handleQRCode(String rawValue) async {
    setState(() => _isProcessing = true);

    try {
      // 解析二维码内容
      final Map<String, dynamic> qrData = json.decode(rawValue);
      final type = qrData['type'] as String?;
      final sessionId = qrData['sessionId'] as String?;

      if (sessionId == null || type == null) {
        _showError('无效的二维码');
        return;
      }

      // 显示确认对话框
      final confirmed = await _showConfirmDialog(type, qrData);
      if (!confirmed) return;

      // 调用 API 扫描二维码
      final api = context.read(apiClientProvider);
      final userId = context.read(appProvider).userId;

      // 扫描
      await api.post<void>(
        '/qr/scan/$sessionId',
        fromJson: (_) => null,
      );

      // 确认
      final response = await api.post<Map<String, dynamic>>(
        '/qr/confirm/$sessionId',
        data: {'operationType': type},
        fromJson: (json) => json,
      );

      if (response.isSuccess) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('操作成功')),
          );
          Navigator.of(context).pop(true);
        }
      } else {
        _showError(response.msg);
      }
    } catch (e) {
      _showError('处理失败: $e');
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  Future<bool> _showConfirmDialog(String type, Map<String, dynamic> qrData) async {
    String title;
    String message;

    switch (type) {
      case 'LOGIN':
        title = '确认登录';
        message = '是否确认在另一台设备上登录 ChainPass？';
        break;
      case 'PAYMENT_CONFIRM':
        title = '确认支付';
        message = '是否确认此支付操作？';
        break;
      case 'DID_REVOKE':
        title = '确认吊销';
        message = '是否确认吊销此 DID？此操作不可撤销。';
        break;
      default:
        title = '确认操作';
        message = '是否确认执行此操作？';
    }

    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('确认'),
          ),
        ],
      ),
    );

    return result ?? false;
  }

  void _showError(String message) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message), backgroundColor: Colors.red),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('扫描二维码'),
        actions: [
          IconButton(
            icon: const Icon(Icons.flash_on),
            onPressed: () => _controller.toggleTorch(),
          ),
        ],
      ),
      body: Stack(
        children: [
          MobileScanner(
            controller: _controller,
            onDetect: _onDetect,
          ),
          // 扫描框
          Center(
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                border: Border.all(color: const Color(0xFF1E88E5), width: 3),
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
          // 提示文字
          Positioned(
            bottom: 100,
            left: 0,
            right: 0,
            child: Center(
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                decoration: BoxDecoration(
                  color: Colors.black54,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  '将二维码放入扫描框中',
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ),
          ),
          // 处理中遮罩
          if (_isProcessing)
            Container(
              color: Colors.black54,
              child: const Center(
                child: CircularProgressIndicator(),
              ),
            ),
        ],
      ),
    );
  }
}