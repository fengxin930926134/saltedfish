import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
	routes: [{ // 重定向
			path: '/login',
			name: 'login',
			component: () => import('./views/login.vue')
		}, {
			path: '/index',
			name: 'main',
			component: () => import('./views/main.vue'),
			children: [{
				path: '/index',
				name: '首页',
				component: () => import('./views/index.vue')
			}, {
				path: '/page2',
				name: 'page2',
				component: () => import('./views/page2.vue')
			}, {
				path: '/page3',
				name: 'page3',
				component: () => import('./views/page3.vue')
			},{
				path: '/room',
				name: 'room',
				component: () => import('./views/room.vue')
			},{
				path: '/landlords',
				name: 'landlords',
				component: () => import('./views/landlords.vue')
			},]
		},
		{ // 重定向
			path: '/',
			redirect: '/index'
		}
	]
})
