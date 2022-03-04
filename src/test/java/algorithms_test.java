import algorithms.Centrality;
import algorithms.RelationshipEdge;
import algorithms.ShortestPath;
import org.jgrapht.*;
import org.jgrapht.graph.*;

public class algorithms_test {
    public static void main(String[] args) {
        // TODO create the graph using the index value as vertex name
             //Integer
        Graph<String, RelationshipEdge> directedGraph = new SimpleDirectedGraph<String, RelationshipEdge>(RelationshipEdge.class);

        directedGraph.addVertex("a");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");
        directedGraph.addVertex("d");
        directedGraph.addVertex("e");
        directedGraph.addVertex("f");
        directedGraph.addVertex("g");
        directedGraph.addVertex("h");
        directedGraph.addVertex("i");

        directedGraph.addEdge("a", "b", new RelationshipEdge("FRIEND"));
        directedGraph.addEdge("b", "d",new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge("d", "c",new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge("c", "a",new RelationshipEdge("FRIEND"));
        directedGraph.addEdge("e", "d",new RelationshipEdge("FRIEND"));
        directedGraph.addEdge("e", "f",new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge("f", "g",new RelationshipEdge("RELATIVE"));
        directedGraph.addEdge("g", "e",new RelationshipEdge("ACQUAINTANCE"));
        directedGraph.addEdge("h", "e",new RelationshipEdge("FRIEND"));

        ShortestPath s = new ShortestPath(directedGraph);

        System.out.println("Dijkstra Shortest Path");
        System.out.println(s.DijkstraSP("a", "c"));
        System.out.println("\n"+"Dijkstra All Shortest Path");
        System.out.println(s.DijkstraAllSP("a"));
        System.out.println("\n"+"Bellman-Ford Shortest Path");
        System.out.println(s.BellmanFordSP("a","c"));
        System.out.println("\n"+"Bellman-Ford All Shortest Path");
        System.out.println(s.BellmanFordAllSP("a"));
        System.out.println("\n"+"Floyd-Warshall Shortest Path");
        System.out.println(s.FloydWarshallSP());

        Centrality c = new Centrality(directedGraph);
        System.out.println("\nBetweenness centrality\n");
        System.out.println(c.Betweenness());
        System.out.println("\nCloseness centrality\n");
        System.out.println(c.Closeness());
        System.out.println("\nEigenVector centrality\n");
        System.out.println(c.EigenVector());
        System.out.println("\nPageRank centrality\n");
        System.out.println(c.PageRank());
        System.out.println("\nClustering coefficient\n");
        System.out.println(c.ClusteringCoefficient());
        System.out.println("\nAverage clustering coefficient\n");
        System.out.println(c.AverageClusteringCoefficient());


      /*
        //create a set which contain all vertex of the graph
        Set<String> GraphSet= directedGraph.vertexSet();

        // Use dijkstra algorithm for searching all shortest path
        System.out.println("all shortest path using Dijkstra\n");
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(directedGraph);




        for (String tempSource : GraphSet){
            for(String tempDest : GraphSet){
                if(!tempSource.equals(tempDest)){   //exclude cases where source = destination
                    System.out.println("shortest path from " + tempSource + " to " + tempDest);
                    ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPathsDijkstra = dijkstraAlg.getPaths(tempSource);
                    System.out.println(iPathsDijkstra.getPath(tempDest) + "\n");
                }
            }
        }


        // Use Bellman-Ford algorithm for searching all shortest path
        System.out.println("all shortest path using Bellman-Ford\n");
        BellmanFordShortestPath<String, Object> BellmanFordAlg = new BellmanFordShortestPath(directedGraph);

        for (String tempSource : GraphSet){
            for(String tempDest : GraphSet){
                if(!tempSource.equals(tempDest)){   //exclude cases where source = destination
                    System.out.println("shortest path from " + tempSource + " to " + tempDest);
                    SingleSourcePaths<String, Object> iPathsBellmanFord = BellmanFordAlg.getPaths(tempSource);
                    System.out.println(iPathsBellmanFord.getPath(tempDest) + "\n");
                }
            }
        }


        // Use Floyd-Warshall algorithm for searching all shortest
        System.out.println("all shortest path using Floyd-Warshall\n");
        FloydWarshallShortestPaths<String, DefaultEdge> FloydWarshallAlg = new FloydWarshallShortestPaths<>(directedGraph);

        for(String tempSource : GraphSet){
            for(String tempDest : GraphSet){
                if(!tempSource.equals(tempDest)){
                    System.out.println("shortest path from " + tempSource + " to "+ tempDest );
                    SingleSourcePaths<String, DefaultEdge> iPathsFloydWarshall = FloydWarshallAlg.getPaths(tempSource);
                    System.out.println(iPathsFloydWarshall.getPath(tempDest) + "\n");
                }
            }
        }



        //calculate Betweenness centrality for all the vertex of the graph
        BetweennessCentrality<String, DefaultEdge> BetweennessCentralityMeasure = new BetweennessCentrality<>(directedGraph);
        System.out.println("\nBetweenness Centrality\n"+BetweennessCentralityMeasure.getScores());

        //calculate Closeness centrality for all the vertex of the graph
        ClosenessCentrality<String,DefaultEdge> ClosenessCentralityMeasure = new ClosenessCentrality<>(directedGraph);
        System.out.println("\nCloseness Centrality\n"+ClosenessCentralityMeasure.getScores());

        //calculate EigenVector centrality for all the vertex of the graph
        EigenvectorCentrality<String,DefaultEdge>  EigenvectorCentralityMeasure= new EigenvectorCentrality<>(directedGraph);
        System.out.println("\nEigenVector Centrality\n"+EigenvectorCentralityMeasure.getScores());

        //calculate PageRank centrality for all the vertex of the graph
        PageRank<String, DefaultEdge> PageRankMeasure = new PageRank<>(directedGraph);
        System.out.println("\nPage Rank Centrality\n"+PageRankMeasure.getScores());

        //calculate Clustering coefficient for all the vertex of the graph and Average coefficient of the network
        ClusteringCoefficient<String, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient<>(directedGraph);
        System.out.println("\nClustering coefficient\n"+ClusteringCoefficientMeasure.getScores());
        System.out.println("\nAverage Clustering coefficient\n"+ClusteringCoefficientMeasure.getAverageClusteringCoefficient());

 */
    }
}
