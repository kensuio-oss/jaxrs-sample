package io.kensu.collector.model;

import io.kensu.dam.model.*;
import io.kensu.dam.util.DataSources;

import java.util.List;

import static io.kensu.utils.TimeUtils.now;

public class DamDataCatalogEntry extends DataSources.DataCatalogEntry {

    // inherited from DataSources.DataCatalogEntry:
    //    public final DataSource damDataSource;
    //    public final Schema damSchema;

    //    public final DataSourceRef damDataSourceRef;
    //    public final SchemaRef damSchemaRef;

    // we use this field instead of inherited damDataSource;
    public final DataSource damDataSourceWithCustomName;

    public final String lineageTitlePrefix;

    /**
     * Modifies the batch by adding the datasource and schema
     * @param batch
     */
    public DamDataCatalogEntry addToBatch(BatchEntityReport batch){
        batch.addDataSourcesItem(new BatchDataSource().timestamp(now()).entity(damDataSourceWithCustomName));
        batch.addSchemasItem(new BatchSchema().timestamp(now()).entity(damSchema));
        return this;
    }

    public DamDataCatalogEntry(String lineageTitlePrefix,
                               String dsLocation,
                               String dsName,
                               String format,
                               PhysicalLocationRef physicalLocationRef,
                               List<FieldDef> fields) {
        super(dsLocation, format, physicalLocationRef, fields);
        this.lineageTitlePrefix = lineageTitlePrefix;
        this.damDataSourceWithCustomName = this.damDataSource.name(dsName);
    }


    @Override
    public String toString() {
        return String.format("DamDataCatalogEntry(\ndatasource: %s\nschema: %s\n)", damDataSource, damSchema);
    }
}
