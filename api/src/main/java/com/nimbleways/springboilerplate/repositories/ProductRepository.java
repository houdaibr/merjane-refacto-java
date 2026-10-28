package com.nimbleways.springboilerplate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nimbleways.springboilerplate.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
