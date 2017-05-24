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
public class SiloPacketData {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<SiloData> siloDataList = new ArrayList<SiloData>();
    private Map<String, ETagData> eTagDataHashMap = new HashMap<String, ETagData>();
    private List<ExtData> extDataList = new ArrayList<ExtData>();

    private String jsonContent;

    private SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public String getJsonContent() {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("silo", siloDataList);
        dataMap.put("tag", eTagDataHashMap);
        dataMap.put("extInfo", extDataList);

        try {
            jsonContent = mapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            logger.error("GatePacketData, getJsonContent error:{}", e.getMessage());
        }

        return jsonContent;
    }

    public void setSiloData(SiloData siloData) {
        siloDataList.add(siloData);
    }

    public void setETagData(ETagData eTagData) {
        eTagDataHashMap.put(eTagData.getEpc(), eTagData);
    }

    public void setExtData(ExtData extData) {
        extDataList.add(extData);
    }

    public List<SiloData> getSiloDataList() {
        return siloDataList;
    }

    public void setSiloDataList(List<SiloData> siloDataList) {
        this.siloDataList = siloDataList;
    }

    public Map<String, ETagData> geteTagDataHashMap() {
        return eTagDataHashMap;
    }

    public List<ExtData> getExtDataList() {
        return extDataList;
    }

    public void setExtDataList(List<ExtData> extDataList) {
        this.extDataList = extDataList;
    }


}
