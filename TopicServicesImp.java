package com.topicallocation.topic.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mongodb.client.model.Collation;
import com.topicallocation.topic.Dto.AlgorithmDto;
import com.topicallocation.topic.Dto.AlgorithmInputDto;
import com.topicallocation.topic.Dto.PreferenceDto;
import com.topicallocation.topic.exception.CommonExceptionAll;
import com.topicallocation.topic.exception.IsTopicException;
import com.topicallocation.topic.model.Sequence;
import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.TopicPreference;
import com.topicallocation.topic.model.User;
import com.topicallocation.topic.repository.PreferenseRepository;
import com.topicallocation.topic.repository.SequenceRepository;
import com.topicallocation.topic.repository.TopicRepository;
import com.topicallocation.topic.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TopicServicesImp implements TopicService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SequenceRepository sequenceRepository;

	@Autowired
	private TopicRepository topicrepository;

	@Autowired
	private PreferenseRepository preferencerepository;

	@Override
	public ResponseEntity<Object> addTopic(Topic topic) {
		// TODO Auto-generated method stub
		Topic dbTopic = topicrepository.findByName(topic.getName());
		if (dbTopic != null) {
			throw new IsTopicException("Topic name already exist");
		}
		// log.info(topic.toString());
		log.info(topic.toString());
		User user = userRepository.findByUserId(topic.getUser().getUserId());
		topic.setTopicId(generateTopicId());
		topic.setUser(user);
		if (topic.getUser().getRole().getName().equals("ADMIN")) {
			topic.setIsApproved(true);
		} else {
			topic.setIsApproved(false);
		}

		topicrepository.save(topic);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}").buildAndExpand(topic.getName())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	private String generateTopicId() {
		String tId = "UFL-T000";
		synchronized (this) {

			Sequence sequence = sequenceRepository.findByName("TopicId");
			tId = tId + sequence.getValue();
			sequence.setValue(sequence.getValue() + 1);
			sequenceRepository.save(sequence);

		}
		return tId;
	}

	@Override
	public ResponseEntity<Object> updateTopic(Topic topic) {
		// TODO Auto-generated method stub
		Topic dbTopic = topicrepository.findByTopicId(topic.getTopicId());
		if (dbTopic == null)
			throw new IsTopicException("Topic not present");
		Topic dbTopic3 = topicrepository.findByName(topic.getName());
		if (dbTopic3 != null)
			throw new IsTopicException("Topic already added : " + dbTopic3.getName().toUpperCase());

		dbTopic.setName(topic.getName());
		dbTopic.setUser(topic.getUser());
		dbTopic.setDescrption(topic.getDescrption());
		topicrepository.save(dbTopic);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}").buildAndExpand(topic.getName())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	@Override
	public ResponseEntity<String> deleteTopic(String topicId) {
		// TODO Auto-generated method stub
		Topic dbTopic = topicrepository.findByTopicId(topicId);
		if (dbTopic == null)
			throw new IsTopicException("Topic not present : " + topicId);
		topicrepository.delete(dbTopic);
		return new ResponseEntity<String>("Topic successfully deleted ", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<List<Topic>> getTopics() {
		// TODO Auto-generated method stub

		List<Topic> topics = topicrepository.findByIsApproved(true);
		return new ResponseEntity<List<Topic>>(topics, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Topic>> getpendingTopics() {
		// TODO Auto-generated method stub
		List<Topic> topics = topicrepository.findByIsApproved(false);
		return new ResponseEntity<List<Topic>>(topics, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> approveTopic(Topic topic) {
		// TODO Auto-generated method stub

		Topic dbTopic = topicrepository.findByTopicId(topic.getTopicId());
		if (dbTopic == null) {
			throw new IsTopicException("Topic not found ");
		}
		dbTopic.setIsApproved(true);
		topicrepository.save(dbTopic);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Object> approveTopics(List<Topic> topics) {
		List<Topic> approvedTopics = new ArrayList<Topic>();
		for (Topic topic : topics) {
			Topic dbTopic = topicrepository.findByTopicId(topic.getTopicId());
			if (dbTopic == null) {
				throw new IsTopicException("Topic not found : " + topic.getName());
			}
			dbTopic.setIsApproved(true);
			approvedTopics.add(dbTopic);
		}
		topicrepository.saveAll(approvedTopics);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Object> rejectTopics(List<Topic> topics) {
		// TODO Auto-generated method stub
		for (Topic topic : topics) {
			Topic dbTopic = topicrepository.findByTopicId(topic.getTopicId());
			if (dbTopic == null) {
				throw new IsTopicException("Topic not found : " + topic.getName());
			}
			topicrepository.delete(dbTopic);
		}
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Object> addPreference(List<TopicPreference> preferences) {
		// TODO Auto-generated method stub
		List<TopicPreference> t = new ArrayList<TopicPreference>();
		if (preferences.size() > 0) {
			for (TopicPreference preference : preferences) {

				TopicPreference newPreferense = new TopicPreference();
				TopicPreference dbPreference = preferencerepository.findByTopicIdAndUserId(preference.getTopicId(),
						preference.getUserId());
				if (dbPreference != null && dbPreference.preference == preference.preference) {
					preferencerepository.delete(dbPreference);
				}

				newPreferense.setDate(LocalDateTime.now());
				newPreferense.setUserId(preference.getUserId());
				newPreferense.setTopicId(preference.getTopicId());
				newPreferense.setPreference(preference.getPreference());

				t.add(newPreferense);

			}
			preferencerepository.saveAll(t);

		}
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<List<PreferenceDto>> getPreference(String userId) {
		// TODO Auto-generated method stub
		User dbUser = userRepository.findByUserId(userId);
		if (dbUser == null) {
			throw new CommonExceptionAll("User not found");
		}
		List<PreferenceDto> preferences = new ArrayList<PreferenceDto>();
		List<TopicPreference> dbPreferences = preferencerepository.findByUserId(dbUser.getUserId());
		if (dbPreferences.size() > 0) {
			for (TopicPreference p : dbPreferences) {
				PreferenceDto preference = new PreferenceDto();
				Topic t = topicrepository.findById(p.getTopicId()).get();
				preference.setPreference(p.getPreference());
				preference.setTopic(t);
				preference.setUser(dbUser);
				preferences.add(preference);
			}

		}

		return new ResponseEntity<List<PreferenceDto>>(preferences, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updatePreference(List<TopicPreference> preferences) {
		// TODO Auto-generated method stub

		List<TopicPreference> t = new ArrayList<TopicPreference>();
		if (preferences.size() > 0) {
			for (TopicPreference preference : preferences) {

				TopicPreference newPreferense = new TopicPreference();
				TopicPreference dbPreference = preferencerepository.findByTopicIdAndUserId(preference.getTopicId(),
						preference.getUserId());
				if (dbPreference != null) {
					preferencerepository.delete(dbPreference);
					newPreferense.setDate(LocalDateTime.now());
					newPreferense.setUserId(preference.getUserId());
					newPreferense.setTopicId(preference.getTopicId());
					newPreferense.setPreference(preference.getPreference());
				}

				t.add(newPreferense);

			}
			preferencerepository.saveAll(t);

		}
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<?> runAlgorithm(AlgorithmInputDto algorthimInputDto) {
		// TODO Auto-generated method stub
		String groupName = "group";
		Integer groupCount = 1;
	      //List<User> allStudents = userRepository.findByRole_RoleId(2);
	     List<User> allocatedStudents = new ArrayList<User>();
        List<AlgorithmDto> groups = new ArrayList<AlgorithmDto>();
		List<Topic> dbtopics = topicrepository.findByIsApproved(true);
		
		for(Topic topic : dbtopics) {
			List<TopicPreference> firstPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),1);
			List<TopicPreference> secondPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),2);
			List<TopicPreference> thirdPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),3);
			List<TopicPreference> fourthPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),4);
			AlgorithmDto algo = new AlgorithmDto();
			List<User> students = new ArrayList<User>();
		if(firstPreferences.size() > 0) {	
		Collections.sort(firstPreferences);
			for(TopicPreference p1 : firstPreferences) {
				User user = userRepository.findByUserId(p1.getUserId());
				if(students.size() <= algorthimInputDto.getNoOfStudentsingroup()) {
					if(user.getRole().getName().equals("STUDENT")){
						students.add(user);
						allocatedStudents.add(user);
					}
					else {
						if(!CheckAssignedGroupsForSupervisor(groups,user,algorthimInputDto.getNoOfgroupsCanSupervisorMonitor())) {
							if(algo.getSupervisor() != null)
							algo.setSupervisor(user);
							
						}
					}
				}else {
					break;
				}
				
			}
		}
		if(secondPreferences.size() > 0) {
			Collections.sort(secondPreferences);
			for(TopicPreference p2 : secondPreferences) {
				
				User user = userRepository.findByUserId(p2.getUserId());
				if(students.size() <= algorthimInputDto.getNoOfStudentsingroup()) {
					if(user.getRole().getName().equals("STUDENT")){
						students.add(user);
						allocatedStudents.add(user);
					}
					else {
						if(!CheckAssignedGroupsForSupervisor(groups,user,algorthimInputDto.getNoOfgroupsCanSupervisorMonitor())) {
							if(algo.getSupervisor() != null)
							algo.setSupervisor(user);
							
						}
					}
				}else {
					break;
				}
				
			}
		}
		if(thirdPreferences.size() > 0) {
			Collections.sort(thirdPreferences);
			for(TopicPreference p3 : thirdPreferences) {
				User user = userRepository.findByUserId(p3.getUserId());
				if(students.size() <= algorthimInputDto.getNoOfStudentsingroup()) {
					if(user.getRole().getName().equals("STUDENT")){
						students.add(user);
						allocatedStudents.add(user);
					}
					else {
						if(!CheckAssignedGroupsForSupervisor(groups,user,algorthimInputDto.getNoOfgroupsCanSupervisorMonitor())) {
							if(algo.getSupervisor() != null)
							algo.setSupervisor(user);
							
						}
					}
				}else {
					break;
				}
				
			}
		}
			
			if(fourthPreferences.size() > 0) {
				Collections.sort(fourthPreferences);
			for(TopicPreference p4 : fourthPreferences) {
				User user = userRepository.findByUserId(p4.getUserId());
				if(students.size() <= algorthimInputDto.getNoOfStudentsingroup()) {
					if(user.getRole().getName().equals("STUDENT")){
						students.add(user);
						allocatedStudents.add(user);
					}
					else {
						if(!CheckAssignedGroupsForSupervisor(groups,user,algorthimInputDto.getNoOfgroupsCanSupervisorMonitor())) {
							if(algo.getSupervisor() != null)
							algo.setSupervisor(user);
							
						}
					}
				}else {
					break;
				}
				
			}
			}
		
		if(students.size() > 0 ) {
			algo.setGroupName(groupName+groupCount);
			groupCount ++;
			algo.setStudents(students);	
			algo.setTopic(topic);
			groups.add(algo);
		}
		
		}
		
		List<User> unassignedStudents = getUnAssignedStudents(allocatedStudents);
		groups = assignTopicForUnassignedStudents(groups,unassignedStudents,algorthimInputDto.getNoOfStudentsingroup());
		return new ResponseEntity<List<AlgorithmDto>>(groups,HttpStatus.OK);
	}
	
	public List<User> getUnAssignedStudents(List<User> allocatedStudents){
		List<User> students = userRepository.findByRole_RoleId(2);
		if(students.size() > 0 && allocatedStudents.size() > 0) {
			students.removeAll(allocatedStudents);
		}
		return students;
	}
	
	public List<AlgorithmDto> assignTopicForUnassignedStudents(List<AlgorithmDto> groups,List<User> students,Integer maxStudentsInGroup){
		for(User student : students ) {
			for(AlgorithmDto group : groups) {
				if(group.getStudents().size() < maxStudentsInGroup ) {
					group.getStudents().add(student);
				}
				
			}
		}
		
		
		return groups;
		
	}
	
	public boolean CheckAssignedGroupsForSupervisor(List<AlgorithmDto> groups,User suprvisor,Integer maxgroupsSupervisorCanMonitor){
		Boolean assignedAsAllocated = false;
		if(groups.size() > 0) {
			List<AlgorithmDto> monitoredGroups = new ArrayList<AlgorithmDto>();
			for(AlgorithmDto group : groups) {
				if(group.getSupervisor() != null) {
					if(group.getSupervisor().getUserId().equals(suprvisor.getUserId())){
						monitoredGroups.add(group);
					}
				}
				
			}
			//List<AlgorithmDto> monitoredGroups = groups.stream().filter(g -> g.getSupervisors().getUserId().equals(Suprvisor.getUserId())).collect(Collectors.toList());
			if(monitoredGroups.size() == maxgroupsSupervisorCanMonitor) {
				assignedAsAllocated = true;
			}
		}
		
		return assignedAsAllocated;
	}

}
