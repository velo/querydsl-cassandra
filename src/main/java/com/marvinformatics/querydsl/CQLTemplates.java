package com.marvinformatics.querydsl;

import com.querydsl.sql.SQLTemplates;

public class CQLTemplates extends SQLTemplates
{

    public static final CQLTemplates CQL = new CQLTemplates();

    protected CQLTemplates()
    {
        super(Keywords.CQL, "\"", '\\', false);
    }

}
