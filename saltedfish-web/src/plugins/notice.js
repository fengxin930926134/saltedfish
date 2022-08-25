//窗体失焦的时候，标题就会闪。
//这里有一个小的知识点，就是浏览器窗体获得焦点和失去焦点，Chrome和FireFox浏览器是window的onfocus, onblur方法；而IE浏览器则是document的onfocusin, onfocusout方法，因此有：
const titleInit = document.title;
// 是否失去焦点
let isShine = false;
// 提示内容
let content = "";
// 定时器
let tt;
// 是否需要通知
let isNotice = false;

let startNotice = (e) => {
    clearNotice();
    if (e) {
        content = e
    }
    isNotice = true;
    tt = setInterval(function() {
        if (isNotice) {
            if (isShine === true) {
                if (titleInit === document.title) {
                    document.title = '【'+ content +'】';
                } else {
                    document.title = titleInit;
                }
            } else {
                document.title = titleInit;
            }
        } else {
            clearNotice()
        }
    }, 500);
};

let stopNotice = () => {
    isNotice = false;
};

let clearNotice = () => {
    tt && clearInterval(tt);
    document.title = titleInit;
};

// for Chrome and FireFox
window.onfocus = function() {
    isShine = false;
};
window.onblur = function() {
    isShine = true;
};

// for IE
document.onfocusin = function() {
    isShine = false;
};
document.onfocusout = function() {
    isShine = true;
};

// 导出
export {startNotice, stopNotice}

