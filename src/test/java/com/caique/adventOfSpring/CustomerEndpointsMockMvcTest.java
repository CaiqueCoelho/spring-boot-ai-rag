package com.caique.adventOfSpring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouterConfiguration.class)
@Import(CustomerHandler.class)
class CustomerEndpointsMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    void getAllCustomers() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        when(customerRepository.findAll()).thenReturn(List.of(new Customer(firstId, firstName, lastName)));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value(firstName))
                .andExpect(jsonPath("$[0].lastName").value(lastName))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()));
    }

    @Test
    void getCustomerById() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        when(customerRepository.findById(firstId)).thenReturn(Optional.of(new Customer(firstId, firstName, lastName)));

        mockMvc.perform(get("/api/customers/" + firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.id").value(firstId.toString()));
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

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","firstName":"%s","lastName":"%s"}
                                """.formatted(firstId, firstName, lastName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.id").value(secondId.toString()));
    }

    @Test
    void deleteCustomer() throws Exception {
        UUID firstId = UUID.randomUUID();
        String firstName = "First Name";
        String lastName = "Last Name";

        mockMvc.perform(delete("/api/customers/" + firstId))
                .andExpect(status().isNoContent());
    }
}
