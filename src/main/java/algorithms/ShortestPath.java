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
    //from x -> y
    //     x -> z
    //     ...
    public List<GraphPath<Integer, RelationshipEdge>> DijkstraAllSP(Integer source){
        List<GraphPath<Integer, RelationshipEdge>> AllSP = new ArrayList<>();
        DijkstraShortestPath<Integer, RelationshipEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        for (Integer destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                SingleSourcePaths<Integer, RelationshipEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
                AllSP.add(iPathsDijkstra.getPath(destination));
            }
        }
        return AllSP;
    }

    public GraphPath<Integer, Object> BellmanFordSP(Integer source, Integer destination){
        BellmanFordShortestPath<Integer, Object> BellmanFordAlg = new BellmanFordShortestPath(graph);
        SingleSourcePaths<Integer, Object> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
        return iPathsBellmanFord.getPath(destination);
    }

    //return shortest path from source to all other nodes using Bellman-Ford Algorithm
    //from x -> y
    //     x -> z
    //     ...
    public List<GraphPath<Integer, DefaultEdge>> BellmanFordAllSP(Integer source){
        List<GraphPath<Integer, DefaultEdge>> AllSP = new ArrayList<>();
        BellmanFordShortestPath<Integer, DefaultEdge> BellmanFordAlg = new BellmanFordShortestPath(graph);
        for (Integer destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                //AllSP.add(String.valueOf(destination));
                SingleSourcePaths<Integer, DefaultEdge> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
                AllSP.add(iPathsBellmanFord.getPath(destination));
                //AllSP.add("\n");
            }
        }
        return AllSP;
    }

    public List<GraphPath<Integer, RelationshipEdge>> FloydWarshallSP() {
        List<GraphPath<Integer, RelationshipEdge>> AllSP = new ArrayList<>();
        FloydWarshallShortestPaths<Integer, RelationshipEdge> FloydWarshallAlg = new FloydWarshallShortestPaths(graph);
        for (Integer tempSource : GraphSet) {
            //AllSP.add("\n from: "+tempSource+"\n");
            for (Integer tempDest : GraphSet) {
                if (!tempSource.equals(tempDest)) {
                    //AllSP.add("to: "+tempDest+": ");
                    SingleSourcePaths<Integer, RelationshipEdge> iPathsFloydWarshall = FloydWarshallAlg.getPaths(tempSource);
                    AllSP.add(iPathsFloydWarshall.getPath(tempDest));
                    //AllSP.add("\n");
                }
            }
        }
        return AllSP;
    }

}
