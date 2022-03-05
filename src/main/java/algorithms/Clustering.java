package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.GirvanNewmanClustering;
import java.util.Set;

public class Clustering {
    final private Graph<String, RelationshipEdge> graph;
    Integer clusterNumber;

    public Clustering(Graph<String, RelationshipEdge> graph, Integer clusterNumber){
        this.graph = graph;
        this.clusterNumber = clusterNumber;
    }

    public Clustering(Graph<String, RelationshipEdge> graph){
        this.graph = graph;
        this.clusterNumber = 2;
    }

    public Integer getClusterNumber(){ return clusterNumber; }

    public void setClusterNumber(Integer newClusterNumber){ this.clusterNumber = newClusterNumber; }

    //The Girvan–Newman algorithm detects communities by progressively removing edges from the original network. The connected components of the remaining network are
    // the communities. Instead of trying to construct a measure that tells us which edges are the most central to communities, the Girvan–Newman algorithm focuses
    // on edges that are most likely "between" communities.
    public Iterable<Set<String>> getClustering(){
        GirvanNewmanClustering<String, RelationshipEdge> cluster = new GirvanNewmanClustering<>(graph, clusterNumber);
        return cluster.getClustering();
    }
}
