/**
 * Copyright (C) 2017 Marvin Herman Froeder (marvin@marvinformatics.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marvinformatics.querydsl;

import com.datastax.driver.core.*;
import com.google.common.base.CaseFormat;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.*;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class HardcodedSchemaGenerator {
    public static void main(String[] args) throws Exception {
        Cluster cluster = Cluster.builder()
                .addContactPoints("localhost")
                .withPort(9042)
                .build();

        Metadata metadata = cluster.getMetadata();
        List<KeyspaceMetadata> keyspaces = metadata.getKeyspaces();
        for (KeyspaceMetadata keyspace : keyspaces) {
            Collection<TableMetadata> tables = keyspace.getTables();
            for (TableMetadata table : tables) {
                String keyspaceName = keyspace.getName();
                String packageName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, keyspaceName)
                        .toLowerCase();

                String tableName = table.getName();
                String className = "Q" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);

                File directory = new File("src/main/java/com/marvinformatics/test", packageName);
                directory.mkdirs();
                File file = new File(directory, className + ".java");
                file.delete();
                try (PrintWriter pw = new PrintWriter(file);) {
                    pw.println("package com.marvinformatics.test." + packageName + ";");
                    pw.println();
                    String variableName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName);
                    pw.println("@javax.annotation.Generated(\"HardcodedSchemaGenerator\")");
                    pw
                            .println("public class " + className + " extends com.querydsl.core.types.dsl.BeanPath<"
                                    + className + "> {");
                    pw.println();
                    pw.println("    public static final " + className + " " + variableName + " = new " + className
                            + "(\"" + variableName + "\");");

                    List<ColumnMetadata> columns = table.getColumns();
                    for (ColumnMetadata column : columns) {
                        String columnName = column.getName();
                        String fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
                        Optional<Class<? extends Path>> pathType = getPathType(column.getType());

                        if (pathType.isPresent()) {
                            pw.println();
                            pw.println(
                                    "    public final " + toString(pathType, column.getType()) + " " + fieldName
                                            + " = " + create(columnName, column.getType()) + ";");
                        } else {
                            pw.println();
                            pw.println(
                                    "    // public final " + column.getType() + " " + fieldName + " = null\");");

                        }
                    }

                    pw.println("    public " + className + "(String variable) {\n" +
                            "        super(" + className + ".class, variable);\n" +
                            "    }");

                    pw.println();
                    pw.println("}");
                }
            }
        }

        cluster.close();
    }

    private static String create(String columnName, DataType type) {
        switch (type.getName()) {
        case TEXT:
            return "createString(\"" + columnName + "\")";
        case BOOLEAN:
            return "createBoolean(\"" + columnName + "\")";
        case TIMESTAMP:
            return "createDateTime(\"" + columnName + "\", " + javaType(type) + ".class)";
        case INT:
        case DOUBLE:
        case BIGINT:
            return "createNumber(\"" + columnName + "\", " + javaType(type) + ".class)";
        case MAP:
            return "createMap(\"" + columnName + "\", "
                    + javaType(type.getTypeArguments().get(0)) + ".class, "
                    + javaType(type.getTypeArguments().get(1)) + ".class,"
                    + pathName(type.getTypeArguments().get(1)) + ".class)";
        case SET:
            return "createSet(\"" + columnName + "\", "
                    + javaType(type.getTypeArguments().get(0)) + ".class, "
                    + pathName(type.getTypeArguments().get(0)) + ".class, "
                    + PathInits.class.getName() + ".DEFAULT)";
        case BLOB:
            return "createSimple(\"" + columnName + "\", byte[].class)";
        }

        return "createSimple(\"" + columnName + "\", " + javaType(type) + ".class)";
    }

    private static String javaType(DataType type) {
        switch (type.getName()) {
        case TEXT:
            return String.class.getName();
        case BOOLEAN:
            return Boolean.class.getName();
        case TIMESTAMP:
            return Timestamp.class.getName();
        case INT:
            return Integer.class.getName();
        case DOUBLE:
            return Double.class.getName();
        case BIGINT:
            return BigInteger.class.getName();
        case BLOB:
            return "byte[]";
        }

        return Object.class.getName();
    }

    private static String toString(Optional<Class<? extends Path>> pathType, DataType type) {
        String pathString = pathType.get().getName();

        switch (type.getName()) {
        case INT:
        case DOUBLE:
        case BIGINT:
            return pathString + "<" + javaType(type) + ">";
        case MAP:
            return pathString + "<" + javaType(type.getTypeArguments().get(0)) + ", "
                    + javaType(type.getTypeArguments().get(1)) + ", "
                    + pathName(type.getTypeArguments().get(1)) + ">";
        case SET:
            return pathString + "<" + javaType(type.getTypeArguments().get(0)) + ", "
                    + pathName(type.getTypeArguments().get(0)) + ">";
        case BLOB:
            return pathString + "<byte[]>";
        }

        return pathString;
    }

    private static String pathName(DataType type) {
        Optional<Class<? extends Path>> pathType = getPathType(type);
        if (pathType.isPresent())
            return toString(pathType, type);
        else
            return type.toString();
    }

    private static Optional<Class<? extends Path>> getPathType(DataType type) {
        switch (type.getName()) {
        case TEXT:
            return Optional.of(StringPath.class);
        case BOOLEAN:
            return Optional.of(BooleanPath.class);
        case TIMESTAMP:
            return Optional.of(DateTimePath.class);
        case INT:
        case DOUBLE:
        case BIGINT:
            return Optional.of(NumberPath.class);
        case MAP:
            return Optional.of(MapPath.class);
        case SET:
            return Optional.of(SetPath.class);
        case BLOB:
            return Optional.of(SimplePath.class);
        }
        return Optional.empty();
    }
}
