package com.tripleyuan.winter.jdbc;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Value;
import com.tripleyuan.winter.jdbc.tx.DataSourceTransactionManager;
import com.tripleyuan.winter.jdbc.tx.PlatformTransactionManager;
import com.tripleyuan.winter.jdbc.tx.TransactionalBeanPostProcessor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
public class JdbcConfiguration {

    @Bean(destroyMethod = "close")
    public DataSource dataSource(
            @Value("${winter.datasource.url}") String url,
            @Value("${winter.datasource.username}") String username,
            @Value("${winter.datasource.password}") String password,
            @Value("${winter.datasource.driver-class-name:}") String driver,
            @Value("${winter.datasource.maximum-pool-size:20}") int maximumPoolSize,
            @Value("${winter.datasource.minimum-pool-size:1}") int minimumPoolSize,
            @Value("${winter.datasource.connection-timeout:30000}") int connTimeout
    ) {
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(false);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driver != null) {
            config.setDriverClassName(driver);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TransactionalBeanPostProcessor transactionalBeanPostProcessor() {
        return new TransactionalBeanPostProcessor();
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
