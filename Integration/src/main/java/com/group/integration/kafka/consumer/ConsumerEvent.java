package com.group.integration.kafka.consumer;

import com.group.integration.domain.dto.MultipartTopicDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerEvent {

	@KafkaListener(topics = "multipart-topic", groupId = "group_1")
	public void listener(MultipartTopicDto message) {

		System.out.println("****************************************");
		System.out.println("****************************************");
		System.out.println("****************************************");
		System.out.println(message.toString());

	}


}
