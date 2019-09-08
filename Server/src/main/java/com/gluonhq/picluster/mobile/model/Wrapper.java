package com.gluonhq.picluster.mobile.model;

public class Wrapper {
    private String appkey;
    private String payload;
    private String uid;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Wrapper{" +
                "appkey='" + appkey + '\'' +
                ", payload='" + payload + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
