package ordering;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.concurrent.atomic.AtomicReference;

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

    public static Int2IntOpenHashMap computeDegrees(IntSet nodes, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inOutEdges) {
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

    public static Double computeJaccardSimilarity(int node, int neighbour, Int2ObjectOpenHashMap<IntArraySet> mapNodeToNeighborhood, int domainSize) {
        // Node's neighborhood
        IntArraySet nodeNeighborhood = mapNodeToNeighborhood.get(node);

        // Neighbor's neighborhood
        IntArraySet neighbourNeighborhood = mapNodeToNeighborhood.get(neighbour);

        // Cardinality of the intersection between neighborhoods
        int neighborhoodsIntersectionCardinality = OrderingUtils.intArraySetIntersection(nodeNeighborhood, neighbourNeighborhood).size();

        // Cardinality of the union between neighborhoods
        int neighborhoodsUnionCardinality = OrderingUtils.intArraySetUnion(nodeNeighborhood, neighbourNeighborhood).size();

        return ((neighborhoodsIntersectionCardinality + 0.5d) / (neighborhoodsUnionCardinality + 0.5d)) * (1d / domainSize);
    }
}
