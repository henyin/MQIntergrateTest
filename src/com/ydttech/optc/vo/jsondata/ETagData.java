package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class ETagData {

    String tid;
    String epc;
    String arriveDT;
    String ant;
    String rssi;

    public ETagData() {
    }

    public ETagData(String tid, String epc, String arriveDT, String ant, String rssi) {
        this.tid = tid;
        this.epc = epc;
        this.arriveDT = arriveDT;
        this.ant = ant;
        this.rssi = rssi;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getArriveDT() {
        return arriveDT;
    }

    public void setArriveDT(String arriveDT) {
        this.arriveDT = arriveDT;
    }

    public String getAnt() {
        return ant;
    }

    public void setAnt(String ant) {
        this.ant = ant;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
