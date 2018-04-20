package com.himly.api.model;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @Data注解会导致栈溢出
 * @Getter ,@Setter 替代@Data,详情见https://blog.csdn.net/zhanlanmg/article/details/50392266
 * 重写toString,hashcode,equals方法
 */

@Entity
@Table(name = "role")
@Data
//@ToString(exclude = "users")
//@EqualsAndHashCode(exclude = "users")
public class Role implements Serializable{

    private static final Long UID = 1354564646565L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;


    @ManyToMany(fetch = FetchType.EAGER,mappedBy = "roles")
    private  Set<User> users = new HashSet<>(16);
}
