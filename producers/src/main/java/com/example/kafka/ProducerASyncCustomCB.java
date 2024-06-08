package com.example.kafka;


import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerASyncCustomCB {


	public static void main(String[] args) throws InterruptedException {
		String topicName = "multipart_topic";

		// 카프카 프로듀서 config 설정
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// 카프카 프로듀서 생성
		KafkaProducer<Integer, String> producer = new KafkaProducer(props);

		// 프로듀서 - Record 생성
		for (int i = 0; i < 20; i++) {
			ProducerRecord<Integer, String> record = new ProducerRecord<>(topicName, i, "hello world - " + i);
			producer.send(record, new CustomCallBack(i));
		}

		Thread.sleep(3000);
		producer.close();
	}


}
