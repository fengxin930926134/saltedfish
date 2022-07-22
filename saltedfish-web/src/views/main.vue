<template>
	<div class="main">
		<el-container>
			<el-header>
				<div class="common_width main_header flex flex_align_center ">
					<!--						logo-->
					<div style="width:20%">
						<router-link to="/index">
							<img src="../assets/logo.png" style="height: 60px;" />
						</router-link>
					</div>
					<!--						菜单-->
					<div style="width:80%"><MyHeader /></div>
				</div>
			</el-header>
			<el-main>
				<div class="main_show common_width">
					<transition name="view"> <router-view /> </transition>
				</div>
			</el-main>
<!--			<el-footer>-->
<!--				<div class="common_width main_footer"><MyFooter /></div>-->
<!--			</el-footer>-->
		</el-container>
	</div>
</template>
<script>
import MyHeader from '../components/Header';
export default {
	components: {
		MyHeader,
		// MyFooter
	},
	data() {
		return {};
	},
	watch: {
		$route(to, from) {}
	},
	mounted() {
		let _this = this;
		window.onresize = function() {
			_this.$store.commit('screenResize');
		};
	}
};
</script>

<style scoped lang="less">
.main {
	.main_header {
		border-bottom: 1px solid @borderc1;
	}
	.main_show {
		padding: 20px 0;
		min-height: 400px;
	}
	.main_footer {
		border-top: 1px solid @borderc1;
	}
}

.view-enter-active {
	transition: opacity 0.4s;
}

.view-leave-active {
}

.view-enter,
.view-leave-active {
	opacity: 0;
}
</style>
