import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CorrelationCalculator {

    private static final long LAG_MINUTES = 30;
    private static final int MIN_OBSERVATIONS = 30;

    /**
     * Stores a correlation coefficient and the number of matched
     * observations used to calculate it.
     */
    public static class CorrelationResult {

        private final double correlation;
        private final int observations;

        public CorrelationResult(double correlation, int observations) {
            this.correlation = correlation;
            this.observations = observations;
        }

        public double getCorrelation() {
            return correlation;
        }

        public int getObservations() {
            return observations;
        }

        public boolean isValid() {
            return Double.isFinite(correlation);
        }
    }

    /**
     * Calculates the Pearson correlation between one stock's returns
     * and another stock's returns 30 minutes later.
     *
     * @param source returns of the potentially leading stock
     * @param target returns of the potentially following stock
     * @return the correlation coefficient and matched observation count
     */
    public static CorrelationResult calculateCorrelation(HashMap<Instant, Double> source, HashMap<Instant, Double> target) {

        double sumX = 0;
        double sumY = 0;
        int count = 0;

        Duration lag = Duration.ofMinutes(LAG_MINUTES);

        // Match source returns with target returns 30 minutes later.
        for (Map.Entry<Instant, Double> entry : source.entrySet()) {

            Instant targetTime = entry.getKey().plus(lag);
            Double targetReturn = target.get(targetTime);
            if (targetReturn == null) {
                continue;
            }

            double sourceReturn = entry.getValue();

            if (!Double.isFinite(sourceReturn) || !Double.isFinite(targetReturn)) {
                continue;
            }

            sumX += sourceReturn;
            sumY += targetReturn;
            count++;
        }

        if (count < MIN_OBSERVATIONS) {
            return new CorrelationResult(Double.NaN, count);
        }

        double meanX = sumX / count;
        double meanY = sumY / count;

        double numerator = 0;
        double sumSquaredX = 0;
        double sumSquaredY = 0;

        // Calculate covariance and squared deviations.
        for (Map.Entry<Instant, Double> entry : source.entrySet()) {

            Instant targetTime = entry.getKey().plus(lag);
            Double targetReturn = target.get(targetTime);

            if (targetReturn == null) {
                continue;
            }

            double sourceReturn = entry.getValue();

            if (!Double.isFinite(sourceReturn) || !Double.isFinite(targetReturn)) {
                continue;
            }

            double deviationX = sourceReturn - meanX;
            double deviationY = targetReturn - meanY;

            numerator += deviationX * deviationY;

            sumSquaredX += deviationX * deviationX;
            sumSquaredY += deviationY * deviationY;
        }

        double denominator = Math.sqrt(sumSquaredX * sumSquaredY);

        // Correlation is undefined if either series has zero variance.
        if (denominator == 0) {
            return new CorrelationResult(Double.NaN, count);
        }

        double correlation = numerator / denominator;

        if (!Double.isFinite(correlation)) {
            return new CorrelationResult(Double.NaN, count);
        }

        // Protect against minor floating-point rounding errors.
        correlation = Math.max(-1.0, Math.min(1.0, correlation));

        return new CorrelationResult(correlation, count);
    }

    public static void main(String[] args) throws IOException {

        HashMap<String, HashMap<Instant, Double>> returns = ReturnCalculator.calculateReturns(DataLoader.loadData("data/semiconductor_prices.csv"));

        String[] tickers = {"NVDA", "AMD", "AVGO", "TSM", "INTC"};

        System.out.println("LEAD-LAG CORRELATION ANALYSIS");
        System.out.println("Lag: 30 minutes");
        System.out.println("==============================================");

        System.out.printf("%-8s %-8s %12s %14s%n", "Source", "Target", "Correlation", "Observations");

        for (String source : tickers) {

            for (String target : tickers) {

                if (source.equals(target)) {
                    continue;
                }

                if (!returns.containsKey(source) || !returns.containsKey(target)) {
                    continue;
                }

                CorrelationResult result = calculateCorrelation(returns.get(source), returns.get(target));

                System.out.printf("%-8s %-8s %12.4f %14d%n", source, target, result.getCorrelation(), result.getObservations());
            }
        }
    }
}
