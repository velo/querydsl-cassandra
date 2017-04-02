package com.marvinformatics.querydsl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.support.FetchableQueryBase;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.sql.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.function.Function;

final public class CQLQuery<T> extends FetchableQueryBase<T, CQLQuery<T>>
{

    private final Cluster cqlCluster;
    private final Session cqlSession;

    public CQLQuery(Cluster cqlCluster, Session cqlSession)
    {
        super(new QueryMixin<CQLQuery<T>>(new DefaultQueryMetadata(), false));
        queryMixin.setSelf(this);
        this.cqlCluster = cqlCluster;
        this.cqlSession = cqlSession;

    }

    @Override
    public T fetchOne()
    {
        if (getMetadata().getModifiers().getLimit() == null)
            limit(1);

        CloseableIterator<T> iterator = iterate();
        return uniqueResult(iterator);
    }

    private QueryMetadata getMetadata()
    {
        return queryMixin.getMetadata();
    }

    @Override
    public CloseableIterator<T> iterate()
    {
        CQLSerializer serializer = createSerializer();
        serializer.serialize(queryMixin.getMetadata(), false);
        ResultSet result = cqlSession.execute(serializer.toString(), serializer.getConstants().toArray());
        Expression<T> expr = projection();

        final Function<Row, T> mapper;
        if (expr == null)
        {
            mapper = row -> (T) row.getObject(1);
        } else if (expr instanceof FactoryExpression)
        {
            mapper = row -> newInstance((FactoryExpression<T>) expr, row, 0);
        } else if (expr.equals(Wildcard.all))
        {
            mapper = row -> {
                Object[] rv = new Object[row.getColumnDefinitions().size()];
                for (int i = 0; i < rv.length; i++)
                {
                    rv[i] = row.getObject(i + 1);
                }
                return (T) rv;
            };
        } else
        {
            mapper = row -> get(row, expr, 1, expr.getType());
        }

        return new CQLIterator<T>(result, mapper);
    }

    private CQLSerializer createSerializer()
    {
        CQLSerializer serializer = new CQLSerializer(new Configuration(CQLTemplates.CQL));
        return serializer;
    }

    private T newInstance(FactoryExpression<T> c, Row row, int offset)
    {
        Object[] args = new Object[c.getArgs().size()];
        for (int i = 0; i < args.length; i++)
        {
            args[i] = get(row, c.getArgs().get(i), offset + i, c.getArgs().get(i).getType());
        }
        return c.newInstance(args);
    }

    private <U> U get(Row row, Expression<?> expr, int i, Class<U> type)
    {
        return row.get(i, type);
    }

    private Expression<T> projection()
    {
        return (Expression<T>) queryMixin.getMetadata().getProjection();
    }

    @Override
    public QueryResults<T> fetchResults()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long fetchCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public <U> CQLQuery<U> select(Expression<U> expr)
    {
        queryMixin.setProjection(expr);
        @SuppressWarnings("unchecked") // This is the new type
        CQLQuery<U> newType = (CQLQuery<U>) this;
        return newType;
    }

    public CQLQuery<Tuple> select(Expression<?>... exprs)
    {
        queryMixin.setProjection(exprs);
        @SuppressWarnings("unchecked") // This is the new type
        CQLQuery<Tuple> newType = (CQLQuery<Tuple>) this;
        return newType;
    }

    public CQLQuery<T> from(Expression<?> arg)
    {
        return queryMixin.from(arg);
    }

    public CQLQuery<T> from(Expression<?>... args)
    {
        return queryMixin.from(args);
    }

    @Override
    public String toString()
    {
        CQLSerializer serializer = createSerializer();
        serializer.serialize(queryMixin.getMetadata(), false);
        return serializer.toString();
    }

}
