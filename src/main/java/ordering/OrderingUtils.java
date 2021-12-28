package ordering;

import it.unimi.dsi.fastutil.ints.*;


import java.util.Arrays;

public class OrderingUtils {

    public static int[] getEdgeEndpoints(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges, int edgeId) {

        for (int firstEndpoint : edges.keySet()) {
            Int2ObjectOpenHashMap<IntArrayList> adjList = edges.get(firstEndpoint);

            for (int secondEndpoint : adjList.keySet()) {
                int _edgeId = adjList.get(secondEndpoint).getInt(0);

                if (_edgeId == edgeId) {
                    return new int[]{firstEndpoint, secondEndpoint};
                }
            }
        }

        return null;
    }

    public static int[] intArrayIntersection(int[] a, int[] b) {
        return Arrays.stream(a).distinct().filter(x -> Arrays.stream(b).anyMatch(y -> y == x)).toArray();
    }

    public static IntArraySet getEdgeNeighborhood(Int2ObjectOpenHashMap<int[]> mapEdgeToEndpoints, int edge) {
        IntArraySet neighborhood = new IntArraySet();
        int[] edgeEndpoints = mapEdgeToEndpoints.get(edge);

        for (int currentEdge : mapEdgeToEndpoints.keySet()) {
            if (currentEdge != edge) {
                int[] currentEdgeEndpoints = mapEdgeToEndpoints.get(currentEdge);

                if (edgeEndpoints[0] == currentEdgeEndpoints[0] ||
                        edgeEndpoints[0] == currentEdgeEndpoints[1] ||
                        edgeEndpoints[1] == currentEdgeEndpoints[0] ||
                        edgeEndpoints[1] == currentEdgeEndpoints[1]) {
                    neighborhood.add(currentEdge);
                }
            }
        }

        return neighborhood;
    }

    public static Double computeSetWeight(IntArraySet edgeSet, Int2IntOpenHashMap domains) {
        double w = 0d;

        for(int edge: edgeSet) {
            int domain_size = domains.get(edge);
            w += 1d / domain_size;
        }

        return w;
    }

    public static Int2IntOpenHashMap computeDegrees(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges) {
        Int2IntOpenHashMap degrees = new Int2IntOpenHashMap();

        for (int firstEndpoint : edges.keySet()) {
            Int2ObjectOpenHashMap<IntArrayList> adjList = edges.get(firstEndpoint);

            degrees.put(firstEndpoint, adjList.size());
        }

        return degrees;
    }

    public static IntArraySet getNodeNeighborhood(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges, int nodeKey) {
        if (edges.containsKey(nodeKey)) {
            return new IntArraySet(edges.get(nodeKey).keySet());
        }
        return new IntArraySet();
    }

    public static IntArraySet intArraySetUnion(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        for (int val : b) {
            result.add(val);
        }

        return result;
    }

    public static Double computeJaccardSimilarity(int node, IntArraySet nodeNeighborhood, int currentNeighbour, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges, int domainSize) {
        // Neighbor's neighborhood
        IntArraySet currentNeighbourNeighborhood = OrderingUtils.intArraySetUnion(OrderingUtils.getNodeNeighborhood(outEdges, currentNeighbour),  // out-neighborhood
                OrderingUtils.getNodeNeighborhood(inEdges, currentNeighbour)    // in-neighborhood
        );

        // Cardinality of the intersection between neighborhoods
        int neighborhoodsIntersectionCardinality = OrderingUtils.intArrayIntersection(currentNeighbourNeighborhood.toIntArray(), nodeNeighborhood.toIntArray()).length;

        return ((neighborhoodsIntersectionCardinality + 0.5d) / (nodeNeighborhood.size() + 0.5d)) * (1d / domainSize);
    }

}
