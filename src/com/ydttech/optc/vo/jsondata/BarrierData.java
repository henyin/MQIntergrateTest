package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class BarrierData {

    String openWay;
    String openDT;

    public BarrierData() {
    }

    public BarrierData(String openWay, String openDT) {
        this.openWay = openWay;
        this.openDT = openDT;
    }

    public String getOpenWay() {
        return openWay;
    }

    public void setOpenWay(String openWay) {
        this.openWay = openWay;
    }

    public String getOpenDT() {
        return openDT;
    }

    public void setOpenDT(String openDT) {
        this.openDT = openDT;
    }
}
