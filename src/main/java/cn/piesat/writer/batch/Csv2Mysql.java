package cn.piesat.writer.batch;

import cn.piesat.utils.JdbcComponent;
import cn.piesat.utils.JdbcFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//mysql 数据写入到mysql中
public class Csv2Mysql {

    public static void main(String[] args) throws IOException {
        File file = new File("E:\\知识图谱数据");
        File[] files = file.listFiles();
        JdbcComponent.executeSql(JdbcFactory.getJdbc(JdbcFactory.DEFAULT_MYSQL),"delete from four_greate_classical_novels");
        for (File innerDir : files) {
            String category = innerDir.getName();
            String realFilePath = innerDir.getAbsolutePath()+"\\triples.csv";
            List<String> lines = FileUtils.readLines(new File(realFilePath));
            List<HashMap<String, Object>> list = doLine(lines, category);
            String sql = JdbcComponent.mapListToMysqlInsertSql(list, "four_greate_classical_novels");
            JdbcComponent.executeSql(JdbcFactory.getJdbc(JdbcFactory.DEFAULT_MYSQL),sql);
        }


        File hundredsOfThousandsOfPeople = new File("E:\\图谱\\person_rel_kg.data");
        List<String> bigLines = FileUtils.readLines(hundredsOfThousandsOfPeople);
        List<HashMap<String, Object>> bigList = doNewLine(bigLines, "十万人物图谱");
        String sql = JdbcComponent.mapListToMysqlInsertSql(bigList, "four_greate_classical_novels");
        JdbcComponent.executeSql(JdbcFactory.getJdbc(JdbcFactory.DEFAULT_MYSQL),sql);
    }


    private static List<HashMap<String,Object>> doLine(List<String> lines,String category){
        List<HashMap<String,Object>> list = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] split = line.split(",");
            if(split.length == 4){
                HashMap<String,Object> map = new HashMap<>();
                map.put("head",split[0].replace("\"",""));
                map.put("tail",split[1].replace("\"",""));
                map.put("label",split[2].replace("\"",""));
                map.put("relation",split[3].replace("\"",""));
                map.put("category",category);
                list.add(map);
            }
        }
        return list;
    }


    private static List<HashMap<String,Object>> doNewLine(List<String> lines,String category){
        List<HashMap<String,Object>> list = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).replaceAll("'",".");
            String[] split = line.split(",");
            if(split.length == 4){
                HashMap<String,Object> map = new HashMap<>();
                map.put("head",split[0].replace("\"",""));
                map.put("tail",split[3].replace("\"",""));
                map.put("label",split[2].replace("\"",""));
                map.put("relation",split[3].replace("\"",""));
                map.put("category",category);
                list.add(map);
            }
        }
        return list;
    }

}
