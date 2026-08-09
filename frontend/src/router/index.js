import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import NodeManage from '../views/NodeManage.vue'
import StrategyManage from '../views/StrategyManage.vue'

const routes = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    redirect: '/nodes',
    children: [
      { path: 'nodes', component: NodeManage, meta: { title: '节点管理' } },
      { path: 'strategies', component: StrategyManage, meta: { title: '规约管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 登录守卫：沿用原系统的随意登录逻辑(localStorage)
router.beforeEach((to, from, next) => {
  const logged = localStorage.getItem('iotgate_login')
  if (to.path !== '/login' && !logged) {
    next('/login')
  } else {
    next()
  }
})

export default router
