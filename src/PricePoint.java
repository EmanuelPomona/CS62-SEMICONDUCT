import java.time.Instant;

public class PricePoint {

    private final Instant timestamp;
    private final double close;

    public PricePoint(Instant timestamp, double close) {
        this.timestamp = timestamp;
        this.close = close;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double getClose() {
        return close;
    }

    public String toString() {
        return timestamp + " |  $" + close;
    }
    
}
