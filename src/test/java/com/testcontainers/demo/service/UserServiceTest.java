package com.testcontainers.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.testcontainers.demo.entity.User;
import com.testcontainers.demo.helper.DBConnectionProvider;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/*
 * Test class using the approach of defining the container as a field
 */
class UserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class.getName());

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    UserService userService;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        DBConnectionProvider connectionProvider = new DBConnectionProvider(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            postgres.getPassword()
        );
        userService = new UserService(connectionProvider);
        userService.clearAll();
    }

    @Test
    void shouldGetCustomers() {
        userService.createUser(new User(1L, "George"));
        userService.createUser(new User(2L, "John"));

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void shouldGet3Customers() {
        userService.createUser(new User(1L, "George"));
        userService.createUser(new User(2L, "John"));
        userService.createUser(new User(3L, "Alex"));

        List<User> users = userService.getAllUsers();
        assertEquals(3, users.size());
    }
}
