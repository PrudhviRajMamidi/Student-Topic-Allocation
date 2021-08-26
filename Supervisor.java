package com.topicallocation.topic.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document(collection = "Supervisor")
public class Supervisor {

	 private String firstName;
	
	  private String lastName;
}
