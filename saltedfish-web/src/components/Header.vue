<!-- 头部左侧不变只有内容区改变 这样的需求可以用嵌套路由来做 -->
<template>
	<div class="header">
<!--		大屏幕-->
		<div v-if="screenType">
			<el-menu
				:default-active="activePath"
				router
				@select="handleSelect"
				class="flex flex_around"
			>
				<el-menu-item v-for="(item, index) in headerList" :key="index" :index="item.path">
					<a
						v-if="item.typ"
						target="_blank"
						:href="item.path"
						:class="item.path === activePath ? 'aPathActive' : ''"
					>
						{{ item.name }}
					</a>
					<router-link
						v-else
						tag="a"
						:to="item.path"
						:class="item.path === activePath ? 'aPathActive' : ''"
					>
						<span class="a-inner">{{ item.name }}</span>
					</router-link>

					</el-menu-item
				>
			</el-menu>
		</div>
<!--		小屏幕-->
		<div v-else class="text_right">
			<el-dropdown trigger="click">
				<span class="">
					<i class="iconfont" style="font-size: 34px;padding-right: 20px;">&#xe60c;</i>
				</span>
				<el-dropdown-menu slot="dropdown">
					<el-dropdown-item
						v-for="(item, index) in headerList"
						:key="index"
						:index="item.path"
					>
						<a
								v-if="item.typ"
								target="_blank"
								:href="item.path"
								:class="item.path === activePath ? 'aPathActive' : ''"
						>
							{{ item.name }}
						</a>
						<router-link
							v-else
							tag="a"
							:to="item.path"
							:class="item.path === activePath ? 'aPathActive' : ''"
						>
							<span class="a-inner">{{ item.name }}</span>
						</router-link>
					</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
		</div>
	</div>
</template>

<script>
export default {
	props: {
		headerActive: {
			type: String,
			default: '/index'
		},
		headerDefault: {
			type: Boolean,
			default: false
		}
	},
	data() {
		return {
			activePath: '/index',
			visibleShow: false,
			screenType: true,
			headerList: [
				{
					path: '/index',
					name: '首页',
					typ: false //是否跳转其它网址
				},
				{
					path: '/page2',
					name: '页面2',
					typ: false //是否跳转其它网址
				},
				{
					path: '/page3',
					name: '页面3',
					typ: false //是否跳转其它网址
				},
				// {
				// 	path: 'http://kmind.top/',
				// 	name: '简搜',
				// 	typ: true //是否跳转其它网址
				// },
			]
		};
	},
	watch: {
		$route(to) {
			this.activePath = to.path;
		},
		'$store.state.w': function() {
			let w = this.$store.state.w;
			if (w < 800) {
				this.screenType = false; //判定是否小屏幕
			} else {
				this.screenType = true;
			}
		}
	},
	methods: {
		handleSelect(key, keyPath) {
			// console.log(key, keyPath);
		}
	},
	mounted() {
		let _this = this;
		_this.activePath = _this.headerDefault ? _this.headerActive : _this.$route.path;
		// console.log(_this.$route.path);
		let getWidth = _this.$store.state.w;
		// alert(getWidth)
		if (getWidth < 800) {
			_this.screenType = false; //判定移动设备
		} else {
			_this.screenType = true;
		}
	}
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="less">
.header {
	.el-menu {
		padding: 0 10px;
		border: none;
		border-radius: 5px;
		.el-menu-item {
			color: @textc1;
			height: 40px;
			line-height: 40px;
			border: none;
			font-size: 24px;
			.iconfont {
				color: @textc1;
			}

			&:hover {
				background: none;
				color: @textc2;
				.iconfont {
					color: @textc2;
				}
			}
			&.is-active {
				color: @textc2 !important;
				background: none;
				border: none;
				.iconfont {
					color: @textc2;
				}
			}
		}
	}
}
/deep/.el-dialog {
	width: 80%;
}
/deep/.el-dialog__body {
	// width: 600px;
	img {
		width: 430px;
		height: 430px;
	}
}
.el-dropdown-menu {
	width: 300px;
	.el-dropdown-menu__item {
		font-size: 30px;
		a{
			display: inline-block;
			width: 100%;
			padding:16px 0;
		}
	}
}
</style>
