package com.topicallocation.topic.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document(collection = "Student")
public class Student {

  private String firstName;
	
  private String lastName;
  
  
}
