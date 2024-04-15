package com.testcontainers.demo.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.testcontainers.demo.config.BaseRestAssuredIntegrationTest;
import com.testcontainers.demo.config.PgContainerConfig;
import com.testcontainers.demo.entity.Application;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.TimeUnit;

/*
 * Test class using the approach of having a configuration class with the testcontainers configurations
 */
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test"
    },
    classes = {PgContainerConfig.class}
)
public class DBApplicationTest extends BaseRestAssuredIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DBApplicationTest.class);
    private static StopWatch watch;

    @Autowired
    private PostgreSQLContainer postgresContainer;

    @BeforeAll
    public static void setUp() {
        LOG.info("Test setUp At: " + java.time.LocalDateTime.now());
        watch = new StopWatch();
        watch.start();
    }

    @BeforeEach
    public void setUpIntegrationTest() {
        this.setUpAbstractIntegrationTest();
    }

    @AfterAll
    public static void tearDown() {
        watch.stop();
        float time = (float) watch.getTime(TimeUnit.MILLISECONDS);
        LOG.info("Test tearDown At: {}, duration: {}", java.time.LocalDateTime.now(), time / 1000);
    }

    /**
     * Test case to add an application.
     * Sends a POST request with an application body and expects a status code of 201.
     */
    @Test
    public void addApplication() {
        given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application add\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application")
            .then()
            .statusCode(is(201))
            .body("id", notNullValue())
            .body("name", is("Test Application add"))
            .body("description", is("A test application."))
            .body("owner", is("Kate Williams"));
    }

    /**
     * Test case to add an application that already exists
     * Sends a POST request with an application body and expects a status code of 409.
     */
    @Test
    public void addApplicationAlreadyExists() {
        given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application existing\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application")
            .then()
            .statusCode(is(201));
        given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application existing\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application")
            .then()
            .statusCode(is(409));
    }

    /**
     * Test case to verify the functionality of finding an application.
     * Adds an application and then sends a GET request to find it.
     * Expects the body of the response to match the added application.
     */
    @Test
    public void findApplication() {
        Response responseContent = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application find\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application");
        Application response = responseContent.body().as(Application.class);

        given(requestSpecification)
            .when()
            .get("/api/application/{id}", response.getId())
            .then()
            .body("id", is(response.getId()))
            .body("name", is("Test Application find"))
            .body("description", is("A test application."))
            .body("owner", is("Kate Williams"));
    }

    /**
     * Test case to update an application.
     * Adds an application and then sends a PUT request with an updated application body.
     * Expects a status code of 200.
     */
    @Test
    public void updateApplication() {
        Response responseContent  = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application update\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application");
        Application response = responseContent.body().as(Application.class);

        given(requestSpecification)
            .body("{" +
                "\"id\": \" "+ response.getId() + "\"," +
                "\"name\": \"Updated Application\"," +
                "\"description\" : \"An updated application.\"," +
                "\"owner\": \"John Doe\"" +
                "}")
            .when()
            .put("/api/application")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", is(response.getId()))
            .body("name", is("Updated Application"))
            .body("owner", is("John Doe"))
            .body("description", is("An updated application."));
    }

    /**
     * Test case to verify the deletion of an application.
     * Adds an application and then sends a DELETE request to remove it.
     * Expects a status code of 204.
     */
    @Test
    public void deleteApplication() {
        Response responseContent = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application delete\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application");
        Application response = responseContent.body().as(Application.class);

        given(requestSpecification)
            .when()
            .delete("/api/application/{id}", response.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void addApplication2() {
        given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application 2 add\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application")
            .then()
            .statusCode(is(201))
            .body("id", notNullValue())
            .body("name", is("Test Application 2 add"))
            .body("description", is("A test application."))
            .body("owner", is("Kate Williams"));
    }

    @Test
    public void findApplication2() {
        Response responseContent = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application find 2\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application");
        Application response = responseContent.body().as(Application.class);

        given(requestSpecification)
            .when()
            .get("/api/application/{id}", response.getId())
            .then()
            .body("id", is(response.getId()))
            .body("name", is("Test Application find 2"))
            .body("description", is("A test application."))
            .body("owner", is("Kate Williams"));
    }

    /**
     * Test case to update an application.
     * Adds an application and then sends a PUT request with an updated application body.
     * Expects a status code of 200.
     */
    @Test
    public void updateApplication2() {
        Response response = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application 2 update\"," +
                "\"description\" : \"A test application.\"," +
                "\"owner\": \"Kate Williams\"" +
                "}")
            .when()
            .post("/api/application");
        Application appResponse = response.body().as(Application.class);

        given(requestSpecification)
            .body("{" +
                "\"id\": \" "+ appResponse.getId() + "\"," +
                "\"name\": \"Updated Application 2\"," +
                "\"description\" : \"An updated application 2\"," +
                "\"owner\": \"John Doe\"" +
                "}")
            .when()
            .put("/api/application")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", is(appResponse.getId()))
            .body("name", is("Updated Application 2"))
            .body("owner", is("John Doe"))
            .body("description", is("An updated application 2"));
    }

    /**
     * Test case to verify the deletion of an application.
     * Adds an application and then sends a DELETE request to remove it.
     * Expects a status code of 204.
     */
    @Test
    public void deleteApplication2() {
        Response responseContent = given(requestSpecification)
            .body("{" +
                "\"name\": \"Test Application 2 delete\"," +
                "\"description\" : \"application 2 delete\"," +
                "\"owner\": \"John Doe\"" +
                "}")
            .when()
            .post("/api/application");
        Application response = responseContent.body().as(Application.class);

        given(requestSpecification)
            .when()
            .delete("/api/application/{id}", response.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void healthy() {
        given(requestSpecification)
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200);
    }
}
