package com.group.integration.kafka.producer;

import com.group.integration.domain.dto.MultipartTopicDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ProducerEvent {
	private static final Logger logger = LoggerFactory.getLogger(ProducerEvent.class);
	private final KafkaTemplate<String, MultipartTopicDto> kafkaTemplate;

	public void send(String topic, MultipartTopicDto message) {
		CompletableFuture<SendResult<String, MultipartTopicDto>> send = kafkaTemplate.send(topic, message.getKey(), message);
		send.whenComplete((result, ex) -> {
			if (ex == null) {
				logger.info("메시지 전송 성공: topic=[{}], message=[{}], offset=[{}]",
					topic, message, result.getRecordMetadata().offset());
			} else {
				logger.error("메시지 전송 실패: topic=[{}], message=[{}], 에러=[{}]",
					topic, message, ex.getMessage());
			}
		});
	}

}
