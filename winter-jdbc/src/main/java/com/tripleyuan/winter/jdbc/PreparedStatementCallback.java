package com.tripleyuan.winter.jdbc;

import com.tripleyuan.winter.exception.DataAccessException;
import jakarta.annotation.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;

}
