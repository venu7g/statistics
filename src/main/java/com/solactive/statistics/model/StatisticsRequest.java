package com.solactive.statistics.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

public class StatisticsRequest implements Serializable {


    @NotEmpty
    private String instrument;
    @NotNull
    private double price;
    @NotNull
    private long timestamp;

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsRequest that = (StatisticsRequest) o;
        return Double.compare(that.price, price) == 0 &&
                timestamp == that.timestamp &&
                instrument.equals(that.instrument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, price, timestamp);
    }

    @Override
    public String toString() {
        return "StatisticsRequest{" +
                "instrument='" + instrument + '\'' +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}
