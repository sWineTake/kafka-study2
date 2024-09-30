package com.group.integration.kafka.producer;

import com.group.integration.domain.dto.MultipartTopicDto;
import com.group.integration.kafka.FailureTracker;
import com.group.integration.kafka.RetryQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerEvent {

	private final KafkaTemplate<String, MultipartTopicDto> kafkaTemplate;
	private final FailureTracker failureTracker;
	private final RetryQueue retryQueue;

	public void send(String topic, MultipartTopicDto message) {
		CompletableFuture<SendResult<String, MultipartTopicDto>> send = kafkaTemplate.send(topic, message.getKey(), message);
		send.whenComplete((result, ex) -> {
			// callBack 메서드
			try {
				if (ex == null) {
					// logger.info("메시지 전송 성공: topic=[{}], message=[{}], offset=[{}]", topic, message, result.getRecordMetadata().offset());
					// 전송 성공의 경우에는 따로 로그를 남기지 않음
				} else {
					log.error("메시지 전송 실패: topic=[{}], message=[{}], 에러=[{}]", topic, message, ex.getMessage());
					handleFailure(topic, message, ex);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// S3적재
				boolean isError = ex == null;
				log.info("S3적재: topic=[{}], message=[{}], isError=[{}]", topic, message, isError);
				// s3Service.storeLog(topic, message.toString(), isError);
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
