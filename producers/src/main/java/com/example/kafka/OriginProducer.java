package com.example.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class OriginProducer {

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

		// 프로듀서에 레코드 넣어서 토픽으로 전달
		producer.send(producerRecord);
		// send는 특이한케이스임, 왜냐하면 메인쓰레드가 아닌 다른쓰레드(send network Thread 생성 전송전용 쓰레드)에서 메시지를 전송하기때문에

		// 버퍼 즉시 토픽으로 전달
		producer.flush();
		producer.close();
	}


}
