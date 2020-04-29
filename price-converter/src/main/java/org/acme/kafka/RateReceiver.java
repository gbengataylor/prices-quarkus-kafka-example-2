package org.acme.kafka;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.reactive.messaging.annotations.Broadcast;


/**
 * A bean consuming data from the "rate" Kafka topic and applying some conversion.
 *
 */
@ApplicationScoped
public class RateReceiver {

    @Inject
    ConversionRate conversionRate;

    @Incoming("rate")                                 
    @Outgoing("my-rate-stream")                      
    @Broadcast                                          
    public double process(double rate) {
        System.out.println("received new rate: " + rate);
        conversionRate.setConversionRate(rate);
        return rate;
    }
}