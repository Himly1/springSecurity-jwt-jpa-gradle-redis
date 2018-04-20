package com.himly.api.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Continent continent;

    @Column(length = 3)
    private String code;

    private String name;
}
