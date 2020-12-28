package io.kensu.collector.model.datasource;

public class HttpDatasourceNameFormatter extends DatasourceNameFormatter {
    public static HttpDatasourceNameFormatter INST = new HttpDatasourceNameFormatter();

    public String formatLocation(String tableName, String datasourceFormat){
        return String.format("%s :: %s", datasourceFormat, tableName);
    }
}
