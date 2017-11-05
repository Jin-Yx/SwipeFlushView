package com.penoder.swipeflushview;

import java.util.List;

/**
 * @author Penoder
 * @date 2017/00/00
 */
public class EntityInfo {


    /**
     * reason : 请求成功
     * result : {"list":[{"id":"wechat_20171105019430","title":"薅羊毛原来是从这来的，隔着屏幕都感觉到了满满的疼痛～","source":"冷笑话","firstImg":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-57018705.jpg/640","mark":"","url":"http://v.juhe.cn/weixin/redirect?wid=wechat_20171105019430"},{"id":"wechat_20171105019427","title":"哈哈哈哈哈向装逼界大佬们低头.","source":"吐槽星君","firstImg":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-21160130.jpg/640","mark":"","url":"http://v.juhe.cn/weixin/redirect?wid=wechat_20171105019427"}],"totalPage":19334,"ps":10,"pno":1}
     * error_code : 0
     */

    private String reason;
    private ResultBean result;
    private int error_code;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public static class ResultBean {
        /**
         * list : [{"id":"wechat_20171105019430","title":"薅羊毛原来是从这来的，隔着屏幕都感觉到了满满的疼痛～","source":"冷笑话","firstImg":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-57018705.jpg/640","mark":"","url":"http://v.juhe.cn/weixin/redirect?wid=wechat_20171105019430"},{"id":"wechat_20171105019427","title":"哈哈哈哈哈向装逼界大佬们低头.","source":"吐槽星君","firstImg":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-21160130.jpg/640","mark":"","url":"http://v.juhe.cn/weixin/redirect?wid=wechat_20171105019427"}]
         * totalPage : 19334
         * ps : 10
         * pno : 1
         */

        private int totalPage;
        private int ps;
        private int pno;
        private List<ListBean> list;

        public int getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }

        public int getPs() {
            return ps;
        }

        public void setPs(int ps) {
            this.ps = ps;
        }

        public int getPno() {
            return pno;
        }

        public void setPno(int pno) {
            this.pno = pno;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : wechat_20171105019430
             * title : 薅羊毛原来是从这来的，隔着屏幕都感觉到了满满的疼痛～
             * source : 冷笑话
             * firstImg : http://zxpic.gtimg.com/infonew/0/wechat_pics_-57018705.jpg/640
             * mark :
             * url : http://v.juhe.cn/weixin/redirect?wid=wechat_20171105019430
             */

            private String id;
            private String title;
            private String source;
            private String firstImg;
            private String mark;
            private String url;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getFirstImg() {
                return firstImg;
            }

            public void setFirstImg(String firstImg) {
                this.firstImg = firstImg;
            }

            public String getMark() {
                return mark;
            }

            public void setMark(String mark) {
                this.mark = mark;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
