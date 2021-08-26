package com.topicallocation.topic.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.Topic;

public interface TopicRepository extends MongoRepository<Topic, String>{

	Topic findByName(String name);

	Topic findByTopicId(String id);

	List<Topic> findByIsApproved(boolean b);

}
