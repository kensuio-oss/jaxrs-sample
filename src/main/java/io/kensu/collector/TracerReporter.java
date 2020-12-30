package io.kensu.collector;

import io.kensu.collector.DamProcessEnvironment;
import io.kensu.collector.model.DamBatchBuilder;
import io.kensu.collector.model.DamDataCatalogEntry;
import io.kensu.collector.model.DamSchemaUtils;
import io.kensu.collector.model.SimpleDamLineageBuilder;
import io.kensu.collector.model.datasource.JdbcDatasourceNameFormatter;
import io.kensu.utils.ConcurrentHashMultimap;
import io.kensu.dam.ApiClient;
import io.kensu.dam.ApiException;
import io.kensu.dam.ManageKensuDamEntitiesApi;
import io.kensu.dam.OfflineFileApiClient;
import io.kensu.dam.model.*;
import io.kensu.dam.model.Process;
import io.kensu.jdbc.parser.DamJdbcQueryParser;
import io.kensu.jdbc.parser.ReferencedSchemaFieldsInfo;
// import io.kensu.collector.model.datasource.HttpDatasourceNameFormatter;
// import io.kensu.collector.model.datasource.JdbcDatasourceNameFormatter;
// import io.kensu.jdbc.parser.DamJdbcQueryParser;
// import io.kensu.jdbc.parser.ReferencedSchemaFieldsInfo;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.SpanData;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
//import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;

//import static io.kensu.json.DamJsonSchemaInferrer.DAM_OUTPUT_SCHEMA_TAG;

public class TracerReporter implements Reporter {
    static final Logger logger = Logger.getLogger(TracerReporter.class.getName());

    private final ConcurrentHashMultimap<SpanData> spanChildrenCache = new ConcurrentHashMultimap<>();

    //public TracerReporter(Logger logger, AbstractUrlsTransformer springUrlsTransformer) {
    public TracerReporter() {
    }

    protected DamProcessEnvironment damEnv = new DamProcessEnvironment();

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
            spanChildrenCache.addEntry(maybeParentId, span);
        } else {
            // this is the main SPAN, report all the stuff which was gathered so far
            DamBatchBuilder batchBuilder = new DamBatchBuilder().withDefaultLocation();
            PhysicalLocationRef defaultLocationRef = DamBatchBuilder.DEFAULT_LOCATION_REF;

            String logMessage = createLogMessage(timestamp, "Finish", span);
            logger.fine(logMessage);

            // //   - http.status_code: 200
            // //   - component: java-web-servlet
            // //   - span.kind: server
            // //   - http.url: http://localhost/people/search/findByLastName
            // //   - http.method: GET
            // //  http.request.url.path.pattern	    /v1/visit/{visitId}?outputFormat={outputFormat}
            // //  http.request.url.path.parameters	    visitId
            // //  http.request.url.query.parameters	outputFormat

            // //   - DamOutputSchema: [class FieldDef {
            Integer httpStatus = getTagOrDefault(Tags.HTTP_STATUS, span, 0);
            // FIMXE: need ability to remove param value to get URL pattern!!!
            String httpUrl = getTagOrDefault(Tags.HTTP_URL, span, null);
            String httpMethod = getTagOrDefault(Tags.HTTP_METHOD, span, null);
            String httpPathPattern = getTagOrDefault("http.request.url.path.pattern", span, null);
            String httpPathParameters = getTagOrDefault("http.request.url.path.parameters	", span, null);
            String httpQueryParameters = getTagOrDefault("http.request.url.query.parameters	", span, null);

            String transformedHttpUrl = "http:"+httpMethod+":"+httpPathPattern;
            logger.warning("transformedHttpUrl: "+ transformedHttpUrl);
            if ((httpStatus >= 200) && (httpStatus < 300) && transformedHttpUrl != null && httpMethod != null) {
                List<DamDataCatalogEntry> inputCatalogEntries = new ArrayList<>();
                Set<SpanData> children = spanChildrenCache.get(span.spanId);
                // children might be => "Query" then "serialize"  
                for (SpanData sc : children) {
                    SpanData spanChild = sc;
                    if (spanChild.operationName == "Query") {
                        /*
                            component	-> java-jdbc
                            db.instance	-> classicmodels
                            db.statement -> select productlin0_.productLine as productL1_0_, productlin0_.htmlDescription as htmlDesc2_0_, productlin0_.image as image3_0_, productlin0_.textDescription as textDesc4_0_ from productlines productlin0_ where productlin0_.productLine=?
                            db.type	-> clickhouse
                            internal.span.format -> jaeger
                            peer.address -> localhost:8123
                            peer.service -> classicmodels[clickhouse(localhost:8123)]
                            span.kind -> client
                        */
                        String dbInstance = getTagOrDefault(Tags.DB_INSTANCE, spanChild, "");
                        String dbType = getTagOrDefault(Tags.DB_TYPE, spanChild, "");
                        String dbStatement = getTagOrDefault(Tags.DB_STATEMENT, spanChild, "");
                        // SQL reads
                        DamJdbcQueryParser damJdbcQueryParser = null;
                        ReferencedSchemaFieldsInfo fieldsByTable = null;
                        try {
                            damJdbcQueryParser = new DamJdbcQueryParser(dbInstance, dbType, dbStatement);
                            fieldsByTable = damJdbcQueryParser.guessReferencedInputTableSchemas();
                            inputCatalogEntries.addAll(batchBuilder.addCatalogEntries("SQL Query", fieldsByTable.schema, dbType, defaultLocationRef, JdbcDatasourceNameFormatter.INST));
                        } catch (JSQLParserException e) {
                            logger.log(Level.SEVERE, String.format("unable to parse (dbInstance: %s, dbType: %s, dbStatement: %s) ", dbInstance, dbType, dbStatement), e);
                            if (!dbStatement.startsWith("call next value")) {
                                e.printStackTrace();
                            }
                        }

                        // TODO link http DS with tables in all 'Query's

                        Function<String, Optional<Double>> stod = (s) -> {
                            Double statDoubleValue = null;
                            try {
                                statDoubleValue = Double.parseDouble(s);
                            } catch (NumberFormatException ne) {
                                logger.fine("Can't parse double value of stats sent in query stats span: " + s);
                            }
                            return Optional.<Double>ofNullable(statDoubleValue);
                        };

                        Set<SpanData> queryChildren = spanChildrenCache.get(spanChild.spanId);
                        for (SpanData queryStatsSpan : queryChildren) {
                            if (queryStatsSpan.operationName == "QueryResultStats") {
                                /*
                                    db.column.1.md -> [,products,productL9_1_0_,String,false]
                                    db.column.1.stats-> {(count,12.0)}
                                    db.column.2.md	-> [,products,productC1_1_0_,String,false]
                                    db.column.2.stats-> {(count,12.0)}
                                    db.column.3.md	-> [,products,productC1_1_1_,String,false]
                                    db.column.3.stats-> {(count,12.0)}
                                    db.count	-> 13
                                    internal.span.format -> jaeger
                                */
                                // parsed SQL (used to build schema fields) can give access to original name of column if label are in the .md tags
                                //    => to name stats appropriately (using names like in the schema fields)
                                Function<Integer, String> mkKey = (i) -> { return "db.column."+i+".md";};
                                Function<Integer, String> mkStats = (i) -> { return "db.column."+i+".stats";};
                                int keyIndex = 1;
                                String mdKey = mkKey.apply(keyIndex);
                                Map<String, Map<String, Double>> statsByTable = new HashMap<>();
                                Optional<Double> dbCount = Optional.ofNullable(queryStatsSpan.tags.get("db.count")).flatMap(e -> stod.apply(""+e));
                                while (queryStatsSpan.tags.containsKey(mdKey)) {
                                    Map<String, String> md = (Map<String, String>)queryStatsSpan.tags.get(mdKey);
                                    String db = md.get("schemaName");
                                    db = db.length()==0?dbInstance:db;
                                    String table = md.get("tableName");
                                    Map<String, Double> stats;
                                    if (!statsByTable.containsKey(db+"."+table)) {
                                        Map<String, Double> m = new HashMap<>();
                                        if (dbCount.isPresent()) {
                                            m.put("row.count", dbCount.get());
                                        }
                                        statsByTable.put(db+"."+table, m);
                                    }
                                    stats = statsByTable.get(db+"."+table);
                                    String columnOrAlias = md.get("name");
                                    HashSet<Entry<FieldDef, String>> columns = fieldsByTable.data.get(db+"."+table);
                                    // checking if columnOrAlias is alias (values in data) => if not found the  we consider it as a column 
                                    String column = columnOrAlias;
                                    for (Entry<FieldDef, String> c: columns) {
                                        if (c.getValue().equals(columnOrAlias)) {
                                            column = c.getKey().getName();
                                            break;
                                        }
                                    } 
                                    final String columnFinal = column;               
                                    Map<String, Double> statsTagValue = ((Map<String, Double>)queryStatsSpan.tags.get(mkStats.apply(keyIndex)));
                                    statsTagValue.forEach((k,v) -> stats.put(columnFinal+"."+k, v));
                                    mdKey = mkKey.apply(++keyIndex);
                                }
                                logger.fine("statsByTable: " + statsByTable);
                                // Attach stats to all tables in this 'Query' (******)
                            }
                        }
                 
                    } else if (spanChild.operationName == "serialize") {
                        logger.info(spanChild.toString());
                        /*
                        entity.type	-> com.hotjoe.services.ProductLineService$ProductLineView
                        internal.span.format -> jaeger
                        media.type	-> application/json
                        */
                        //TODO Attach SAME (******) stats to all tables in this 'Query'
                        // create schema and stats out of the entities to the endpoint -> KensuTracingInterceptorFeature
                        // link all tables to endpoint and attach stats to endpoint
                    }



                    //TODO remove spans from cache!!!
                    


                }

            //Set<FieldDef> damOutputFields = getTagOrDefault(DAM_OUTPUT_SCHEMA_TAG, span, null);
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
            }
        }
    }

    protected void reportBatchToDam(DamBatchBuilder batchBuilder){
        try {
            ApiClient apiClient;
            if (damEnv.isOffline()) {
                apiClient = new OfflineFileApiClient();
            } else {
                String authToken = damEnv.damIngestionToken();
                String serverHost = damEnv.damIngestionUrl();
                apiClient = new ApiClient()
                        .setBasePath(serverHost)
                        .addDefaultHeader("X-Auth-Token", authToken);
            }
            ManageKensuDamEntitiesApi apiInstance = new ManageKensuDamEntitiesApi(apiClient);
            BatchEntityReportResult result = apiInstance.reportEntityBatch(batchBuilder.getCompactedBatch());
            logger.info(String.format("DAM reportEntityBatch result: %s", result));
        } catch (javax.ws.rs.ProcessingException e){
            if (e.getMessage().equals("Already connected")){
                logger.log(Level.SEVERE, "Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch - SSL verification issue:", e);
            } else {
                logger.log(Level.SEVERE, "Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch", e);
            }
        } catch (ApiException | RuntimeException e) {
            logger.log(Level.SEVERE, "Exception when calling ManageKensuDAMEntitiesApi#reportEntityBatch", e);
        }
    }

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

