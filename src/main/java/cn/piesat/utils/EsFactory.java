package cn.piesat.utils;

import java.util.Arrays;

public class EsFactory {


    public static final String DEFAULT_ES = "default_es";
    public static final String DEFAULT_CLICKHOUSE = "default_clickhouse";


    public static EsComponent.EsParam getEsParam(String jdbcName){
        EsComponent.EsParam esParam  = null;
        if(jdbcName.equals(DEFAULT_ES)){
            esParam = EsComponent.EsParam.builder()
                    .username("elastic")
                    .password("elastic")
                    .port(9200)
                    .hostList(Arrays.asList("192.168.1.123")).build();
        }else{
            System.out.println("unknown es source"+ jdbcName);
        }
        return esParam;


    }


}
