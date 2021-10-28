/*
 *
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: develop
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.kensu.dim.client.model;

import java.util.Objects;

/**
 * FieldDef
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-12-31T08:50:25.403201+01:00[Europe/Brussels]")
public class FieldDef {
    public static final String JSON_PROPERTY_NAME = "name";
    private String name;

    public static final String JSON_PROPERTY_FIELD_TYPE = "fieldType";
    private String fieldType;

    public static final String JSON_PROPERTY_NULLABLE = "nullable";
    private Boolean nullable;


    public FieldDef name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public FieldDef fieldType(String fieldType) {

        this.fieldType = fieldType;
        return this;
    }

    public String getFieldType() {
        return fieldType;
    }


    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }


    public FieldDef nullable(Boolean nullable) {

        this.nullable = nullable;
        return this;
    }


    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public Boolean isNullable() {
        return getNullable();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldDef fieldDef = (FieldDef) o;
        return Objects.equals(this.name, fieldDef.name) &&
                Objects.equals(this.fieldType, fieldDef.fieldType) &&
                Objects.equals(this.nullable, fieldDef.nullable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fieldType, nullable);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FieldDef {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    fieldType: ").append(toIndentedString(fieldType)).append("\n");
        sb.append("    nullable: ").append(toIndentedString(nullable)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
