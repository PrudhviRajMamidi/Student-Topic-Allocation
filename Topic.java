package com.topicallocation.topic.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document(collection = "Topic")
public class Topic {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	private String topicId;
	private String name;
	private String descrption;
	private Boolean isApproved;
	private User user;
	
}
