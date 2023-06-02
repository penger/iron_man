package cn.piesat.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions;
import org.apache.flink.table.api.*;

public class flinkApp {

    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        TableEnvironment tableEnvironment = TableEnvironment.create(settings);
        tableEnvironment.createTemporaryTable("source_table", TableDescriptor.forConnector("datagen")
                .schema(Schema.newBuilder()
                        .column("f0", DataTypes.STRING())
                        .build())
                .option(DataGenConnectorOptions.ROWS_PER_SECOND,100L).build()
        );
        //创建表
//        tableEnvironment.executeSql("create temporary table sink_table with ('connector' = 'blackhole') like source_table");
//        //
//        Table source_table = tableEnvironment.from("source_table");
        Table midtable = tableEnvironment.sqlQuery("select * from source_table");

        midtable.execute().print();

//        TableResult result = midtable.executeInsert("sink_table");
//        System.out.println(result);

    }
}
