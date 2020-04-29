package org.acme.kafka;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

/**
 * Bean that Receives in-memory channel and uses kakfa app.properties to write to kafka topic
 */
@ApplicationScoped
public class ConversionRate {
    @Incoming("in-mem-conversion-rate")
    @Outgoing("conversion-rate")
    public Double sendRate(Double rate) {
        return rate;
    }
}