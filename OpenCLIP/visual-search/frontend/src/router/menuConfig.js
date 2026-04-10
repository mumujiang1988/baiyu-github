/**
 * 从路由配置中提取菜单项
 * 支持隐藏路由、权限控制、多级菜单
 */

export function generateMenuItems(routes, parentPath = '') {
  const menuItems = []
  
  routes.forEach(route => {
    // 跳过隐藏的路由
    if (route.meta?.hidden) return
    
    // 跳过 Layout 组件本身
    if (route.component?.name === 'Layout') {
      // 处理 Layout 的子路由
      if (route.children && route.children.length > 0) {
        menuItems.push(...generateMenuItems(route.children, parentPath))
      }
      return
    }
    
    const fullPath = parentPath ? `${parentPath}/${route.path}` : route.path
    
    const menuItem = {
      path: fullPath,
      name: route.name,
      title: route.meta?.title || '未命名',
      icon: route.meta?.icon,
      affix: route.meta?.affix || false,
      children: []
    }
    
    // 递归处理子路由
    if (route.children && route.children.length > 0) {
      menuItem.children = generateMenuItems(route.children, fullPath)
    }
    
    menuItems.push(menuItem)
  })
  
  return menuItems
}
