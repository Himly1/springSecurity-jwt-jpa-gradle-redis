package com.himly.api.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 *@author himly z1399956473@gmail.com
 */
@Entity
@Data
@Table(name = "thrid_party_account")
public class ThirdPartyAccount implements Serializable{

    @Transient
    private static final Long UID = 1354654654645L;

    @Transient
    public static final Integer STUDENT = 0;

    @Transient
    public static final Integer TEACHER = 1;

    @Transient
    public static final Integer ADMIN_MANAGER= 2;

    @Transient
    public static final Integer CAS = 0;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long targetId;

    private Integer targetType;

    private String account;

    private Integer accountType;

    private Long casServerTargetId;
}
