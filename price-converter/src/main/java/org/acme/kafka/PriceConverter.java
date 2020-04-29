package org.acme.kafka;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A bean consuming data from the "prices" Kafka topic and applying some conversion.
 * The result is pushed to the "my-data-stream" stream which is an in-memory stream.
 */
@ApplicationScoped
public class PriceConverter {

    @Inject 
    ConversionRate conversionRate;

    @Incoming("prices")                                 
    @Outgoing("my-data-stream")                  
    @Broadcast                                          
    public double process(int priceInUsd) {
        System.out.println("received price: " + priceInUsd + " conversion rate is " + conversionRate.getConversionRate());
        return priceInUsd * conversionRate.getConversionRate();
    }

}