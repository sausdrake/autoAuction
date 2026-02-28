package com.example.autoauction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Database connected successfully!");
            System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("URL: " + conn.getMetaData().getURL());
        }
    }
}