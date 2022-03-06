import algorithms.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

public class algorithms_test {
    public static void main(String[] args) {

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

        directedGraph.addEdge(0,1, new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(1, 3,new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(3, 2,new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge(2, 0,new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(4, 3,new RelationshipEdge("FRIEND"));
        directedGraph.addEdge(4, 5,new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(5, 6,new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge(6, 4,new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge(7, 4,new RelationshipEdge("FRIEND"));

        //ShortestPath testing
        ShortestPath s = new ShortestPath(directedGraph);
        System.out.println("Dijkstra Shortest Path");
        System.out.println(s.DijkstraSP(0, 2));
        System.out.println("\n"+"Dijkstra All Shortest Path");
        System.out.println(s.DijkstraAllSP(0));
        System.out.println("\n"+"Bellman-Ford Shortest Path");
        System.out.println(s.BellmanFordSP(0,2));
        System.out.println("\n"+"Bellman-Ford All Shortest Path");
        System.out.println(s.BellmanFordAllSP(0));
        System.out.println("\n"+"Floyd-Warshall Shortest Path");
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
        System.out.println("\n"+ C.getClustering());

        Clustering C2 = new Clustering(directedGraph);
        System.out.println(C2.getClustering());

        C2.setClusterNumber(4);
        System.out.println(C2.getClustering()+"\n");
        System.out.println("Cluster number: "+C2.getClusterNumber()+"\n");
        //Label Hashmap testing
        VertexLabelMap vertexMap = new VertexLabelMap();
        vertexMap.setVertexLabel(0,"a");
        for(Integer iterator : directedGraph.vertexSet()) {
            System.out.println("label of vertex: "+iterator+":"+vertexMap.getVertexLabel(iterator));
        }
    }
}
