import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/redirect/:path(.*)',
    component: () => import('../views/Redirect.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    redirect: '/search'
  },
  {
    path: '/search',
    name: 'ImageSearch',
    component: () => import('../components/ImageSearch.vue'),
    meta: { 
      title: '图像检索',
      icon: 'Search',
      affix: true // 固定标签
    }
  },
  {
    path: '/ingest',
    name: 'ProductIngest',
    component: () => import('../components/ProductIngest/index.vue'),
    meta: { 
      title: '产品入库',
      icon: 'Upload',
      affix: false
    }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: () => import('../components/ProductList.vue'),
    meta: { 
      title: '产品管理',
      icon: 'Management',
      affix: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 以图搜品系统`
  }
  next()
})

export default router
