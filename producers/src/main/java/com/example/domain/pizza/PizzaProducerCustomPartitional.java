package com.example.domain.pizza;


import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class PizzaProducerCustomPartitional {

	public static final String TOPIC_NAME = "pizza_topic_partitional";

	public static void main(String[] args) {
		// 카프카 프로듀서 config 설정
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

		props.put("custom.specialKey", "P001");

		// custom 파티셔너 사용법
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.example.config.CustomPartitioner");

		// 카프카 프로듀서 생성
		KafkaProducer<String, String> producer = new KafkaProducer(props);

		System.out.println("######### producer #########");
		System.out.println(producer.metrics().get("buffer-memory"));

		sendMessage(-1, 100, 100, 100, false, producer);

		producer.close();
	}


	public static void sendMessage(int iterCount, // 전체 메시지 수(반복 횟수 but, -1 == 무한루프)
	                               int interIntervalMills, // 1건 별로 쉬는 타임
	                               int intervalMills, // 쓰레드 슬립 시간
	                               int intervalCount, // 특정 갯수마다 슬립
	                               boolean sync,
	                               KafkaProducer<String, String> producer) {

		PizzaMessage pizzaMessage = new PizzaMessage();
		int iterSeq = 0;

		long seed = 2022;
		Random random = new Random(seed);
		Faker faker = Faker.instance(random);

		while (iterSeq++ != iterCount) {
			HashMap<String, String> message = pizzaMessage.produce_msg(faker, random, iterSeq);
			ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, message.get("key"), message.get("message"));

			sendMessage(producer, producerRecord, message, sync);

			// 특정 갯수마다 슬립
			if (intervalCount > 0 && iterSeq % intervalCount == 0) {
				try {
					System.out.println("######### intervalCount : " + intervalCount + " #########");
					System.out.println("######### intervalMills : " + intervalMills + " #########");
					Thread.sleep(intervalMills);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


			if (interIntervalMills > 0) {
				try {
					System.out.println("!!!!!!!! interIntervalMills : " + interIntervalMills + " !!!!!!!!");
					Thread.sleep(interIntervalMills);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		}

	}

	public static void sendMessage(KafkaProducer<String, String> producer, ProducerRecord<String, String> record, HashMap<String, String> message, boolean sync) {
		if (sync) { // 동기
			try {
				RecordMetadata recordMetadata = producer.send(record).get();
				System.out.println("sync sent : " + recordMetadata.partition() + " - " + recordMetadata.offset()  + " - " + recordMetadata.timestamp());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // 비동기
			producer.send(record, (metadata, exception) -> {
				if (exception != null) {
					exception.printStackTrace();
				} else {
					System.out.println("async sent : " + message.get("key") + " - " + message.get("message"));
					System.out.println("details : " + metadata.partition() + " - " + metadata.offset() + " - " + metadata.timestamp());
				}
			});
		}
	}

}
