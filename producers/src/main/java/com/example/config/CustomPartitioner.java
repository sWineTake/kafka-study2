package com.example.config;


import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.StickyPartitionCache;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

public class CustomPartitioner implements Partitioner {

    private final StickyPartitionCache stickyPartitionCache = new StickyPartitionCache();
    private String specialKeyName;

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitionInfos = cluster.partitionsForTopic(topic);
        // 파티션 갯수
        int numPartitions = partitionInfos.size();
        // 스페셜 파티션으로 보낼 범위
        int numSpecPartitions = (int)(numPartitions * 0.5);
        int partitionIndex = 0;

        if (keyBytes == null) {
            throw new InvalidRecordException("키 값은 필수입력입니다.");
        }

        if (key.equals(specialKeyName)) {
            partitionIndex = Utils.toPositive(Utils.murmur2(valueBytes)) % numSpecPartitions;
        } else {
            partitionIndex = (Utils.toPositive(Utils.murmur2(keyBytes)) % (numPartitions - numSpecPartitions)) + numSpecPartitions;
        }

        System.out.println("key : " + key.toString() + ", is send to partitions : " + partitionIndex);
        return partitionIndex;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        specialKeyName = configs.get("custom.specialKey").toString();

    }

    @Override
    public void close() {
        // 파티셔너가 종료될때 사용는거라 선언 X
    }
}
