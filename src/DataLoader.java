import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class DataLoader {

    public static HashMap<String, ArrayList<PricePoint>> loadData(String filePath) throws IOException {
        HashMap<String, ArrayList<PricePoint>> stockData = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String header = reader.readLine();

            if (!"timestamp,ticker,open,high,low,close,volume".equals(header)) {
                throw new IOException("Unexpected CSV header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",", -1);

                if (values.length != 7) {
                    throw new IOException("Invalid CSV row at line " + lineNumber);
                }

                Instant timestamp = Instant.parse(values[0]);
                String ticker = values[1].trim();
                double close = Double.parseDouble(values[5]);

                if (ticker.isEmpty() || !Double.isFinite(close) || close <= 0) {
                    throw new IOException("Invalid stock data at line " + lineNumber);
                }

                PricePoint point = new PricePoint (timestamp, close);

                if (!stockData.containsKey(ticker)) {
                    stockData.put(ticker, new ArrayList<>());
                }

                stockData.get(ticker).add(point);
            }

        }

        for (ArrayList<PricePoint> prices : stockData.values()) {
            prices.sort(Comparator.comparing(PricePoint::getTimestamp));

            for (int i = 1; i < prices.size(); i++) {
                if (prices.get(i).getTimestamp().equals(prices.get(i-1).getTimestamp())) {
                    throw new IOException("Duplicate timestamp.");
                }
            } 
        }

        return stockData;
    }

    public static void main(String[] args) throws IOException {
        HashMap<String, ArrayList<PricePoint>> stockData = loadData("data/semiconductor_prices.csv");

        int totalRecords = 0;

        ArrayList<String> tickers = new ArrayList<>(stockData.keySet());

        Collections.sort(tickers);

        System.out.println("DATA LOADING COMPLETE");
        System.out.println("==============================");
        System.out.println("Total assets: " + stockData.size());

        for (String ticker : tickers) {

            int count = stockData.get(ticker).size();
            System.out.println(ticker + ": " + count + " records");

            totalRecords += count;
        }

        System.out.println("==============================");
        System.out.println("Total records: " + totalRecords);

        if (totalRecords < 1000) {
            throw new IOException("Insufficient dataset size.");
        }

    }
    
}
