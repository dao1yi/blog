import {createRouter, createWebHashHistory} from 'vue-router'

const router=createRouter({
  history: createWebHashHistory(),
  routes:[
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      component: () => import('./views/Login.vue')
    },
    {
      path: '/',
      component: () => import('./views/Layout.vue'),
      children: [
        {
          path: 'dashboard',
          component: () => import('./views/dashboard/Index.vue'),
        },
        {
          path: 'articles',
          component: () => import('./views/article/List.vue')
        },
        {
          path: 'articles/create',
          component: () => import('./views/article/Create.vue')
        },
        {
          path: 'articles/:id/edit',
          component: () => import('./views/article/Edit.vue')
        },
        {
          path: 'articles/:id',
          component: () => import('./views/article/Detail.vue')
        },
        {
          path: 'files',
          component: () => import('./views/file/List.vue')
        },
        {
          path: 'user/list',
          component: () => import('./views/user/List.vue'),
        },
        {
          path: 'resume',
          component: () => import('./views/resume/Index.vue'),
        }
      ]
    }
  ]
})

// 路由守卫
// router.beforeEach((to, from, next) => {
//   const token = localStorage.getItem('token')
//   const userRole = localStorage.getItem('userRole')

//   if (to.path === '/login') {
//     next()
//   } else if (!token) {
//     next('/login')
//   } else if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
//     next('/articles')
//   } else {
//     next()
//   }
// })

export default router