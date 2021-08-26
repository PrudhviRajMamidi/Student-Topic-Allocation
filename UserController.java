package com.topicallocation.topic.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.topicallocation.topic.Dto.Userdto;
import com.topicallocation.topic.model.Feature;
import com.topicallocation.topic.model.User;
import com.topicallocation.topic.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping
	ResponseEntity<Object> saveUser(@RequestBody User user) {
		return userService.save(user);

	}

	@GetMapping("/{id}")
	ResponseEntity<?> getUserByid(@PathVariable String id){
		return userService.getUserById(id);

	}
	
	@PutMapping("/{id}")
	ResponseEntity<User> modifyUser(@PathVariable String id,@RequestBody User user){
		return userService.modifyUser(id,user);

	}
	
	@DeleteMapping("/{id}")
	ResponseEntity<?> deleteUser(@PathVariable String id){
		return userService.deleteUser(id);

	}
	
	@GetMapping("/roleName/{roleName}")
	ResponseEntity<?> getUsersByRole(@PathVariable String roleName){
		return userService.getUsersByRole(roleName);

	}
	
	
	@PostMapping("/login")
	ResponseEntity<?> LoginUser(@RequestBody Userdto user){
		return userService.LoginUser(user);

	}
	
	@PostMapping("/feature")
	ResponseEntity<?> enableAccess(@RequestBody List<Feature> feature){
		return userService.enableAccess(feature);

	}
	
	@GetMapping("/feature")
	ResponseEntity<?> getFeature(){
		return userService.getFeature();

	}


}
