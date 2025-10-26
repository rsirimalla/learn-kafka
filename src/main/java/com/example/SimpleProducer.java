package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleProducer {
    
    public static void main(String[] args) {
        // 1. Create producer properties
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // 2. Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        // 3. Send messages
        for (int i = 0; i < 10; i++) {
            String key = "user-" + i;
            String value = "Hello Kafka! Message " + i;
            
            ProducerRecord<String, String> record = 
                new ProducerRecord<>("my-first-topic", key, value);
            
            // Asynchronous send
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception == null) {
                    System.out.printf("Sent: key=%s, partition=%d, offset=%d%n", 
                        key, metadata.partition(), metadata.offset());
                } else {
                    System.err.println("Error sending message: " + exception.getMessage());
                }
            });
        }
        
        // 4. Flush and close (important!)
        producer.flush();
        producer.close();
        
        System.out.println("Messages sent successfully!");
    }
}