package com.topicallocation.topic.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.User;

public interface UserRepository extends MongoRepository<User, String>{

	User findByFirstNameAndLastName(String firstName, String lastName);

	User findByUserId(String id);

	List<User> findByRole_RoleId(Integer roleId);

	User findByUsername(String username);

	User findByUsernameAndPassword(String username, String password);

}
