package com.topicallocation.topic.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.topicallocation.topic.Dto.AlgorithmInputDto;
import com.topicallocation.topic.Dto.PreferenceDto;
import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.TopicPreference;
import com.topicallocation.topic.service.TopicService;

@RestController
@RequestMapping("/topic")
public class TopicController {

	
	@Autowired
	private TopicService topicService;
	
	@PostMapping
	private ResponseEntity<Object> addTopic(@RequestBody Topic topic){
		return  topicService.addTopic(topic);
	}
	
	@PutMapping
	private ResponseEntity<Object> updateTopic(@RequestBody Topic topic){
		return  topicService.updateTopic(topic);
	}
	
	@DeleteMapping("/{id}")
	private ResponseEntity<String> deleteTopic(@PathVariable String topcId){
		 return topicService.deleteTopic(topcId);
	}
	
	@GetMapping
	private ResponseEntity<List<Topic>> getTopics(){
		 return topicService.getTopics();
	}
	
	@GetMapping("/pending")
	private ResponseEntity<List<Topic>> getpendingTopics(){
		 return topicService.getpendingTopics();
	}
	
	@PostMapping("/approve")
	private ResponseEntity<Object> approveTopic(@RequestBody Topic topic){
		return  topicService.approveTopic(topic);
	}
	
	@PostMapping("/approvetopics")
	private ResponseEntity<Object> approveTopics(@RequestBody List<Topic> topics){
		return  topicService.approveTopics(topics);
	}
	
	@PostMapping("/rejecttopics")
	private ResponseEntity<Object> rejectTopics(@RequestBody List<Topic> topics){
		return  topicService.rejectTopics(topics);
	}
	
	@PostMapping("/preference")
	private ResponseEntity<Object> addPreference(@RequestBody List<TopicPreference> preferences){
		return  topicService.addPreference(preferences);
	}
	
	@GetMapping("/preference/{userId}")
	private ResponseEntity<List<PreferenceDto>> getPreference(@PathVariable String userId){
		return  topicService.getPreference(userId);
	}
	
	@PutMapping("/preference")
	private ResponseEntity<Object> updatePreference(@RequestBody List<TopicPreference> preferences){
		return  topicService.updatePreference(preferences);
	}
	
	@PostMapping("/algorithm")
	private ResponseEntity<?> runAlgorithm(@RequestBody AlgorithmInputDto algorthimInputDto){
		return  topicService.runAlgorithm(algorthimInputDto);
	}
	
}
