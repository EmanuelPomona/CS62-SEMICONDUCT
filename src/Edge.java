/**
 *
 * Edge Class represents a directed relationship between two stocks.
 * The source stock is the potential leader, the destination stock is the
 * potential follower (its return 30 minutes later), and the weight is the
 * lead-lag Pearson correlation between them.
 */
public class Edge {

    private final String source;
    private final String destination;
    private final double weight;
    private final int observations;

    /**
     * Creates a directed edge.
     *
     * @param source the ticker of the leading stock
     * @param destination the ticker of the following stock
     * @param weight the lead-lag correlation (between -1.0 and 1.0)
     * @param observations the number of matched return pairs behind the correlation
     */
    public Edge(String source, String destination, double weight, int observations) {

        if (source == null || destination == null || source.isEmpty() || destination.isEmpty()) {
            throw new IllegalArgumentException("Edge tickers cannot be null or empty.");
        }

        if (!Double.isFinite(weight)) {
            throw new IllegalArgumentException("Edge weight must be a finite number.");
        }

        if (observations < 0) {
            throw new IllegalArgumentException("Observation count cannot be negative.");
        }

        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.observations = observations;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public int getObservations() {
        return observations;
    }

    public String toString() {
        return String.format("%s -> %s (corr=%.4f, n=%d)", source, destination, weight, observations);
    }

}