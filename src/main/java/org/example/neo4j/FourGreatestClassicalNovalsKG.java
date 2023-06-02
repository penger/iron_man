package org.example.neo4j;

import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import cn.piesat.utils.NeoComponent;
import cn.piesat.writer.batch.Jdbc2Neo4j;
import cn.piesat.writer.core.impl.Neo4jInsertEdgeExecutor;
import cn.piesat.writer.core.impl.Neo4jInsertNodeExecutor;

/**
 * 四大名著
 */
public class FourGreatestClassicalNovalsKG {
    public static void main(String[] args) {
        NeoComponent.NeoParam neoParam = NeoComponent.getDefaultNeoParam();
        Jdbc2Neo4j jdbc2Neo4j = new Jdbc2Neo4j();

//        //第一步建立唯一约束 & 索引
//        String createConstraints = "CREATE CONSTRAINT FOR(n:character) REQUIRE (n.name) is UNIQUE";
//        String createIndex="CREATE INDEX FOR (p:character) ON (p.name)";
//        Jdbc2Neo4j.executeCypher(createConstraints,neoParam);
//        Jdbc2Neo4j.executeCypher(createIndex,neoParam);

//        String createConstraints2 = "CREATE CONSTRAINT FOR(n:novels) REQUIRE (n.name) is UNIQUE";
//        String createIndex2="CREATE INDEX FOR (p:novels) ON (p.name)";
//        Jdbc2Neo4j.executeCypher(createConstraints2,neoParam);
//        Jdbc2Neo4j.executeCypher(createIndex2,neoParam);

        long start = System.currentTimeMillis();
        //写入节点
        JdbcComponent.JdbcParam jdbcParam = JdbcFactory.getJdbc(JdbcFactory.DEFAULT_MYSQL);
        String personSql="select head as name from four_greate_classical_novels ";
        //写入第一批 角色
        Neo4jInsertNodeExecutor insertNodeExecutor = new Neo4jInsertNodeExecutor("character", "name");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,personSql, 100,insertNodeExecutor);
        //写入第二批 角色
        String personSql2 = "select tail as name from four_greate_classical_novels";
        Neo4jInsertNodeExecutor insertNodeExecutor2 = new Neo4jInsertNodeExecutor("character", "name");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,personSql2,100,insertNodeExecutor2);

        //写入 小说名称

        String novelsSql = "select category as name  from four_greate_classical_novels";
        Neo4jInsertNodeExecutor insertNodeExecutor3 = new Neo4jInsertNodeExecutor("novels", "name");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,novelsSql,100,insertNodeExecutor3);

        // 固定的关系
        //建立 角色和小说之间的关系 需要遍历两次 head-> category  tail->category ,当已经存在 固定关系的时候会，后续添加关系会忽略
        String characterAndNovelsSQL = "select head as name, category title from four_greate_classical_novels ";
        Neo4jInsertEdgeExecutor insertEdgeExecutor = new Neo4jInsertEdgeExecutor("character", "novels", "name", "title", "name","name","belong");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,characterAndNovelsSQL,500,insertEdgeExecutor);

        String characterAndNovelsSQL2 = "select head as name, category title from four_greate_classical_novels ";
        Neo4jInsertEdgeExecutor insertEdgeExecutor2 = new Neo4jInsertEdgeExecutor("character", "novels", "name", "title", "name","name","belong");
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,characterAndNovelsSQL2,500,insertEdgeExecutor2);



        //非固定的关系
        //建立 角色和角色之间的关系  A-->B 存在多个关系
        String characterAndCharacterSQL = "select head, tail, label, relation from four_greate_classical_novels ";
        Neo4jInsertEdgeExecutor insertEdgeExecutor3 = new Neo4jInsertEdgeExecutor("character", "character", "head", "tail","name","name", null);
        jdbc2Neo4j.writeJdbcDataToNeo4j(neoParam,jdbcParam,characterAndCharacterSQL,500,insertEdgeExecutor3);


        System.out.println("used :"+(System.currentTimeMillis()-start)/1000+"s ");


    }
}
