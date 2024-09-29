package com.group.integration.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Util {

	private final S3Client s3Client;
	@Value("${logging.bucket.name}")
	private String bucketName;

	public void uploadFile(MultipartFile file) throws IOException {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(file.getOriginalFilename())
			.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
	}

	public List<String> listFiles() {
		ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
			.bucket(bucketName)
			.build();

		ListObjectsV2Response listObjResponse = s3Client.listObjectsV2(listObjectsReqManual);
		return listObjResponse.contents().stream()
			.map(S3Object::key)
			.collect(Collectors.toList());
	}
}
