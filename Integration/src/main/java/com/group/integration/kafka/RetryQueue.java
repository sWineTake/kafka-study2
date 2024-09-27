package com.group.integration.kafka;

import com.group.integration.domain.dto.MultipartTopicDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;


@Service
@Slf4j
@RequiredArgsConstructor
public class RetryQueue {
	// 재시도 횟수
	private static final int MAX_RETRY_ATTEMPTS = 3;
	private final Queue<RetryMessage> retryQueue = new ConcurrentLinkedQueue<>();
	private final KafkaTemplate<String, MultipartTopicDto> kafkaTemplate;

	// 재시도 대상 메시지를 큐에 추가
	public void addForRetry(String topic, MultipartTopicDto message) {
		retryQueue.offer(new RetryMessage(topic, message.getKey(), message, 0));
	}

	@Scheduled(fixedRate = 10000) // 1분마다 실행
	public void processRetryQueue() {
		RetryMessage retryMessage;
		while ((retryMessage = retryQueue.poll()) != null) {
			try {
				if (retryMessage.getRetryCount() < MAX_RETRY_ATTEMPTS) {
					kafkaTemplate.send(retryMessage.getTopic(), retryMessage.getKey(), retryMessage.getMessage());
					log.info("Retry successful for message: {}", retryMessage.getMessage());
				} else {
					sendToDeadLetterQueue(retryMessage);
				}
			} catch (Exception e) {
				// 재발송 실패 시 처리
				retryMessage.incrementRetryCount(); // 재시도 횟수 증가

				if (retryMessage.getRetryCount() < MAX_RETRY_ATTEMPTS) {
					retryQueue.offer(retryMessage); // 다시 큐에 넣기
				} else {
					sendToDeadLetterQueue(retryMessage); // 최대 재시도 횟수 초과 시 데드 레터 큐로 보내기
				}
			}
		}
	}
	private void sendToDeadLetterQueue(RetryMessage retryMessage) {
		try {
			// retryQueue에서 retryMessage를 제거
			retryQueue.remove(retryMessage);
			// 추가적인 알림 발송, 데이터베이스에 기록 등

		} catch (Exception e) {
			// 여기서 추가적인 오류 처리 로직을 구현할 수 있습니다.
			log.error("Failed to send message to dead letter queue: {}", retryMessage, e);
			// 예: 알림 발송, 데이터베이스에 기록 등
		}
	}
}
