package io.kensu.collector;

import io.kensu.collector.DamProcessEnvironment;
import io.kensu.collector.model.DamBatchBuilder;
import io.kensu.collector.model.DamDataCatalogEntry;
import io.kensu.collector.model.DamSchemaUtils;
import io.kensu.collector.model.SimpleDamLineageBuilder;
// import io.kensu.jdbc.parser.DamJdbcQueryParser;
// import io.kensu.jdbc.parser.ReferencedSchemaFieldsInfo;
// import io.kensu.dam.ApiClient;
// import io.kensu.dam.ApiException;
// import io.kensu.dam.ManageKensuDamEntitiesApi;
// import io.kensu.dam.OfflineFileApiClient;
// import io.kensu.dam.model.*;
// import io.kensu.dam.model.Process;
// import io.kensu.collector.model.datasource.HttpDatasourceNameFormatter;
// import io.kensu.collector.model.datasource.JdbcDatasourceNameFormatter;
// import io.kensu.utils.ConcurrentHashMultimap;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.SpanData;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
//import net.sf.jsqlparser.JSQLParserException;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

//import static io.kensu.json.DamJsonSchemaInferrer.DAM_OUTPUT_SCHEMA_TAG;

public class TracerReporter implements Reporter {
    static final Logger logger = Logger.getLogger(TracerReporter.class.getName());

    //private final Logger logger;
    //protected final AbstractUrlsTransformer urlsTransformer;

    //private final ConcurrentHashMultimap<SpanData> spanChildrenCache = new ConcurrentHashMultimap<>();

    //public TracerReporter(Logger logger, AbstractUrlsTransformer springUrlsTransformer) {
    public TracerReporter() {
        //this.logger = logger;
        //this.urlsTransformer = springUrlsTransformer;
    }

    //protected DamProcessEnvironment damEnv = new DamProcessEnvironment();

    @Override
    public void start(Instant timestamp, SpanData span) {
        String logMessage = createLogMessage(timestamp, "start", span);
        logger.info(logMessage);
    }

    protected <T> T getTagOrDefault(String tagKey, SpanData span, T defaultValue) {
        return (T)(span.tags.getOrDefault(tagKey, defaultValue));
    }

    protected <T> T getTagOrDefault(Tag<T> tag, SpanData span, T defaultValue) {
        return getTagOrDefault(tag.getKey(), span, defaultValue);
    }

    @Override
    public void finish(Instant timestamp, SpanData span) {
        String maybeParentId = span.references.get("child_of");
        if (maybeParentId != null) {
            //spanChildrenCache.addEntry(maybeParentId, span);
        } else {
            // this is the main SPAN, report all the stuff which was gathered so far
            // DamBatchBuilder batchBuilder = new DamBatchBuilder().withDefaultLocation();
            // PhysicalLocationRef defaultLocationRef = DamBatchBuilder.DEFAULT_LOCATION_REF;

            // String logMessage = createLogMessage(timestamp, "Finish", span);


            // logger.debug(logMessage);

            // //   - http.status_code: 200
            // //   - component: java-web-servlet
            // //   - span.kind: server
            // //   - http.url: http://localhost/people/search/findByLastName
            // //   - http.method: GET
            // //   - DamOutputSchema: [class FieldDef {
            // Integer httpStatus = getTagOrDefault(Tags.HTTP_STATUS, span, 0);
            // // FIMXE: need ability to remove param value to get URL pattern!!!
            // String httpUrl = getTagOrDefault(Tags.HTTP_URL, span, null);
            // String httpMethod = getTagOrDefault(Tags.HTTP_METHOD, span, null);
            // String transformedHttpUrl = urlsTransformer.transformUrl(httpMethod, httpUrl);
            // logger.warn(String.format("transformedHttpUrl: %s ( httpStatus: %d\nhttpUrl: %s\nhttpMethod: %s )", transformedHttpUrl, httpStatus, httpUrl, httpMethod));
            // if ((httpStatus >= 200) && (httpStatus < 300) && transformedHttpUrl != null && httpMethod != null) {
            //     Set<FieldDef> damOutputFields = getTagOrDefault(DAM_OUTPUT_SCHEMA_TAG, span, null);
            //     // FIXME: last resort in case schema wasn't resolved or empty
            //     damOutputFields = (damOutputFields == null) ? DamSchemaUtils.EMPTY_SCHEMA : damOutputFields;
            //     String endpointName = String.format("HTTP %s", httpMethod);
            //     DamDataCatalogEntry outputCatalogEntry = batchBuilder.addCatalogEntry(
            //             "create",
            //             damOutputFields,
            //             transformedHttpUrl,
            //             endpointName,
            //             defaultLocationRef,
            //             HttpDatasourceNameFormatter.INST
            //     );
            //     logger.warn("outputCatalogEntry: " + outputCatalogEntry);

            //     List<DamDataCatalogEntry> inputCatalogEntries = new ArrayList<>();
            //     List<DamDataCatalogEntry> writesCatalogEntries = new ArrayList<>();
            //     Set<SpanData> children = spanChildrenCache.get(span.spanId);
            //     if (children != null) {
            //         logger.debug(String.format("CHILDREN (count = %d):", children.size()));
            //         children.forEach(childSpan -> {
            //             logger.debug(createLogMessage(timestamp, "child", childSpan));
            //             if (getTagOrDefault(Tags.COMPONENT, childSpan, "").equals("java-jdbc")) {
            //                 String dbInstance = getTagOrDefault(Tags.DB_INSTANCE, childSpan, "");
            //                 String dbType = getTagOrDefault(Tags.DB_TYPE, childSpan, "");
            //                 String dbStatement = getTagOrDefault(Tags.DB_STATEMENT, childSpan, "");
            //                 // SQL reads
            //                 try {
            //                     DamJdbcQueryParser damJdbcQueryParser = new DamJdbcQueryParser(dbInstance, dbType, dbStatement, logger);
            //                     ReferencedSchemaFieldsInfo fieldsByTable = damJdbcQueryParser.guessReferencedInputTableSchemas();
            //                     inputCatalogEntries.addAll(batchBuilder.addCatalogEntries("create", fieldsByTable.schema, dbType, defaultLocationRef, JdbcDatasourceNameFormatter.INST));
            //                 } catch (JSQLParserException e) {
            //                     logger.error(String.format("unable to parse (dbInstance: %s, dbType: %s, dbStatement: %s) ", dbInstance, dbType, dbStatement));
            //                     if (!dbStatement.startsWith("call next value")) {
            //                         e.printStackTrace();
            //                     }
            //                 }
            //                 // SQL writes
            //                 try {
            //                     DamJdbcQueryParser damJdbcQueryParser = new DamJdbcQueryParser(dbInstance, dbType, dbStatement, logger);
            //                     ReferencedSchemaFieldsInfo fieldsByTable = damJdbcQueryParser.guessReferencedOutputTableSchemas();
            //                     writesCatalogEntries.addAll(batchBuilder.addCatalogEntries(fieldsByTable.lineageOperation, fieldsByTable.schema, dbType, defaultLocationRef, JdbcDatasourceNameFormatter.INST));
            //                 } catch (JSQLParserException e) {
            //                     logger.error(String.format("unable to parse (dbInstance: %s, dbType: %s, dbStatement: %s) ", dbInstance, dbType, dbStatement));
            //                     if (!dbStatement.startsWith("call next value")) {
            //                         e.printStackTrace();
            //                     }
            //                 }
            //             }
            //         });
            //     }
            //     // Add all-to-all lineage between all inputs and the HTTP output
            //     logger.warn("inputCatalogEntries: " + inputCatalogEntries);
            //     Process process = damEnv.enqueProcess(batchBuilder);
            //     ProcessRun processRun = damEnv.enqueProcessRun(process, endpointName, batchBuilder);
            //     String inputToHttpOutputOp = "APPEND";
            //     new SimpleDamLineageBuilder(
            //             process,
            //             processRun,
            //             inputCatalogEntries,
            //             outputCatalogEntry,
            //             inputToHttpOutputOp,
            //             "create"
            //     ).addToBatch(batchBuilder);
            //     // each write will need a different lineage as operation logic may be different
            //     logger.warn("writesCatalogEntries: " + writesCatalogEntries);
            //     String inputToJdbcWriteOp = "APPEND";
            //     writesCatalogEntries.forEach(jdbcWriteOutput -> {
            //         new SimpleDamLineageBuilder(
            //                 process,
            //                 processRun,
            //                 Collections.singletonList(outputCatalogEntry),
            //                 jdbcWriteOutput,
            //                 inputToJdbcWriteOp,
            //                 jdbcWriteOutput.lineageTitlePrefix
            //         ).addToBatch(batchBuilder);
            //     });
            //     reportBatchToDam(batchBuilder);
            // }
        }
    }

    // protected void reportBatchToDam(DamBatchBuilder batchBuilder){
    //     try {

    //         ApiClient apiClient;
    //         if (damEnv.isOffline()) {
    //             apiClient = new OfflineFileApiClient();
    //         } else {
    //             String authToken = damEnv.damIngestionToken();
    //             String serverHost = damEnv.damIngestionUrl();
    //             apiClient = new ApiClient()
    //                     .setBasePath(serverHost)
    //                     .addDefaultHeader("X-Auth-Token", authToken);
    //         }
    //         ManageKensuDamEntitiesApi apiInstance = new ManageKensuDamEntitiesApi(apiClient);
    //         BatchEntityReportResult result = apiInstance.reportEntityBatch(batchBuilder.getCompactedBatch());
    //         logger.info(String.format("DAM reportEntityBatch result: %s", result));
    //     } catch (javax.ws.rs.ProcessingException e){
    //         if (e.getMessage().equals("Already connected")){
    //             logger.error("Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch - " +
    //                     "SSL verification issue:", e);
    //         } else {
    //             logger.error("Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch", e);
    //         }
    //     } catch (ApiException | RuntimeException e) {
    //         logger.error("Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch", e);
    //     }
    // }

    @Override
    public void log(Instant timestamp, SpanData span, Map<String, ?> fields) {
        String logMessage = createLogMessage(timestamp, "Log", span, fields);
        logger.fine(logMessage);
    }

    private String createLogMessage(Instant timestamp, String step, SpanData span) {
        StringBuilder sb = new StringBuilder();
        sb.append("{"+timestamp+"} "+step+" span " + span.spanId  + " of trace " + span.context().toSpanId() + " for operation " + span.operationName + "\n");
        sb.append(" + tags:" + "\n");
        for (Map.Entry<String, Object> entry : span.tags.entrySet()) {
            sb.append("   - " + entry.getKey() + ": " + entry.getValue() + "\n");
        }
        sb.append(" + references:" + "\n");
        for (Map.Entry<String, String> entry : span.references.entrySet()) {
            sb.append("   - " + entry.getKey() + ": " + entry.getValue() + "\n");
        }

        return sb.toString();
    }

    private String createLogMessage(Instant timestamp, String step, SpanData span, Map<String, ?> fields) {
        StringBuilder sb = new StringBuilder(createLogMessage(timestamp, step, span));
        sb.append(" + fields" + "\n");
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            sb.append("   - " + entry.getKey() + ": " + entry.getValue() + "\n");
        }
        return sb.toString();
    }
}

