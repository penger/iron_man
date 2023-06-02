package cn.piesat.writer.batch;

import cn.piesat.utils.*;
import cn.piesat.writer.core.Neo4jExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * jdbc 数据写入到 neo4j中
 * 1.可以根据别名来 重命名写入的 mapping字段
 * 参考：https://blog.csdn.net/u013850277/article/details/88904303
 */

public class Jdbc2Neo4j {

    private static final Logger logger = LogManager.getLogger(Jdbc2Neo4j.class);

    public void writeJdbcDataToNeo4j(NeoComponent.NeoParam neoParam , JdbcComponent.JdbcParam jdbcParam, String sql, Integer batchSize , Neo4jExecutor neo4jExecutor) {

        Driver driver = NeoComponent.getDriver(neoParam);
        Session session = driver.session();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JdbcComponent.getConnection(jdbcParam);
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // clickhouse 不支持 fetchsize 为负数
            if(jdbcParam.getDriver().contains("mysql")){
                ps.setFetchSize(Integer.MIN_VALUE);
            }
            rs = ps.executeQuery();
            ResultSetMetaData colData = rs.getMetaData();
            ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
            // bulkProcessor 添加的数据支持的方式并不多，查看其api发现其支持map键值对的方式，故笔者在此将查出来的数据转换成hashMap方式
            HashMap<String, Object> map = null;
            int count = 0;
            String c = null;
            Object v = null;
            while (rs.next()) {
                count++;
                map = new HashMap<String, Object>(128);
                for (int i = 1; i <= colData.getColumnCount(); i++) {
                    //获取 label
                    c = colData.getColumnLabel(i);
                    v = rs.getObject(c);
                    map.put(c, v);
                }
                dataList.add(map);
                // 每10万条写一次，不足的批次的最后再一并提交
                if (count % batchSize == 0) {
                    logger.info("Mysql handle data number : " + count);
                    // 将数据添加到 bulkProcessor 中
                    for (HashMap<String, Object> hashMap2 : dataList) {
                       neo4jExecutor.execute(session,hashMap2);
                    }
                    // 每提交一次便将map与list清空
                    map.clear();
                    dataList.clear();
                }
            }

            // count % 100000 处理未提交的数据
            for (HashMap<String, Object> hashMap2 : dataList) {
                neo4jExecutor.execute(session,hashMap2);
            }
            logger.info("-------------------------- Finally insert number total : " + count);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            try {
                JdbcComponent.close(rs, ps, conn);
                NeoComponent.closeDriver(driver);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }



    public static void executeCypher(String cypher ,NeoComponent.NeoParam neoParam){
        Driver driver = NeoComponent.getDriver(neoParam);
        Session session = driver.session();
        session.run(cypher);
        session.close();
        driver.close();
    }



}


