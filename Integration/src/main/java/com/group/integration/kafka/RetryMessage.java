package com.group.integration.kafka;

import com.group.integration.domain.dto.MultipartTopicDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RetryMessage<T, K> {
	private String topic;
	private String key;
	private MultipartTopicDto message;
	private int retryCount;

	public RetryMessage(String topic, String key, MultipartTopicDto message, int retryCount) {
		this.topic = topic;
		this.key = key;
		this.message = message;
		this.retryCount = retryCount;
	}

	public void incrementRetryCount() {
		this.retryCount++;
	}
}
