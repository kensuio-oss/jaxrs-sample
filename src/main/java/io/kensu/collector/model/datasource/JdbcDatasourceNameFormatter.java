package io.kensu.collector.model.datasource;

public class JdbcDatasourceNameFormatter extends DatasourceNameFormatter {
    public static JdbcDatasourceNameFormatter INST = new JdbcDatasourceNameFormatter();

    public String formatLocation(String tableName, String datasourceFormat){
        return String.format("%s://%s", datasourceFormat, tableName);
    }
}
