import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/colors.dart';
import '../../router/routes.dart';

/// 底部导航栏
class AppBottomNav extends StatelessWidget {
  final String? currentRoute;

  const AppBottomNav({super.key, this.currentRoute});

  @override
  Widget build(BuildContext context) {
    final location = currentRoute ?? GoRouterState.of(context).matchedLocation;

    int currentIndex = _getCurrentIndex(location);

    return BottomNavigationBar(
      currentIndex: currentIndex,
      onTap: (index) => _onTap(context, index),
      type: BottomNavigationBarType.fixed,
      selectedItemColor: AppColors.primary,
      unselectedItemColor: AppColors.textSecondary,
      backgroundColor: Colors.white,
      elevation: 8,
      items: const [
        BottomNavigationBarItem(
          icon: Icon(Icons.home_outlined),
          activeIcon: Icon(Icons.home),
          label: '首页',
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.badge_outlined),
          activeIcon: Icon(Icons.badge),
          label: '身份',
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.wallet_outlined),
          activeIcon: Icon(Icons.wallet),
          label: '支付',
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.person_outline),
          activeIcon: Icon(Icons.person),
          label: '我的',
        ),
      ],
    );
  }

  int _getCurrentIndex(String location) {
    if (location.startsWith(Routes.home)) return 0;
    if (location.startsWith(Routes.identity)) return 1;
    if (location.startsWith(Routes.payment)) return 2;
    if (location.startsWith(Routes.user)) return 3;
    return 0;
  }

  void _onTap(BuildContext context, int index) {
    final routes = [Routes.home, Routes.didManage, Routes.wallet, Routes.profile];
    final currentLocation = GoRouterState.of(context).matchedLocation;

    if (routes[index] != currentLocation) {
      context.go(routes[index]);
    }
  }
}

/// 导航项配置
class NavItem {
  final String route;
  final IconData icon;
  final IconData activeIcon;
  final String label;

  const NavItem({
    required this.route,
    required this.icon,
    required this.activeIcon,
    required this.label,
  });
}

/// 预定义导航项
const List<NavItem> defaultNavItems = [
  NavItem(
    route: Routes.home,
    icon: Icons.home_outlined,
    activeIcon: Icons.home,
    label: '首页',
  ),
  NavItem(
    route: Routes.didManage,
    icon: Icons.badge_outlined,
    activeIcon: Icons.badge,
    label: '身份',
  ),
  NavItem(
    route: Routes.wallet,
    icon: Icons.wallet_outlined,
    activeIcon: Icons.wallet,
    label: '支付',
  ),
  NavItem(
    route: Routes.profile,
    icon: Icons.person_outline,
    activeIcon: Icons.person,
    label: '我的',
  ),
];