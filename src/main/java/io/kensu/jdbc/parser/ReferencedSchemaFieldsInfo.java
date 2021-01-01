package io.kensu.jdbc.parser;

import java.util.Map;

import io.kensu.dim.client.model.FieldDef;
import io.kensu.utils.ConcurrentHashMultimap;

public class ReferencedSchemaFieldsInfo {
    public final ConcurrentHashMultimap<Map.Entry<FieldDef, String>> data;
    public final ConcurrentHashMultimap<FieldDef> control, schema;
    public final String lineageOperation;

    public ReferencedSchemaFieldsInfo(String lineageOperation, ConcurrentHashMultimap<Map.Entry<FieldDef, String>> data, ConcurrentHashMultimap<FieldDef> control){
        this.lineageOperation = lineageOperation;
        this.data = data;
        this.control = control;
        this.schema = new ConcurrentHashMultimap<FieldDef>();
        data.forEach((s, fs) -> fs.forEach(f -> this.schema.addEntry(s, f.getKey())));
        control.forEach((s, fs) -> fs.forEach(f -> this.schema.addEntry(s, f)));
    }
}
