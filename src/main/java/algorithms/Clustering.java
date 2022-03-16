package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.graph.AsUndirectedGraph;
import java.util.Set;
/**
 *
 * This class include most of the popular clustering algorithm implementations
 *
 */
public class Clustering {
    final private Graph<Integer, RelationshipEdge> graph;
    private Integer clusterNumber;

    /**
     *
     * Class constructor, construct a Clustering object
     *
     * @param graph the input network
     * @param clusterNumber the desired final number of cluster
     *
     */
    public Clustering(Graph<Integer, RelationshipEdge> graph, Integer clusterNumber){
        this.graph = new AsUndirectedGraph<>(graph);  //transform a directed graph to an undirected Graph
        this.clusterNumber  = clusterNumber;
    }

    /**
     * Class constructor, construct a Clustering object
     *
     * @param graph the input network
     *
     */
    public Clustering(Graph<Integer, RelationshipEdge> graph){
        this.graph = new AsUndirectedGraph<>(graph);  //transform a directed graph to an undirected Graph
    }

    /**
     *
     * Label Propagation clustering algorithm
     *
     * @return a Set of Integer which contain all the vertex grouped by cluster
     *
     */
    public Iterable<Set<Integer>> LabelPropagation(){
        LabelPropagationClustering<Integer, RelationshipEdge> cluster = new LabelPropagationClustering<>(graph);
        return cluster.getClustering();
    }

    /**
     *
     * K-Spanning Tree clustering algorithm
     *
     * @return a Set of Integer of {@param clusterNumber} which contain all the vertex grouped by cluster
     *
     */
    public Iterable<Set<Integer>> KSpanningTree() {
        KSpanningTreeClustering<Integer, RelationshipEdge> cluster = new KSpanningTreeClustering<>(graph,clusterNumber);
        return cluster.getClustering();
    }

}
