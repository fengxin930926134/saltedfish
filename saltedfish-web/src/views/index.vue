<template>
	<div class="box" >
		<el-card :body-style="{ padding: '0px'}" class="card" v-for="item in cardList" :key="item.id">
<!--			<img src="https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png" class="image" />-->
			<el-image
					style="width: 100%; height: 300px"
					src="https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png"
					fit="cover"></el-image>
			<div style="padding: 14px;">
				<span>{{item.name}}</span>
				<div>
					<el-tooltip class="item" effect="light" :content="item.desc" placement="top">
						<span class="desc tmp">{{ item.desc }}</span>
					</el-tooltip>
					<el-button type="text" class="button" @click="goRoomList(item.id)">进入</el-button>
				</div>
			</div>
		</el-card>
	</div>
</template>

<script>
	export default {
		data() {
			return {
				"cardList": [
					{
						id:"LANDLORDS",
						name:"欢乐斗地主",
						desc:"描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述",
					},
					{
						id:2,
						name:"斗地主",
						desc:"描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述",
					},
					{
						id:3,
						name:"斗地主",
						desc:"描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述",
					},
				]
			}
		},
		watch:{},
		methods: {
			goRoomList: function (id) {
				// this.$router.push('/room');
				this.$router.push({ path: '/room', query: { id: id } })
			}
		},
		created() {
		},
		mounted() {}
	}
</script>

<style scoped lang="less">
.box {
	display: flex; flex-flow:row wrap; justify-content: space-around;
	.card {
		flex-grow: 1;
		margin: 10px
	}
}

.desc {
	font-size: 13px;
	color: #999;
}

.tmp{
	white-space:nowrap;/*强制单行显示*/
	text-overflow:ellipsis;/*超出部分省略号表示*/
	overflow:hidden;/*超出部分隐藏*/
	width: 260px;/*设置显示的最大宽度*/
	display:inline-block;
}
.tmp-next{
	vertical-align: top;
}
</style>
