package com.practice.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class FileProducer {

	private static final String TOPIC_NAME = "file_topic";
	private static final String FILE_PATCH = "/Users/twocowsong/Documents/01.study/02.source/01.java/02.inflean/09.kafka/kafka/practice/src/main/resources/pizza_sample.txt";

	public static void main(String[] args) throws InterruptedException {

		// 카프카 프로듀서 config 설정
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// 카프카 프로듀서 생성
		KafkaProducer<String, String> producer = new KafkaProducer(props);
		sendFileMessage(producer);

		Thread.sleep(3000);
		producer.close();
	}

	private static void sendFileMessage(KafkaProducer<String, String> producer) {
		try {
			FileReader fileReader = new FileReader(FILE_PATCH);
			BufferedReader bf = new BufferedReader(fileReader);

			String line = "";
			while ( (line = bf.readLine()) != null) {
				String[] split = line.split(",");
				StringBuffer value = new StringBuffer();

				for (int i = 1; i < split.length; i++) {
					value.append(split[i]);
					if (i != split.length) value.append(",");
				}

				sendMessage(producer, split[0].toString(), value.toString());
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static void sendMessage(KafkaProducer<String, String> producer, String key, String value) {
		ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);
		System.out.println("key : " + key + ", value : " + value);

		producer.send(record, (m, e) -> {
			if (e != null) {
				System.out.println("error : " + e.toString());
			} else {
				System.out.println("\nRecord sent to partition " + m.partition() + "\nwith offset " + m.offset());
			}
		});

	}

}
