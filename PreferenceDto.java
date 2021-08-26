package com.topicallocation.topic.Dto;

import java.util.List;

import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreferenceDto {
	
	private Topic  topic;
	
	private User user;
	
    private Integer preference;
	
	
	
	

}
