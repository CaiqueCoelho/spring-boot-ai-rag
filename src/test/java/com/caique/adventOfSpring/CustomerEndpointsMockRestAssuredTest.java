package com.caique.adventOfSpring;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustomerEndpointsMockRestAssuredTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.2")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    private static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @LocalServerPort
    private int port;

    @MockBean
    private CustomerRepository customerRepository;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void getAllCustomers() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";
        when(customerRepository.findAll()).thenReturn(List.of(new Customer(firstId, firstName, lastName)));

        given()
        .when()
                .get("/api/customers")
                        .then()
                                .statusCode(200)
                .contentType("application/json")
                .body("[0].id", equalTo(firstId.toString()))
                .body("[0].firstName", equalTo(firstName))
                .body("[0].lastName", equalTo(lastName));
    }

    @Test
    void getCustomerById() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        when(customerRepository.findById(firstId)).thenReturn(Optional.of(new Customer(firstId, firstName, lastName)));

        given()
                .when()
                .get("/api/customers/"  + firstId)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("id", equalTo(firstId.toString()))
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName));
    }

    @Test
    void createCustomer() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        Customer firstCustomer = new Customer(firstId, firstName, lastName);
        Customer secondCustomer = new Customer(secondId, firstName, lastName);

        when(customerRepository.save(any(Customer.class))).thenReturn(secondCustomer);

        given()
                .contentType(ContentType.JSON)
                .when()
                .body("""
                                {"id":"%s","firstName":"%s","lastName":"%s"}
                                """.formatted(firstId, firstName, lastName))
                .post("/api/customers")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("id", equalTo(secondId.toString()))
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName));
    }

    @Test
    void deleteCustomer() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        given()
                .when()
                .delete("/api/customers/"  + firstId)
                .then()
                .statusCode(204);
    }
}
