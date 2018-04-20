package com.himly.api.model;

import lombok.Data;

import javax.persistence.*;

/**
 *
 */
@Entity
@Data
@Table(name = "cas_server")
public class CasServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Integer target_type;

    private Long target_id;

    private String cas_server_location;
}
