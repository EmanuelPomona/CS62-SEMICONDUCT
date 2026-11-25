import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * TheInfluenceGraph Class implements a directed weighted graph of stocks.
 * Each vertex is a ticker and each Edge points from a leading stock to a
 * following stock. We keep two adjacency lists: outgoing (who does this stock
 * lead?) and incoming (who leads this stock?). Storing both makes "top-k leaders"
 * and "top-k followers" lookups quick without scanning every edge.
 * There is at most one edge per ordered pair (A -> B), and no self-loops.
 */
public class InfluenceGraph {

    private final HashMap<String, ArrayList<Edge>> outgoing;
    private final HashMap<String, ArrayList<Edge>> incoming;
    private int edgeCount;

    public InfluenceGraph() {
        outgoing = new HashMap<>();
        incoming = new HashMap<>();
        edgeCount = 0;
    }

    /**
     * Adds a vertex if it is not already in the graph.
     *
     * @param ticker the ticker symbol
     */
    public void addVertex(String ticker) {

        if (ticker == null || ticker.isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty.");
        }

        if (!outgoing.containsKey(ticker)) {
            outgoing.put(ticker, new ArrayList<>());
            incoming.put(ticker, new ArrayList<>());
        }
    }

    /**
     * Adds a directed edge. Missing vertices are created automatically.
     * If an edge for the same (source, destination) pair already exists,
     * it is replaced by the new one.
     *
     * @param edge the edge to add
     */
    public void addEdge(Edge edge) {

        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null.");
        }

        String source = edge.getSource();
        String destination = edge.getDestination();

        if (source.equals(destination)) {
            throw new IllegalArgumentException("Self-loops are not allowed.");
        }

        addVertex(source);
        addVertex(destination);

        removeEdge(source, destination);

        outgoing.get(source).add(edge);
        incoming.get(destination).add(edge);
        edgeCount++;
    }

    /**
     * Removes the edge from source to destination if it exists.
     *
     * @param source the leading ticker
     * @param destination the following ticker
     * @return true if an edge was removed
     */
    public boolean removeEdge(String source, String destination) {

        if (!hasVertex(source) || !hasVertex(destination)) {
            return false;
        }

        Edge existing = getEdge(source, destination);

        if (existing == null) {
            return false;
        }

        outgoing.get(source).remove(existing);
        incoming.get(destination).remove(existing);
        edgeCount--;

        return true;
    }

    public boolean hasVertex(String ticker) {
        return outgoing.containsKey(ticker);
    }

    public boolean hasEdge(String source, String destination) {
        return getEdge(source, destination) != null;
    }

    /**
     * Finds the edge from source to destination.
     *
     * @return the edge, or null if there is none
     */
    public Edge getEdge(String source, String destination) {

        ArrayList<Edge> edges = outgoing.get(source);

        if (edges == null) {
            return null;
        }

        for (Edge edge : edges) {
            if (edge.getDestination().equals(destination)) {
                return edge;
            }
        }

        return null;
    }

    /**
     * @param ticker the stock
     * @return a read-only list of edges leaving this stock (stocks it leads)
     */
    public List<Edge> getOutgoingEdges(String ticker) {
        checkVertex(ticker);
        return Collections.unmodifiableList(outgoing.get(ticker));
    }

    /**
     * @param ticker the stock
     * @return a read-only list of edges entering this stock (stocks that lead it)
     */
    public List<Edge> getIncomingEdges(String ticker) {
        checkVertex(ticker);
        return Collections.unmodifiableList(incoming.get(ticker));
    }

    public int outDegree(String ticker) {
        checkVertex(ticker);
        return outgoing.get(ticker).size();
    }

    public int inDegree(String ticker) {
        checkVertex(ticker);
        return incoming.get(ticker).size();
    }

    /**
     * @return a sorted list of every ticker in the graph
     */
    public ArrayList<String> getVertices() {
        ArrayList<String> vertices = new ArrayList<>(outgoing.keySet());
        Collections.sort(vertices);
        return vertices;
    }

    /**
     * @return every edge in the graph
     */
    public ArrayList<Edge> getAllEdges() {
        ArrayList<Edge> edges = new ArrayList<>();

        for (ArrayList<Edge> list : outgoing.values()) {
            edges.addAll(list);
        }

        return edges;
    }

    public int vertexCount() {
        return outgoing.size();
    }

    public int edgeCount() {
        return edgeCount;
    }

    private void checkVertex(String ticker) {
        if (!hasVertex(ticker)) {
            throw new IllegalArgumentException("Unknown ticker: " + ticker);
        }
    }

    public static void main(String[] args) {

        InfluenceGraph graph = new InfluenceGraph();

        graph.addEdge(new Edge("NVDA", "AMD", 0.12, 500));
        graph.addEdge(new Edge("NVDA", "TSM", 0.08, 500));
        graph.addEdge(new Edge("AMD", "NVDA", -0.05, 500));
        graph.addEdge(new Edge("TSM", "INTC", 0.10, 480));

        System.out.println("INFLUENCE GRAPH TEST");
        System.out.println("==============================");
        System.out.println("Vertices: " + graph.vertexCount());
        System.out.println("Edges: " + graph.edgeCount());

        for (String ticker : graph.getVertices()) {
            System.out.println("\n" + ticker + " (out=" + graph.outDegree(ticker) + ", in=" + graph.inDegree(ticker) + ")");

            for (Edge edge : graph.getOutgoingEdges(ticker)) {
                System.out.println("  leads:    " + edge);
            }

            for (Edge edge : graph.getIncomingEdges(ticker)) {
                System.out.println("  led by:   " + edge);
            }
        }

        graph.addEdge(new Edge("NVDA", "AMD", 0.20, 510));
        System.out.println("\nAfter replacing NVDA -> AMD, edges: " + graph.edgeCount());
        System.out.println(graph.getEdge("NVDA", "AMD"));
    }

}