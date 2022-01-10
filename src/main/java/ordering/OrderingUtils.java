package ordering;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class OrderingUtils {

    public static NodesPair getEdgeEndpoints(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges, int edgeId) {

        for (int firstEndpoint : edges.keySet()) {
            Int2ObjectOpenHashMap<IntArrayList> adjList = edges.get(firstEndpoint);

            for (int secondEndpoint : adjList.keySet()) {
                int _edgeId = adjList.get(secondEndpoint).getInt(0);

                if (_edgeId == edgeId) {
                    return new NodesPair(firstEndpoint, secondEndpoint);  // Endpoints are lexicographically ordered
                }
            }
        }

        return null;
    }

    public static ObjectArraySet<NodesPair> getPairNeighborhood(NodesPair pair, Int2ObjectOpenHashMap<IntArraySet> map_node_to_neighborhood) {
        ObjectArraySet<NodesPair> neighborhood = new ObjectArraySet<>();

        int node = pair.getFirstEndpoint().intValue();

        for (int neighbour : map_node_to_neighborhood.get(node)) {
            neighborhood.add(new NodesPair(node, neighbour));
        }

        node = pair.getSecondEndpoint().intValue();

        for (int neighbour : map_node_to_neighborhood.get(node)) {
            neighborhood.add(new NodesPair(node, neighbour));
        }

        return neighborhood;
    }

    public static Double computeSetWeight(ObjectArraySet<NodesPair> pair_set, Int2IntOpenHashMap domains) {
        double w = 0d;
        for (NodesPair pair : pair_set) {
            int domain_size = domains.get(pair.getId().intValue());
            w += 1d / domain_size;

        }

        return w;
    }

    public static IntArraySet getNodeNeighborhood(int nodeKey, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inOutEdges) {
        IntArraySet neighborhood = new IntArraySet();

        if (inEdges.containsKey(nodeKey)) {
            neighborhood.addAll(inEdges.get(nodeKey).keySet());
        }

        if (outEdges.containsKey(nodeKey)) {
            neighborhood.addAll(outEdges.get(nodeKey).keySet());
        }

        if (inOutEdges.containsKey(nodeKey)) {
            neighborhood.addAll(inOutEdges.get(nodeKey).keySet());
        }

        return neighborhood;
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
