package cn.piesat.utils;

public class JdbcFactory {


    public static final String DEFAULT_MYSQL = "default_mysql";
    public static final String DEFAULT_CLICKHOUSE = "default_clickhouse";


    public static JdbcComponent.JdbcParam getJdbc(String jdbcName){
        JdbcComponent.JdbcParam jdbcParam = null;
        if(jdbcName.equals(DEFAULT_CLICKHOUSE)){
             jdbcParam = JdbcComponent.JdbcParam.builder()
                    .url("jdbc:clickhouse://192.168.1.123:8123/default")
                    .username("")
                    .password("")
                    .driver("ru.yandex.clickhouse.ClickHouseDriver")
                    .build();
        }else if(jdbcName.equals(DEFAULT_MYSQL)){
            jdbcParam = JdbcComponent.JdbcParam.builder()
                    .url("jdbc:mysql://192.168.1.123:3306/etl")
                    .username("cassini")
                    .password("9ijn)OKM")
                    .driver("com.mysql.jdbc.Driver")
                    .build();
        }else{
            System.out.println("unknown jdbc source"+ jdbcName);
        }
        return jdbcParam;


    }


}
