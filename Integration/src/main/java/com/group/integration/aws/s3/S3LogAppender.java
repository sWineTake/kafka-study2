package com.group.integration.aws.s3;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class S3LogAppender extends AppenderBase<ILoggingEvent> {

	/*private S3Client s3Client;

	@Override
	public void start() {
		if (bucketName == null || region == null) {
			addError("Bucket name and region must be set");
			return;
		}

		try {
			S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
			if (accessKey != null && secretKey != null) {
				AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
				builder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
			}

			s3Client = builder.build();

			// Test connection
			ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
			System.out.println("S3 client initialized: " + listBucketsResponse.buckets());

			super.start();
		} catch (Exception e) {
			addError("Failed to initialize S3 client", e);
		}
	}

	@Override
	protected void append(ILoggingEvent iLoggingEvent) {
		String logMessage = iLoggingEvent.getFormattedMessage() + "\n";
		String fileName = generateFileName();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(fileName)
			.build();
		try {
			PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromString(logMessage));
			System.out.println("Uploaded log to S3: " + putObjectResponse);
		} catch (Exception e) {
			addError("Failed to upload log to S3", e);
		}
	}

	private String generateFileName() {
		LocalDateTime now = LocalDateTime.now();
		return String.format("logs/%s.log", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
	}*/

	private static final Logger logger = LoggerFactory.getLogger(S3LogAppender.class);
	private ScheduledExecutorService scheduler;
	private RollingFileAppender<ILoggingEvent> fileAppender;
	private S3Client s3Client;

	private String bucketName;
	private String region;
	private String accessKey;
	private String secretKey;

	private static final String LOG_PREFIX = "logs/";
	private static final int UPLOAD_INTERVAL_MINUTES = 60; // 1 hour
	private static final String LOCAL_LOG_DIR = "logs";

	@Override
	public void start() {
		if (bucketName == null || region == null) {
			addError("Bucket name and region must be set");
			return;
		}

		try {
			initializeS3Client();
			initializeFileAppender();
			initializeScheduler();

			super.start();
		} catch (Exception e) {
			addError("Failed to initialize appender", e);
			logger.error("Appender initialization failed", e);
		}
	}

	// S3 클라이언트 초기화 로직
	private void initializeS3Client() {
		try {
			S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
			if (accessKey != null && secretKey != null) {
				AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
				builder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
			}

			s3Client = builder.build();

			// Test connection
			ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
			System.out.println("S3 client initialized: " + listBucketsResponse.buckets());

			super.start();
		} catch (Exception e) {
			addError("Failed to initialize S3 client", e);
		}
	}

	private void initializeFileAppender() {
		fileAppender = new RollingFileAppender<>();
		fileAppender.setContext(getContext());
		fileAppender.setFile(LOCAL_LOG_DIR + "/application.log");

		TimeBasedRollingPolicy<?> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setFileNamePattern(LOCAL_LOG_DIR + "/application-%d{yyyy-MM-dd}.log");
		rollingPolicy.setMaxHistory(3); // 3일간의 로그 파일 유지
		rollingPolicy.setParent(fileAppender);
		rollingPolicy.setContext(getContext());
		rollingPolicy.start();

		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.start();
	}

	private void initializeScheduler() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(this::uploadLogsToS3, UPLOAD_INTERVAL_MINUTES, UPLOAD_INTERVAL_MINUTES, TimeUnit.MINUTES);
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		fileAppender.doAppend(eventObject);
	}

	private void uploadLogsToS3() {
		File logDir = new File(LOCAL_LOG_DIR);
		File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));

		if (logFiles != null) {
			for (File logFile : logFiles) {
				if (isYesterdayLog(logFile)) {
					uploadFileToS3(logFile);
					logFile.delete();
				}
			}
		}
	}

	private boolean isYesterdayLog(File file) {
		LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
		String yesterdayString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		return file.getName().contains(yesterdayString);
	}

	private void uploadFileToS3(File file) {
		try {
			String compressedFileName = compressFile(file);
			File compressedFile = new File(compressedFileName);

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(LOG_PREFIX + compressedFile.getName())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromFile(compressedFile));
			logger.info("Successfully uploaded log to S3: {}", compressedFile.getName());

			compressedFile.delete();
		} catch (Exception e) {
			addError("Failed to upload log to S3", e);
			logger.error("Failed to upload log to S3", e);
		}
	}

	private String compressFile(File input) throws IOException {
		String gzipFileName = input.getAbsolutePath() + ".gz";
		try (GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(new File(gzipFileName).toPath()));
		     FileInputStream in = new FileInputStream(input)) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		}
		return gzipFileName;
	}

	@Override
	public void stop() {
		uploadLogsToS3(); // 남은 로그 파일 업로드
		if (scheduler != null) {
			scheduler.shutdown();
			try {
				if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
					scheduler.shutdownNow();
				}
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
			}
		}
		if (fileAppender != null) {
			fileAppender.stop();
		}
		super.stop();
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
