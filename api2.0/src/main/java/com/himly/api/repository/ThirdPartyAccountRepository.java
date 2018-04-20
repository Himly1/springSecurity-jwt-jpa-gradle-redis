package com.himly.api.repository;

import com.himly.api.model.ThirdPartyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * create_at:MaZheng
 * create_by:${date} ${time}
 */
public interface ThirdPartyAccountRepository extends JpaRepository<ThirdPartyAccount,Long>{

    ThirdPartyAccount findByAccountAndAccountType(String account,Integer accountType);
}
