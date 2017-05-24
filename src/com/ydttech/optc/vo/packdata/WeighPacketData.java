package com.ydttech.optc.vo.packdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydttech.optc.vo.jsondata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ean.Chung on 2017/5/15.
 */
public class WeighPacketData {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<LaneData> laneDataList = new ArrayList<LaneData>();
    private Map<String, ETagData> eTagDataHashMap = new HashMap<String, ETagData>();
    private List<LPRData> lprDataList = new ArrayList<LPRData>();
    private List<BarrierData> barrierDataList = new ArrayList<BarrierData>();
    private List<ExtData> extDataList = new ArrayList<ExtData>();

    private String jsonContent;

    private SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public String getJsonContent() {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("lane", laneDataList);
        dataMap.put("lpr", lprDataList);
        dataMap.put("barrier", barrierDataList);
        dataMap.put("tag", eTagDataHashMap);
        dataMap.put("extInfo", extDataList);

        try {
            jsonContent = mapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            logger.error("GatePacketData, getJsonContent error:{}", e.getMessage());
        }

        return jsonContent;
    }

    public void setLaneData(LaneData laneData) {
        laneDataList.add(laneData);
    }

    public void setETagData(ETagData eTagData) {
        eTagDataHashMap.put(eTagData.getEpc(), eTagData);
    }

    public void setLPRData(LPRData lprData) {
        lprDataList.add(lprData);
    }

    public void setBarrierData(BarrierData barrierData) {
        barrierDataList.add(barrierData);
    }

    public void setExtData(ExtData extData) {
        extDataList.add(extData);
    }

    public List<LaneData> getLaneDataList() {
        return laneDataList;
    }

    public Map<String, ETagData> geteTagDataHashMap() {
        return eTagDataHashMap;
    }

    public List<LPRData> getLprDataList() {
        return lprDataList;
    }

    public List<BarrierData> getBarrierDataList() {
        return barrierDataList;
    }

    public List<ExtData> getExtDataList() {
        return extDataList;
    }
}
