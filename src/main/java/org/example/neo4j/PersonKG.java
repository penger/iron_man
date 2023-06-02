package org.example.neo4j;

import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import cn.piesat.utils.NeoComponent;
import cn.piesat.writer.batch.Jdbc2Neo4j;
import cn.piesat.writer.core.impl.Neo4jInsertEdgeExecutor;
import cn.piesat.writer.core.impl.Neo4jInsertNodeExecutor;

public class PersonKG {
    public static void main(String[] args) {
        NeoComponent.NeoParam neoParam = NeoComponent.getDefaultNeoParam();

        //第一步建立唯一约束 & 索引
//        String createConstraints = "CREATE CONSTRAINT FOR(n:person) REQUIRE (n.id) is UNIQUE";
//        String createIndex="CREATE INDEX FOR (p:person) ON (p.id)";
//        Jdbc2Neo4j.executeCypher(createConstraints,neoParam);

        Jdbc2Neo4j jdbc2Neo4j = new Jdbc2Neo4j();

        //写入节点
        JdbcComponent.JdbcParam jdbcParam = JdbcFactory.getJdbc(JdbcFactory.DEFAULT_MYSQL);
        String personSql="select id,name,address ,province ,phone_number ,ssn , birthday, email  from person ";
        //写入person
        Neo4jInsertNodeExecutor insertNodeExecutor = new Neo4jInsertNodeExecutor("Person", "name");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,personSql, 100,insertNodeExecutor);
        //写入省份
        String provinceSql = "select province as name from person";
        Neo4jInsertNodeExecutor insertNodeExecutor2 = new Neo4jInsertNodeExecutor("Province", "name");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,provinceSql,100,insertNodeExecutor2);

        //建立省份和person的关系
        // 实际执行的 cypher 为： match(h:person{id:'188499352954601472'}) ,(t:province{name:'贵州省'}) create (h)-[:Lives{address:'毕节'}]->(t)
        String personAndProvinceSQL = "select name a_name,province b_name ,address from person p ";
        Neo4jInsertEdgeExecutor insertEdgeExecutor = new Neo4jInsertEdgeExecutor("Person", "Province", "name", "name","a_name","b_name", "lives");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,personAndProvinceSQL,100,insertEdgeExecutor);

    }
}
