package com.caique.adventOfSpring;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface CustomerRepository extends ListCrudRepository<Customer, UUID> {
}
