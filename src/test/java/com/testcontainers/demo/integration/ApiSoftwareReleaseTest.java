package com.testcontainers.demo.integration;

import com.testcontainers.demo.config.BaseRestAssuredIntegrationTest;
import com.testcontainers.demo.config.PgContainerConfig;
import io.restassured.response.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/*
 * Test class using the approach of having a configuration class with the testcontainers configurations
 */
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test",
    },
    classes = {PgContainerConfig.class}
)
public class ApiSoftwareReleaseTest extends BaseRestAssuredIntegrationTest {

    public MockWebServer gitClientMockWebServer = new MockWebServer();

    @BeforeEach
    public void setUpIntegrationTest() {
        this.setUpAbstractIntegrationTest();
    }

    @BeforeEach
    public void setUp() throws IOException {
        gitClientMockWebServer = new MockWebServer();
        gitClientMockWebServer.start(9091);
    }

    @AfterEach
    public void tearDown() throws IOException {
        gitClientMockWebServer.shutdown();
    }

    @Test
    public void addSoftwareRelease() {
        given(requestSpecification)
            .body("""
                {
                    "releaseDate":"2021-12-31",
                    "description":"A test release"
                }
                """)
            .when()
            .post("/api/softwareRelease")
            .then()
            .statusCode(201)
            .header("Location", matchesPattern(".*/softwareRelease/\\d+"));
    }

    /**
     * Test case to add software release with application in one request.
     * Expected a response header containing the ID of the created software release
     */
    @Test
    public void addSoftwareReleaseWithApps() {
        given(requestSpecification)
            .body("""
                {
                    "releaseDate": "2021-12-31",
                    "description": "A test release",
                    "applications": [
                        {"name": "New App1", "description": "App added with release", "owner": "Jane Doe"},
                        {"name": "New App2", "description": "Another app added with release", "owner": "Jane Doe"}
                    ]
                }
                """)
            .when()
            .post("/api/softwareRelease")
            .then()
            .statusCode(201)
            .header("Location", matchesPattern(".*/softwareRelease/\\d+"));
    }

    /**
     * Test case to link application to a software release
     * Expected the status code 200 at the link request
     */
    @Test
    public void createLinkBetweenAppAndSoftwareRelease() {
        // create an application
        Response appResponse = given(requestSpecification)
            .body("""
                {
                    "name": "Test app",
                    "description": "A test application.",
                    "owner": "Kate Williams"
                }
                """)
            .when()
            .post("/api/application");
        Integer appId = getIdFromResponseHeader(appResponse);
        // create a release
        Response releaseResponse = given(requestSpecification)
            .body("""
                {
                    "releaseDate": "2024-12-31",
                    "description": "A test release"
                }
                """)
            .when()
            .post("/api/softwareRelease");
        Integer releaseId = getIdFromResponseHeader(releaseResponse);
        // link application to release
        given(requestSpecification)
            .when()
            .put("/api/softwareRelease/{appId}/{releaseId}", appId, releaseId)
            .then()
            .statusCode(200);
    }

    /**
     * Test case to find a release by id.
     * Sends a GET request to find the added software release.
     * Expects the body of the response to match the added release and contain the git Tag specific to the release and application from the mock client.
     */
    @Test
    public void findSoftwareReleaseWithTagsFromGit() throws InterruptedException {
        // create an application
        Response appResponse = given(requestSpecification)
            .body("""
                {
                    "name": "Test_V1",
                    "description": "A test application.",
                    "owner": "Kate Williams"
                }
                """)
            .when()
            .post("/api/application");
        Integer appId = getIdFromResponseHeader(appResponse);
        ;
        // create a release
        Response releaseResponse = given(requestSpecification)
            .body("""
                {
                    "releaseDate": "2025-12-31",
                    "description": "A test release"
                }
                """)
            .when()
            .post("/api/softwareRelease");
        Integer releaseId = getIdFromResponseHeader(releaseResponse);
        // link application to release
        given(requestSpecification)
            .when()
            .put("/api/softwareRelease/{appId}/{releaseId}", appId, releaseId)
            .then();

        gitClientMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("v1.1.2025"));

        given(requestSpecification)
            .when()
            .get("/api/softwareRelease/{releaseId}", releaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("description", is("A test release"))
            .body("releaseDate", is("2025-12-31"))
            .body("applications.name", contains("Test_V1"))
            .body("applications.description", contains("A test application."))
            .body("gitTags", contains("v1.1.2025"));

        RecordedRequest request = gitClientMockWebServer.takeRequest();
        Assertions.assertEquals(request.getRequestUrl().queryParameter("releaseDate"), "2025-12-31");
        Assertions.assertEquals(request.getRequestUrl().queryParameter("applications"), "[Test_V1]");
    }

    /**
     * Test case to find a software release by id.
     * Sends a GET request to find the added software release.
     * Expects the body of the response to match the added release and contain the git Tag from the mock client.
     */
    @Test
    public void findSoftwareRelease2WithTagsFromGit() throws InterruptedException {
        // create an application
        Response appResponse = given(requestSpecification)
            .body("""
                {
                    "name": "Test_V2",
                    "description": "A test application.",
                    "owner": "Kate Williams"
                }
                """)
            .when()
            .post("/api/application");
        Integer appId = getIdFromResponseHeader(appResponse);
        ;
        // create a release
        Response releaseResponse = given(requestSpecification)
            .body("""
                {
                    "releaseDate": "2026-12-31",
                    "description": "Test V2 release"
                }
                """)
            .when()
            .post("/api/softwareRelease");
        Integer releaseId = getIdFromResponseHeader(releaseResponse);

        // link application to release
        given(requestSpecification)
            .when()
            .put("/api/softwareRelease/{appId}/{rId}", appId, releaseId)
            .then()
            .statusCode(200);

        gitClientMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("v2.1.2026"));

        given(requestSpecification)
            .when()
            .get("/api/softwareRelease/{releaseId}", releaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("description", is("Test V2 release"))
            .body("releaseDate", is("2026-12-31"))
            .body("applications.name", contains("Test_V2"))
            .body("applications.description", contains("A test application."))
            .body("gitTags", contains("v2.1.2026"));

        RecordedRequest request = gitClientMockWebServer.takeRequest();
        Assertions.assertEquals(request.getRequestUrl().queryParameter("releaseDate"), "2026-12-31");
        Assertions.assertEquals(request.getRequestUrl().queryParameter("applications"), "[Test_V2]");
    }

    @Test
    public void findReleaseWithIncompleteData() {
        // create a release
        Response response = given(requestSpecification)
            .body("""
                {
                    "releaseDate": "2021-12-31",
                    "description": "A test release"
                }
                """)
            .when()
            .post("/api/softwareRelease");

        Integer releaseId = getIdFromResponseHeader(response);

        gitClientMockWebServer.enqueue(new MockResponse().setResponseCode(404));

        given(requestSpecification)
            .when()
            .get("/api/softwareRelease/{releaseId}", releaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("description", is("A test release"))
            .body("gitTags", empty());
    }

    /**
     * Check the health of the application instance that was started using the testcontainers configuration for the API case
     */
    @Test
    public void healthy() {
        given(requestSpecification)
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200);
    }

    private static Integer getIdFromResponseHeader(Response response) {
        String headerWithId = response.getHeader("Location");
        return Integer.parseInt(headerWithId.substring(headerWithId.lastIndexOf("/") + 1));
    }
}
