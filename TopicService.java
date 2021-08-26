package com.topicallocation.topic.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.topicallocation.topic.Dto.AlgorithmInputDto;
import com.topicallocation.topic.Dto.PreferenceDto;
import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.TopicPreference;

public interface TopicService {

	ResponseEntity<Object> addTopic(Topic topic);

	ResponseEntity<Object> updateTopic(Topic topic);

	ResponseEntity<String>  deleteTopic(String topcId);

	ResponseEntity<List<Topic>> getTopics();

	ResponseEntity<List<Topic>> getpendingTopics();

	ResponseEntity<Object> approveTopic(Topic topic);

	ResponseEntity<Object> approveTopics(List<Topic> topics);

	ResponseEntity<Object> rejectTopics(List<Topic> topics);

	ResponseEntity<Object> addPreference(List<TopicPreference> preferences);

	ResponseEntity<List<PreferenceDto>> getPreference(String userId);

	ResponseEntity<Object> updatePreference(List<TopicPreference> preferences);

	ResponseEntity<?> runAlgorithm(AlgorithmInputDto algorthimInputDto);

}
