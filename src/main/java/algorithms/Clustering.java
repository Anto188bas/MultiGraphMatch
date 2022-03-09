package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.Set;

public class Clustering {
    final private Graph<Integer, RelationshipEdge> graph;
    private Integer clusterNumber;

    public Clustering(Graph<Integer, RelationshipEdge> graph, Integer clusterNumber){
        this.graph = new AsUndirectedGraph<>(graph);  //transform a directed graph to an undirected Graph
        this.clusterNumber  = clusterNumber;
    }

    public Clustering(Graph<Integer, RelationshipEdge> graph){
        this.graph = new AsUndirectedGraph<>(graph);  //transform a directed graph to an undirected Graph
    }

    public Iterable<Set<Integer>> LabelPropagation(){
        LabelPropagationClustering<Integer, RelationshipEdge> cluster = new LabelPropagationClustering<>(graph);
        return cluster.getClustering();
    }
    public Iterable<Set<Integer>> KSpanningTree() {
        KSpanningTreeClustering<Integer, RelationshipEdge> cluster = new KSpanningTreeClustering<>(graph,clusterNumber);
        return cluster.getClustering();
    }
}
