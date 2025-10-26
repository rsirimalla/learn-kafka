# Kafka Quickstart Guide (2-3 Hours) - Updated for 2025

## Overview
This guide will help you learn Apache Kafka fundamentals with hands-on Docker setup and Java examples. Updated for **Apache Kafka 4.1** (latest as of October 2025), which runs entirely **without ZooKeeper** using the new **KRaft mode**.

## Part 1: Core Concepts (15 minutes)

### What Makes Kafka Different?

**Key Kafka Concepts:**

1. **Topics**: Categories/feeds where records are published (like channels)
2. **Partitions**: Topics are split into partitions for parallelism and ordering
3. **Producers**: Applications that publish messages to topics
4. **Consumers**: Applications that subscribe to topics and process messages
5. **Consumer Groups**: Multiple consumers working together to process a topic
6. **Brokers**: Kafka servers that store data and serve clients
7. **Controllers** (NEW in KRaft): Specialized Kafka nodes that manage cluster metadata using the Raft consensus protocol
8. **Offset**: Unique ID for each message in a partition (like a bookmark)

**Major Change in 2025: No More ZooKeeper!**
- **Kafka 4.0+** completely removed ZooKeeper dependency
- **KRaft mode** (Kafka Raft) is now the only way to run Kafka
- Metadata is now managed internally by Kafka controllers using the Raft consensus algorithm
- Simpler architecture, easier deployment, better scalability

**Key Differences from Simple Pub/Sub:**
- **Persistence**: Messages are stored on disk (configurable retention)
- **Replay**: Consumers can reread messages by resetting offsets
- **Ordering**: Guaranteed within a partition, not across partitions
- **Scalability**: Horizontal scaling through partitions
- **No External Dependencies**: Self-contained with KRaft (no ZooKeeper needed)

### Message Flow
```
Producer → Topic (Partition 0, 1, 2...) → Consumer Group
                                            ↓
                                    Consumer 1, 2, 3...
```

## Part 2: Docker Setup (15 minutes)

### docker-compose.yml

Create a `docker-compose.yml` file (updated for Kafka 4.x with KRaft - **No ZooKeeper needed!**):

```yaml
version: '3.8'

services:
  kafka:
    image: confluentinc/cp-kafka:7.8.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      # KRaft mode configuration (No ZooKeeper!)
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:9093'
      KAFKA_LISTENERS: 'PLAINTEXT://kafka:19092,CONTROLLER://kafka:9093,PLAINTEXT_HOST://0.0.0.0:9092'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:19092,PLAINTEXT_HOST://localhost:9092'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
      KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
      KAFKA_INTER_BROKER_LISTENER_NAME: 'PLAINTEXT'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_LOG_DIRS: '/tmp/kraft-combined-logs'
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:19092
      DYNAMIC_CONFIG_ENABLED: 'true'
```

**What's Different in 2025:**
- ✅ **No ZooKeeper** container needed!
- ✅ Single Kafka container runs in **combined mode** (broker + controller)
- ✅ Uses **KRaft** consensus protocol for metadata management
- ✅ Simpler setup with fewer components
- ✅ Using **Confluent's packaged image** for better command-line experience (Kafka binaries are in PATH)

### Start Kafka

```bash
# Start services
docker-compose up -d

# Check if containers are running (you should see kafka and kafka-ui, NO zookeeper!)
docker ps

# View Kafka UI at http://localhost:8080
```

**Note on Docker Image:** We're using Confluent's packaged Kafka image (`confluentinc/cp-kafka`) instead of the raw Apache image. Both run the same Apache Kafka core, but Confluent's image has Kafka commands pre-configured in the PATH, making it much easier to use. This is purely for convenience - you're still learning standard Apache Kafka!

### Create a Test Topic

```bash
# Enter the Kafka container
docker exec -it kafka bash

# Create a topic with 3 partitions
# Notice: commands work directly now (no need for full paths like /opt/kafka/bin/...)
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic my-first-topic \
  --partitions 3 \
  --replication-factor 1

# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Describe the topic
kafka-topics --describe --bootstrap-server localhost:9092 --topic my-first-topic

# Exit container
exit
```

**Note:** All commands now use `--bootstrap-server` instead of `--zookeeper`. This is the KRaft way!

## Part 3: Java Project Setup (15 minutes)

### Maven Project Structure

```
kafka-demo/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    ├── SimpleProducer.java
                    ├── SimpleConsumer.java
                    ├── AdvancedProducer.java
                    └── AdvancedConsumer.java
```

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>kafka-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <kafka.version>4.1.0</kafka.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>${kafka.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

**What's New in 2025:**
- ✅ **Java 17** is now required (was Java 11 in older versions)
- ✅ **Kafka 4.1.0** is the latest stable version
- ✅ Improved performance and new features like Queue semantics (preview)

## Part 4: Simple Producer (20 minutes)

### SimpleProducer.java

```java
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
```

**Key Points:**
- **Bootstrap Servers**: Initial connection point to Kafka cluster
- **Serializers**: Convert objects to bytes (String → bytes)
- **Key**: Determines which partition the message goes to (same key → same partition)
- **Asynchronous**: `send()` is non-blocking; use callbacks to confirm
- **Flush**: Ensures all messages are sent before closing

## Part 5: Simple Consumer (20 minutes)

### SimpleConsumer.java

```java
package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SimpleConsumer {
    
    public static void main(String[] args) {
        // 1. Create consumer properties
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // 2. Create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        
        // 3. Subscribe to topic(s)
        consumer.subscribe(Collections.singletonList("my-first-topic"));
        
        // 4. Poll for messages
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Received: key=%s, value=%s, partition=%d, offset=%d%n",
                        record.key(), record.value(), record.partition(), record.offset());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
```

**Key Points:**
- **Group ID**: Consumers in the same group share partitions
- **auto.offset.reset**: `earliest` (from beginning) or `latest` (new messages only)
- **Poll**: Fetches messages in batches
- **Infinite Loop**: Consumers typically run continuously

## Part 6: Advanced Concepts (30 minutes)

### AdvancedProducer.java - With Partitioning Strategy

```java
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
```

**Advanced Producer Concepts:**
- **acks=all**: Strongest durability, waits for all replicas
- **Batching**: Groups messages for efficiency
- **Partitioning**: 
  - With key: hash(key) % num_partitions
  - Without key: round-robin
  - Manual: specify partition number

### AdvancedConsumer.java - Manual Offset Control

```java
package com.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AdvancedConsumer {
    
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "advanced-consumer-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual commit
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("my-first-topic"));
        
        try {
            int messageCount = 0;
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    // Process message
                    System.out.printf("Processing: key=%s, value=%s, partition=%d, offset=%d%n",
                        record.key(), record.value(), record.partition(), record.offset());
                    
                    messageCount++;
                    
                    // Simulate processing
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Commit offsets manually after processing batch
                if (!records.isEmpty()) {
                    consumer.commitSync();
                    System.out.println("Committed offsets for " + records.count() + " messages");
                }
                
                // Stop after processing 20 messages (for demo)
                if (messageCount >= 20) {
                    break;
                }
            }
        } finally {
            consumer.close();
        }
    }
}
```

**Advanced Consumer Concepts:**
- **Manual Commit**: Control when offsets are committed
- **commitSync()**: Blocks until commit succeeds
- **commitAsync()**: Non-blocking commit
- **Rebalancing (Improved in Kafka 4.0!)**: New consumer group protocol (KIP-848) provides faster, smoother rebalances without "stop-the-world" pauses
- **Consumer Groups**: When consumers join/leave, partitions are reassigned much more efficiently than before

## Part 7: Hands-On Exercises (45 minutes)

### Exercise 1: Multiple Consumers (15 min)

Run multiple consumers in the same group to see load balancing:

```bash
# Terminal 1
java -cp target/classes com.example.SimpleConsumer

# Terminal 2
java -cp target/classes com.example.SimpleConsumer

# Terminal 3 - Run producer
java -cp target/classes com.example.SimpleProducer
```

**Observe**: Each consumer gets different partitions. Messages with the same key go to the same consumer.

### Exercise 2: Different Consumer Groups (15 min)

Create a second consumer with a different group:

```java
// Change GROUP_ID_CONFIG to "different-group"
props.put(ConsumerConfig.GROUP_ID_CONFIG, "different-group");
```

**Observe**: Both consumer groups receive all messages independently (broadcast behavior).

### Exercise 3: Offset Management (15 min)

Test offset reset:

```bash
# Reset offsets to earliest
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group my-consumer-group \
  --reset-offsets \
  --to-earliest \
  --topic my-first-topic \
  --execute

# View consumer group details
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group my-consumer-group \
  --describe
```

## Part 8: Common Patterns (15 minutes)

### 1. Exactly Once Processing

```java
// Producer
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transaction-id");

// Consumer
props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
```

### 2. Message Filtering by Partition

```java
// Assign specific partitions instead of subscribe
TopicPartition partition0 = new TopicPartition("my-first-topic", 0);
consumer.assign(Collections.singletonList(partition0));
```

### 3. Error Handling

```java
producer.send(record, (metadata, exception) -> {
    if (exception != null) {
        // Log to error topic or retry queue
        System.err.println("Failed to send: " + exception.getMessage());
    }
});
```

## Part 9: What's New in Kafka 4.0+ (2025)

### Major Changes You Should Know

1. **ZooKeeper Removed (Kafka 4.0)**
   - All clusters now run in KRaft mode
   - Simpler architecture with fewer moving parts
   - Faster metadata updates and leader elections
   - No need to manage a separate ZooKeeper ensemble

2. **New Consumer Group Protocol (KIP-848)**
   - **Faster rebalancing**: No more "stop-the-world" pauses
   - **Incremental rebalancing**: Only affected partitions are rebalanced
   - **Better for large clusters**: Scales to hundreds of consumers
   - Already enabled by default in Kafka 4.0+

3. **Queues for Kafka (KIP-932) - Preview**
   - Point-to-point messaging patterns (like traditional queues)
   - Multiple consumers can process messages from the same partition
   - Use "share groups" for queue-like behavior
   - Currently in preview, not production-ready yet

4. **Eligible Leader Replicas (ELR)**
   - Improves partition availability during failures
   - Prevents data loss during leader elections
   - Enabled by default in Kafka 4.1

5. **Java 17 Required**
   - Brokers and tools require Java 17 (not Java 11)
   - Better performance and security
   - Clients still support Java 11

### Migration Notes

If you're upgrading from older Kafka versions:
- **From Kafka 3.x with ZooKeeper**: Use Kafka 3.9 as a "bridge release" to migrate to KRaft
- **From Kafka 3.x with KRaft**: Direct upgrade to 4.x is supported
- **From Kafka 2.x**: Must upgrade to 3.x first, then to 4.x

## Part 10: Key Takeaways

### When to Use Kafka
✅ High-throughput event streaming
✅ Message replay needed
✅ Ordered processing within categories
✅ Multiple consumers need same data
✅ Durable message storage

### Kafka vs. Traditional Message Queues
- **Kafka**: Distributed commit log, message replay, high throughput
- **RabbitMQ**: Traditional queuing, complex routing, lower throughput
- **AWS SQS**: Simple queuing, no message replay, fully managed

### Best Practices
1. **Keys**: Use meaningful keys for ordering and partitioning
2. **Partitions**: More partitions = more parallelism (but more overhead)
3. **Consumer Groups**: One group per application/use case
4. **Error Handling**: Use dead letter topics for failed messages
5. **Monitoring**: Track lag, throughput, and consumer health

## Part 11: What's Next?

### Immediate Next Steps:
1. Add JSON serialization with Jackson
2. Implement error handling and retry logic
3. Create a dead letter queue pattern
4. Add monitoring with JMX metrics
5. Experiment with the new consumer group protocol

### Advanced Topics (2025 Edition):
- **Kafka Streams** for stream processing with the new rebalance protocol
- **Schema Registry** for message evolution
- **Kafka Connect** for integrations
- **Share Groups** (preview) for queue-like behavior
- **KRaft controller management** and dynamic membership
- **Kubernetes deployment** with KRaft mode
- **Multi-datacenter replication** with Cluster Linking

## Useful Commands Cheatsheet

```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Create topic
docker exec -it kafka kafka-topics --create --topic my-topic --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092

# Delete topic
docker exec -it kafka kafka-topics --delete --topic my-topic --bootstrap-server localhost:9092

# Console producer
docker exec -it kafka kafka-console-producer --topic my-first-topic --bootstrap-server localhost:9092

# Console consumer (from beginning)
docker exec -it kafka kafka-console-consumer --topic my-first-topic --from-beginning --bootstrap-server localhost:9092

# List consumer groups
docker exec -it kafka kafka-consumer-groups --list --bootstrap-server localhost:9092

# View consumer group lag
docker exec -it kafka kafka-consumer-groups --describe --group my-consumer-group --bootstrap-server localhost:9092

# View KRaft metadata (NEW in Kafka 4.0+)
docker exec -it kafka kafka-metadata-quorum --bootstrap-server localhost:9092 describe --status

# View cluster metadata
docker exec -it kafka kafka-metadata-shell --snapshot /tmp/kraft-combined-logs/__cluster_metadata-0/00000000000000000000.log --print

# Stop all
docker-compose down
```

**Note:** All commands now use `--bootstrap-server` instead of the old `--zookeeper` parameter. This is a key change in Kafka 4.0+!

## Troubleshooting

**"kafka-topics: command not found" error:**
- If you see this error, you might be using the raw `apache/kafka` image instead of `confluentinc/cp-kafka`
- Solution: Use the Confluent image as shown in the tutorial (it has better PATH configuration)
- Alternative: Use full paths like `/opt/kafka/bin/kafka-topics.sh` if using Apache's image

**Port already in use:**
```bash
# Change ports in docker-compose.yml
ports:
  - "9094:9092"  # Change first number
```

**Cannot connect to Kafka:**
- Check containers are running: `docker ps`
- Check logs: `docker logs kafka`
- Ensure ADVERTISED_LISTENERS matches your connection
- **Note**: No ZooKeeper to troubleshoot anymore!

**Consumer not receiving messages:**
- Check consumer group offsets
- Verify topic exists
- Check auto.offset.reset setting

**KRaft-specific issues:**
- If cluster won't start, check `CLUSTER_ID` is set correctly
- Ensure `KAFKA_PROCESS_ROLES` includes 'broker,controller' for single-node
- Check `KAFKA_CONTROLLER_QUORUM_VOTERS` configuration

**Java version errors:**
- Kafka 4.0+ requires **Java 17** for brokers and tools
- Kafka clients support Java 11+
- Update your Java installation if needed

## Resources

**Official Documentation (2025):**
- [Kafka 4.1 Documentation](https://kafka.apache.org/documentation/)
- [KRaft Mode Overview](https://kafka.apache.org/documentation/#kraft)
- [Kafka Improvement Proposals (KIPs)](https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Improvement+Proposals)

**Learning Resources:**
- [Confluent Kafka Tutorials](https://kafka-tutorials.confluent.io/)
- [Kafka: The Definitive Guide (Book)](https://www.confluent.io/resources/kafka-the-definitive-guide/)
- [Apache Kafka 4.0 Release Blog](https://www.confluent.io/blog/latest-apache-kafka-release/)

**What's New in 2025:**
- [KIP-848: Next-Gen Consumer Rebalance Protocol](https://cwiki.apache.org/confluence/display/KAFKA/KIP-848)
- [KIP-932: Queues for Kafka](https://cwiki.apache.org/confluence/display/KAFKA/KIP-932)
- [Migration from ZooKeeper to KRaft Guide](https://kafka.apache.org/documentation/#kraft_zk_migration)

Happy Learning! 🚀

---
**Last Updated:** October 2025 | **Kafka Version:** 4.1.0 | **Mode:** KRaft (No ZooKeeper)