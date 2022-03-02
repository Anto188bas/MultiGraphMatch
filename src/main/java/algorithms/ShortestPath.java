package algorithms;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.DefaultEdge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShortestPath {
    final private Graph<String, DefaultEdge> graph;
    final private Set<String> GraphSet;
    public ShortestPath(Graph<String,DefaultEdge> graph){
        this.graph = graph;
        GraphSet = graph.vertexSet();
    }

    public String DijkstraSP(String source, String destination){
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        SingleSourcePaths<String, DefaultEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
        return String.valueOf(iPathsDijkstra.getPath(destination));
    }

    //return shortest path from source to all other nodes using Dijkstra
    public List<String> DijkstraAllSP(String source){
        List<String> AllSP = new ArrayList<>();
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        for (String destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                SingleSourcePaths<String, DefaultEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
                AllSP.add(String.valueOf(iPathsDijkstra.getPath(destination)));
            }
        }
        return AllSP;
    }

    public String BellmanFordSP(String source, String destination){
        BellmanFordShortestPath<String, Object> BellmanFordAlg = new BellmanFordShortestPath(graph);
        SingleSourcePaths<String, Object> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
        return String.valueOf(iPathsBellmanFord.getPath(destination));
    }

    //return shortest path from source to all other nodes using Bellman-Ford
    public List<String> BellmanFordAllSP(String source){
        List<String> AllSP = new ArrayList<>();
        BellmanFordShortestPath<String, DefaultEdge> BellmanFordAlg = new BellmanFordShortestPath(graph);
        for (String destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                SingleSourcePaths<String, DefaultEdge> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
                AllSP.add(String.valueOf(iPathsBellmanFord.getPath(destination)));
            }
        }
        return AllSP;
    }

    public List<String> FloydWarshallSP() {
        List<String> AllSP = new ArrayList<>();
        FloydWarshallShortestPaths<String, DefaultEdge> FloydWarshallAlg = new FloydWarshallShortestPaths<>(graph);
        for (String tempSource : GraphSet) {
            AllSP.add("\n from: "+tempSource+"\n");
            for (String tempDest : GraphSet) {
                if (!tempSource.equals(tempDest)) {
                    AllSP.add("to: "+tempDest+": ");
                    SingleSourcePaths<String, DefaultEdge> iPathsFloydWarshall = FloydWarshallAlg.getPaths(tempSource);
                    AllSP.add(String.valueOf(iPathsFloydWarshall.getPath(tempDest)));
                    AllSP.add("\n");
                }
            }
        }
        return AllSP;
    }
}
