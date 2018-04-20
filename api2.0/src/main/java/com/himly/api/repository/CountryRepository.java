package com.himly.api.repository;


import com.himly.api.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    Country findByName(String name);
}
