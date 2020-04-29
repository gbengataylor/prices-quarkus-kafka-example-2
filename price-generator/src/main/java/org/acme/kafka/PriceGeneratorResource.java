package org.acme.kafka;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class PriceGeneratorResource {

    @Inject
    PriceAndRateService service;

    @GET
    @Path("/set-price")
    @Produces(MediaType.TEXT_PLAIN)
    public Integer setPrice() { 
        return service.setPrice();
    }

    @GET
    @Path("/set-conversion-rate")
    @Produces(MediaType.TEXT_PLAIN)
    public Double setConversionRate() { 
       return service.setConversionRate(); 
    }
}