package org.example;

import cn.piesat.utils.NeoComponent;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.IntegerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Neo4jDemo {
    public static void main(String[] args) {
        Driver driver = NeoComponent.getDriver(NeoComponent.getDefaultNeoParam());
        Session session = driver.session();
        int count = session.run("MATCH(n:Person{name:'gongx'}) return count(n) as count").single().get("count").asInt();
        System.out.println(count);
        session.close();
        driver.close();
    }
}
