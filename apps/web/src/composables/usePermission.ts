import { ref, computed } from 'vue'
import { useUserStore } from '@/stores'

/**
 * 权限检查
 */
export function usePermission() {
  const userStore = useUserStore()

  const hasPermission = (permission: string): boolean => {
    return userStore.hasPermission(permission)
  }

  const hasAnyPermission = (permissions: string[]): boolean => {
    return permissions.some((p) => hasPermission(p))
  }

  const hasAllPermissions = (permissions: string[]): boolean => {
    return permissions.every((p) => hasPermission(p))
  }

  const hasRole = (role: string): boolean => {
    return userStore.hasRole(role)
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some((r) => hasRole(r))
  }

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
  }
}