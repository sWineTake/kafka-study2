package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class WakeUpKafka {

    private static final String TOPIC_NAME = "pizza_topic";

    public static void main(String[] args) {

        Properties pro = new Properties();
        pro.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        pro.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pro.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pro.put(ConsumerConfig.GROUP_ID_CONFIG, "group_01");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(pro);

        // 구독
        consumer.subscribe(List.of(TOPIC_NAME));

        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("main thread exit");
                consumer.wakeup();

                try {
                    mainThread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // poll 구현
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));// 메인쓰레드가 아닌, 폴쓰레드만 따로 작동됨
                // 가져온 레코드에 상세 메시지를 루프를 돌면서 조회
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("key : " + record.key() + ", partition : " + record.partition() + ", offset : " + record.offset() +  ", value : " + record.value());
                }
            }
        } catch (WakeupException ex) {
            System.out.println("error WakeupException");
        } finally {
            System.out.println("finally consumer is closing");
            consumer.close();
        }

    }

}
