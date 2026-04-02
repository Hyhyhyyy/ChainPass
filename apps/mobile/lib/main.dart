import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'app.dart';
import 'core/theme/app_theme.dart';
import 'router/app_router.dart';
import 'shared/providers/app_provider.dart';
import 'l10n/app_localizations.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 初始化 Hive
  // await Hive.initFlutter();

  runApp(
    const ProviderScope(
      child: ChainPassApp(),
    ),
  );
}

class ChainPassApp extends ConsumerWidget {
  const ChainPassApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appState = ref.watch(appProvider);
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'ChainPass',
      debugShowCheckedModeBanner: false,

      // 主题配置
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: appState.themeMode,

      // 路由配置
      routerConfig: router,

      // 国际化配置
      localizationsDelegates: const [
        AppLocalizationsDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('zh', 'CN'),
        Locale('en', 'US'),
      ],
      locale: appState.locale,
    );
  }
}