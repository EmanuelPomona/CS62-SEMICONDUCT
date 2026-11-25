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

        }
    }
    
}
