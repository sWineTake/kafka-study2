package com.group.integration.kafka.consumer;

import com.group.integration.domain.dto.MultipartTopicDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumerEvent {

	@KafkaListener(topics = "multipart-topic", groupId = "group_1",
					containerFactory = "kafkaListenerContainerFactory")
	public void listener(@Payload MultipartTopicDto message,
	                     @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
	                     @Header(KafkaHeaders.OFFSET) long offset,
	                     Acknowledgment acknowledgment) {
		System.out.println(message.toString());

		try {
			// 메시지 처리 로직
			processMessage(message);

			// 메시지 처리 성공 시 오프셋 커밋
			acknowledgment.acknowledge();

		} catch (Exception e) {
			e.printStackTrace();
			// 에러 처리 로직 (예: 데드 레터 큐로 전송)
			handleProcessingError(message, partition, offset, e);
		}

	}

	private void processMessage(MultipartTopicDto message) {
		// 실제 메시지 처리 로직 구현

	}

	private void handleProcessingError(MultipartTopicDto message, int partition, long offset, Exception e) {
		// 에러 처리 로직 구현
		// 예: 데드 레터 큐로 전송, 재시도 로직 등

	}


}
