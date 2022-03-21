package ordering;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
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

    public static Int2IntOpenHashMap computeEdgesDegree(ObjectArraySet<NodesPair> pairs,  Int2IntOpenHashMap nodes_degree) {
        Int2IntOpenHashMap map_pair_to_degree = new Int2IntOpenHashMap();
        for(NodesPair pair : pairs) {
            int first_endpoint_degree = nodes_degree.get(pair.getFirstEndpoint().intValue());
            int second_endpoint_degree = nodes_degree.get(pair.getSecondEndpoint().intValue());
            map_pair_to_degree.put(pair.getId().intValue(), (first_endpoint_degree + second_endpoint_degree));        }

        return map_pair_to_degree;
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

    static int shortestCycleLen(QueryStructure queryStructure)
    {
        int n = queryStructure.getQuery_nodes().keySet().size();

        Vector<Integer>[] gr = new Vector[n];

        for(int k : queryStructure.getQuery_nodes().keySet()) {
            gr[k] = new Vector<>();
            for(int i : queryStructure.getMap_node_to_neighborhood().get(k)) {
                gr[k].add(i);
            }
        }

        // To store length of the shortest cycle
        int ans = Integer.MAX_VALUE;

        // For all vertices
        for (int i = 0; i < n; i++)
        {

            // Make distance maximum
            int[] dist = new int[n];
            Arrays.fill(dist, (int) 1e9);

            // Take a imaginary parent
            int[] par = new int[n];
            Arrays.fill(par, -1);

            // Distance of source to source is 0
            dist[i] = 0;
            Queue<Integer> q = new LinkedList<>();

            // Push the source element
            q.add(i);

            // Continue until queue is not empty
            while (!q.isEmpty())
            {

                // Take the first element
                int x = q.poll();

                // Traverse for all it's childs
                for (int child : gr[x])
                {
                    // If it is not visited yet
                    if (dist[child] == (int) (1e9))
                    {

                        // Increase distance by 1
                        dist[child] = 1 + dist[x];

                        // Change parent
                        par[child] = x;

                        // Push into the queue
                        q.add(child);
                    } else if (par[x] != child && par[child] != x)
                        ans = Math.min(ans, dist[x] + dist[child] + 1);
                }
            }
        }

            return ans;
//        // If graph contains no cycle
//        if (ans == Integer.MAX_VALUE)
//            return -1;
//
//            // If graph contains cycle
//        else
    }
}
