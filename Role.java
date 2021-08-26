package com.topicallocation.topic.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Document(collection = "Role")
public class Role {

	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;

	private Integer roleId;

	private String name;
	
	private String displayName;
}
