package com.marvinformatics.querydsl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.mysema.commons.lang.CloseableIterator;

import java.util.Iterator;
import java.util.function.Function;

public class CQLIterator<T> implements CloseableIterator<T>
{

    private final ResultSet result;
    private final Iterator<Row> rowIterator;
    private final Function<Row, T> mapper;

    public CQLIterator(ResultSet result, Function<Row, T> mapper)
    {
        this.result = result;
        this.rowIterator = result.iterator();
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext()
    {
        return !result.isExhausted();
    }

    @Override
    public T next()
    {
        return mapper.apply(rowIterator.next());
    }

    @Override
    public void close()
    {
    }

}
