package io.kensu.collector.model;

import io.kensu.dam.model.BatchEntityReport;
import io.kensu.dam.model.Process;
import io.kensu.dam.model.ProcessRun;
import io.kensu.dam.model.ProcessRunRef;
import io.kensu.dam.util.DataSources;
import io.kensu.dam.util.Lineages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleDamLineageBuilder extends Lineages.LineageDef {

    public static final Lineages.TimestampProvider NOW = Lineages.NOW;

    public ProcessRun processRun;

    public SimpleDamLineageBuilder(Process process,
                                   ProcessRun processRun,
                                   List<? extends DataSources.DataCatalogEntry> inputs,
                                   DataSources.DataCatalogEntry output,
                                   String operationLogic,
                                   String lineageTitlePrefix) {
        super(process,
                "%s %s".format(lineageTitlePrefix, output.damDataSource.getName()), // lineage name - auto prefixed with process name
                inputs,
                Collections.singletonList(output),
                Lineages.FULL,
                operationLogic,
                Lineages.NOW);

        this.processRun = processRun;
    }

    public DamBatchBuilder addToBatch(DamBatchBuilder batchBuilder){
        boolean includeDefinitions = true;
        BatchEntityReport finalLineageBatch = this.damBatch;
        if (inputs.size() > 0) {
            finalLineageBatch =  runIn(new ProcessRunRef().byPK(processRun.getPk()), NOW.apply(),  includeDefinitions);
        } else {
            // remove lineage if there's no inputs
            finalLineageBatch = finalLineageBatch.processLineages(new ArrayList<>());
        }
        // remove datasources and schemas which are already added earlier/elsewhere in DamBatchBuilder
        finalLineageBatch = finalLineageBatch
                .dataSources(new ArrayList<>())
                .schemas(new ArrayList<>());
        return batchBuilder.addFromOtherBatch(finalLineageBatch);
    }
}
