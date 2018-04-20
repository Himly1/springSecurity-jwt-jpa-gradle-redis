package com.himly.api.repository;

import com.himly.api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 *
 * @author himly z1399956473@gmail.com
 *
 * 无需实现,无需编写sql,sql由方法名动态生成
 */
public interface RoleRepository extends JpaRepository<Role,Long>{

    Role getByRole(String role);

    List<Role> findAllByRole(String role);
}
