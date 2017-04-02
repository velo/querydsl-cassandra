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

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SchemaAndTable;

import java.util.Arrays;

public class CQLSerializer extends SQLSerializer {

    public CQLSerializer(Configuration conf) {
        super(conf);
    }

    @Override
    public Void visit(Path<?> path, Void context) {
        appendAsColumnName(path, false);
        return null;
    }

    public void handle(String template, Object... args) {
        if (template.contains("limit")) {
            handleTemplate(TemplateFactory.DEFAULT.create(template.replace("{0}", args[0].toString())),
                    Arrays.asList());
        } else {
            handleTemplate(TemplateFactory.DEFAULT.create(template), Arrays.asList(args));
        }
    }

}
