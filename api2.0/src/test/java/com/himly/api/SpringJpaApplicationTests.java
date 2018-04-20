package com.himly.api;

import com.himly.api.model.Role;
import com.himly.api.model.User;
import com.himly.api.repository.RoleRepository;
import com.himly.api.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringJpaApplicationTests {

	@Autowired
	UserRepository  userRepository;


	@Autowired
	RoleRepository roleRepository;

	@Autowired
	RedisTemplate<String,String> redisTemplate;



	@Test
	public void contextLoads() {
	}




//	@Test
//	public void userRepositoryTest() throws Exception{
//
//		Role role = new Role();
//		role.setRole("ROLE_ADMIN");
//
//		role = roleRepository.save(role);
//
//		User user = new User();
//
//		user.setName("cj");
//		user.setPassword("1234");
//
//		user.getRoles().add(role);
//		userRepository.save(user);
//	}
//
//	@Test
//	public void roleRepositoryTest() throws Exception{
//
//		List<Role> roles = roleRepository.findAllByRole("ROLE_ADMIN");
//
//		System.out.println("result is=="+roles);
//	}


//	@Test
//	public void redisTest() {
//
////		Long size = redisTemplate.opsForList().size("sdfsf");
//
//		redisTemplate.opsForList().leftPush("sss","ssssss");
//
////		System.out.println(size);
//
//	}
}
