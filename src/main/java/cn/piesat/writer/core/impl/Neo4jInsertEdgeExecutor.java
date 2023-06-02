package cn.piesat.writer.core.impl;

import cn.piesat.writer.core.Neo4jExecutor;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public
class Neo4jInsertEdgeExecutor implements Neo4jExecutor {
    private static final Logger logger = LogManager.getLogger(Neo4jInsertEdgeExecutor.class);

    // match(h:person{id:'188499352954601472'}) ,(t:province{name:'贵州省'}) create (h)-[:Lives{address:'毕节'}]->(t)
    private String headLabel;
    private String tailLabel;
    private String headQueryColumn;
    private String tailQueryColumn;

    private String headColumn;
    private String tailColumn;
    private String relationType;

    @Override
    public void execute(Session session, Map<String, Object> queryMap) {
        //解决 sql 中无法出现同一个 查询字段的情况 比如两个实体的唯一属性都是 name ，无法通过同一个sql查询
        Object headValue = queryMap.remove(headQueryColumn);
        Object tailValue = queryMap.remove(tailQueryColumn);
        List<String> collectionStr = new ArrayList<>();

        String realRelationType = relationType;
        //针对 多种关系的 情况 ( 查询指定一个字段为 label ,剩余的作为属性
        if(relationType == null){
            realRelationType = queryMap.remove("label").toString();
        }
        //查看是否存在该 标签对应的关系，如果存在，直接略过
        String checkRelationExistCypher = "match(n:"+headLabel+"{"+headColumn+":'"+headValue+"'})-[k:"+realRelationType+"]->(m:"+tailLabel+"{"+tailColumn+":'"+tailValue+"'}) return count(k) as count";
        int count = session.run(checkRelationExistCypher).single().get("count").asInt();
        //已经存在此唯一关系，忽略添加关系
        if(count>0) {
            logger.info("relation already exist skip !");
            return;
        }

        Set<String> keySet = queryMap.keySet();
        for (String key : keySet) {
            String tempStr = key+":'"+ queryMap.get(key)+"'";
            collectionStr.add(tempStr);
        }
        String jsonStr =  "";

        //如果为空，没有属性
        if(collectionStr.size()>0) {
            jsonStr = "{"+ collectionStr.stream().collect(Collectors.joining(","))+"}";
        }
        String cypher = "match(h:"+headLabel+"{"+headColumn+":'"+headValue+"'}) ,(t:"+tailLabel+"{"+tailColumn+":'"+tailValue+"'}) create (h)-[:"+realRelationType+jsonStr+"]->(t)";
        logger.info("create relation cypher is "+cypher );
        session.run(cypher);
    }
}