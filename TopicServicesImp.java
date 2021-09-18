package com.topicallocation.topic.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.topicallocation.topic.Dto.AlgorithmDto;
import com.topicallocation.topic.Dto.AlgorithmInputDto;
import com.topicallocation.topic.Dto.PreferenceDto;
import com.topicallocation.topic.Dto.userPreferenceDto;
import com.topicallocation.topic.exception.CommonExceptionAll;
import com.topicallocation.topic.exception.IsTopicException;
import com.topicallocation.topic.model.Group;
import com.topicallocation.topic.model.Sequence;
import com.topicallocation.topic.model.Topic;
import com.topicallocation.topic.model.TopicPreference;
import com.topicallocation.topic.model.User;
import com.topicallocation.topic.repository.GroupsRepository;
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
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private GroupsRepository groupRepository;

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
			throw new IsTopicException("Topic does not  present : " + topicId);
		topicrepository.delete(dbTopic);
		return new ResponseEntity<String>("Topic successfully deletd ", HttpStatus.OK);

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
		// send email for approval or rejection 
		for(Topic topic: topics) {
			emailService.sendTopicApprovalMail(topic);
		}
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
			emailService.sendTopicApprovalMail(topic);
			// send email for approval or rejection
		}
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Object> addPreference(List<TopicPreference> preferences) {
		// TODO Auto-generated method stub
		List<TopicPreference> t = new ArrayList<TopicPreference>();
		if (preferences.size() > 0) {
			for (TopicPreference preference : preferences) {
                  TopicPreference userPreferense = preferencerepository.findByUserIdAndPreference(preference.getUserId(), preference.getPreference());
                  if(userPreferense != null) {
                	  preferencerepository.delete(userPreferense);
                  }
				TopicPreference newPreferense = new TopicPreference();
				newPreferense.setDate(LocalDateTime.now());
				newPreferense.setUserId(preference.getUserId());
				newPreferense.setTopicId(preference.getTopicId());
				newPreferense.setPreference(preference.getPreference());
                if(preference.getUserId() != null && preference.getTopicId() != null) {
                	t.add(newPreferense);
                }
				

			}
			preferencerepository.saveAll(t);

		}
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<List<userPreferenceDto>> getPreference(String userId) {
		// TODO Auto-generated method stub
		User dbUser = userRepository.findByUserId(userId);
		if (dbUser == null) {
			throw new CommonExceptionAll("User not found");
		}
		List<userPreferenceDto> preferences = new ArrayList<userPreferenceDto>();
		List<TopicPreference> dbPreferences = preferencerepository.findByUserId(dbUser.getUserId());
		if (dbPreferences.size() > 0) {
			for (TopicPreference p : dbPreferences) {
				userPreferenceDto preference = new userPreferenceDto();
				Topic t = topicrepository.findById(p.getTopicId()).get();
				preference.setPreference(p.getPreference());
				preference.setTopic(t);
				preference.setUser(dbUser);
				preferences.add(preference);
			}

		}

		return new ResponseEntity<List<userPreferenceDto>>(preferences, HttpStatus.OK);
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

	
	
	public List<User> getUnAssignedStudents(List<User> allocatedStudents){
		List<User> students = userRepository.findByRole_RoleId(2);
		if(students.size() > 0 && allocatedStudents.size() > 0) {
			students.removeAll(allocatedStudents);
		}
		return students;
	}
	
	public List<AlgorithmDto> assignTopicForUnassignedStudents(List<AlgorithmDto> groups,List<User> students,Integer maxStudentsInGroup){
		List<User> allocatedStudents = new ArrayList<User>();
		for(User student : students ) {
			for(AlgorithmDto group : groups) {
				if(group.getStudents().size() < maxStudentsInGroup ) {
					if(!allocatedStudents.contains(student)) {
						group.getStudents().add(student);
						allocatedStudents.add(student);
					}
					
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
			if(monitoredGroups.size() == maxgroupsSupervisorCanMonitor) {
				assignedAsAllocated = true;
			}
		}
		
		return assignedAsAllocated;
	}
	
	public List<AlgorithmDto> assignsupervisorForUnassignedGroups(List<AlgorithmDto> groups,Integer maxNogroupsSupervisorcanManage,List<User> allocatedSupervisors){
     	List<User> Supervisors = userRepository.findByRole_RoleId(3);
		Supervisors.remove(allocatedSupervisors);
     	for(AlgorithmDto group : groups) {
     		if(group.getSupervisor() == null) {
     			for(User supervisor : Supervisors) {
     				List<AlgorithmDto> allacoted = groups.stream().filter(g -> g.getSupervisor() != null && g.getSupervisor().equals(supervisor)).collect(Collectors.toList());
     				if(allacoted.size() < maxNogroupsSupervisorcanManage ) {
     					group.setSupervisor(supervisor);
     				}
     			}
     		}
     	}
		return groups;
		
	}
	
	
	public List<PreferenceDto> getPreferenceDtoList(){
        List<Topic> dbtopics = topicrepository.findByIsApproved(true);
        List<PreferenceDto> preferencesByTopic = new ArrayList<PreferenceDto>();
        for(Topic topic : dbtopics) {
        	boolean isElgabel = false;
        	PreferenceDto preferenceDto = new PreferenceDto();
        	preferenceDto.setTopic(topic);
			List<TopicPreference> firstPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),1);
			List<TopicPreference> secondPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),2);
			List<TopicPreference> thirdPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),3);
			List<TopicPreference> fourthPreferences =preferencerepository.findByTopicIdAndPreference(topic.getId(),4);
			
			if(firstPreferences.size() > 0) {
				Collections.sort(firstPreferences);
				List<User> s1 = new ArrayList<User>();
				List<User> supervisors = new ArrayList<User>();
				for(TopicPreference p1 : firstPreferences) {
					User user = userRepository.findByUserId(p1.getUserId());
					if(user != null) {
						if(user.getRole().getName().equals("STUDENT")) {
							s1.add(user);
						}else {
							supervisors.add(user);
						}
					}
				}
				if(!s1.isEmpty()) {
					preferenceDto.setFirstPreferenceStudents(s1);
					isElgabel = true;
				}
				if(!supervisors.isEmpty()) {
					preferenceDto.setFirstPreferenceSupervisor(supervisors);
					isElgabel = true;
				}
				
				
			}
			
			if(secondPreferences.size() > 0) {
				Collections.sort(secondPreferences);
				List<User> s2 = new ArrayList<User>();
				List<User> supervisors = new ArrayList<User>();
				for(TopicPreference p1 : secondPreferences) {
					User user = userRepository.findByUserId(p1.getUserId());
					if(user != null) {
						if(user.getRole().getName().equals("STUDENT")) {
							s2.add(user);
						}else {
							supervisors.add(user);
						}
					}	
				}
				if(!s2.isEmpty()) {
					preferenceDto.setSecondPreferenceStudents(s2);
					isElgabel = true;
				}
				if(!supervisors.isEmpty()) {
					preferenceDto.setSecondPreferenceSupervisor(supervisors);
					isElgabel = true;
				}
			}
			
			if(thirdPreferences.size() > 0) {
				Collections.sort(thirdPreferences);
				List<User> s3 = new ArrayList<User>();
				List<User> supervisors = new ArrayList<User>();
				for(TopicPreference p2 : thirdPreferences) {
					User user = userRepository.findByUserId(p2.getUserId());
					if(user != null) {
						if(user.getRole().getName().equals("STUDENT")) {
							s3.add(user);
						}
						else {
							supervisors.add(user);
						}
					}	
				}
				if(!s3.isEmpty()) {
					preferenceDto.setThirdPreferenceStudents(s3);
					isElgabel = true;
				}
				if(!supervisors.isEmpty()) {
					preferenceDto.setThirdPreferenceSupervisor(supervisors);
					isElgabel = true;
				}
				
			}
			
			if(fourthPreferences.size() > 0) {
				Collections.sort(fourthPreferences);
				List<User> s4 = new ArrayList<User>();
				List<User> supervisors = new ArrayList<User>();
				for(TopicPreference p4 : fourthPreferences) {
					User user = userRepository.findByUserId(p4.getUserId());
					if(user != null) {
						if(user.getRole().getName().equals("STUDENT")) {
							s4.add(user);
						}
						else {
							supervisors.add(user);
						}
					}
				}
				if(!s4.isEmpty()) {
					preferenceDto.setFourthPreferenceStudents(s4);
					isElgabel = true;
				}
				if(!supervisors.isEmpty()) {
					preferenceDto.setFourthPreferenceSupervisor(supervisors);
					isElgabel = true;
				}
				
				
			}
		
			if(isElgabel) {
				preferencesByTopic.add(preferenceDto);
			}
			
        }
		return preferencesByTopic;
	}
	
	@Override
	public ResponseEntity<?> runAlgorithm(AlgorithmInputDto algInputDto){
		     String groupName = "group";
		     Integer groupCount = 1;
		     List<User> students = userRepository.findByRole_RoleId(2);
	     List<User> notallocatedStudents = new ArrayList<User>();
        List<AlgorithmDto> groups = new ArrayList<AlgorithmDto>();
        List<PreferenceDto> preferences = getPreferenceDtoList();
        if(students == null) {
        	throw new IsTopicException("Students does not Exist");
        }
        
        if(preferences == null && preferences.size() == 0) {
        	throw new IsTopicException("preferences does not Exist");
        }
        
        for(User student : students) {
        	Boolean isAssigned = false;
        	List<TopicPreference> studentPreferences = preferencerepository.findByUserId(student.getUserId());
        	if(studentPreferences != null) {
        		AlgorithmDto alg = new AlgorithmDto();
        		studentPreferences = getSortedByPreference(studentPreferences);
        		for(int i =0;i< studentPreferences.size();i++) {
        			if(isAssigned) {
        				break;
        			}
        			if(studentPreferences.get(i).getPreference() == 1) {
        				String topic_id = studentPreferences.get(i).getTopicId();
        				if(checkFirstPreferenceElgibality(preferences, student,topic_id, algInputDto.getMaxNoOfStudentsingroup())) {
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < algInputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    	preferences = removeStudentFromPreferences(topic_id, student, preferences);
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
            			
            		}
        			if(studentPreferences.get(i).getPreference() == 2 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        					if(checksecondPreferenceElgibality(preferences, student,topic_id, algInputDto.getMaxNoOfStudentsingroup())) {
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < algInputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    	preferences = removeStudentFromPreferences(topic_id, student, preferences);
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
        				}
        				
        			}
        			
        			if(studentPreferences.get(i).getPreference() == 3 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        					if(checkThirdPreferenceElgibality(preferences, student,topic_id, algInputDto.getMaxNoOfStudentsingroup())) {
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < algInputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    	preferences = removeStudentFromPreferences(topic_id, student, preferences);
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
        				}
        				
        			}
        			if(studentPreferences.get(i).getPreference() == 4 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        					if(checkFourthPreferenceElgibality(preferences, student,topic_id, algInputDto.getMaxNoOfStudentsingroup())) {
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < algInputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    	preferences = removeStudentFromPreferences(topic_id, student, preferences);
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
        				}
        				
        			}
        			if(isAssigned && alg.getStudents() != null) {
       				 groups.add(alg);
       				preferences = removeStudentFromPreferences(alg.getTopic().getId(), student, preferences);
                }
        			
        		}
        		if(!isAssigned) {
        			notallocatedStudents.add(student);
        		}
        		 
        		
        	}else {
        		if(!isAssigned) {
        			notallocatedStudents.add(student);
        	}
              
        	
        }
        }
		 groups = assignTopicForUnassignedStudentsbyCheckingPreference(groups, notallocatedStudents, algInputDto,preferences);
		 groups = assignSupervisor(groups, preferences, algInputDto);
		 for(AlgorithmDto group : groups) {
			 group.setGroupName(groupName+ " "+ groupCount);
			 groupCount++;
		 }
		return new ResponseEntity<List<AlgorithmDto>>(groups,HttpStatus.OK);
	}
	
	public List<TopicPreference> getSortedByPreference(List<TopicPreference> preferences){
		Comparator<TopicPreference> cpreference = new Comparator<TopicPreference>() {

			@Override
			public int compare(TopicPreference p1, TopicPreference p2) {
				if(p1.getPreference() > p2.getPreference()) 
					return 1;
				else
			      return -1;
			}
			
		};
		
		Collections.sort(preferences,cpreference);
		
		return preferences;
	}
	
	public AlgorithmDto getAlgorithmObject(User student,String topic_id,AlgorithmDto alg) {
		List<User> Lstudent = new ArrayList<User>();
		Lstudent.add(student);
		alg.setTopic(topicrepository.findById(topic_id).get());
		if(alg != null) {
			if(alg.getStudents() != null) {
				alg.getStudents().add(student);
				
			}else {
				alg.setStudents(Lstudent);
			}
		}
		
		return alg;
	}
	
	
	public boolean checkFirstPreferenceElgibality(List<PreferenceDto> prefernces,User student,String topicID,Integer maxNoOfStudents) {
		  Boolean isElagable = false;
		List<PreferenceDto> preference = prefernces.stream().filter(p -> p.getTopic().getId().equals(topicID)).collect(Collectors.toList());
		if(preference != null) {
			if(preference.get(0).getFirstPreferenceStudents() != null) {
				int id = preference.get(0).getFirstPreferenceStudents().indexOf(student);
				 if(id < maxNoOfStudents) {
					 isElagable= true;
				 }
				 else {
					 isElagable= false;
				 }
			}
			
		}
		return isElagable;
	}
	
	public boolean checksecondPreferenceElgibality(List<PreferenceDto> prefernces,User student,String topicID,Integer maxNoOfStudents) {
		  Boolean isElagable = false;
		List<PreferenceDto> preference = prefernces.stream().filter(p -> p.getTopic().getId().equals(topicID)).collect(Collectors.toList());
		if(preference != null) {
			if(preference.get(0).getSecondPreferenceStudents() != null) {
				int id = preference.get(0).getSecondPreferenceStudents().indexOf(student);
				 if(id < maxNoOfStudents) {
					 isElagable= true;
				 }
				 else {
					 isElagable= false;
				 }
			}
			
		}
		return isElagable;
	}
	
	public boolean checkThirdPreferenceElgibality(List<PreferenceDto> prefernces,User student,String topicID,Integer maxNoOfStudents) {
		  Boolean isElagable = false;
		List<PreferenceDto> preference = prefernces.stream().filter(p -> p.getTopic().getId().equals(topicID)).collect(Collectors.toList());
		if(preference != null) {
			if(preference.get(0).getThirdPreferenceStudents() != null) {
				int id = preference.get(0).getThirdPreferenceStudents().indexOf(student);
				 if(id < maxNoOfStudents) {
					 isElagable= true;
				 }
				 else {
					 isElagable= false;
				 }
			}
			
		}
		return isElagable;
	}
	
	public boolean checkFourthPreferenceElgibality(List<PreferenceDto> prefernces,User student,String topicID,Integer maxNoOfStudents) {
		  Boolean isElagable = false;
		List<PreferenceDto> preference = prefernces.stream().filter(p -> p.getTopic().getId().equals(topicID)).collect(Collectors.toList());
		if(preference != null) {
			if(preference.get(0).getFourthPreferenceStudents() != null) {
				int id = preference.get(0).getFourthPreferenceStudents().indexOf(student);
				 if(id < maxNoOfStudents) {
					 isElagable= true;
				 }
				 else {
					 isElagable= false;
				 }
			}
			
		}
		return isElagable;
	}
	
	public List<PreferenceDto> removeStudentFromPreferences(String topic_id,User student,List<PreferenceDto> preferences){
		List<TopicPreference> studentPreferences = preferencerepository.findByUserId(student.getUserId());
		List<TopicPreference> topicPreference = studentPreferences.stream().filter(p -> p.getTopicId().equals(topic_id)).collect(Collectors.toList());
		studentPreferences.remove(topicPreference.get(0));
		if(studentPreferences.size() > 0) {
			for(TopicPreference tpreference : studentPreferences) {
				 List<PreferenceDto> fpreference =preferences.stream().filter(p -> p.getTopic().getId().equals(tpreference.getTopicId())).collect(Collectors.toList());
				if(fpreference != null) {
					if(!fpreference.isEmpty()) {
				 int index = preferences.indexOf(fpreference.get(0));
				if(preferences.get(index).getFirstPreferenceStudents() != null) {
					if(!preferences.get(index).getFirstPreferenceStudents().isEmpty()) {
						preferences.get(index).getFirstPreferenceStudents().remove(student);
					}	
				}
				if(preferences.get(index).getSecondPreferenceStudents() != null) {
					if(!preferences.get(index).getSecondPreferenceStudents().isEmpty()) {
						preferences.get(index).getSecondPreferenceStudents().remove(student);
					}	
				}
				
				if(preferences.get(index).getThirdPreferenceStudents() != null) {
					if(!preferences.get(index).getThirdPreferenceStudents().isEmpty()) {
						preferences.get(index).getThirdPreferenceStudents().remove(student);
					}	
				}
				
				if(preferences.get(index).getFourthPreferenceStudents() != null) {
					if(!preferences.get(index).getFourthPreferenceStudents().isEmpty()) {
						preferences.get(index).getFourthPreferenceStudents().remove(student);
					}	
				}
			}
				}
		}
		}
		return preferences;
	}
	
	public List<AlgorithmDto> checkMinNumberInGroup(List<AlgorithmDto> groups,AlgorithmInputDto inputDto,List<User> unassignedStudents){
		
		List<AlgorithmDto> lessStudents = groups.stream().filter(g -> g.getStudents().size() < inputDto.getMinNoOfStudentsingroup()).collect(Collectors.toList());
		List<User> assigned = new ArrayList<User>();
		if(!lessStudents.isEmpty()) {
			for(AlgorithmDto group : lessStudents) {
				unassignedStudents.removeAll(assigned);
				if(!unassignedStudents.isEmpty()) {
					for(int i=0;i<unassignedStudents.size();i++) {
						if(group.getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
							int index = groups.indexOf(group);
							groups.get(index).getStudents().add(unassignedStudents.get(i));
							assigned.add(unassignedStudents.get(i));
						}else {
							break;
						}
						
						
					}
				}else {
					break;
				}
				
			}
		}
		//end
		List<AlgorithmDto> lessStudentsGroups = groups.stream().filter(g -> g.getStudents().size() < inputDto.getMinNoOfStudentsingroup()).collect(Collectors.toList());
		groups.removeAll(lessStudentsGroups);
		List<Topic> topics = new ArrayList<Topic>();
		if(lessStudentsGroups.size() > 0) {
			for(AlgorithmDto lgroup : lessStudentsGroups) {
				topics.add(lgroup.getTopic());
				for(User student : lgroup.getStudents()) {
					 boolean isAssigned = false;
					TopicPreference preference = preferencerepository.findByUserIdAndPreference(student.getUserId(),1);
					if(preference != null) {
						System.out.println(preference.getUserId());
					if(!preference.getTopicId().equals(lgroup.getTopic().getId())) {
						List<AlgorithmDto> group = groups.stream().filter(g -> g.getTopic().getId().equals(preference.getTopicId()) && g.getStudents().size() < inputDto.getMaxNoOfStudentsingroup()).collect(Collectors.toList());
						if(group.size() >0) {
							Integer index = groups.indexOf(group.get(0));
							if(groups.get(index).getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
								groups.get(index).getStudents().add(student);
								unassignedStudents.remove(student);
								isAssigned = true;
								break;
							}
							
						}
						
						
					}
					}
					
					
					if(!isAssigned) {
						TopicPreference preference2 = preferencerepository.findByUserIdAndPreference(student.getUserId(),2);
						if(preference2 != null) {
							if(!preference.getTopicId().equals(lgroup.getTopic().getId())) {
								List<AlgorithmDto> group = groups.stream().filter(g -> g.getTopic().getId().equals(preference2.getTopicId()) && g.getStudents().size() < inputDto.getMaxNoOfStudentsingroup()).collect(Collectors.toList());
								if(group.size() >0) {
									Integer index = groups.indexOf(group.get(0));
									groups.get(index).getStudents().add(student);
									isAssigned = true;
									unassignedStudents.remove(student);
									break;
								}
								
								
							}
						}
					
					}
					
					if(!isAssigned) {
					TopicPreference preference3 = preferencerepository.findByUserIdAndPreference(student.getUserId(),3);
					if(preference3 != null) {
						if(!preference.getTopicId().equals(lgroup.getTopic().getId())) {
							List<AlgorithmDto> group = groups.stream().filter(g -> g.getTopic().getId().equals(preference3.getTopicId()) && g.getStudents().size() < inputDto.getMaxNoOfStudentsingroup()).collect(Collectors.toList());
							if(group.size() >0) {
								Integer index = groups.indexOf(group.get(0));
								groups.get(index).getStudents().add(student);
								unassignedStudents.remove(student);
								isAssigned = true;
								break;
							}
							
							
						}
					}
					
					}
					if(!isAssigned) {
					TopicPreference preference4 = preferencerepository.findByUserIdAndPreference(student.getUserId(),4);
					if(preference4 != null) {
						if(!preference.getTopicId().equals(lgroup.getTopic().getId())) {
							List<AlgorithmDto> group = groups.stream().filter(g -> g.getTopic().getId().equals(preference4.getTopicId()) && g.getStudents().size() < inputDto.getMaxNoOfStudentsingroup()).collect(Collectors.toList());
							if(group.size() >0) {
								Integer index = groups.indexOf(group.get(0));
								groups.get(index).getStudents().add(student);
								unassignedStudents.remove(student);
								isAssigned = true;
								break;
							}
							
							
						}	
					}
					
					
					}
					if(!isAssigned) {
					unassignedStudents.add(student);
					}
				
				}
		}
		
		}
		
		if(!unassignedStudents.isEmpty()) {
			List<User> users = new ArrayList<User>();
			for(AlgorithmDto group: groups) {
				users.addAll(group.getStudents());	
			}	
			unassignedStudents = checkUnassignedStudents(users, unassignedStudents);					
			List<Topic> unassignedTopics = getUnassignedTopics(groups); 
			if(unassignedStudents.size() > 0 && unassignedStudents.size() >= inputDto.getMinNoOfStudentsingroup() && unassignedStudents.size() >= inputDto.getMaxNoOfStudentsingroup()) {
				List<User> assignedstudents = new ArrayList<User>();
				AlgorithmDto alg = new AlgorithmDto();
				for(User student: unassignedStudents) {
					if(!users.contains(student)) {
						if(assignedstudents.size() < inputDto.getMaxNoOfStudentsingroup())
						{
							assignedstudents.add(student);
						}
					}
				}
				if (assignedstudents.size() > inputDto.getMinNoOfStudentsingroup())
				{
					alg.setStudents(assignedstudents);
					alg.setTopic(unassignedTopics.get(0));
					groups.add(alg);
					users.addAll(assignedstudents);
					unassignedStudents.removeAll(assignedstudents);
				}
			}

			else if(unassignedStudents.size() > 0 && unassignedStudents.size() >= inputDto.getMinNoOfStudentsingroup() && unassignedStudents.size() <= inputDto.getMaxNoOfStudentsingroup()) {
				List<User> assignedstudents = new ArrayList<User>();
				AlgorithmDto alg = new AlgorithmDto();
				for(User student: unassignedStudents) {
					if(!users.contains(student)) {
						assignedstudents.add(student);
						users.add(student);
					}
				}
				alg.setStudents(assignedstudents);
				alg.setTopic(unassignedTopics.get(1));
				groups.add(alg);
				unassignedStudents.removeAll(assignedstudents);
			}
			else if(unassignedStudents.size() > 0 && unassignedStudents.size() < inputDto.getMinNoOfStudentsingroup() && unassignedStudents.size() > 1) {
				List<User> assignedstudents = new ArrayList<User>();
				AlgorithmDto alg = new AlgorithmDto();
				for(User student: unassignedStudents) {
					if(!users.contains(student)) {
						assignedstudents.add(student);
						users.add(student);
					}
				}
				alg.setStudents(assignedstudents);
				alg.setTopic(unassignedTopics.get(2));
				groups.add(alg);
				unassignedStudents.removeAll(assignedstudents);
			} 
			if(unassignedStudents.size() == 1) {
					if(!users.contains(unassignedStudents.get(0))){
					groups.get(0).getStudents().add(unassignedStudents.get(0));
				}
			}
		}
		
		return groups;
	
	}
	public List<User> checkUnassignedStudents(List<User> users,List<User> unAllocatedStudents) {
		List<User> students = userRepository.findByRole_RoleId(2);
		students.removeAll(users);
//		for (User Student: unAllocatedStudents) {
//			if (users.contains(Student)) {
//				students.remove(Student);
//			}
//		}
		return students;
	}
	public List<Topic> getUnassignedTopics(List<AlgorithmDto> groups) {
		List<Topic> topics = topicrepository.findByIsApproved(true);
		List<Topic> dbtopics = topics;
		for(AlgorithmDto alg:groups) {
			for (int i = 0; i < topics.size(); i++) {
				if (topics.get(i).getId().equals(alg.getTopic().getId())) {
					dbtopics.remove(topics.get(i));
				}
			}
		}
		return dbtopics;
	}
	public List<AlgorithmDto> assignTopicForUnassignedStudentsbyCheckingPreference(List<AlgorithmDto> groups,List<User> students,AlgorithmInputDto inputDto,List<PreferenceDto> preferences){
		//List<User> allocatedStudents = new ArrayList<User>();
		List<User> notallocatedStudents = new ArrayList<User>();
		for(User student : students) {
			boolean isAssigned = false;
			List<TopicPreference> studentPreferences = preferencerepository.findByUserId(student.getUserId());
			if(!studentPreferences.isEmpty()) {
				studentPreferences = getSortedByPreference(studentPreferences);
				AlgorithmDto alg = new AlgorithmDto();
        		for(int i =0;i< studentPreferences.size();i++) {
        			if(isAssigned) {
        				break;
        			}
        			if(studentPreferences.get(i).getPreference() == 1) {
        				String topic_id = studentPreferences.get(i).getTopicId();
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				
            			
            		}
        			
        			if(studentPreferences.get(i).getPreference() == 2 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
        				}
        			//secon preference ends here
        			
        			if(studentPreferences.get(i).getPreference() == 3 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        						
        				}
        				
        			}
        			
        			if(studentPreferences.get(i).getPreference() == 4 && !isAssigned) {
        				if(studentPreferences.get(i).getTopicId() != null) {
        					String topic_id = studentPreferences.get(i).getTopicId();
        						if(groups.size() > 0) {
        							List<AlgorithmDto> fgroup = groups.stream().filter(f -> f.getTopic().getId().equals(topic_id)).collect(Collectors.toList());
        							if(fgroup.size() > 0) {
        								int index = groups.indexOf(fgroup.get(0));
        								if(groups.get(index).getStudents() != null) {
        								    if(groups.get(index).getStudents().size() < inputDto.getMaxNoOfStudentsingroup()) {
        								    	groups.get(index).getStudents().add(student);
        								    	isAssigned = true;
        								    }
        									
        								}
        								else {
        									alg = getAlgorithmObject(student, topic_id, alg);
                							isAssigned = true;
        								}
        								
        							}
        							else {
        								alg = getAlgorithmObject(student, topic_id, alg);
            							isAssigned = true;
        							}
        							}
        						else {
        							alg = getAlgorithmObject(student, topic_id, alg);
        							isAssigned = true;
        						}
        				}
        				
        			}
        			if(isAssigned) {
        				if(isAssigned && alg.getStudents() != null) {
              				 groups.add(alg);
                       }
        			}
        			}
        		if(!isAssigned) {
    					notallocatedStudents.add(student);
    			}
        			//for finishes here	
			}
			else {
				notallocatedStudents.add(student);
	
			}
			}
		
	     groups = checkMinNumberInGroup(groups, inputDto, notallocatedStudents);
		return groups;
	}
	
	public List<AlgorithmDto> assignSupervisor(List<AlgorithmDto> groups,List<PreferenceDto> preferences,AlgorithmInputDto input){
		List<User> allocatedSupervisor = new ArrayList<User>();
		List<User> supervisors = userRepository.findByRole_RoleId(3);
		List<AlgorithmDto> allocatedGroups = new ArrayList<AlgorithmDto>();
		
		for(AlgorithmDto group : groups) {
		 List<PreferenceDto> selectedPreference = preferences.stream().filter(p -> p.getTopic().getId().equals(group.getTopic().getId())).collect(Collectors.toList());
			if(!selectedPreference.isEmpty()) {
				   if(selectedPreference.get(0).getFirstPreferenceSupervisor() != null) {
					   if(!selectedPreference.get(0).getFirstPreferenceSupervisor().isEmpty()) {
						   for(User supervisor : selectedPreference.get(0).getFirstPreferenceSupervisor()){
							   if(!allocatedSupervisor.contains(supervisor))
							   {
								   group.setSupervisor(supervisor);
								   allocatedSupervisor.add(supervisor);
								   allocatedGroups.add(group);
								   break;
							   }   
						   }
							
					   }  
				   }
			}
			
			
		}
		// 
		for(AlgorithmDto group : groups) {
			if(group.getSupervisor() == null) {
		 List<PreferenceDto> selectedPreference = preferences.stream().filter(p -> p.getTopic().getId().equals(group.getTopic().getId())).collect(Collectors.toList());
			if(!selectedPreference.isEmpty()) {
				   if(selectedPreference.get(0).getSecondPreferenceSupervisor() != null) {
					   if(!selectedPreference.get(0).getSecondPreferenceSupervisor().isEmpty()) {
						   for(User supervisor : selectedPreference.get(0).getSecondPreferenceSupervisor()){
							   if(!allocatedSupervisor.contains(supervisor))
							   {
								   group.setSupervisor(supervisor);
								   allocatedSupervisor.add(supervisor);
								   allocatedGroups.add(group);
								   break;
							   }   
						   }
							
					   }  
				   }
			}
			}
		}
		
		for(AlgorithmDto group : groups) {
			if(group.getSupervisor() == null) {
		 List<PreferenceDto> selectedPreference = preferences.stream().filter(p -> p.getTopic().getId().equals(group.getTopic().getId())).collect(Collectors.toList());
			if(!selectedPreference.isEmpty()) {
				   if(selectedPreference.get(0).getThirdPreferenceSupervisor() != null) {
					   if(!selectedPreference.get(0).getThirdPreferenceSupervisor().isEmpty()) {
						   for(User supervisor : selectedPreference.get(0).getThirdPreferenceSupervisor()){
							   if(!allocatedSupervisor.contains(supervisor))
							   {
								   group.setSupervisor(supervisor);
								   allocatedSupervisor.add(supervisor);
								   allocatedGroups.add(group);
								   break;
							   }   
						   }
							
					   }  
				   }
			}
		}
		}
		
		for(AlgorithmDto group : groups) {
			if(group.getSupervisor() == null) {
		 List<PreferenceDto> selectedPreference = preferences.stream().filter(p -> p.getTopic().getId().equals(group.getTopic().getId())).collect(Collectors.toList());
			if(!selectedPreference.isEmpty()) {
				   if(selectedPreference.get(0).getFourthPreferenceSupervisor() != null) {
					   if(!selectedPreference.get(0).getFourthPreferenceSupervisor().isEmpty()) {
						   for(User supervisor : selectedPreference.get(0).getFourthPreferenceSupervisor()){
							   if(!allocatedSupervisor.contains(supervisor))
							   {
								   group.setSupervisor(supervisor);
								   allocatedSupervisor.add(supervisor);
								   allocatedGroups.add(group);
								   break;
							   }   
						   }
							
					   }  
				   }
			}
		}
				
		}
		List<AlgorithmDto> notallocatedGroup = groups.stream().filter(g -> g.getSupervisor() == null).collect(Collectors.toList());
		supervisors.removeAll(allocatedSupervisor);
		if(!notallocatedGroup.isEmpty() && supervisors.isEmpty()) {
			if(supervisors.size() == notallocatedGroup.size()) {
				for(int i=0;i<supervisors.size();i++) {
					int index = groups.indexOf(notallocatedGroup.get(i));
					groups.get(index).setSupervisor(supervisors.get(i));
				}
			}else if(supervisors.size() > notallocatedGroup.size()) {
				for(int i=0;i<notallocatedGroup.size();i++) {
					int index = groups.indexOf(notallocatedGroup.get(i));
					groups.get(index).setSupervisor(supervisors.get(i));
				}
			}
			else {
				for(int i=0;i<supervisors.size();i++) {
					int index = groups.indexOf(notallocatedGroup.get(i));
					groups.get(index).setSupervisor(supervisors.get(i));
				}
			}
			List<AlgorithmDto> notallocatedFinalCheck = groups.stream().filter(g -> g.getSupervisor() == null).collect(Collectors.toList());
			if(!notallocatedFinalCheck.isEmpty()) {
				List<User> latestsupervisorList  = userRepository.findByRole_RoleId(3);
				for(int i=0;i<notallocatedFinalCheck.size();i++) {
					if(!CheckAssignedGroupsForSupervisor(groups, latestsupervisorList.get(i),input.getNoOfgroupsCanSupervisorMonitor())) {
						int index = groups.indexOf(notallocatedFinalCheck.get(i));
						groups.get(index).setSupervisor(latestsupervisorList.get(i));
					}
				}
			}
		
		
		
		}
		return groups;
	}

	@Override
	public ResponseEntity<?> saveAlgorithm(List<AlgorithmDto> algorthimDto) {
		// TODO Auto-generated method stub
		List<Group> groups = new ArrayList<Group>();
		for(AlgorithmDto alg : algorthimDto) {
			Group group = new Group();
			group.setGroupName(alg.getGroupName());
			group.setTopic(alg.getTopic());
			group.setStudents(alg.getStudents());
			group.setSupervisor(alg.getSupervisor());
			groups.add(group);
		}
		
		groupRepository.saveAll(groups);
		
		for(AlgorithmDto alg : algorthimDto) {
			emailService.sendgroupdDetailsMail(alg);
		}
		return new ResponseEntity<List<Group>>(groups,HttpStatus.OK);
	}

}
