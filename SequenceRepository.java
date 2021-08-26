package com.topicallocation.topic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.topicallocation.topic.model.Sequence;

public interface SequenceRepository extends MongoRepository<Sequence, String>{

	Sequence findByName(String string);

}
