import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import GenerateView from '../views/GenerateView.vue'
import ExamView from '../views/ExamView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView
    },
    {
      path: '/generate',
      name: 'generate',
      component: GenerateView
    },
    {
      path: '/exam',
      name: 'exam',
      component: ExamView
    }
  ]
})

export default router
