package io.netty.learn.practice.Serializable;

import java.io.Serializable;

public class SubscribeResp implements Serializable {

    /**
     * 默认序列id
     */

    private static final long serialVersionUID = 1L;

    private int subReqId;

    private int respCode;

    private String desc;

    public int getSubReqId() {
        return subReqId;
    }

    public void setSubReqId(int subReqId) {
        this.subReqId = subReqId;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SubscribeResp{" +
                "subReqId=" + subReqId +
                ", respCode=" + respCode +
                ", desc='" + desc + '\'' +
                '}';
    }


}
