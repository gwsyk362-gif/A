import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainView from '../views/user/MainView.vue'
import MainContent from '../views/user/MainContent.vue'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/main',
    component: MainView,
    children: [
      {
        path: '',
        name: 'MainContent',
        component: MainContent
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('../views/user/UserView.vue')
      }
    ]
  },
  {
    path: '/manager',
    name: 'Manager',
    component: () => import('../views/manager/ManagerView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录与权限拦截
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('currentUser')

  // 如果访问非登录页，且没有用户信息，强制跳回登录页
  if (to.name !== 'Login' && !userStr) {
    next({ name: 'Login' })
  } else {
    // 管理员权限校验
    if (userStr && to.path.startsWith('/manager')) {
      const user = JSON.parse(userStr);
    }
    next()
  }
})

export default router
