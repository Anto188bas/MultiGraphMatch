import algorithms.*;
import com.google.gson.Gson;
import configuration.Configuration;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class algorithms_test {
    public static void main(String[] args) throws IOException {

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

        //ShortestPath testing
        ShortestPath s = new ShortestPath(directedGraph);
        System.out.println("Dijkstra Shortest Path");
        System.out.println(s.DijkstraSP(0, 2));
        System.out.println("\n" + "Dijkstra All Shortest Path");
        System.out.println(s.DijkstraAllSP(0));
        System.out.println("\n" + "Bellman-Ford Shortest Path");
        System.out.println(s.BellmanFordSP(0, 2));
        System.out.println("\n" + "Bellman-Ford All Shortest Path");
        System.out.println(s.BellmanFordAllSP(0));
        System.out.println("\n" + "Floyd-Warshall Shortest Path");
        System.out.println(s.FloydWarshallSP());

        //Centrality testing
        Centrality c = new Centrality(directedGraph);
        System.out.println("\nBetweenness centrality:");
        System.out.println(c.Betweenness());
        System.out.println("\nCloseness centrality:");
        System.out.println(c.Closeness());
        System.out.println("\nEigenVector centrality:");
        System.out.println(c.EigenVector());
        System.out.println("\nPageRank centrality:");
        System.out.println(c.PageRank());
        System.out.println("\nClustering coefficient:");
        System.out.println(c.ClusteringCoefficient());
        System.out.println("\nAverage clustering coefficient:");
        System.out.println(c.AverageClusteringCoefficient());

        //Clustering testing
        Clustering C = new Clustering(directedGraph, 3);
        System.out.println("\n" + C.getClustering());

        Clustering C2 = new Clustering(directedGraph);
        System.out.println(C2.getClustering());

        C2.setClusterNumber(4);
        System.out.println(C2.getClustering() + "\n");
        System.out.println("Cluster number: " + C2.getClusterNumber() + "\n");
        //Label Hashmap testing
        VertexLabelMap vertexMap = new VertexLabelMap();
        vertexMap.setVertexLabel(0, "a");
        for (Integer iterator : directedGraph.vertexSet()) {
            System.out.println("label of vertex: " + iterator + ":" + vertexMap.getVertexLabel(iterator));
        }


        //GSON TESTING have to remove "\n" in shortestpath and
        File output = new File("/home/rosario/Scrivania/prova.json");
        FileWriter writer = new FileWriter(output);
        Gson gson = new Gson();
        System.out.println("Dijkstra Shortest Path");
        System.out.println(gson.toJson(s.DijkstraSP(0, 2)));
        writer.write(gson.toJson(s.DijkstraSP(0, 2)));
        writer.flush();
        writer.close();
        System.out.println("\n" + "Dijkstra All Shortest Path");
        System.out.println(gson.toJson(s.DijkstraAllSP(0)));
        System.out.println("\n" + "Bellman-Ford Shortest Path");
        System.out.println(gson.toJson(s.BellmanFordSP(0, 2)));
        System.out.println("\n" + "Bellman-Ford All Shortest Path");
        System.out.println(gson.toJson(s.BellmanFordAllSP(0)));
        System.out.println("\n" + "Floyd-Warshall Shortest Path");
        System.out.println(gson.toJson(s.FloydWarshallSP()));




    }
}
