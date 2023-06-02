package cn.piesat.writer.batch;

import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.*;

public class Jdbc2Excel {
    private static final Logger logger = LogManager.getLogger(Jdbc2Excel.class);

    public static void writeJdbcDataToExcel(String excelPath, JdbcComponent.JdbcParam jdbcParam, String indexName, String sql, Integer batchSize) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JdbcComponent.getConnection(jdbcParam);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            logger.info("sql is : {}" ,sql);
            List<String> names = getHead(sql);
            List<List<String>> datas = new ArrayList<>();
            while(resultSet.next()){
                LinkedList<String> record  = new LinkedList<>();
                for (int i = 1; i <= names.size(); i++) {
                    Object object = resultSet.getObject(i);
                    if(Objects.isNull(object)){
                        record.add("");
                    }else{
                        record.add(object.toString());
                    }
                }
                datas.add(record);
            }
            File excelFile = new File(excelPath);
            EasyExcel.write(excelFile).head(getHeadList(names))
                    .sheet("结果详情")
                    .doWrite(datas);
        }catch (Exception e ){
            logger.error("error export ");
            throw new RuntimeException(e);
        }finally {
            try {
                JdbcComponent.close(rs,ps,conn);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

    }


    /**
     * 根据select来生成表头
     * @param sql
     * @return
     */
    private static List<String> getHead(String sql) {
        String[] split = sql.split("from");
        String[] split2 = sql.split("FROM");
        String selectStr = split[0];
        if (split[0].length() > split2[0].length()) {
            selectStr = split2[0];
        }
        //每一行解析为一个查询字段
        List<String> list = new ArrayList<>();
        if (selectStr.contains("\n")) {
            String[] lines = selectStr.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String[] columns = line.split(" ");
                String column = columns[columns.length - 1].replace(",", "");
                if ("select".equalsIgnoreCase(column) || StringUtils.isBlank(column)) {
                    continue;
                }
                list.add(column.replace("'", ""));
            }
        } else {
            String[] filedList = selectStr.split(",");
            for (int i = 0; i < filedList.length; i++) {
                String[] smallContent = filedList[i].trim().split(" ");
                String field = smallContent[smallContent.length - 1];
                list.add(field.replace("'", ""));
            }
        }
        return list;
    }


    private static List<List<String>> getHeadList(List<String> names) {
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            List<String> innerList = Arrays.asList(names.get(i));
            list.add(innerList);
        }
        return list;
    }

}
