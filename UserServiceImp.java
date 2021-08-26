package com.topicallocation.topic.service;

import java.net.URI;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import com.topicallocation.topic.Dto.Userdto;
import com.topicallocation.topic.exception.CommonExceptionAll;
import com.topicallocation.topic.exception.IsUserException;
import com.topicallocation.topic.exceptionHandler.CustomException;
import com.topicallocation.topic.model.Feature;
import com.topicallocation.topic.model.Role;
import com.topicallocation.topic.model.Sequence;
import com.topicallocation.topic.model.User;
import com.topicallocation.topic.repository.FeatureRepository;
import com.topicallocation.topic.repository.RoleRepository;
import com.topicallocation.topic.repository.SequenceRepository;
import com.topicallocation.topic.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImp implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SequenceRepository sequenceRepository;

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private FeatureRepository featureRepository;
	

	@Override
	public ResponseEntity<Object> save(User user) {
		User dbuser = userRepository.findByFirstNameAndLastName(user.getFirstName(), user.getLastName());
		if (dbuser != null) {
			throw new CommonExceptionAll(user.getRole().getName() + " " + "already exist : " + dbuser.getUserId());
		}
		user.setFullName(user.getFirstName() + " " + user.getLastName());
		user.setIsAccountNonExpired(true);
		user.setRole(roleRepository.findByName(user.getRole().getName()));
		user.setIsAccountNonLocked(true);
		user.setIsEnabled(true);
		user.setUsername(generateUserName(Character.toString(user.getFirstName().charAt(0))+ user.getLastName().charAt(0)));
		user.setUserId(generateUserId());
		user.setPassword(generatePassword());
		userRepository.save(user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{fullName}").buildAndExpand(user.getFullName())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	private String generateUserId() {
		String uId = "UFL-U000";
		synchronized (this) {

			Sequence sequence = sequenceRepository.findByName("UserId");
			uId = uId + sequence.getValue();
			sequence.setValue(sequence.getValue() + 1);
			sequenceRepository.save(sequence);

		}
		return uId;
	}

	private String generateId(String role) {
		String uId = "UFL-U000";
		synchronized (this) {

			Sequence sequence = sequenceRepository.findByName("UserId");
			uId = uId + sequence.getValue();
			sequence.setValue(sequence.getValue() + 1);
			sequenceRepository.save(sequence);

		}
		return uId;
	}

	private String generateUserName(String name) {
		return name.toUpperCase() + generatePassword();
	}

	private String generatePassword() {
		String code = "";
		Random rand = new Random();// Generate random numbers.
		synchronized (this) {
			for (int a = 0; a < 6; a++) {
				code += rand.nextInt(10);// Generate 6-digit verification code.
			}
		}
		return code;
	}

	@Override
	public ResponseEntity<?> getUserById(String id) {
		return new ResponseEntity<User>(userRepository.findByUserId(id),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<User> modifyUser(String id, User user) {
		// TODO Auto-generated method stub
	  User dbUser = userRepository.findByUserId(id);
	   if(dbUser == null) {
		   throw new IsUserException("user not found please reach admin");   
	   }
	   dbUser.setFirstName(user.getFirstName());
	   dbUser.setLastName(user.getLastName());
	   dbUser.setEmail(user.getEmail());
	   dbUser.setFullName(user.getFirstName() + " "+ user.getLastName());
	   userRepository.save(dbUser);
		
		return new ResponseEntity<User>(dbUser,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deleteUser(String id) {
		// TODO Auto-generated method stub
		User dbUser = userRepository.findByUserId(id);
		   if(dbUser == null) {
			   throw new IsUserException("user not found please reach admin");   
		   }
		   userRepository.delete(dbUser);
		   
		   return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<List<User>> getUsersByRole(String roleName) {
		// TODO Auto-generated method stub
		Role role = roleRepository.findByName(roleName);
		//log.info("role found : "+role.getRoleId());
		
		if(role == null) {
			   throw new CommonExceptionAll("Something went wrong please reach technical team");   
		   }
		List<User> users = userRepository.findByRole_RoleId(role.getRoleId());
		return new ResponseEntity<List<User>>(users,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> LoginUser(Userdto user) {
		User dbUser = userRepository.findByUsernameAndPassword(user.getUsername(),user.getPassword());
		//log.info("user name : "+user.getUsername() + "paassword  : "+user.getPassword());
		   if(dbUser == null) {
			   throw new CommonExceptionAll("Inavalid user name or password");   
		   }
		   
		return new ResponseEntity<User>(dbUser,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Feature> enableAccess(List<Feature> feature) {
	
		
		for(Feature f : feature) {
			Feature dbFeature = featureRepository.findByName(f.getName());
			if(dbFeature == null) {
				   throw new IsUserException("feature does not exist");   
			   }
			dbFeature.setIsEnabled(f.getIsEnabled());
			
			featureRepository.save(dbFeature);
		}
		
		// TODO Auto-generated method stub
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<?> getFeature() {
		// TODO Auto-generated method stub
		
		List<Feature> features = featureRepository.findAll();
		return new ResponseEntity<List<Feature>>(features,HttpStatus.OK);
	}

	

	

}
