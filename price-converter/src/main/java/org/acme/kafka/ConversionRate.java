package org.acme.kafka;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConversionRate {
    public static final double CONVERSION_RATE = 0.88;

    protected double conversionRate = CONVERSION_RATE;

	public double getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(double conversionRate) {
		this.conversionRate = conversionRate;
	}
}