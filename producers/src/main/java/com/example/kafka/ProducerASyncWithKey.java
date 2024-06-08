package com.example.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.shaded.com.google.protobuf.StringValue;

import javax.swing.text.html.parser.Parser;
import java.util.Properties;

public class ProducerASyncWithKey {


	public static void main(String[] args) throws InterruptedException {
		String topicName = "multipart_topic";

		// 카프카 프로듀서 config 설정
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// 카프카 프로듀서 생성
		KafkaProducer<String, String> producer = new KafkaProducer(props);

		// 프로듀서 - Record 생성
		for (int i = 0; i < 20; i++) {
			ProducerRecord<String, String> record = new ProducerRecord<>(topicName, String.valueOf(i), "hello world - " + i);

			// 메시지 전송 : https://confirmed-text-b1d.notion.site/Kafka-Producer-send-4f26c8aaa0164c45b424247946ce4081?pvs=4
			producer.send(record, (m, e) -> {
				if (e != null) {
					System.out.println("error : " + e.toString());
				} else {
					System.out.println("\nRecord sent to partition " + m.partition() + "\nwith offset " + m.offset());
				}
			});
		}

		Thread.sleep(3000);
		producer.close();
	}


}
