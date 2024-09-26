package com.group.integration.domain.controller;

import com.group.integration.domain.dto.MultipartTopicDto;
import com.group.integration.kafka.producer.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaSendController {

	private final ProducerEvent producerEvent;

	@PostMapping("/send")
	public void send(@RequestBody MultipartTopicDto request) {
		producerEvent.send("multipart-topic", request);
	}

}
