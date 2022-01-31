package fd.app;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EnableKafka
@SpringBootApplication
public class TextEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextEventsApplication.class, args);
    }

    @Bean
    public CommandLineRunner start(EventProducer eventProducer) {
        return args -> eventProducer.sendMessage();
    }
}

class EventBuilder {
    private EventBuilder() {}

    public static String createEvent() {
        return UUID.randomUUID().toString();
    }
}

@Service
class EventProducer {
    private static Logger logger = LoggerFactory.getLogger(EventProducer.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${topic.name}")
    private String topic;
    @Value("${topic.partitions-num}")
    private int partitions;

    public void sendMessage() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            logger.info("Sending Message !!!");

            String message = EventBuilder.createEvent();
            kafkaTemplate.send(new ProducerRecord<>(topic, partitions, String.valueOf(System.currentTimeMillis()), message));

            logger.info("Message sended");
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}

@Service
class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "text", groupId = "text-consumer")
    public void onMessageListener(ConsumerRecord<String, String> consumerRecord) {
        logger.info("Receiving Message");
        System.out.println(consumerRecord.key() +" - "+ consumerRecord.value());
    }
}