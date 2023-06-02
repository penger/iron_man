package org.example;

import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import cn.piesat.writer.batch.Jdbc2Excel;
import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.*;

public class Jdbc2ExcelExample {
    private static final Logger logger = LogManager.getLogger(Jdbc2ExcelExample.class);

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            JdbcComponent.JdbcParam jdbcParam = JdbcFactory.getJdbc(JdbcFactory.DEFAULT_CLICKHOUSE);
            String indexName = "dish";
            String sql = "SELECT id, name, sponsor, event, venue, place, physical_description, occasion, notes, call_number, keywords, `language`, `date`, location, location_type, currency, currency_symbol, status, page_count, dish_count FROM menu ";
//            String sql = "SELECT id, name, description, menus_appeared, times_appeared, first_appeared, last_appeared, lowest_price, highest_price FROM dish ";
            String excelPath = "d:/test2.xlsx";
            Integer batchSize = 100;
            Jdbc2Excel.writeJdbcDataToExcel(excelPath,jdbcParam, indexName, sql,batchSize);

            logger.info(" use time: " + (System.currentTimeMillis() - startTime) / 1000 + "s");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }


}
