package com.marvinformatics.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SchemaAndTable;

import java.util.Arrays;

public class CQLSerializer extends SQLSerializer
{

    public CQLSerializer(Configuration conf)
    {
        super(conf);
    }

    @Override
    public Void visit(Path<?> path, Void context)
    {
        appendAsColumnName(path, false);
        return null;
    }

    public void handle(String template, Object... args)
    {
        if (template.contains("limit"))
        {
            handleTemplate(TemplateFactory.DEFAULT.create(template.replace("{0}", args[0].toString())),
                    Arrays.asList());
        } else
        {
            handleTemplate(TemplateFactory.DEFAULT.create(template), Arrays.asList(args));
        }
    }

}
