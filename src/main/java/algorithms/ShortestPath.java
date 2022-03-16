package algorithms;

import algorithms.algorithmsUtility.RelationshipEdge;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.DefaultEdge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * This class include most of the popular shortest path algorithm implementations
 *
 */
public class ShortestPath {
    final private Graph<Integer, RelationshipEdge> graph;
    final private Set<Integer> GraphSet;

    /**
     *
     * Class constructor, construct a ShortestPath object
     *
     * @param graph the input network
     *
     */
    public ShortestPath(Graph<Integer, RelationshipEdge> graph) {
        this.graph = graph;
        GraphSet = graph.vertexSet();
    }

    /**
     *
     * Dijkstra shortest path algorithm
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @return return the shortest path calculated using Dijkstra algorithm
     *
     */
    public GraphPath<Integer, RelationshipEdge> DijkstraSP(Integer source, Integer destination) {
        DijkstraShortestPath<Integer, RelationshipEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        SingleSourcePaths<Integer, RelationshipEdge> iPathsDijkstra = dijkstraAlg.getPaths(source);
        return iPathsDijkstra.getPath(destination);
    }

    /**
     *
     * Dijsktra all shortest path algorithm
     *
     * @param source the source vertex id
     * @return a list which contain all the shortest path from the source to all the other network vertex
     *
     */
    public List<GraphPath<Integer, RelationshipEdge>> DijkstraAllSP(Integer source) {
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

    /**
     *
     * Bellman - Ford shortest path algorithm
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @return return the shortest path calculated using Bellman - Ford algorithm
     *
     */
    public GraphPath<Integer, Object> BellmanFordSP(Integer source, Integer destination) {
        BellmanFordShortestPath<Integer, Object> BellmanFordAlg = new BellmanFordShortestPath(graph);
        SingleSourcePaths<Integer, Object> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
        return iPathsBellmanFord.getPath(destination);
    }

    /**
     *
     *  Bellman - Ford  all shortest path algorithm
     *
     * @param source the source vertex id
     * @return a list which contain all the shortest path from the source to all the other network vertex
     *
     */
    public List<GraphPath<Integer, DefaultEdge>> BellmanFordAllSP(Integer source) {
        List<GraphPath<Integer, DefaultEdge>> AllSP = new ArrayList<>();
        BellmanFordShortestPath<Integer, DefaultEdge> BellmanFordAlg = new BellmanFordShortestPath(graph);
        for (Integer destination : GraphSet){
            if(!destination.equals(source)){   //exclude cases where source = destination
                SingleSourcePaths<Integer, DefaultEdge> iPathsBellmanFord = BellmanFordAlg.getPaths(source);
                AllSP.add(iPathsBellmanFord.getPath(destination));
            }
        }
        return AllSP;
    }
}
