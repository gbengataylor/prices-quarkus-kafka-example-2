package org.acme.kafka;

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.annotations.Stream;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.axle.core.eventbus.EventBus;

@ApplicationScoped
public class PriceAndRateService {

    @Inject EventBus bus;
   private Random random = new Random();
    
	public Integer setPrice() {
        int price = random.nextInt(100);
        // send price to in-memory bus
        bus.<Integer>send("in-mem-prices", Integer.valueOf(price));
        return price;
    }
    
    @Inject
    @Channel("in-mem-conversion-rate") // map to kafka topic
    //@Stream
    Emitter<Double> emitter;

	public Double setConversionRate() {
       double rate = random.nextDouble();
       emitter.send(rate);
       return rate;
	}
}