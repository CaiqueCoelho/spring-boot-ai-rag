package com.caique.adventOfSpring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Component
public class CustomerHandler {

    @Autowired
    private CustomerRepository customerRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerHandler.class);

    private final MeterRegistry meterRegistry;

    private final Counter callCounter;

    public CustomerHandler(@Autowired(required = false) MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();

        Counter registeredCounter = Counter.builder("get-all-customers")
                .description("Tells how many times the get all customers endpoint has being called since startup")
                .register(this.meterRegistry);

        this.callCounter = registeredCounter != null ? registeredCounter : Counter.builder("get-all-customers")
                .description("Tells how many times the get all customers endpoint has being called since startup")
                .register(new SimpleMeterRegistry());
    }

    public ServerResponse getAll() {
        LOGGER.info("This is a info log");
        LOGGER.debug("This is a debug log");
        LOGGER.warn("This is a warn log");
        LOGGER.error("This is a error log");
        callCounter.increment();

        return ServerResponse.ok().body(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ServerResponse getById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return ServerResponse.ok().body(customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id)));
    }

    @DeleteMapping("/{id}")
    public ServerResponse deleteById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        customerRepository.deleteById(id);
        return ServerResponse.noContent().build();
    }

    @PostMapping
    public ServerResponse createCustomer(ServerRequest request) throws ServletException, IOException {
        Customer customer = request.body(Customer.class);
        var createdCustomer = customerRepository.save(customer);
        return ServerResponse.created(URI.create("/api/customers/" + createdCustomer.getId())).body(createdCustomer);
    }
}