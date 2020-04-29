package org.acme.kafka;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;
import javax.ws.rs.core.MediaType;


import io.smallrye.reactive.messaging.annotations.Channel;

/**
 * A simple resource retrieving the in-memory "my-data-stream" and sending the items as server-sent events.
 */
@Path("/rate")
public class RateResource {

    @Inject
    @Channel("my-rate-stream") Publisher<Double> rate; 

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS) 
    @SseElementType("text/plain") 
    public Publisher<Double> stream() { 
        System.out.println("streaming current rate...");
        return rate;
    }

}