package io.kensu.collector.model.datasource;

public abstract class DatasourceNameFormatter {
    abstract public String formatLocation(String tableName, String datasourceFormat);

    public String formatName(String tableName, String datasourceFormat) {
        return formatLocation(tableName, datasourceFormat);
    }
}
