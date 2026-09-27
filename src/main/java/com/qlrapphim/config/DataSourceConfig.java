package com.qlrapphim.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Dùng OracleDataSource làm underlying source cho HikariCP.
 * Đây là cách DUY NHẤT để truyền oracle.jdbc.defaultNChar=true
 * vào Oracle JDBC driver khi dùng HikariCP.
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() throws SQLException {
        // Set System property trước mọi thứ
        System.setProperty("oracle.jdbc.defaultNChar", "true");
        System.setProperty("oracle.jdbc.fanEnabled", "false");

        // ── 1. Tạo OracleDataSource với NChar property ───────────────
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(jdbcUrl);
        ods.setUser(username);
        ods.setPassword(password);

        // setConnectionProperty chạy TRỰC TIẾP vào Oracle driver
        Properties oracleProps = new Properties();
        oracleProps.setProperty("oracle.jdbc.defaultNChar", "true");
        oracleProps.setProperty("oracle.jdbc.J2EE13Compliant", "true");
        oracleProps.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        ods.setConnectionProperties(oracleProps);

        // ── 2. Bọc OracleDataSource vào HikariCP ─────────────────────
        HikariConfig hikariConfig = new HikariConfig();
        // Dùng setDataSource thay vì setJdbcUrl để Oracle props được giữ nguyên
        hikariConfig.setDataSource(ods);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionInitSql("SELECT 1 FROM DUAL");
        hikariConfig.setPoolName("OraclePool-UTF8");

        return new HikariDataSource(hikariConfig);
    }
}
