package com.himly.api.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Country country;
}
