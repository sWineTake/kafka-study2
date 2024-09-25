package com.example.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ASyncProducerWithIntegerKey {

	public static void main(String[] args) throws InterruptedException {
		// 카프카 프로듀서 객체 config 설정
		String topicName = "multipart-topic";

		Properties properties = new Properties();
		// bootstrap.servers, key.serializer.class, value.serializer.class
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// 카프카 프로듀서 객체 생성
		KafkaProducer<Integer, String> producer = new KafkaProducer<>(properties);

		// 프로듀서 레코드 객체 생성
		for (int i = 0; i < 20; i++) {
			ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(
				topicName // 토픽 명
				, i , "hello world" + i // 값
			);

			// 프로듀서에 레코드 넣어서 토픽으로 전달 -> 동기로 진행
			producer.send(producerRecord,
				// 해당 콜백은 메인쓰레드가 아닌 sendNetWorkThread가 전송함
				(data, e) -> {
					if (e != null) {
						// error
						e.printStackTrace();
						return;
					}

					// 정상적으로 완료시 브로커로부터 데이터값을 받음
					System.out.println("###### record metadata received #####");
					System.out.println(data.partition());
					System.out.println(data.offset());
					System.out.println(data.timestamp());
				});
		}

		// 메인쓰레드가 종류되면 sendNetWorkThread도 종료되기에 main쓰레드를 잠시 정지시킴
		Thread.sleep(3000);
		producer.close();
	}


}
