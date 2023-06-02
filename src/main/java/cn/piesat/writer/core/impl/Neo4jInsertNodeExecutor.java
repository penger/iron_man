package cn.piesat.writer.core.impl;

import cn.piesat.writer.core.Neo4jExecutor;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Session;

import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Neo4jInsertNodeExecutor implements Neo4jExecutor {
    private static final Logger logger = LogManager.getLogger(Neo4jInsertNodeExecutor.class);
    private String label;
    private String uniqueColumn;
    @Override
    public void execute(Session session, Map<String,Object> queryMap) {
        String createTemplate = queryMap.keySet().stream().map(x -> x + ":$" + x).collect(Collectors.joining(","));
        Object uniqueColumnValue = queryMap.get(uniqueColumn);
        boolean notExists = session.run("MATCH(n:"+label+"{"+uniqueColumn+":'"+uniqueColumnValue+"'}) return count(n) as count").single().get("count").asInt()<1;
//        boolean notExists = true;
        if(notExists) {
            session.executeWriteWithoutResult(x->{
                logger.info("insert node {} {}",label,uniqueColumnValue);
                x.run("CREATE(p:"+label+"{"+createTemplate+"})", queryMap);
            });
        }else{
            logger.info(" already exist skip !");
        }
    }
}