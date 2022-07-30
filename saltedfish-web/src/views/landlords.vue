<template>
    <div>
        <div class="pai-border m-b-20" style="height: 400px; overflow: auto;">
            <p class="m-b-20">&nbsp;</p>
            <p class="m-b-20" v-for="(item, i) in gameData.log" v-bind:key="i">{{item}}</p>
            <p class="m-b-20" ref="bt">&nbsp;</p>
        </div>
        <div class="cards m-b-20" v-if="!gameData.landlord && gameData.currentSort === gameData.sort">
            <el-button @click="beLandlord(true)">叫（抢）地主</el-button>
            <el-button @click="beLandlord(false)">不叫（抢）</el-button>
        </div>
        <div class="cards m-b-20" style="color: darkred;" v-if="gameData.landlord === userId">
            地主
        </div>
        <div class="cards m-b-20 m-t-40">
            玩家 {{userId}}
        </div>
        <div class="cards m-b-20">
            <div :class="!isSelect[i]?'pai pai-border':'pai pai-border pai-click'"
                 v-for="(item, i) in gameData.handCards"
                 v-bind:key="i"
                 @click="cardsOnClick(i)">{{item}}
            </div>
        </div>
        <div class="cards" v-if="gameData.landlord && gameData.currentSort === gameData.sort">
            <el-button round type="primary" @click="playBrand(true)">出牌</el-button>
            <el-button round @click="playBrand(false)">过牌</el-button>
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
            // 出牌
            playBrand(is) {
                let indexList = [];
                this.isSelect.forEach((value, index) => {
                    if (value) {
                        indexList.push(index);
                    }
                });
                let values = this.gameData.handCards.filter((value, index) => {
                    return indexList.indexOf(index) !== -1
                });
                this.$axios.post("/netty/playBrand", {
                    userId: this.userId,
                    play: is,
                    brand: values
                }).then((e) => {
                    if (e && e.status === 200) {
                        this.gameData.handCards = this.gameData.handCards.filter((value, index) => {
                            return indexList.indexOf(index) === -1
                        });
                        indexList.forEach(item => {
                            this.$set(this.isSelect, item, false);
                        });
                    }
                });
            },
            // 叫地主和不叫地主
            beLandlord(is) {
                this.$axios.post("/netty/beLandlord", {
                    userId: this.userId,
                    isBeLandlord: is
                });
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
                        } else {
                            this.$router.push("/index")
                        }
                    });
                }
            },
        },
        created() {
            this.userId = getWsUserId();
            // 监听socket消息
            setMessageListener((obj) => {
                if (obj) {
                    switch (obj.msgType) {
                        case "PUSH_LOG": {
                            this.gameData.log.push(obj.content);
                            this.$refs.bt.scrollIntoView();
                        }
                            break;
                        case "NEXT_OPERATION":
                            this.gameData.currentSort = Number.parseInt(obj.content);
                            break;
                        case "LANDLORD_BEGIN_PLAY": {
                            let p = JSON.parse(obj.content);
                            this.gameData.currentSort = p.currentSort;
                            this.gameData.landlord = p.landlord;
                            if (p.landlord === this.userId) {
                                this.getGameInfo();
                            }
                        }
                            break;
                        case "GAME_OVER": {
                            this.$message.success("游戏结束, 3秒之后回到房间列表");
                            setTimeout(() => {
                                this.$router.push({ path: '/room', query: { id: "LANDLORDS" } })
                            }, 3000)
                        }
                            break;
                        // 重开
                        case "BEGIN_GAME": {
                            this.getGameInfo();
                        }
                            break;
                        default:
                            console.error("未处理类型")
                    }
                }
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
