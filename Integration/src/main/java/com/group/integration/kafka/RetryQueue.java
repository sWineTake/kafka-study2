package com.group.integration.kafka;

import com.group.integration.domain.dto.MultipartTopicDto;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


@Component
public class RetryQueue {
	private final Queue<RetryMessage> retryQueue = new ConcurrentLinkedQueue<>();

	public void addForRetry(String topic, MultipartTopicDto message) {
		retryQueue.offer(new RetryMessage(topic, message));
	}

	// 주기적으로 실행되는 메서드 (예: @Scheduled 어노테이션 사용)
	public void processRetryQueue() {
		RetryMessage retryMessage = retryQueue.poll();
		if (retryMessage != null) {
			// 재시도 로직 구현
		}
	}

	private static class RetryMessage {
		private final String topic;
		private final MultipartTopicDto message;

		public RetryMessage(String topic, MultipartTopicDto message) {
			this.topic = topic;
			this.message = message;
		}
	}
}
