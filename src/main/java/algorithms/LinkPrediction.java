package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.linkprediction.CommonNeighborsLinkPrediction;
import org.jgrapht.alg.linkprediction.JaccardCoefficientLinkPrediction;
import org.jgrapht.alg.linkprediction.PreferentialAttachmentLinkPrediction;

public class LinkPrediction {
    final private Graph<Integer, RelationshipEdge> graph;

    /**
     *
     * Class constructor, construct a LinkPrediction object
     * @param graph the input network
     *
     */
    public LinkPrediction(Graph<Integer, RelationshipEdge> graph) {
        this.graph = graph;
    }

    /**
     *
     * Predict link using Preferential Attachment
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @return the probability of existence of the edge (prediction score)
     *
     */
    public double PreferentialAttachmentPrediction(int u, int v) {
        PreferentialAttachmentLinkPrediction<Integer, RelationshipEdge> prediction  = new PreferentialAttachmentLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

    /**
     *
     * Predict links using the number of common neighbors
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @return the probability of existence of the edge (prediction score)
     *
     */
    public double CommonNeighborsPrediction(int u, int v){
        CommonNeighborsLinkPrediction<Integer, RelationshipEdge> prediction = new CommonNeighborsLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

    /**
     *
     * Predict links using the Jaccard coefficient
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @return the probability of existence of the edge (prediction score)
     *
     */
    public double JaccardCoefficientPrediction(int u, int v) {
        JaccardCoefficientLinkPrediction<Integer, RelationshipEdge> prediction = new JaccardCoefficientLinkPrediction<>(graph);
        return prediction.predict(u,v);
    }

}
