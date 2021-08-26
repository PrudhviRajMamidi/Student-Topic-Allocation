package com.topicallocation.topic.Dto;

import java.util.List;

import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.User;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class AlgorithmDto {
	
	public Topic topic;
	public List<User> students;
	public User supervisor;
	public String groupName;

}
