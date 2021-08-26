package com.topicallocation.topic.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Data
public class TopicPreference implements Comparable<TopicPreference>{
	
	@Id
	public String id;
	
	public String topicId;
	
	public String userId;
	
	public Integer preference;
	
	public LocalDateTime date;

	@Override
	public int compareTo(TopicPreference o) {
		return getDate().compareTo(o.getDate());
	}
	
    

}
