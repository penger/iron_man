package org.example;

import cn.piesat.utils.EsComponent;
import cn.piesat.utils.EsFactory;
import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import cn.piesat.writer.batch.Jdbc2ES;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Jdbc2EsExample {
    private static final Logger logger = LogManager.getLogger(Jdbc2EsExample.class);

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            JdbcComponent.JdbcParam jdbcParam = JdbcFactory.getJdbc(JdbcFactory.DEFAULT_CLICKHOUSE);
            String indexName = "dish";
//            String sql = "select id,name,address ,province ,phone_number ,ssn , birthday, email  from person ";
            String sql = "SELECT id, name, description, menus_appeared, times_appeared, first_appeared, last_appeared, lowest_price, highest_price FROM dish ";
            EsComponent.EsParam esParam = EsFactory.getEsParam(EsFactory.DEFAULT_ES);
            Integer batchSize = 100;
            Jdbc2ES.writeJdbcDataToES(esParam,jdbcParam, indexName, sql,batchSize);

            logger.info(" use time: " + (System.currentTimeMillis() - startTime) / 1000 + "s");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
