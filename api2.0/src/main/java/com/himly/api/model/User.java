package com.himly.api.model;


import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author himly z1399956473@gmail.com
 *
 * @Getter @Setter 替代@Data,详情见https://blog.csdn.net/zhanlanmg/article/details/50392266
 * 重写toString,hashcode,equals方法
 */


@Entity
@Table(name = "user")
@Data
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
public class User implements Serializable{

    private static final Long UID = 13165454611654L;

    public static final Integer STUDENT = 0;

    public static final Integer TEACHER = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer type;

    @Column(nullable = false)
    private String account;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "role_id"))
    private  Set<Role> roles = new HashSet<>(16);

}
