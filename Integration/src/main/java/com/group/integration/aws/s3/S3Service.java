package com.group.integration.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Client s3Client;
	@Value("${logging.bucket.name}")
	private String bucketName;

	public void storeLog(String topic, String message, boolean isError) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
		String logType = isError ? "error" : "success";
		String key = String.format("logs/%s/%s_%s.log", logType, topic, timestamp);
		String logContent = String.format("[%s] Topic: %s, Message: %s", timestamp, topic, message);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromString(logContent));
	}
}
