import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 
 * ReturnCalculator Class that calculates the returns from historical stock prices.
 * We cannot compare stock movements soley on their dollar price changes/movements asset X could have prev and curr of
 * $100 and $105 and the return is 5%, and asset Y could be $200 and $210 and so the return is also 5% however their dollar
 * movement is NOT the same so thats why we need to calculate their percentages. This is calculated using current minus previous divided by previous. 
 */
public class ReturnCalculator {

    private static final long INTERVAL_MINUTES = 30;

    /**
     * Calculates the percentage return between two closing prices.
     *
     * @param previous the previous closing price
     * @param current the current closing price
     * @return the percentage return as a decimal
     */
    public static double calculateReturn(double previous, double current) {
        return (current - previous) / previous;
    }

    /**
     * Calculates historical 30-minute returns for every stock.
     *
     * @param stockData chronological price histories organized by ticker
     * @return stock returns organized by ticker and timestamp
     */
    public static HashMap<String, HashMap<Instant, Double>> calculateReturns(HashMap<String, ArrayList<PricePoint>> stockData) {

        HashMap<String, HashMap<Instant, Double>> allReturns = new HashMap<>();

        for (String ticker : stockData.keySet()) {

            ArrayList<PricePoint> prices = stockData.get(ticker);
            HashMap<Instant, Double> stockReturns = new HashMap<>();

            for (int i = 1; i < prices.size(); i++) {

                PricePoint previous = prices.get(i - 1);
                PricePoint current = prices.get(i);

                Duration elapsed = Duration.between(previous.getTimestamp(), current.getTimestamp());

                // Skip overnight gaps and missing trading intervals.
                if (!elapsed.equals(Duration.ofMinutes(INTERVAL_MINUTES))) {
                    continue;
                }

                double stockReturn = calculateReturn(previous.getClose(), current.getClose());
                stockReturns.put(current.getTimestamp(), stockReturn);
            }

            allReturns.put(ticker, stockReturns);
        }

        return allReturns;
    }

    public static void main(String[] args) throws IOException {

        HashMap<String, ArrayList<PricePoint>> stockData = DataLoader.loadData("data/semiconductor_prices.csv");

        HashMap<String, HashMap<Instant, Double>> returns = calculateReturns(stockData);

        int totalReturns = 0;

        ArrayList<String> tickers = new ArrayList<>(returns.keySet());
        Collections.sort(tickers);

        System.out.println("RETURN CALCULATION COMPLETE");
        System.out.println("==============================");

        for (String ticker : tickers) {

            int count = returns.get(ticker).size();
            System.out.println(ticker + ": " + count + " returns");
            totalReturns += count;
        }

        System.out.println("==============================");
        System.out.println("Total assets: " + returns.size());
        System.out.println("Total returns: " + totalReturns);

        // Display the first five NVIDIA returns.
        if (returns.containsKey("NVDA")) {

            HashMap<Instant, Double> nvdaReturns = returns.get("NVDA");
            ArrayList<Instant> timestamps = new ArrayList<>(nvdaReturns.keySet());

            Collections.sort(timestamps);

            System.out.println("\nSample NVDA returns:");

            for (int i = 0; i < Math.min(5, timestamps.size()); i++) {

                Instant timestamp = timestamps.get(i);
                double value = nvdaReturns.get(timestamp);

                System.out.printf("%s | %+.4f%%%n", timestamp, value * 100);
            }
        }
    }
    
}
