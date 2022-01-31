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
public class SingleEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingleEventsApplication.class, args);
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

    public static Person createPerson() {
        return Person.newBuilder()
                .setId(1L)
                .setNom("fode")
                .setEmail("fode@gmail.Com")
                .setAdresse("pikine")
                .build();
    }

    public static Product createProduct() {
        return Product.newBuilder()
                .setId(1L)
                .setName("HP")
                .setPrice(700.0)
                .build();
    }
}

@Service
class EventProducer {
    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);
    @Autowired
    private KafkaTemplate<String, Person> personTemplate;
    @Autowired
    private KafkaTemplate<String, Product> productTemplate;
    @Value("${topic.person-name}")
    private String personTopic;
    @Value("${topic.product-name}")
    private String productTopic;
    @Value("${topic.partitions-num}")
    private int partitions;

    public void sendPersonEvent() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            logger.info("Sending Person Event");

            var person = EventBuilder.createPerson();
            personTemplate.send(new ProducerRecord<>(personTopic, partitions, String.valueOf(person.getId()), person));

            logger.info("Person Event is sended");
        }, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    public void sendProductEvent() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            logger.info("Sending Product Event");

            var product = EventBuilder.createProduct();
            productTemplate.send(new ProducerRecord<>(productTopic, partitions, String.valueOf(product.getId()), product));

            logger.info("Product Event is sended");
        }, 3000, 3000, TimeUnit.MILLISECONDS);
    }
}

@Service
class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "product", groupId = "product-consumer")
    public void onProductEventListener(ConsumerRecord<String, Product> consumerRecord) {
        logger.info("Received Product Event");
        System.out.println(consumerRecord.value());
    }

    @KafkaListener(topics = "person", groupId = "person-consumer")
    public void onPersonEventListener(ConsumerRecord<String, Person> consumerRecord) {
        logger.info("Received Person Event");
        System.out.println(consumerRecord.value());
    }
}