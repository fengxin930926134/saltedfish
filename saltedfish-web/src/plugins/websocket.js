import {uuid} from "./uuid";
import {Message} from "element-ui";

let wsurl = process.env.VUE_APP_WS_URL;
let ws = null;
let websocket_callback = null;
// 避免重复连接
let lockReconnect = false;
let tt;
// 重连次数
let reconnectNumber = 0;
// 最大重连次数
const reconnectMaxNumber = 20;
// 心跳检测间隔时间，单位毫秒
const heartbeatTime = 30 * 1000;
// 用户id
const wsUserIdKey = "wsUserId";
console.log("userId =", localStorage.getItem(wsUserIdKey));
/**
 * 已发送消息
 */
const msgMap = new Map();
let inspectMsg;

let checkConnectStatus = () => {
    if (!isConnect()) {
        Message.warning("未曾连接服务器，无法操作！");
    }
};

/**
 * 获取id
 */
let getWsUserId = () => {
    return localStorage.getItem(wsUserIdKey);
};

/**
 * 是否是连接状态
 */
let isConnect = () => {
    return ws.readyState === ws.OPEN
};

/**
 * 初始化WebSocket
 */
let initWebSocket = () => {
    //初始化 websocket
    ws = new WebSocket(wsurl);
    ws.onmessage = websocketonmessage;
    ws.onopen = websocketonopen;
    ws.onerror = websocketonerror;
    ws.onclose = websocketclose;
    inspectMsg && clearInterval(inspectMsg);
    inspectMsg = setInterval(() => {
        let time = new Date().getTime();
        msgMap.forEach((k, v) => {
            if (v.sendTime < time - 3000) {
                v.sendTime = time;
                send(v)
            }
        })
    }, 3000)
};

/**
 * 发送消息
 * @param data
 */
let sendMsg = (data) => {
    // 判断 data 数据类型
    if (typeof data != 'string') {
        data.msgId = uuid();
        data.sendTime = new Date().getTime();
        msgMap.set(data.msgId, data);
        data = JSON.stringify(data)
    }
    // 判断 websocket 的状态
    if (isConnect()) {
        // 已经打开,可以直接发送
        send(data)
    } else if (ws.readyState === ws.CONNECTING) {
        // 正在开启状态中,则 1 秒后重新发送
        setTimeout(() => {
            send(data)
        }, 1000)
    } else {
        // 重连
        reconnect();
    }
};

/**
 * 需要转成string发送
 */
let send = (msg) => {
    console.log("send -> " + msg);
    ws.send(msg)
};

/**
 * 重连
 */
let reconnect = () => {
    //如果连接被锁定了，就说明当前正在重连
    if (lockReconnect) {
        return;
    }
    lockReconnect = true;
    reconnectNumber++;
    //清除定时器
    tt && clearTimeout(tt);
    tt = setTimeout(() => {
        console.log('reconnect websocket');
        if (reconnectNumber > reconnectMaxNumber) {
            console.log('stop reconnect websocket');
            return;
        }
        initWebSocket();
        lockReconnect = false;
    }, reconnectNumber * 1000 + 3000)
};

/**
 * 心跳请求构建对象
 */
const heartCheck = {
    timeout: heartbeatTime,
    timeOutObj: null,
    serverTimeOutObj: null,
    start: function (executenow) {
        if (executenow) {
            sendHeartbeatRequest();
        }
        const self = this;
        this.timeOutObj && clearTimeout(this.timeOutObj);
        this.serverTimeOutObj && clearTimeout(this.serverTimeOutObj);
        this.timeOutObj = setTimeout(() => {
            sendHeartbeatRequest();
            self.serverTimeOutObj = setTimeout(() => {
                // 手动断开
                closeWebsocket();
            }, self.timeout)
        }, self.timeout)
    }
};

// 发送心跳请求
let sendHeartbeatRequest = () => {
    sendMsg({
        msgType: "HEART_BEAT",
        // 临时userId
        content: getWsUserId(wsUserIdKey)
    });
};

//获取 websocket 推送的数据
let websocketonmessage = e => {
    console.log("receive -> " + e.data);
    // 收到消息刷新心跳
    heartCheck.start(false);
    // 处理
    try {
        let obj = JSON.parse(e.data);
        if (obj.msgType === "REPLY") {
            msgMap.delete(obj.msgId);
        } else {
            // 回复
            let copy = JSON.parse(JSON.stringify(obj));
            copy.content = null;
            copy.errcode = "0000";
            copy.errmsg = "ok";
            copy.msgType = "REPLY";
            send(JSON.stringify(copy));
            // 处理
            if (obj.msgType === "WS_USER_ID") {
                // 检查保存userId
                if (obj.content) {
                    let value = getWsUserId(wsUserIdKey);
                    if (!value || value !== obj.content) {
                        localStorage.setItem(wsUserIdKey, obj.content)
                    }
                }
            } else if (websocket_callback) {
                websocket_callback(obj)
            }
        }
    } catch (e1) {
        console.error(e1)
    }
};

let setMessageListener = (callback) => {
    websocket_callback = callback;
};

// 连接成功
let websocketonopen = () => {
    console.log('open websocket success');
    // 重置重连次数
    reconnectNumber = 0;
    // 启动心跳检测
    heartCheck.start(true)
};

// 连接失败时重新连接
let websocketonerror = () => {
    // 重连
    reconnect()
};

/**
 * CloseEvent.code: code是错误码，是整数类型
 * CloseEvent.reason: reason是断开原因，是字符串
 * CloseEvent.wasClean: wasClean表示是否正常断开，是布尔值。一般异常断开时，该值为false
 */
let websocketclose = e => {
    console.log('websocket close, code=' + e.code + ' reason=' + e.reason + ' wasClean=' + e.wasClean);
    // 重连
    reconnect()
};

// 手动关闭 websocket
let closeWebsocket = () => {
    console.log('websocket close');
    ws.close()
};

// 窗口关闭，关闭websocket
window.onbeforeunload = function () {
    closeWebsocket();
};

// 导出
export {initWebSocket, sendMsg, closeWebsocket, setMessageListener, isConnect, checkConnectStatus, getWsUserId}
