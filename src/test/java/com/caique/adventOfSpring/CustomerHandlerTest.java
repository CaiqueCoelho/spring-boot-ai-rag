package com.caique.adventOfSpring;

import jakarta.servlet.ServletException;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JsonContentAssert;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerHandlerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MeterRegistry meterRegistry;

    private CustomerHandler customerHandler;

    @BeforeEach
    void setUp() {
        customerHandler = new CustomerHandler(meterRegistry);
        ReflectionTestUtils.setField(customerHandler, "customerRepository", customerRepository);
    }

    @Test
    void getAll() throws ServletException, IOException {
        UUID id = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        when(customerRepository.findAll()).thenReturn(List.of(new Customer(id, firstName, lastName)));

        ServerResponse serverResponse = customerHandler.getAll();

        String body = ServerResponseExtractor.extractBody(serverResponse);

        new JsonContentAssert(this.getClass(), body).isEqualToJson("""
                    [{"id": "%s", "firstName": "First Name", "lastName": "Last Name"}]
                """.formatted(id));
    }

    @Test
    void getById() throws ServletException, IOException {
        UUID id = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        when(customerRepository.findById(id)).thenReturn(Optional.of(new Customer(id, firstName, lastName)));

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.pathVariable("id")).thenReturn(id.toString());

        ServerResponse serverResponse = customerHandler.getById(serverRequest);

        String body = ServerResponseExtractor.extractBody(serverResponse);

        new JsonContentAssert(this.getClass(), body).isEqualToJson("""
                    {"id": "%s", "firstName": "First Name", "lastName": "Last Name"}
                """.formatted(id));
    }

    @Test
    void deleteById() {
        UUID id = UUID.randomUUID();

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.pathVariable("id")).thenReturn(id.toString());

        ServerResponse serverResponse = customerHandler.deleteById(serverRequest);

        assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createCustomer() throws ServletException, IOException {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        Customer firstCustomer = new Customer(firstId, firstName, lastName);
        Customer secondCustomer = new Customer(secondId, firstName, lastName);

        when(customerRepository.save(any(Customer.class))).thenReturn(secondCustomer);

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.body(Customer.class)).thenReturn(firstCustomer);

        ServerResponse serverResponse = customerHandler.createCustomer(serverRequest);

        String body = ServerResponseExtractor.extractBody(serverResponse);

        new JsonContentAssert(this.getClass(), body).isEqualToJson("""
                    {"id": "%s", "firstName": "First Name", "lastName": "Last Name"}
                """.formatted(secondId));
    }
}