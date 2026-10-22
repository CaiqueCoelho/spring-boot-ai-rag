package com.caique.adventOfSpring;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustomerEndpointsTest {

    private RequestSpecification spec;

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


    @BeforeEach
    void beforeEach(RestDocumentationContextProvider restDocumentation) {
        this.spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation).operationPreprocessors())
                .build();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void getAllCustomers() throws Exception {
        UUID firstId = UUID.fromString("57dd4c28-fc17-4ffc-942c-914d90404e68");
        String firstName = "Caique";
        String lastName = "Coelho";

        List<Customer> customers = given(this.spec)
                .log().all()
                .filter(document("{method-name}",
                        ResourceSnippetParameters.builder()
                                .tag("tag-customer")
                                .summary("Returns all customers")
                                .privateResource(false)
                                .deprecated(false)
                                .description("Returns all the customers")
                                .responseFields(commonCustomerFields()),
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ))
                .when()
                .get("/api/customers")
                .then()
                .contentType("application/json")
                .statusCode(200)
                .extract()
                .body().jsonPath().getList(".", Customer.class);

        Customer customer = customers.get(0);
        Assertions.assertNotNull(customer.getId());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
    }

    private FieldDescriptor[] commonCustomerFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Identifier"),
                fieldWithPath("firstName").description("FirstName"),
                fieldWithPath("lastName").description("LastName")
        };

    }

    @Test
    void getCustomerById() {
        UUID firstId = UUID.fromString("57dd4c28-fc17-4ffc-942c-914d90404e68");
        String firstName = "Caique";
        String lastName = "Coelho";

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
        String firstName = "Icaro";
        String lastName = "Coelho";

        Customer firstCustomer = new Customer(null, firstName, lastName);

        var customer = given()
                .contentType(ContentType.JSON)
                .when()
                .body("""
                                {"firstName":"%s","lastName":"%s"}
                                """.formatted(firstName, lastName))
                .post("/api/customers")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName))
                .extract()
                .body().jsonPath().getObject(".", Customer.class);

        assertEquals(firstName, customer.getFirstName());
    }

    @Test
    void deleteCustomer() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "Caique";
        String lastName = "Coelho";

        given()
                .when()
                .delete("/api/customers/"  + firstId)
                .then()
                .statusCode(204);
    }
}
