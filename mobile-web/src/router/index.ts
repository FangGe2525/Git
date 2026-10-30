import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory('/m/'),
  routes: [
    { path: '/', redirect: '/tickets' },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    { path: '/report', name: 'report', component: () => import('../views/Report.vue') },
    { path: '/tickets', name: 'tickets', component: () => import('../views/MyTickets.vue') },
    { path: '/tickets/:id', name: 'ticket-detail', component: () => import('../views/TicketDetail.vue') }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return '/tickets'
  }
  return true
})

export default router
