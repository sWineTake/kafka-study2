package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class SimpleKafka {

    private static final String TOPIC_NAME = "simple-topic";

    public static void main(String[] args) {

        Properties pro = new Properties();
        pro.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        pro.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pro.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pro.put(ConsumerConfig.GROUP_ID_CONFIG, "group_01");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(pro);

        // 구독
        consumer.subscribe(List.of(TOPIC_NAME));

        // poll 구현
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));// 메인쓰레드가 아닌, 폴쓰레드만 따로 작동됨
            // 가져온 레코드에 상세 메시지를 루프를 돌면서 조회
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("record.key : " + record.key() + ", record.value : " + record.value() + ", partition : " + record.partition() + "");
            }
        }

        // consumer.close();

    }

}
