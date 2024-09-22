package com.example.kafka;


import jdk.jfr.Frequency;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class SyncProducer {

	public static void main(String[] args) {
		// 카프카 프로듀서 객체 config 설정
		String topicName = "simple-topic";

		Properties properties = new Properties();
		// bootstrap.servers, key.serializer.class, value.serializer.class
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// 카프카 프로듀서 객체 생성
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		// 프로듀서 레코드 객체 생성
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
			 topicName // 토픽 명
			, "id-001" // 키
			, "hello world" // 값
		) ;

		// 프로듀서에 레코드 넣어서 토픽으로 전달 -> 동기로 진행
		try {
			RecordMetadata recordMetadata = producer.send(producerRecord).get();
			System.out.println("###### record metadata received #####");
			System.out.println(recordMetadata.partition());
			System.out.println(recordMetadata.offset());
			System.out.println(recordMetadata.timestamp());

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			producer.close();
		}
	}


}
