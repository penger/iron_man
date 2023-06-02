//package cn.piesat.writer.batch;
//
//import org.apache.http.HttpHost;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.elasticsearch.action.bulk.BulkProcessor;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.concurrent.TimeUnit;
//
///**
// * ES 数据直接写入到 ES
// * 1.可以根据别名来 重命名写入的 mapping字段
// * 参考：https://blog.csdn.net/u013850277/article/details/88904303
// */
//
//public class MysqlData2Clickhouse {
//
//    private static final Logger logger = LogManager.getLogger(MysqlData2Clickhouse.class);
//
//    public static void main(String[] args) {
//        try {
//            long startTime = System.currentTimeMillis();
//            String tableName = "testTable";
//            writeMysqlDataToES(tableName);
//
//            logger.info(" use time: " + (System.currentTimeMillis() - startTime) / 1000 + "s");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 将mysql 数据查出组装成es需要的map格式，通过批量写入clickhouse中
//     *
//     * @param tableName
//     */
//    private static void writeMysqlDataToES(String tableName) {
//
//        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("eshost", 9200, "http")));// 初始化
//        BulkProcessor bulkProcessor = getBulkProcessor(client);
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DBHelper.getConn();
//            logger.info("Start handle data :" + tableName);
//
//            String sql = "SELECT * from " + tableName;
//
//            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//            ps.setFetchSize(Integer.MIN_VALUE);
//            rs = ps.executeQuery();
//
//            ResultSetMetaData colData = rs.getMetaData();
//
//            ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
//
//            // bulkProcessor 添加的数据支持的方式并不多，查看其api发现其支持map键值对的方式，故笔者在此将查出来的数据转换成hashMap方式
//            HashMap<String, String> map = null;
//            int count = 0;
//            String c = null;
//            String v = null;
//            while (rs.next()) {
//                count++;
//                map = new HashMap<String, String>(128);
//                for (int i = 1; i <= colData.getColumnCount(); i++) {
//                    c = colData.getColumnName(i);
//                    v = rs.getString(c);
//                    map.put(c, v);
//                }
//                dataList.add(map);
//                // 每10万条写一次，不足的批次的最后再一并提交
//                if (count % 100000 == 0) {
//                    logger.info("Mysql handle data number : " + count);
//                    // 将数据添加到 bulkProcessor 中
//                    for (HashMap<String, String> hashMap2 : dataList) {
//                        bulkProcessor.add(new IndexRequest(tableName.toLowerCase(), "gzdc", hashMap2.get("S_GUID"))
//                                .source(hashMap2));
//                    }
//                    // 每提交一次便将map与list清空
//                    map.clear();
//                    dataList.clear();
//                }
//            }
//
//            // count % 100000 处理未提交的数据
//            for (HashMap<String, String> hashMap2 : dataList) {
//                bulkProcessor.add(
//                        new IndexRequest(tableName.toLowerCase(), "gzdc", hashMap2.get("S_GUID")).source(hashMap2));
//            }
//
//            logger.info("-------------------------- Finally insert number total : " + count);
//            // 将数据刷新到es, 注意这一步执行后并不会立即生效，取决于bulkProcessor设置的刷新时间
//            bulkProcessor.flush();
//
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                rs.close();
//                ps.close();
//                conn.close();
//                boolean terminatedFlag = bulkProcessor.awaitClose(150L, TimeUnit.SECONDS);
//                client.close();
//                logger.info(terminatedFlag);
//            } catch (Exception e) {
//                logger.error(e.getMessage());
//            }
//        }
//    }
//
//}
//
//
