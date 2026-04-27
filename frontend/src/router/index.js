import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainView from '../views/MainView.vue'
import MainContent from '../views/MainContent.vue'

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
        component: () => import('../views/UserView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('currentUser')
  if (to.name !== 'Login' && !user) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
