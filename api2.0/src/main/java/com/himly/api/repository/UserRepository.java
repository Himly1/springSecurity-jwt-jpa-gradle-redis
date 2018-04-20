package com.himly.api.repository;

import com.himly.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;



/**
 * @author himly z1399956473@gmail.com
 */
public interface UserRepository extends JpaRepository<User,Long>{

    User findByName(String name);

    User findByNameAndId(String name,Long id);

    User findByAccount(String account);
}
