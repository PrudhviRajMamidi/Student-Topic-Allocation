package com.topicallocation.topic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.Feature;

public interface FeatureRepository extends MongoRepository<Feature, String>{

	Feature findByName(String name);

}
