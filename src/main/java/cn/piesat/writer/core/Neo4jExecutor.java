package cn.piesat.writer.core;

import org.neo4j.driver.Session;

import java.util.Map;

public interface Neo4jExecutor{
    void execute(Session session, Map<String,Object> queryMap);
}