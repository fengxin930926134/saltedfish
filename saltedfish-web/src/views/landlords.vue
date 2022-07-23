<template>
    <div>游戏界面</div>
</template>

<script>
    import {setMessageListener, getWsUserId} from '../plugins/websocket'

    export default {
        data() {
            return {}
        },
        watch: {},
        methods: {
            getGameInfo() {
                if (this.$route.query.roomId == null) {
                    this.$router.push("/index")
                } else {
					this.$axios.post("/netty/getGameInfo", {
						wsUserId: getWsUserId(),
						roomId: this.$route.query.roomId,
						roomType: "LANDLORDS"
					}).then((e) => {
						if (e && e.status === 200) {
							console.log(e.datas)
						}
					});
				}
            }
        },
        created() {
            // 监听socket消息
            setMessageListener((obj) => {
                console.log(obj);
            });
            // 获取牌组信息
            this.getGameInfo()
        },
        mounted() {
        }
    }
</script>

<style scoped lang="less">

</style>
