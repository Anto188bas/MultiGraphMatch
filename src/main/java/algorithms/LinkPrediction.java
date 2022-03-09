package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.linkprediction.CommonNeighborsLinkPrediction;
import org.jgrapht.alg.linkprediction.JaccardCoefficientLinkPrediction;
import org.jgrapht.alg.linkprediction.PreferentialAttachmentLinkPrediction;

public class LinkPrediction {
    final private Graph<Integer, RelationshipEdge> graph;

    public LinkPrediction(Graph<Integer, RelationshipEdge> graph) {
        this.graph = graph;
    }

    //Predict links using Preferential Attachment.
    public double PreferentialAttachmentPrediction(int u, int v) {
        PreferentialAttachmentLinkPrediction<Integer, RelationshipEdge> prediction  = new PreferentialAttachmentLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

    //Predict links using the number of common neighbors.
    public double CommonNeighborsPrediction(int u, int v){
        CommonNeighborsLinkPrediction<Integer, RelationshipEdge> prediction = new CommonNeighborsLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

    //Predict links using the Jaccard coefficient.
    public double JaccardCoefficientPrediction(int u, int v) {
        JaccardCoefficientLinkPrediction<Integer, RelationshipEdge> prediction = new JaccardCoefficientLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

}
