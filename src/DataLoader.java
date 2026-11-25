import java.time.Instant;


public class DataLoader {

    private final Instant timestamp;
    private final double close;

    public PricePoint(Instant timestamp, double close) {
        this.timestamp = timestamp;
        this.close = close;
    }
    
}
