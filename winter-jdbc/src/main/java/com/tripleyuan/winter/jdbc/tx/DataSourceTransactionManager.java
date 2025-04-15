package com.tripleyuan.winter.jdbc.tx;

import com.tripleyuan.winter.exception.TransactionException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager implements PlatformTransactionManager, InvocationHandler {

    static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();
    final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TransactionStatus ts = transactionStatus.get();
        if (ts == null) {
            // begin new transactions
            try (Connection conn = dataSource.getConnection()) {
                final boolean autoCommit = conn.getAutoCommit();
                if (autoCommit) {
                    conn.setAutoCommit(false);
                }

                transactionStatus.set(new TransactionStatus(conn));

                try {
                    Object ret = method.invoke(proxy, args);
                    conn.commit();
                    return ret;
                } catch (InvocationTargetException ex) {
                    TransactionException te = new TransactionException(ex.getCause());
                    try {
                        conn.rollback();
                    } catch (SQLException sqlEx) {
                        te.addSuppressed(sqlEx);
                    }
                    throw te;
                } finally {
                    transactionStatus.remove();
                    if (autoCommit) {
                        conn.setAutoCommit(true);
                    }
                }
            }
        } else {
            // join current transactions
            return method.invoke(proxy, args);
        }
    }

}
