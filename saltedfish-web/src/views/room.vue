<template>
    <div>
        <!--		查询-->
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
            <el-form-item label="房间名">
                <el-input v-model="formInline.user" placeholder="房间名"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="fetchData()">查询</el-button>
                <el-button type="primary" @click="openCreateRoom()">创建</el-button>
                <el-button type="primary" @click="goBack()">返回</el-button>
            </el-form-item>
        </el-form>
        <!--房间列表-->
        <el-table
                :data="tableData"
                style="width: 100%">
            <el-table-column
                    type="index">
            </el-table-column>
            <el-table-column
                    prop="roomName"
                    label="房间名">
            </el-table-column>
            <el-table-column
                    prop="number"
                    label="人数/总数">
                <template slot-scope="scope">
                    {{(scope.row.userIds? scope.row.userIds.length:0) + "/" + scope.row.number}}
                </template>
            </el-table-column>
            <el-table-column
                    prop="playing"
                    label="状态"
                    :filters="[{ text: '游戏中', value: true }, { text: '等待中', value: false }]"
                    :filter-method="filterTag"
                    filter-placement="bottom-end">
                <template slot-scope="scope">
                    <el-tag
                            :type="scope.row.playing ? 'danger' : 'success'"
                            disable-transitions>{{!scope.row.playing? '等待中' : '游戏中'}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <el-button
                            size="mini"
                            @click="handleAdd(scope.$index, scope.row)">加入
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-dialog
                title="等待中"
                :visible.sync="dialogVisible"
                :close-on-click-modal="false"
                :show-close="false"
                :center="true"
                width="30%">
            <span style="text-align: center;">当前所在房间人数{{this.roomCurrentNumber}}，满足{{this.roomNumber}}人游戏开始</span>
            <span slot="footer" class="dialog-footer">
				<el-button type="primary" @click="dialogVisible = false">退 出</el-button>
			</span>
        </el-dialog>
    </div>
</template>

<script>
    import {setMessageListener, isConnect, getWsUserId} from '../plugins/websocket'

    export default {
        data() {
            return {
                // 查询表单
                formInline: {
                    user: '',
                    region: ''
                },
                // 表格数据
                tableData: [],
                // 是否在房间
                dialogVisible: false,
                // 当前所在房间id
                roomId: null,
                // 当前所在房间人数
                roomCurrentNumber: 0,
                // 当前所在房间满足多少人开游戏
                roomNumber: 0,
            }
        },
        watch: {},
        methods: {
            filterTag(value, row) {
                return row.playing === value;
            },
            /**
             * 检查是否加入房间
             */
            inspectJoinRoom() {
                this.$axios.get("/netty/inspectJoinRoom/" + getWsUserId()).then((e) => {
                    if (e && e.datas) {
                        // 已加入房间
                        this.roomId = e.datas.id;
                        if (!e.datas.playing) {
                            this.roomNumber = e.datas.number;
                            this.roomCurrentNumber = e.datas.userIds.length;
                            this.dialogVisible = true;
                        } else {
                            // 重连游戏
                            this.beginGame();
                        }
                    }
                });
            },
            /**
             * 获取房间数据
             */
            fetchData() {
                if (this.$route.query.id == null) {
                    this.goBack();
                } else {
                    if (!isConnect()) {
                        this.$message.warning("未曾连接服务器，无法操作！");
                    } else {
                        this.$axios.get("/netty/roomList/" + this.$route.query.id).then((e) => {
                            if (e && e.datas) {
                                this.tableData = e.datas
                            }
                        });
                    }
                }
            },
            quitRoom() {
                if (this.roomId) {
                    this.$axios.post("/netty/quitRoom", {
                        "wsUserId": getWsUserId(),
                        "roomId": this.roomId,
                    }).then((e) => {
                        if (e && e.status === 200) {
                            this.roomId = null;
                            this.fetchData();
                        }
                    });
                } else {
                    this.$message.warning("未加入房间！")
                }
            },
            /**
             * 打开创建房间
             */
            openCreateRoom() {
                this.$prompt('请输入房间名', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消'
                }).then(({value}) => {
                    this.$axios.post("/netty/createRoom", {
                        "wsUserId": getWsUserId(),
                        "roomName": value,
                        "roomTypeEnum": this.$route.query.id
                    }).then((e) => {
                        if (e && e.status === 200) {
                            this.roomId = e.datas.id;
                            this.fetchData();
                            this.roomNumber = e.datas.number;
                            this.roomCurrentNumber = e.datas.userIds.length;
                            this.dialogVisible = true
                        }
                    });
                });
            },
            goBack() {
                this.$router.push("/index")
            },
            handleAdd(index, row) {
                this.$axios.post("/netty/joinRoom", {
                    "wsUserId": getWsUserId(),
                    "roomId": row.id,
                }).then((e) => {
                    if (e && e.status === 200) {
                        this.roomId = row.id;
                        this.roomNumber = row.number;
                        // 弹出
                        this.dialogVisible = true;
                        this.fetchData();
                    }
                });
            },
            // 跳转到游戏界面
            beginGame() {
                switch (this.$route.query.id) {
                    case "LANDLORDS":
                        this.$router.push({path: '/landlords', query: {roomId: this.roomId}});
                        break;
                    default:
                        this.$message.error("进入游戏异常")
                }
            }
        },
        created() {
            this.inspectJoinRoom();
            // watch 路由的参数，以便再次获取数据
            this.$watch(
                () => this.$route.query.id,
                () => {
                    this.fetchData()
                },
                // 组件创建完后立即获取数据，
                // 此时 data 已经被 observed 了
                {immediate: true}
            );
            // 监听房间弹窗
            this.$watch(
                () => this.dialogVisible,
                () => {
                    if (!this.dialogVisible) {
                        this.quitRoom();
                        this.roomCurrentNumber = 0;
                        this.roomNumber = 0;
                    }
                }, // 组件创建完后不立即获取数据
                {immediate: false}
            );
            // 监听socket消息
            setMessageListener((obj) => {
                console.log(obj);
                switch (obj.msgType) {
                    case "UPDATE_ROOM_NUMBER": {
                        this.roomCurrentNumber = obj.content;
                        if (!this.dialogVisible) {
                            this.dialogVisible = true
                        }
                    }
                        break;
                    case "BEGIN_GAME": {
                        this.beginGame();
                    }
                        break;
                    default:
                        console.error("类型未处理...")
                }
            })
        },
        mounted() {
        }
    }
</script>

<style scoped lang="less">
    /*.el-dialog--center .el-dialog__body {*/
    /*	!* text-align: initial; *!*/
    /*	padding: 0.333333rem 0.333333rem 0.4rem;*/
    /*}*/
</style>
