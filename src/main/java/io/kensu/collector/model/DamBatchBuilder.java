package io.kensu.collector.model;

import io.kensu.collector.model.datasource.DatasourceNameFormatter;
import io.kensu.dam.model.*;
import io.kensu.dam.model.Process;
import io.kensu.dam.util.Compact;
import io.kensu.utils.ConcurrentHashMultimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.kensu.utils.ListUtils.orEmptyList;
import static io.kensu.utils.TimeUtils.now;

public class DamBatchBuilder {
    protected BatchEntityReport batch = new BatchEntityReport();;

    public static PhysicalLocation DEFAULT_LOCATION = new PhysicalLocation()
            .name("unknown")
            .lat(52.520645)
            .lon(13.409779)
            .pk(new PhysicalLocationPK().country("unknown").city("unknown"));
    public static PhysicalLocationRef DEFAULT_LOCATION_REF = new PhysicalLocationRef().byPK(DEFAULT_LOCATION.getPk());

    public DamBatchBuilder withDefaultLocation() {
        batch.addPhysicalLocationsItem(new BatchPhysicalLocation().timestamp(now()).entity(DEFAULT_LOCATION));
        return this;
    }

    public DamDataCatalogEntry addCatalogEntry(String lineageTitlePrefix,
                                               Set<FieldDef> fields,
                                               String table,
                                               String datasourceFormat,
                                               PhysicalLocationRef locationRef,
                                               DatasourceNameFormatter dsNameFormatter) {
        return new DamDataCatalogEntry(
                lineageTitlePrefix,
                dsNameFormatter.formatLocation(table, datasourceFormat),
                dsNameFormatter.formatName(table, datasourceFormat),
                datasourceFormat,
                locationRef,
                new ArrayList<>(fields)
        ).addToBatch(this.batch);
    }

    public List<DamDataCatalogEntry> addCatalogEntries(String lineageTitlePrefix,
                                                       ConcurrentHashMultimap<FieldDef> fieldsByTable,
                                                       String datasourceFormat,
                                                       PhysicalLocationRef locationRef,
                                                       DatasourceNameFormatter dsNameFormatter) {
        List<DamDataCatalogEntry> catalogEntries = new ArrayList<>();
        fieldsByTable.forEach((table, fields) -> {
            DamDataCatalogEntry entry = addCatalogEntry(lineageTitlePrefix, fields, table, datasourceFormat, locationRef, dsNameFormatter);
            catalogEntries.add(entry);
        });
        return catalogEntries;
    }

    public BatchEntityReport getBatch() {
        return batch;
    }

    public BatchEntityReport getCompactedBatch(){
        return Compact.GenUIDBatchEntityReport.compact(getBatch());
    }


    public DamBatchBuilder addFromOtherBatch(BatchEntityReport other){
        orEmptyList(other.getProjects()).forEach(item -> batch.addProjectsItem(item));
        orEmptyList(other.getProcesses()).forEach(item -> batch.addProcessesItem(item));
        orEmptyList(other.getProcessRuns()).forEach(item -> batch.addProcessRunsItem(item));
        orEmptyList(other.getProcessRunStats()).forEach(item -> batch.addProcessRunStatsItem(item));
        orEmptyList(other.getProcessLineages()).forEach(item -> batch.addProcessLineagesItem(item));
        orEmptyList(other.getLineageRuns()).forEach(item -> batch.addLineageRunsItem(item));
        orEmptyList(other.getSchemas()).forEach(item -> batch.addSchemasItem(item));
        orEmptyList(other.getSchemaFieldTags()).forEach(item -> batch.addSchemaFieldTagsItem(item));
        orEmptyList(other.getPhysicalLocations()).forEach(item -> batch.addPhysicalLocationsItem(item));
        orEmptyList(other.getDataSources()).forEach(item -> batch.addDataSourcesItem(item));
        orEmptyList(other.getCodeVersions()).forEach(item -> batch.addCodeVersionsItem(item));
        orEmptyList(other.getCodeBases()).forEach(item -> batch.addCodeBasesItem(item));
        orEmptyList(other.getUsers()).forEach(item -> batch.addUsersItem(item));
        orEmptyList(other.getDataStats()).forEach(item -> batch.addDataStatsItem(item));
        orEmptyList(other.getModels()).forEach(item -> batch.addModelsItem(item));
        orEmptyList(other.getModelTrainings()).forEach(item -> batch.addModelTrainingsItem(item));
        orEmptyList(other.getModelMetrics()).forEach(item -> batch.addModelMetricsItem(item));
        return this;
    }

    public void add(CodeBase codeBase){
        batch.addCodeBasesItem(new BatchCodeBase().timestamp(now()).entity(codeBase));
    }

    public void add(CodeVersion codeVersion){
        batch.addCodeVersionsItem(new BatchCodeVersion().timestamp(now()).entity(codeVersion));
    }

    public void add(Process process){
        batch.addProcessesItem(new BatchProcess().timestamp(now()).entity(process));
    }

    public void add(User user){
        batch.addUsersItem(new BatchUser().timestamp(now()).entity(user));
    }

    public void add(Project project){
        batch.addProjectsItem(new BatchProject().timestamp(now()).entity(project));
    }

    public void add(ProcessRun processRun){
        getBatch().addProcessRunsItem(new BatchProcessRun().timestamp(now()).entity(processRun));
    }
}
