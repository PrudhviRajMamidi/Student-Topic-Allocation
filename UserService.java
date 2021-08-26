package com.topicallocation.topic.service;

import java.util.List;

import org.springframework.http.ResponseEntity;


import com.topicallocation.topic.Dto.Userdto;
import com.topicallocation.topic.model.Feature;
import com.topicallocation.topic.model.User;

public interface UserService {

	ResponseEntity<Object> save(User user);

	ResponseEntity<?> getUserById(String id);

	ResponseEntity<User> modifyUser(String id, User user);

	ResponseEntity<?> deleteUser(String id);

	ResponseEntity<?> getUsersByRole(String roleName);

	ResponseEntity<?> LoginUser(Userdto user);

	ResponseEntity<Feature> enableAccess(List<Feature> feature);

	ResponseEntity<?> getFeature();

}
