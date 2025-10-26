package com.example;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class AdvancedProducer {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Performance tuning
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Batch messages for 10ms
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batch size
        
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        // Example 1: Synchronous send (wait for confirmation)
        ProducerRecord<String, String> record1 = 
            new ProducerRecord<>("my-first-topic", "key1", "Sync message");
        RecordMetadata metadata = producer.send(record1).get(); // .get() blocks
        System.out.println("Sync send - Partition: " + metadata.partition() + 
                          ", Offset: " + metadata.offset());
        
        // Example 2: Send to specific partition
        ProducerRecord<String, String> record2 = 
            new ProducerRecord<>("my-first-topic", 2, "key2", "Message to partition 2");
        producer.send(record2);
        
        // Example 3: Send without key (round-robin to partitions)
        ProducerRecord<String, String> record3 = 
            new ProducerRecord<>("my-first-topic", "No key message");
        producer.send(record3);
        
        producer.flush();
        producer.close();
    }
}