package algorithms;

import algorithms.algorithmsUtility.RelationshipEdge;
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
    public HashMap<Integer, Double> Betweenness(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        BetweennessCentrality<Integer, DefaultEdge> BetweennessCentralityMeasure = new BetweennessCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, BetweennessCentralityMeasure.getVertexScore(index));
        return centralityValue;
    }

    /**
     *
     * Computes the Closeness centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the Closeness centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, Double> Closeness(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        ClosenessCentrality<Integer, DefaultEdge> ClosenessCentralityMeasure = new ClosenessCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, ClosenessCentralityMeasure.getVertexScore(index));
        return centralityValue;
    }

    /**
     *
     * Computes the EigenVector centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the EigenVector centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, Double> EigenVector(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        EigenvectorCentrality<Integer, DefaultEdge> EigenVectorMeasure = new EigenvectorCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, EigenVectorMeasure.getVertexScore(index));
        return centralityValue;
    }

    /**
     *
     * Computes the PageRank centrality of each vertex of a graph
     *
     * @return a Hashmap which contains the PageRank centrality of each vertex of the graph
     *
     */
    public HashMap<Integer, Double> PageRank(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        PageRank<Integer, DefaultEdge> PageRankMeasure = new PageRank(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, PageRankMeasure.getVertexScore(index));
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
    public HashMap<Integer, Double> Katz(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        KatzCentrality<Integer, DefaultEdge> KatzMeasure = new KatzCentrality(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, KatzMeasure.getVertexScore(index));
        return centralityValue;
    }

    /**
     *
     * Computes the Clustering Coefficient of each vertex of a graph
     *
     * @return a Hashmap which contains the Clustering coefficient of each vertex of the graph
     *
     */
    public HashMap<Integer, Double> ClusteringCoefficient(){
        HashMap<Integer, Double> centralityValue = new HashMap<>();
        ClusteringCoefficient<Integer, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient(graph);
        for (Integer index : GraphSet)
            centralityValue.put(index, ClusteringCoefficientMeasure.getVertexScore(index));
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
