package com.tripleyuan.winter.jdbc;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DataSourceUtils {

    public static void releaseConnection(@Nullable Connection con) {
        try {
            con.close();
        } catch (SQLException ex) {
            log.debug("Couldn't close JDBC connection", ex);
        } catch (Throwable ex) {
            log.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }
}
