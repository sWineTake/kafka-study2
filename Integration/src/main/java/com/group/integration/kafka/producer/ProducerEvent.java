package com.group.integration.kafka.producer;

import com.group.integration.domain.dto.MultipartTopicDto;
import com.group.integration.kafka.FailureTracker;
import com.group.integration.kafka.RetryQueue;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.RecordTooLargeException;
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
	private final FailureTracker failureTracker;
	private final RetryQueue retryQueue;

	public void send(String topic, MultipartTopicDto message) {
		CompletableFuture<SendResult<String, MultipartTopicDto>> send = kafkaTemplate.send(topic, message.getKey(), message);
		send.whenComplete((result, ex) -> {
			// callBack 메서드
			if (ex == null) {
				// logger.info("메시지 전송 성공: topic=[{}], message=[{}], offset=[{}]", topic, message, result.getRecordMetadata().offset());

			} else {
				logger.error("메시지 전송 실패: topic=[{}], message=[{}], 에러=[{}]", topic, message, ex.getMessage());
				handleFailure(topic, message, ex);
			}
		});
	}

	private void handleFailure(String topic, MultipartTopicDto message, Throwable ex) {
		failureTracker.recordFailure(topic, message, ex);
		if (shouldRetry(ex)) {
			retryQueue.addForRetry(topic, message);
		}
	}

	private boolean shouldRetry(Throwable ex) {
		// 재시도 가능한 예외인지 판단하는 로직
		return !(ex instanceof InvalidTopicException || ex instanceof RecordTooLargeException);
	}

}
