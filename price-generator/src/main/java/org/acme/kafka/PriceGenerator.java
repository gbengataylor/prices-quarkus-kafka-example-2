package org.acme.kafka;

import io.quarkus.vertx.ConsumeEvent;
import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Broadcast;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.vertx.core.Vertx;

import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

/**
 * A bean producing random prices every 5 seconds.
 * The prices are written to a Kafka topic (prices). The Kafka configuration is specified in the application configuration.
 */
@ApplicationScoped
public class PriceGenerator {

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaBootstrapServer;
    KafkaProducer<String, Integer> producer;

    
    @PostConstruct
    void initKafkaClient() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaBootstrapServer);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        producer = KafkaProducer.create(vertx, config);
    }

    private Random random = new Random();

    @Outgoing("generated-price")                        
    public Flowable<Integer> generate() {      
        Flowable<Integer> number =  Flowable.interval(10, TimeUnit.SECONDS)
                .map(tick -> random.nextInt(100));
        System.out.println("generated number " + number.toString());
        return number;
    } 


    @ConsumeEvent("prices")// consume in memroy
  // @Incoming("gen-prices") // user kafka
   // @Outgoing("generated-price")
    //@Broadcast
    public Integer generatePrice(Integer price) {            
        System.out.println("generated price " + price);
        try {
            KafkaProducerRecord<String, Integer> record = 
            KafkaProducerRecord.create("prices" /*topic*/, price);
            producer.write(record, done -> System.out.println("Kafka message sent: prices - " + price.toString()));
         } catch (Exception e) {

            // allow to run this functionality if Kafka hasn't been set up

        }
        return price;
    }        

}