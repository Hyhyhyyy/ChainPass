import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../router/routes.dart';
import 'bottom_nav.dart';

/// 带底部导航的页面包装器
class ScaffoldWrapper extends StatelessWidget {
  final Widget child;
  final String? currentRoute;
  final bool showBottomNav;
  final PreferredSizeWidget? appBar;
  final Widget? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;

  const ScaffoldWrapper({
    super.key,
    required this.child,
    this.currentRoute,
    this.showBottomNav = true,
    this.appBar,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: appBar,
      body: child,
      bottomNavigationBar: showBottomNav ? AppBottomNav(currentRoute: currentRoute) : null,
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
    );
  }
}

/// 带 AppBar 的页面包装器
class PageScaffold extends StatelessWidget {
  final String title;
  final Widget body;
  final List<Widget>? actions;
  final Widget? leading;
  final bool showBackButton;
  final bool showBottomNav;
  final Widget? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;
  final bool centerTitle;

  const PageScaffold({
    super.key,
    required this.title,
    required this.body,
    this.actions,
    this.leading,
    this.showBackButton = true,
    this.showBottomNav = false,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
    this.centerTitle = true,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
        centerTitle: centerTitle,
        leading: showBackButton
            ? leading ?? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => Navigator.of(context).pop(),
              )
            : leading,
        actions: actions,
      ),
      body: body,
      bottomNavigationBar: showBottomNav ? const AppBottomNav() : null,
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
    );
  }
}