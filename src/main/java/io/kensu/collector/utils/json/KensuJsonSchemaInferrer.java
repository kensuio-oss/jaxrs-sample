package io.kensu.collector.utils.json;

import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.common.base.Joiner;
import io.kensu.dim.client.model.FieldDef;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KensuJsonSchemaInferrer {
    private static final Logger logger = Logger.getLogger(KensuJsonSchemaInferrer.class.getName());

    public static String extractFieldType(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return "string";
        } else if (o instanceof Number) {
            return "number";
        } else if (o instanceof Date || o instanceof java.sql.Date) {
            return "date";
        } else if (o instanceof Byte) {
            return "byte";
        } else if (o instanceof Character) {
            return "char";
        } else if (o instanceof Boolean) {
            return "boolean";
        } else if (o instanceof Object[] || o instanceof Collection) {
            // we try the first one
            if (o instanceof Collection) {
                if (!((Collection<?>) o).isEmpty()) {
                    return extractFieldType(((Collection<?>) o).iterator().next());
                } else {
                    // we can't do much about it
                    return null;
                }
            } else {
                if (((Object[]) o).length != 0) {
                    return extractFieldType(((Object[]) o)[0]);
                } else {
                    // we can't do much about it
                    return null;
                }
            }
        } else {
            logger.warning("Can't extract type of value of type: " + o.getClass().getName());
            return "unknown";
        }
    }

    private static void replaceWithIncByX(String k, Double x, Map<String, Double> m) {
        replaceInMap(k, x, (old, n) -> old+x, m);
    }

    private static void replaceWithIncBy1(String k, Map<String, Double> m) {
        replaceWithIncByX(k, 1d, m);
    }

    private static void replaceInMap(String k, Double def, BiFunction<Double,Double, Double> f, Map<String, Double> m) {
        m.replace(k, m.compute(k, (a, v) -> v==null?def:f.apply(v, def)));
    }

    private static void accumulateStats(String type, Object value, Map<String, Double> current) {
        replaceWithIncBy1("count", current);
        if (value == null) {
            replaceWithIncBy1("na.count", current);
        } else {
            switch (type) {
                case "number":
                    Double d = ((Number) value).doubleValue();
                    replaceWithIncByX("sum", d, current);
                    replaceInMap("min", d, (old, n) -> old<n?old:n, current);
                    replaceInMap("max", d, (old, n) -> old>n?old:n, current);
                    replaceInMap("mean", d, (old, n) -> current.get("sum")/current.get("count"), current);
                    break;
                case "boolean":
                    Boolean b = (Boolean) value;
                    if (b) {
                        replaceWithIncBy1("count.true", current);
                    } else {
                        replaceWithIncBy1("count.false", current);
                    }
                    break;
                case "string":
                case "date":
                case "byte":
                case "char":
                case "unknown":
                default: // null types
                    break;
            }
        }
    }

    public static Map.Entry<Set<FieldDef>, Map<String, Map<String, Double>>> inferSchemaAndStats(String json) {
        logger.fine("JSON to be flattened: " + json);
        JsonFlattener flattener = new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS);
        Map<String, Object> flattenJson = flattener.flattenAsMap();
        Map<String, String> schemaPrep = new HashMap<>();
        flattenJson.entrySet().stream().forEach(e -> {
            String property = e.getKey();
            Object value = e.getValue();
            String propPrep = property.replaceAll("\\[\\d+\\]", "[]")
                                        .replaceFirst("^\\[\\]\\.", ""); //FIXME skipping []. due to bug in UI (apparently)
            String type = extractFieldType(value);
            if (type != null) {
                schemaPrep.put(propPrep, type);
            }
        });
        logger.fine("Flattened JSON: " + flattenJson.toString());

        // FIXME / TODO... handle more than one level of arrays...
        Map<String, Map<String, Double>> allStats = new HashMap<>();
        schemaPrep.entrySet().forEach(scp -> {
            String keyPrep = scp.getKey();
            String typePrep = scp.getValue();
            List<Object> values = flattenJson.entrySet().stream()
                                        .filter(e -> e.getKey().replaceAll("\\[\\d+\\]", "[]")
                                                                .replaceFirst("^\\[\\]\\.", "")
                                                                .equals(keyPrep)
                                        )
                                        .map(e -> e.getValue())
                                        .collect(Collectors.toList());
            Map<String, Double> stats = new HashMap<>();
            values.forEach(v -> accumulateStats(typePrep, v, stats));
            logger.fine("Stats for "+keyPrep+" from JSON: " + Joiner.on(",").withKeyValueSeparator("=").join(stats));
            allStats.put(keyPrep, stats);
        });

        Set<FieldDef> fields = schemaPrep.entrySet().stream()
                                        .map(e -> new FieldDef().name(e.getKey()).fieldType(e.getValue()).nullable(false)) //fixme nullable
                                        .collect(Collectors.toSet());
        return Map.entry(fields, allStats);
    }

}
