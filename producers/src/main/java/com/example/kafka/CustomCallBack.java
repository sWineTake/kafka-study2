package com.example.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class CustomCallBack implements Callback {

	private int seq;
	public CustomCallBack(int seq) {
		this.seq = seq;
	}

	@Override
	public void onCompletion(RecordMetadata m, Exception e) {
		if (e != null) {
			System.out.println("error : " + e);
		} else {
			System.out.println("seq : " + seq + ", partition : " + m.partition() + ", offset : " + m.offset());
		}
	}

}
