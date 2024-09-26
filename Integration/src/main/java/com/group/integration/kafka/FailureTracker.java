package com.group.integration.kafka;


import com.group.integration.domain.dto.MultipartTopicDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FailureTracker {

	// 실패 누적 카운트 집계
	private final ConcurrentHashMap<String, AtomicInteger> failureCountByTopic = new ConcurrentHashMap<>();

	public void recordFailure(String topic, MultipartTopicDto message, Throwable ex) {
		// 실패 카운트 1 증가
		failureCountByTopic.computeIfAbsent(topic, k -> new AtomicInteger()).incrementAndGet();
		// todo) 여기에 데이터베이스나 모니터링 시스템에 실패 정보를 기록하는 로직 추가


	}

	public int getFailureCount(String topic) {
		return failureCountByTopic.getOrDefault(topic, new AtomicInteger()).get();
	}
}
