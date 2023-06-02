package cn.piesat.writer.batch;

import cn.piesat.utils.EsComponent;
import cn.piesat.utils.EsFactory;
import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * ES 数据直接写入到 ES
 * 1.可以根据别名来 重命名写入的 mapping字段
 * 参考：https://blog.csdn.net/u013850277/article/details/88904303
 */

public class Jdbc2ES {

    private static final Logger logger = LogManager.getLogger(Jdbc2ES.class);

    /**
     */
    public static void writeJdbcDataToES(EsComponent.EsParam esParam, JdbcComponent.JdbcParam jdbcParam, String indexName, String sql, Integer batchSize) {

        RestHighLevelClient client = EsComponent.getRestHighLevelClient(esParam);
        BulkProcessor bulkProcessor = getBulkProcessor(client);
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

            ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

            // bulkProcessor 添加的数据支持的方式并不多，查看其api发现其支持map键值对的方式，故笔者在此将查出来的数据转换成hashMap方式
            HashMap<String, String> map = null;
            int count = 0;
            String c = null;
            String v = null;
            while (rs.next()) {
                count++;
                map = new HashMap<String, String>(128);
                for (int i = 1; i <= colData.getColumnCount(); i++) {
                    //获取 label
                    c = colData.getColumnLabel(i);
                    v = rs.getString(c);
                    map.put(c, v);
                }
                dataList.add(map);
                // 每10万条写一次，不足的批次的最后再一并提交
                if (count % batchSize == 0) {
                    logger.info("Mysql handle data number : " + count);
                    // 将数据添加到 bulkProcessor 中
                    for (HashMap<String, String> hashMap2 : dataList) {
                        bulkProcessor.add(new IndexRequest(indexName.toLowerCase()).id(hashMap2.get("id"))
                                .source(hashMap2));
                    }
                    // 每提交一次便将map与list清空
                    map.clear();
                    dataList.clear();
                }
            }

            // count % 100000 处理未提交的数据
            for (HashMap<String, String> hashMap2 : dataList) {
                bulkProcessor.add(
                        new IndexRequest(indexName.toLowerCase()).id(hashMap2.get("id")).source(hashMap2));
            }
            logger.info("-------------------------- Finally insert number total : " + count);
            // 将数据刷新到es, 注意这一步执行后并不会立即生效，取决于bulkProcessor设置的刷新时间
            bulkProcessor.flush();

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                JdbcComponent.close(rs, ps, conn);
                boolean terminatedFlag = bulkProcessor.awaitClose(150L, TimeUnit.SECONDS);
                client.close();
                logger.info(terminatedFlag);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * 创建bulkProcessor并初始化
     *
     * @param client
     * @return
     */
    private static BulkProcessor getBulkProcessor(RestHighLevelClient client) {

        BulkProcessor bulkProcessor = null;
        try {

            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                    logger.info("Try to insert data number : " + request.numberOfActions());
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    logger.info("************** Success insert data number : " + request.numberOfActions() + " , id: "
                            + executionId);
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    logger.error("Bulk is unsuccess : " + failure + ", executionId: " + executionId);
                }
            };

            BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = (request, bulkListener) -> client
                    .bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

            //	bulkProcessor = BulkProcessor.builder(bulkConsumer, listener).build();
            BulkProcessor.Builder builder = BulkProcessor.builder(bulkConsumer, listener);
            builder.setBulkActions(5000);
            builder.setBulkSize(new ByteSizeValue(100L, ByteSizeUnit.MB));
            builder.setConcurrentRequests(10);
            builder.setFlushInterval(TimeValue.timeValueSeconds(100L));
            builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3));
            // 注意点：在这里感觉有点坑，官网样例并没有这一步，而笔者因一时粗心也没注意，在调试时注意看才发现，上面对builder设置的属性没有生效
            bulkProcessor = builder.build();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                bulkProcessor.awaitClose(100L, TimeUnit.SECONDS);
                client.close();
            } catch (Exception e1) {
                logger.error(e1.getMessage());
            }
        }
        return bulkProcessor;
    }
}


