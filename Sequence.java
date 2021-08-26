package com.topicallocation.topic.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@Document(collection = "sequence")
public class Sequence {
	
	@Id
	private String id;

	private String name;

	private Integer value;

}
