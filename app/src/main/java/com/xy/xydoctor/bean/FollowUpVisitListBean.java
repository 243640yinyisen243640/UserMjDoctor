package com.xy.xydoctor.bean;

import java.util.List;

public class FollowUpVisitListBean {

    /**
     * morepage : true
     * data : [{"id":54,"times":9,"addtime":"2019-07-23 10:25","status":5},{"id":53,"times":7,"addtime":"2019-07-22 16:55","status":2},{"id":42,"times":4,"addtime":"2019-06-26 11:00","status":2},{"id":40,"times":2,"addtime":"2019-06-24 16:09","status":2}]
     */

    private boolean morepage;
    private List<DataBean> data;

    public boolean isMorepage() {
        return morepage;
    }

    public void setMorepage(boolean morepage) {
        this.morepage = morepage;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 54
         * times : 9
         * addtime : 2019-07-23 10:25
         * status : 5
         */

        private int id;
        private int times;
        private String addtime;
        private String status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }

        public String getAddtime() {
            return addtime;
        }

        public void setAddtime(String addtime) {
            this.addtime = addtime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
