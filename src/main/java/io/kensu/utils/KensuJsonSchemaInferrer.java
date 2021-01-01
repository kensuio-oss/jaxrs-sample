package io.kensu.utils;

import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import io.kensu.collector.model.DamSchemaUtils;
import io.kensu.dim.client.model.FieldDef;

import java.util.*;
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

    public static Set<FieldDef> inferSchema(String json) {
        JsonFlattener flattener = new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS);
        Map<String, Object> flattenJson = flattener.flattenAsMap();
        Map<String, String> schemaPrep = new HashMap<>();
        // TODO compute stats
        flattenJson.entrySet().stream().forEach(e -> {
            String property = e.getKey();
            Object value = e.getValue();
            String propPrep = property.replaceAll("\\[\\d+\\]", "[]");
            String type = extractFieldType(value);
            if (type != null) {
                schemaPrep.put(propPrep, type);
            }
        });
        logger.info(flattenJson.toString());
        Set<FieldDef> fields = schemaPrep.entrySet().stream()
                                        .map(e -> new FieldDef().name(e.getKey()).fieldType(e.getValue()))
                                        .collect(Collectors.toSet());
        return fields;
    }

}
