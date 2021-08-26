package com.topicallocation.topic.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Document(collection = "User")
public class User {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String firstName;
	
	private String lastName;
	
	private String email;
	  
	private String username;

	@JsonIgnore
	private String password;

	private String fullName;

	private Role role;

	private String mobile;
	
	private Integer otp;

	private Boolean isAgreeTerms;

	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private Boolean isEnabled;
	
	private String userId;
}
