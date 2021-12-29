package ordering;

import it.unimi.dsi.fastutil.ints.*;


import java.util.Arrays;

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

    public static IntArraySet getEdgeNeighborhood(Int2ObjectOpenHashMap<NodesPair> mapEdgeToEndpoints, int edge) {
        IntArraySet neighborhood = new IntArraySet();
        NodesPair edgeEndpoints = mapEdgeToEndpoints.get(edge);

        for (int currentEdge : mapEdgeToEndpoints.keySet()) {
            if (currentEdge != edge) {
                NodesPair currentEdgeEndpoints = mapEdgeToEndpoints.get(currentEdge);

                if (edgeEndpoints.getFirstEndpoint().equals(currentEdgeEndpoints.getFirstEndpoint()) ||
                        edgeEndpoints.getFirstEndpoint().equals(currentEdgeEndpoints.getSecondEndpoint()) ||
                        edgeEndpoints.getSecondEndpoint().equals(currentEdgeEndpoints.getFirstEndpoint()) ||
                        edgeEndpoints.getSecondEndpoint().equals(currentEdgeEndpoints.getSecondEndpoint())) {
                    neighborhood.add(currentEdge);
                }
            }
        }

        return neighborhood;
    }

    public static Double computeSetWeight(IntArraySet edgeSet, Int2IntOpenHashMap domains) {
        double w = 0d;

        for (int edge : edgeSet) {
            int domain_size = domains.get(edge);
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
