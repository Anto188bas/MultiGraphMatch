import algorithms.*;
import com.google.gson.Gson;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class algorithms_test {
    public static void main(String[] args) throws IOException {
        File ShortestPathDir = new File("./OutputTest/ShortestPath");
        if (!ShortestPathDir.exists()){ ShortestPathDir.mkdirs(); }
        File CentralityDir = new File("./OutputTest/Centrality");
        if (!CentralityDir.exists()){ CentralityDir.mkdirs(); }
        File ClusteringDir = new File("./OutputTest/Clustering");
        if (!ClusteringDir.exists()){ ClusteringDir.mkdirs(); }

        Graph<Integer, RelationshipEdge> directedGraph = new SimpleDirectedGraph<>(RelationshipEdge.class);
        directedGraph.addVertex(0);
        directedGraph.addVertex(1);
        directedGraph.addVertex(2);
        directedGraph.addVertex(3);
        directedGraph.addVertex(4);
        directedGraph.addVertex(5);
        directedGraph.addVertex(6);
        directedGraph.addVertex(7);
        directedGraph.addVertex(8);

        directedGraph.addEdge(0, 1, new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(1, 3, new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(3, 2, new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge(2, 0, new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(4, 3, new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(4, 5, new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(5, 6, new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(6, 4, new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge(7, 4, new RelationshipEdge("FRIEND"));


        FileWriter writer;

        //ShortestPath testing writing JSON
        ShortestPath s = new ShortestPath(directedGraph);
        Gson gson = new Gson();

        File DijkstraSP = new File("./OutputTest/ShortestPath/DijkstraSP.json");
        writer = new FileWriter(DijkstraSP);
        System.out.println("Dijkstra Shortest Path");
        System.out.println(gson.toJson(s.DijkstraSP(0, 2)));
        writer.write(gson.toJson(s.DijkstraSP(0, 2)));
        writer.flush();
        writer.close();

        File DijkstraAllSP = new File("./OutputTest/ShortestPath/DijkstraAllSP.json");
        writer = new FileWriter(DijkstraAllSP);
        System.out.println("\n" + "Dijkstra All Shortest Path");
        System.out.println(gson.toJson(s.DijkstraAllSP(0)));
        writer.write(gson.toJson(s.DijkstraAllSP(0)));
        writer.flush();
        writer.close();

        File BellmanFordSP = new File("./OutputTest/ShortestPath/BellmanFordSP.json");
        writer = new FileWriter(BellmanFordSP);
        System.out.println("\n" + "Bellman-Ford Shortest Path");
        System.out.println(gson.toJson(s.BellmanFordSP(0, 2)));
        writer.write(gson.toJson(s.BellmanFordSP(0, 2)));
        writer.flush();
        writer.close();

        File BellmanFordAllSP = new File("./OutputTest/ShortestPath/BellmanFordAllSP.json");
        writer = new FileWriter(BellmanFordAllSP);
        System.out.println("\n" + "Bellman-Ford All Shortest Path");
        System.out.println(gson.toJson(s.BellmanFordAllSP(0)));
        writer.write(gson.toJson(s.BellmanFordAllSP(0)));
        writer.flush();
        writer.close();

        File FloydWarshallSP = new File("./OutputTest/ShortestPath/FloydWarshallSP.json");
        writer = new FileWriter(FloydWarshallSP);
        System.out.println("\n" + "Floyd-Warshall Shortest Path");
        System.out.println(gson.toJson(s.FloydWarshallSP()));
        writer.write(gson.toJson(s.FloydWarshallSP()));
        writer.flush();
        writer.close();


        //Centrality testing
        Centrality c = new Centrality(directedGraph);

        File Betweenness = new File("./OutputTest/Centrality/Betweenness.json");
        writer = new FileWriter(Betweenness);
        System.out.println("\nBetweenness centrality:");
        System.out.println(c.Betweenness());
        writer.write(gson.toJson(c.Betweenness()));
        writer.flush();
        writer.close();

        File Closeness = new File("./OutputTest/Centrality/Closeness.json");
        writer = new FileWriter(Closeness);
        System.out.println("\nCloseness centrality:");
        System.out.println(c.Closeness());
        writer.write(gson.toJson(c.Closeness()));
        writer.flush();
        writer.close();

        File EigenVector = new File("./OutputTest/Centrality/EigenVector.json");
        writer = new FileWriter(EigenVector);
        System.out.println("\nEigenVector centrality:");
        System.out.println(c.EigenVector());
        writer.write(gson.toJson(c.EigenVector()));
        writer.flush();
        writer.close();

        File PageRank = new File("./OutputTest/Centrality/PageRank.json");
        writer = new FileWriter(PageRank);
        System.out.println("\nPageRank centrality:");
        System.out.println(c.PageRank());
        writer.write(gson.toJson(c.PageRank()));
        writer.flush();
        writer.close();

        File ClusteringCoefficient = new File("./OutputTest/Centrality/ClusteringCoefficient.json");
        writer = new FileWriter(ClusteringCoefficient);
        System.out.println("\nClustering coefficient:");
        System.out.println(c.ClusteringCoefficient());
        writer.write(gson.toJson(c.ClusteringCoefficient()));
        writer.flush();
        writer.close();

        File AverageClusteringCoefficient = new File("./OutputTest/Centrality/AverageClusteringCoefficient.json");
        writer = new FileWriter(AverageClusteringCoefficient);
        System.out.println("\nAverage clustering coefficient:");
        System.out.println(c.AverageClusteringCoefficient());
        writer.write(gson.toJson(c.AverageClusteringCoefficient()));
        writer.flush();
        writer.close();

        //Clustering testing
        Clustering C = new Clustering(directedGraph, 3);

        File Clustering = new File("./OutputTest/Clustering/Clustering.json");
        writer = new FileWriter(Clustering);
        System.out.println("\n"+C.getClustering());
        writer.write(gson.toJson(C.getClustering()));
        writer.flush();
        writer.close();

        Clustering C2 = new Clustering(directedGraph);

        File Clustering2 = new File("./OutputTest/Clustering/Clustering2.json");
        writer = new FileWriter(Clustering2);
        System.out.println(C2.getClustering());
        writer.write(gson.toJson(C.getClustering()));
        writer.flush();
        writer.close();


        C2.setClusterNumber(4);

        File Clustering3 = new File("./OutputTest/Clustering/Clustering3.json");
        writer = new FileWriter(Clustering3);
        System.out.println(C2.getClustering() + "\n");
        System.out.println("Cluster number: " + C2.getClusterNumber() + "\n");
        writer.write(gson.toJson(C.getClustering()));
        writer.flush();
        writer.close();


        //Label Hashmap testing
        VertexLabelMap vertexMap = new VertexLabelMap();
        vertexMap.setVertexLabel(0, "a");
        for (Integer iterator : directedGraph.vertexSet()) {
            System.out.println("label of vertex: " + iterator + ":" + vertexMap.getVertexLabel(iterator));
        }

    }
}
