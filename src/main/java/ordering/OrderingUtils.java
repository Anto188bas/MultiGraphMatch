package ordering;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.function.IntConsumer;

public class OrderingUtils {

    public static Double computeSetWeight(ObjectArraySet<NodesPair> pair_set, Int2ObjectOpenHashMap<NodesPair> map_id_to_pair) {
        double w = 0d;
        for (NodesPair pair : pair_set) {
            int domain_size = map_id_to_pair.get(pair.getId().intValue()).getDomain_size();
            w += 1d / domain_size;
        }

        return w;
    }

    public static IntArraySet intArraySetUnion(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        result.addAll(b);

        return result;
    }

    public static IntArraySet intArraySetIntersection(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        result.retainAll(b);

        return result;
    }

    public static Int2ObjectOpenHashMap<IntArraySet> mergeInt2ObjectOpenHashMap(Int2ObjectOpenHashMap<IntArraySet> a, Int2ObjectOpenHashMap<IntArraySet> b) {
        Int2ObjectOpenHashMap<IntArraySet> result = new Int2ObjectOpenHashMap<>();
        b.keySet().forEach((IntConsumer) (key) -> {
            if (a.containsKey(key)) {
                result.put(key, intArraySetUnion(a.get(key), b.get(key)));
            } else {
                result.put(key, b.get(key));
            }
        });

        return result;
    }

    public static Int2IntOpenHashMap computeNodesDegree(IntSet nodes, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inOutEdges) {
        Int2IntOpenHashMap degrees = new Int2IntOpenHashMap();

        for (int node : nodes) {

            int degree = 0;

            if (inEdges.containsKey(node)) {
                degree += inEdges.get(node).size();
            }

            if (outEdges.containsKey(node)) {
                degree += outEdges.get(node).size();
            }

            if (inOutEdges.containsKey(node)) {
                degree += inOutEdges.get(node).size();
            }

            degrees.put(node, degree);
        }

        return degrees;
    }

    public static Int2IntOpenHashMap computeEdgesDegree(ObjectArraySet<NodesPair> pairs, Int2IntOpenHashMap nodes_degree) {
        Int2IntOpenHashMap map_pair_to_degree = new Int2IntOpenHashMap();
        for (NodesPair pair : pairs) {
            int first_endpoint_degree = nodes_degree.get(pair.getFirstEndpoint().intValue());
            int second_endpoint_degree = nodes_degree.get(pair.getSecondEndpoint().intValue());
            map_pair_to_degree.put(pair.getId().intValue(), (first_endpoint_degree + second_endpoint_degree));
        }

        return map_pair_to_degree;
    }

    public static Double computePairScore(int node, int neighbour, Int2ObjectOpenHashMap<IntArraySet> mapNodeToNeighborhood, int domainSize, int neighbour_degree) {
        // Score(u, v) = Jacc(u, v) * Deg(v) + 1/|Dom(u, v)|

        // Node's neighborhood
        IntArraySet nodeNeighborhood = mapNodeToNeighborhood.get(node);

        // Neighbor's neighborhood
        IntArraySet neighbourNeighborhood = mapNodeToNeighborhood.get(neighbour);

        // Cardinality of the intersection between neighborhoods
        int neighborhoodsIntersectionCardinality = OrderingUtils.intArraySetIntersection(nodeNeighborhood, neighbourNeighborhood).size();

        // Cardinality of the union between neighborhoods
        int neighborhoodsUnionCardinality = OrderingUtils.intArraySetUnion(nodeNeighborhood, neighbourNeighborhood).size();

        // Jaccard
        double jaccard = ((neighborhoodsIntersectionCardinality + 0.5d) / (neighborhoodsUnionCardinality + 0.5d));

        // Score
        return jaccard * neighbour_degree * (1d / domainSize);
    }

    public static Double computeSimplifiedPairScore(int domainSize, int neighbour_degree) {
        // Score(u, v) = Deg(v) + 1/|Dom(u, v)|
        return neighbour_degree * (1d / domainSize);
    }
}
