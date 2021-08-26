package com.topicallocation.topic.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.TopicPreference;

public interface PreferenseRepository extends MongoRepository<TopicPreference,String>{

	TopicPreference findByTopicIdAndUserId(String topic_id, String user_id);

	List<TopicPreference> findByUserId(String userId);

	List<TopicPreference> findByTopicId(String topicId);

	List<TopicPreference> findByTopicIdAndPreference(String topicId, Integer i);

}
