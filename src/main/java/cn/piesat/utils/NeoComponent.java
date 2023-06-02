package cn.piesat.utils;

import lombok.Builder;
import lombok.Data;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.Serializable;

public class NeoComponent {
    private NeoComponent(){
    }

    @Data
    @Builder
    public static class NeoParam implements Serializable{
        private String uri;
        private String username;
        private String password;
    }


    public static NeoParam getDefaultNeoParam(){
        return NeoParam.builder()
                .uri("bolt://192.168.1.123:7687")
                .username("neo4j")
                .password("happy_cassini")
                .build();
    }


    public static Driver getDriver(NeoParam neoParam){
        return GraphDatabase.driver(neoParam.getUri(), AuthTokens.basic(neoParam.getUsername(), neoParam.getPassword()));
    }

    public static void closeDriver(Driver driver){
        driver.close();
    }

}
