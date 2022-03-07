package algorithms;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.DefaultEdge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShortestPath {
    final private Graph<Integer, RelationshipEdge> graph;
    final private Set<Integer> GraphSet;

    public ShortestPath(Graph<Integer, RelationshipEdge> graph){
        this.graph = graph;
        GraphSet = graph.vertexSet();
    }

    public GraphPath<Integer, RelationshipEdge> DijkstraSP(Integer source, Integer destination){
        DijkstraShortestPath<Integer, RelationshipEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        SingleSourcePaths<Integer, RelationshipEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
        return iPathsDijkstra.getPath(destination);
    }

    //return shortest path from source to all other nodes using Dijkstra algorithm
    // TODO return an object to create a json
    public List<String> DijkstraAllSP(Integer source){
        List<String> AllSP = new ArrayList<>();
        DijkstraShortestPath<Integer, RelationshipEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        for (Integer destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                AllSP.add(String.valueOf(destination)+": ");
                SingleSourcePaths<Integer, RelationshipEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
                AllSP.add(String.valueOf(iPathsDijkstra.getPath(destination)));
            }
        }
        return AllSP;
    }

    public GraphPath<Integer, Object> BellmanFordSP(Integer source, Integer destination){
        BellmanFordShortestPath<Integer, Object> BellmanFordAlg = new BellmanFordShortestPath(graph);
        SingleSourcePaths<Integer, Object> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
        return iPathsBellmanFord.getPath(destination);
    }

    // TODO return an object to create a json
    //return shortest path from source to all other nodes using Bellman-Ford
    public List<String> BellmanFordAllSP(Integer source){
        List<String> AllSP = new ArrayList<>();
        BellmanFordShortestPath<Integer, DefaultEdge> BellmanFordAlg = new BellmanFordShortestPath(graph);
        for (Integer destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                AllSP.add(String.valueOf(destination));
                SingleSourcePaths<Integer, DefaultEdge> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
                AllSP.add(String.valueOf(iPathsBellmanFord.getPath(destination)));
                AllSP.add("\n");
            }
        }
        return AllSP;
    }

    // TODO return an object to create a json
    public List<String> FloydWarshallSP() {
        List<String> AllSP = new ArrayList<>();
        FloydWarshallShortestPaths<Integer, DefaultEdge> FloydWarshallAlg = new FloydWarshallShortestPaths(graph);
        for (Integer tempSource : GraphSet) {
            AllSP.add("\n from: "+tempSource+"\n");
            for (Integer tempDest : GraphSet) {
                if (!tempSource.equals(tempDest)) {
                    AllSP.add("to: "+tempDest+": ");
                    SingleSourcePaths<Integer, DefaultEdge> iPathsFloydWarshall = FloydWarshallAlg.getPaths(tempSource);
                    AllSP.add(String.valueOf(iPathsFloydWarshall.getPath(tempDest)));
                    AllSP.add("\n");
                }
            }
        }
        return AllSP;
    }
}
