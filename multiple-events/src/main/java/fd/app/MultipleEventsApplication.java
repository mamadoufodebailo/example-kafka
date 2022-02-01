package fd.app;

import fd.app.avro.Person;
import fd.app.avro.Product;
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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EnableKafka
@SpringBootApplication
public class MultipleEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleEventsApplication.class, args);
    }

    @Bean
    public CommandLineRunner start(EventProducer eventProducer) {
        return args -> {
            eventProducer.sendProductEvent();
            eventProducer.sendPersonEvent();
        };
    }
}

class EventBuilder {
    private EventBuilder() {}

    public static Person createEventPerson() {
        return Person.newBuilder()
                .setId(1L)
                .setNom("fode")
                .setEmail("fode.diallo@baamtu.com")
                .setAdresse("pikine")
                .build();
    }

    public static Product createEventProduct() {
        return Product.newBuilder()
                .setId(1L)
                .setName("HP")
                .setPrice(1000.0)
                .build();
    }
}

@Service
class EventProducer {
    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);
    @Autowired
    private KafkaTemplate<String, Product> productKafkaTemplate;
    @Autowired
    private KafkaTemplate<String, Person> personKafkaTemplate;
    @Value("${topic.name}")
    private String topicName;
    @Value("${topic.partitions-num}")
    private int partitions;

    public void sendProductEvent() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            logger.info("Sending Product Event");

            var product = EventBuilder.createEventProduct();
            productKafkaTemplate.send(new ProducerRecord<>(topicName, partitions, String.valueOf(product.getId()), product));

            logger.info("Product Event is Sended");
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void sendPersonEvent() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            logger.info("Sending Person event");

            var person = EventBuilder.createEventPerson();
            personKafkaTemplate.send(new ProducerRecord<>(topicName, partitions, String.valueOf(person.getId()), person));

            logger.info("Person Event is Sended");
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}

@Service
class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "all-events", groupId = "consumer-person-events")
    public void onPersonMessageListener(ConsumerRecord<String, Person> record) {
        logger.info("Receiving event Person");
        System.out.println(record.key() +" - "+ record.value());
    }

    @KafkaListener(topics = "all-events", groupId = "consumer-product-events")
    public void onProductMessageListener(ConsumerRecord<String, Product> record) {
        logger.info("Receiving event Product");
        System.out.println(record.key() +" - "+ record.value());
    }
}