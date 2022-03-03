package algorithms;
import org.jgrapht.*;
import org.jgrapht.alg.scoring.*;
import org.jgrapht.graph.DefaultEdge;
import java.util.HashMap;
import java.util.Set;

public class Centrality{
    final private Graph<String, RelationshipEdge> graph;
    final private Set<String> GraphSet;
    public Centrality(Graph<String, RelationshipEdge> graph){
        this.graph = graph;
        GraphSet = graph.vertexSet();
    }

    public HashMap<String, String> Betweenness(){
        HashMap<String, String> centralityValue = new HashMap<>();
        BetweennessCentrality<String, DefaultEdge> BetweennessCentralityMeasure = new BetweennessCentrality(graph);
        for (String index : GraphSet)
            centralityValue.put(index, String.valueOf(BetweennessCentralityMeasure.getVertexScore(index)));
        return centralityValue;
    }

    public HashMap<String, String> Closeness(){
        HashMap<String, String> centralityValue = new HashMap<>();
        ClosenessCentrality<String, DefaultEdge> ClosenessCentralityMeasure = new ClosenessCentrality(graph);
        for (String index : GraphSet)
            centralityValue.put(index, String.valueOf(ClosenessCentralityMeasure.getVertexScore(index)));
        return centralityValue;
    }

    public HashMap<String, String> EigenVector(){
        HashMap<String, String> centralityValue = new HashMap<>();
        EigenvectorCentrality<String, DefaultEdge> EigenVectorMeasure = new EigenvectorCentrality(graph);
        for (String index : GraphSet)
            centralityValue.put(index, String.valueOf(EigenVectorMeasure.getVertexScore(index)));
        return centralityValue;
    }

    public HashMap<String, String> PageRank(){
        HashMap<String, String> centralityValue = new HashMap<>();
        PageRank<String, DefaultEdge> PageRankMeasure = new PageRank(graph);
        for (String index : GraphSet)
            centralityValue.put(index, String.valueOf(PageRankMeasure.getVertexScore(index)));
        return centralityValue;
    }

    public HashMap<String, String> ClusteringCoefficient(){
        HashMap<String, String> centralityValue = new HashMap<>();
        ClusteringCoefficient<String, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient(graph);
        for (String index : GraphSet)
            centralityValue.put(index, String.valueOf(ClusteringCoefficientMeasure.getVertexScore(index)));
        return centralityValue;
    }

    public double AverageClusteringCoefficient(){
        ClusteringCoefficient<String, DefaultEdge> ClusteringCoefficientMeasure = new ClusteringCoefficient(graph);
        return ClusteringCoefficientMeasure.getAverageClusteringCoefficient();
    }
}
