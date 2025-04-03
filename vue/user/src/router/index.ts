import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import ArticleList from '@/views/ArticleList.vue'
import ArticleDetail from '@/views/ArticleDetail.vue'
import UserCenter from '@/views/UserCenter.vue'
import Login from '@/views/Login.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/articles',
      name: 'articles',
      component: ArticleList
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: ArticleDetail
    },
    {
      path: '/user',
      name: 'user-center',
      component: UserCenter,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router 