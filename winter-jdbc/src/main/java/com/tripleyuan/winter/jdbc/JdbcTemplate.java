package com.tripleyuan.winter.jdbc;

import com.tripleyuan.winter.exception.DataAccessException;
import com.tripleyuan.winter.jdbc.tx.TransactionalUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Number queryForNumber(String sql, Object... args) throws DataAccessException {
        return queryForObject(sql, NumberRowMapper.instance, args);
    }

    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) throws DataAccessException {
        if (clazz == String.class) {
            return (T) queryForObject(sql, StringRowMapper.instance, args);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) queryForObject(sql, BooleanRowMapper.instance, args);
        }
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            return (T) queryForObject(sql, NumberRowMapper.instance, args);
        }
        return queryForObject(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    T t = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (t == null) {
                                t = rowMapper.mapRow(rs, rs.getRow());
                            } else {
                                throw new DataAccessException("Multiple rows found.");
                            }
                        }
                    }
                    if (t == null) {
                        throw new DataAccessException("Empty result set.");
                    }
                    return t;
                });
    }

    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... args) throws DataAccessException {
        return queryForList(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    List<T> list = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(rowMapper.mapRow(rs, rs.getRow()));
                        }
                    }
                    return list;
                });
    }

    public Number updateAndReturnGeneratedKey(String sql, Object... args) throws DataAccessException {
        return execute(
                // PreparedStatementCreator
                (Connection con) -> {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    bindArgs(ps, args);
                    return ps;
                },
                // PreparedStatementCallback
                (PreparedStatement ps) -> {
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        throw new DataAccessException("0 rows inserted.");
                    }
                    if (n > 1) {
                        throw new DataAccessException("Multiple rows inserted.");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        while (keys.next()) {
                            return (Number) keys.getObject(1);
                        }
                    }
                    throw new DataAccessException("Should not reach here.");
                });
    }


    public int update(String sql, Object... args) {
        return execute(preparedStatementCreator(sql, args), ps -> ps.executeUpdate());
    }

    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        return execute(con -> {
            try (PreparedStatement ps = psc.createPreparedStatement(con)) {
                return action.doInPreparedStatement(ps);
            }
        });
    }

    public <T> T execute(ConnectionCallback<T> action) {
        // use current connection if exists
        Connection con = TransactionalUtils.getCurrentConnection();
        if (con != null) {
            try {
                return action.doInConnection(con);
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
        }

        // get a new connection
        try {
            con = dataSource.getConnection();
            boolean autoCommit = con.getAutoCommit();
            if (!autoCommit) {
                con.setAutoCommit(true);
            }
            T t = action.doInConnection(con);
            if (!autoCommit) {
                con.setAutoCommit(false);
            }
            return t;
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        } finally {
            DataSourceUtils.releaseConnection(con);
        }
    }

    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            bindArgs(ps, args);
            return ps;
        };
    }

    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }
}

class StringRowMapper implements RowMapper<String> {

    static StringRowMapper instance = new StringRowMapper();

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {

    static BooleanRowMapper instance = new BooleanRowMapper();

    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean(1);
    }
}

class NumberRowMapper implements RowMapper<Number> {

    static NumberRowMapper instance = new NumberRowMapper();

    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(1);
    }
}
