package com.topicallocation.topic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.Role;

public interface RoleRepository extends MongoRepository<Role, String>{

	Role findByName(String name);

}
