package algorithms;

import org.jgrapht.*;
import org.jgrapht.alg.scoring.*;
import org.jgrapht.graph.DefaultEdge;
import java.util.HashMap;
import java.util.Set;
/**
 *
 * This class include all the popular network centrality measure implementations
 *
 */
public class Centrality {
    final private Graph<Integer, RelationshipEdge> graph;
    final private Set<Integer> GraphSet;

    /**
     *
     * Class constructor, construct a Centrality object
     *
     * @param graph the input network
     *
     */
    public Centrality(Graph<Integer, RelationshipEdge> graph) {
        this.graph = graph;
        GraphSet = graph.vertexSet();
    }

    /**
     *
     * Computes the betweenness centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the betweenness centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, String> Betweenness(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        BetweennessCentrality<Integer, DefaultEdge> BetweennessCentralityMeasure = new BetweennessCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(BetweennessCentralityMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     * Computes the Closeness centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the Closeness centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, String> Closeness(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        ClosenessCentrality<Integer, DefaultEdge> ClosenessCentralityMeasure = new ClosenessCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(ClosenessCentralityMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     * Computes the EigenVector centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the EigenVector centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, String> EigenVector(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        EigenvectorCentrality<Integer, DefaultEdge> EigenVectorMeasure = new EigenvectorCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(EigenVectorMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     * Computes the PageRank centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the PageRank centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, String> PageRank(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        PageRank<Integer, DefaultEdge> PageRankMeasure = new PageRank(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(PageRankMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     *
     * Computes the Katz centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the Katz centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, String> Katz(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        KatzCentrality<Integer, DefaultEdge> KatzMeasure = new KatzCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(KatzMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     * Computes the Clustering Coefficient of each vertex of a graph
     *
     * @return a Hashmap which contains the Clustering coefficient of each vertex of the graph
     *
     */
    public HashMap<Integer, String> ClusteringCoefficient(){
        HashMap<Integer, String> centralityValue = new HashMap<>();
        ClusteringCoefficient<Integer, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, String.valueOf(ClusteringCoefficientMeasure.getVertexScore(index)));
        return centralityValue;
    }

    /**
     *
     * Compute the Average Clustering coefficient of the graph
     *
     * @return the average clustering coefficient of the graph
     */
    public double AverageClusteringCoefficient(){
        ClusteringCoefficient<Integer, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient(graph);
        return ClusteringCoefficientMeasure.getAverageClusteringCoefficient();
    }
}
