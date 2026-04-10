import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/search'
  },
  {
    path: '/search',
    name: 'ImageSearch',
    component: () => import('../components/ImageSearch.vue'),
    meta: { title: '图像检索' }
  },
  {
    path: '/ingest',
    name: 'ProductIngest',
    component: () => import('../components/ProductIngest/index.vue'),
    meta: { title: '产品入库' }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: () => import('../components/ProductList.vue'),
    meta: { title: '产品管理' }
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
