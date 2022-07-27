<template>
    <div>
        <div class="pai-border m-b-20" style="height: 400px; overflow: auto;">
            <p>&nbsp;</p>
            <p v-for="(item, i) in gameData.log" v-bind:key="i">{{item}}</p>
            <p ref="bt">&nbsp;</p>
        </div>
        <div class="cards m-b-20" v-if="!gameData.landlord">
            <el-button @click="beLandlord(true)">叫（抢）地主</el-button>
            <el-button @click="beLandlord(false)">不叫（抢）</el-button>
        </div>
        <div class="cards m-b-20 m-t-40">
            玩家 {{userId}} {{gameData.landlord === userId? '地主':''}}
        </div>
        <div class="cards m-b-20">
            <div :class="!isSelect[i]?'pai pai-border':'pai pai-border pai-click'"
                 v-for="(item, i) in gameData.handCards"
                 v-bind:key="i"
                 @click="cardsOnClick(i)">{{item}}
            </div>
        </div>
        <div class="cards" v-if="gameData.landlord">
            <el-button>出牌</el-button>
            <el-button>过牌</el-button>
        </div>
    </div>
</template>

<script>
    import {setMessageListener, getWsUserId} from '../plugins/websocket'

    export default {
        data() {
            return {
                userId: '',
                // 选中的牌
                isSelect: [],
                // 游戏数据
                gameData: {},
            }
        },
        watch: {},
        methods: {
            // 叫地主和不叫地主
            beLandlord(is) {
                console.log(is?"叫":"不叫");
            },
            // 选牌
            cardsOnClick(index) {
                this.$set(this.isSelect, index, !this.isSelect[index]);
                console.log(this.isSelect);
            },
            // 获取游戏信息
            getGameInfo() {
                if (this.$route.query.roomId == null) {
                    this.$router.push("/index")
                } else {
                    this.$axios.post("/netty/getGameInfo", {
                        wsUserId: this.userId,
                        roomId: this.$route.query.roomId,
                        roomType: "LANDLORDS"
                    }).then((e) => {
                        if (e && e.status === 200) {
                            this.gameData = JSON.parse(e.datas);
                        }
                    });
                }
            }
        },
        created() {
            this.userId = getWsUserId();
            // 监听socket消息
            setMessageListener((obj) => {
                console.log(obj);
            });
            // 获取牌组信息
            this.getGameInfo()
        },
        mounted() {
            this.$refs.bt.scrollIntoView();
        }
    }
</script>

<style scoped lang="less">
    .pai {
        width: 40px;
        height: 50px;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .pai-border {
        border: 2px solid @borderc1;
        border-radius: 4px;
    }

    .pai-click {
        box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
        color: #409eff;
        background: #ecf5ff;
        border-color: #b3d8ff;
    }

    .cards {
        display: flex;
        flex-flow: row wrap;
        justify-content: center;
    }
</style>
