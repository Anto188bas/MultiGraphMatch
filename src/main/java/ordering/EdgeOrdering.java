package ordering;


import cypher.models.QueryEdge;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;


public class EdgeOrdering {
    public static void computeEdgeOrdering(QueryStructure queryStructure, Int2IntOpenHashMap domains) {
        IntArrayList selectedEdges = new IntArrayList();

        // NODES
        System.out.println("QUERY NODES");
        IntSet nodeKeys = queryStructure.getQuery_nodes().keySet();

        for (int key : nodeKeys) {
            QueryNode node = queryStructure.getQuery_nodes().get(key);
            System.out.println("Key: " + key + "\tName: " + node.getNode_name() + "\tLabels: " + node.getLabels());
        }

        System.out.println();

        // EDGES
        System.out.println("QUERY EDGES");
        IntSet unusedEdgeKeys = queryStructure.getQuery_edges().clone().keySet(); // We clone queryStructure.getQuery_edges() in order to preserve the original key set
        for (int key : unusedEdgeKeys) {
            QueryEdge edge = queryStructure.getQuery_edges().get(key);
            System.out.println("Key: " + key + "\tName: " + edge.getEdge_name() + "\tLabel: " + edge.getEdge_label());
        }
        System.out.println();

        System.out.println("IN-EDGES");
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges = queryStructure.getQuery_pattern().getIn_edges();
        System.out.println(inEdges);
        System.out.println();

        System.out.println("OUT-EDGES");
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges = queryStructure.getQuery_pattern().getOut_edges();
        System.out.println(outEdges);
        System.out.println();

        System.out.println("IN-OUT-EDGES");
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inOutEdges = queryStructure.getQuery_pattern().getIn_out_edges();
        System.out.println(inOutEdges);
        System.out.println();

        // AUXILIARY INFO
        System.out.println("AUXILIARY INFO");
        Int2ObjectOpenHashMap<int[]> mapEdgeToEndpoints = new Int2ObjectOpenHashMap<>();// N.B. endpoints are not ordered!

        for (int edgeKey : unusedEdgeKeys) {
            int[] endpoints = OrderingUtils.getEdgeEndpoints(outEdges, edgeKey);

            if (endpoints != null) {
                mapEdgeToEndpoints.put(edgeKey, endpoints);

                System.out.println("EDGE: " + edgeKey + "\tENDPOINTS: [" + endpoints[0] + ", " + endpoints[1] + "]");
            }
        }
        System.out.println();

        Int2ObjectOpenHashMap<IntArraySet> mapEdgeToNeighborhood = new Int2ObjectOpenHashMap<>();

        for (int edgeKey : unusedEdgeKeys) {
            IntArraySet edgeNeighborhood = OrderingUtils.getEdgeNeighborhood(mapEdgeToEndpoints, edgeKey);
            mapEdgeToNeighborhood.put(edgeKey, edgeNeighborhood);
            System.out.println("EDGE: " + edgeKey + "\tNEIGHBORHOOD: " + edgeNeighborhood);
        }
        System.out.println();

        // First edge of the ordering

        // We search the node with the higher degree
        Int2IntOpenHashMap degrees = OrderingUtils.computeDegrees(outEdges);
        IntArrayList maxNodes = new IntArrayList();
        int maxDegree = 0;

        for (int key : nodeKeys) {
            int currentDegree = degrees.get(key);

            if (currentDegree > maxDegree) {
                maxDegree = currentDegree;
                maxNodes = new IntArrayList();
                maxNodes.push(key);
            } else if (currentDegree == maxDegree) {
                maxNodes.push(key);
            }
        }

        // There can be multiple nodes with the same max degree.
        System.out.println("NODE(S) WITH MAX DEGREE: " + maxNodes);

        Int2ObjectOpenHashMap<Double> mapEdgeToJaccardSimilarity = new Int2ObjectOpenHashMap<>();

        // For each node having max-degree we consider his neighborhood.
        for (int node : maxNodes) {
            // We compute the Jaccard similarity between the node and all his neighbours.
            IntArraySet outNeighborhood = OrderingUtils.getNodeNeighborhood(outEdges, node); // out-neighborhood
            IntArraySet inNeighborhood = OrderingUtils.getNodeNeighborhood(inEdges, node); // in-neighborhood
            IntArraySet neighborhood = OrderingUtils.intArraySetUnion(outNeighborhood, inNeighborhood); // union of in-neighborhood and out-neighborhood

            for (int outNeighbour : outNeighborhood) {
                int edgeId = outEdges.get(node).get(outNeighbour).getInt(0);
                int domainSize = domains.get(edgeId);

                Double jaccardSimilarity = OrderingUtils.computeJaccardSimilarity(node, neighborhood, outNeighbour, inEdges, outEdges, domainSize);
                mapEdgeToJaccardSimilarity.put(edgeId, jaccardSimilarity);
                System.out.println("EDGE ID: " + edgeId + "\tJACCARD SIMILARITY: " + jaccardSimilarity);
            }

            for (int inNeighbour : inNeighborhood) {
                int edgeId = inEdges.get(node).get(inNeighbour).getInt(0);
                int domainSize = domains.get(edgeId);

                Double jaccardSimilarity = OrderingUtils.computeJaccardSimilarity(node, neighborhood, inNeighbour, inEdges, outEdges, domainSize);
                mapEdgeToJaccardSimilarity.put(edgeId, jaccardSimilarity);
                System.out.println("EDGE ID: " + edgeId + "\tJACCARD SIMILARITY: " + jaccardSimilarity);
            }
        }

        // We search the edge having the maximum Jaccard Similarity
        int maxQueryEdge = -1;
        Double maxJaccardSimilarity = -1d;

        for (int currentQueryEdge : mapEdgeToJaccardSimilarity.keySet()) {
            Double currentJaccardSimilarity = mapEdgeToJaccardSimilarity.get(currentQueryEdge);

            if (currentJaccardSimilarity > maxJaccardSimilarity) {
                maxJaccardSimilarity = currentJaccardSimilarity;
                maxQueryEdge = currentQueryEdge;
            }
        }

        selectedEdges.push(maxQueryEdge);
        unusedEdgeKeys.remove(maxQueryEdge);

        System.out.println("FIRST EDGE OF THE ORDERING: " + maxQueryEdge);

        // Residual edges ordering
        while (!unusedEdgeKeys.isEmpty()) {
            IntArraySet orderingNeighborhood = new IntArraySet();


            for (int edge : selectedEdges) {
                IntArraySet neighborhood = mapEdgeToNeighborhood.get(edge);

                for (int neighbour : neighborhood) {
                    // We don't consider edges already added to the ordering
                    if (!selectedEdges.contains(neighbour)) {
                        orderingNeighborhood.add(neighbour);
                    }
                }
            }

            Int2ObjectOpenHashMap<double[]> neighborhoodWeights = new Int2ObjectOpenHashMap<>();

            for (int current : orderingNeighborhood) {
                IntArraySet neighborhood = mapEdgeToNeighborhood.get(current).clone(); // We clone the neighborhood because we modify it as we calculate the weights

                IntArraySet os = new IntArraySet();
                IntArraySet ns = new IntArraySet();
                IntArraySet rs = new IntArraySet();

                // weights[0] = w_os
                // weights[1] = w_ns
                // weights[2] = w_rs
                // weights[3] = domain cardinality reciprocal
                double[] weights = new double[4];

                Double w_os, w_ns, w_rs;

                for (int neighbour : neighborhood) {
                    if (selectedEdges.contains(neighbour)) {
                        os.add(neighbour);
                    }
                }

                // Let's remove the OS edges from the neighborhood. OS, NS and RS must be pairwise disjoint.
                neighborhood.removeAll(os); //// We use this IntArraySet because we can't remove elements during a for-each loop (tests have already been done)

                w_os = OrderingUtils.computeSetWeight(os, domains);
                weights[0] = w_os;

                // NS (neighbour edges having at least a common node with at least one edges into OS)
                for (int neighbour : neighborhood) {
                    int[] currentNeighbourEndpoints = mapEdgeToEndpoints.get(neighbour);
                    for(int edge: os) {
                        int[] currentOsEdgeEndpoints = mapEdgeToEndpoints.get(edge);
                        if (currentOsEdgeEndpoints[0] == currentNeighbourEndpoints[0] ||
                                currentOsEdgeEndpoints[0] == currentNeighbourEndpoints[1] ||
                                currentOsEdgeEndpoints[1] == currentNeighbourEndpoints[0] ||
                                currentOsEdgeEndpoints[1] == currentNeighbourEndpoints[1]) {
                            ns.add(neighbour);
                            break;
                        }
                    }
                }

                // Let's remove the NS edges from the neighborhood. OS, NS and RS must be pairwise disjoint.
                neighborhood.removeAll(ns);

                w_ns = OrderingUtils.computeSetWeight(ns, domains);
                weights[1] = w_ns;

                // RS (neighbour edges having no common nodes with edges into OS and NS)
                for (int neighbour : neighborhood) {
                    boolean commonNodes = false;
                    int[] currentNeighbourEndpoints = mapEdgeToEndpoints.get(neighbour);

                    // Searching common nodes between current edge and edges into OS
                    for (int edge : os) {
                        int[] currentOsEdgeEndpoints = mapEdgeToEndpoints.get(edge);

                        if (currentOsEdgeEndpoints[0] == currentNeighbourEndpoints[0] ||
                                currentOsEdgeEndpoints[0] == currentNeighbourEndpoints[1] ||
                                currentOsEdgeEndpoints[1] == currentNeighbourEndpoints[0] ||
                                currentOsEdgeEndpoints[1] == currentNeighbourEndpoints[1]) {
                            commonNodes = true;
                            break;
                        }
                    }

                    // Searching for common nodes between the current edge and edges into NS.
                    // We don't need to execute this piece of code if we have already found common nodes.
                    if (!commonNodes) {

                        for (int edge : ns) {
                            int[] currentNsEdgeEndpoints = mapEdgeToEndpoints.get(edge);

                            if (currentNsEdgeEndpoints[0] == currentNeighbourEndpoints[0] ||
                                    currentNsEdgeEndpoints[0] == currentNeighbourEndpoints[1] ||
                                    currentNsEdgeEndpoints[1] == currentNeighbourEndpoints[0] ||
                                    currentNsEdgeEndpoints[1] == currentNeighbourEndpoints[1]) {
                                commonNodes = true;
                                break;
                            }
                        }
                    }

                    // If there are no common nodes, we can add current edge to RS
                    if (!commonNodes) {
                        rs.add(neighbour);
                    }
                }

                // Let's remove the RS edges from the neighborhood. OS, NS and RS must be pairwise disjoint
                neighborhood.removeAll(rs);

                w_rs = OrderingUtils.computeSetWeight(rs, domains);
                weights[2] = w_rs;

                // Domain size reciprocal
                int domain_size = domains.get(current);
                weights[3] = 1d / domain_size;

                System.out.println("********************************************************************");
                System.out.println("CURRENT EDGE: " + current);
                System.out.println("OS: " + os);
                System.out.println("NS: " + ns);
                System.out.println("RS: " + rs);
                System.out.println("W_OS: " + w_os);
                System.out.println("W_NS: " + w_ns);
                System.out.println("W_RS: " + w_rs);
                System.out.println("W_DOMAIN: " + weights[3]);
                System.out.println("********************************************************************");

                neighborhoodWeights.put(current, weights);
            }

            /*
             Here we select the next node of the ordering.
             Criteria:
             1. node with maximum w_os;
             2. in case of a tie, node with maximum w_ns;
             3. in case of a tie, node with maximum w_rs;
             4. in case of a tie, node with maximum domain cardinality reciprocal
             5. in case of a tie, first node between nodes having maximum domain cardinality reciprocal
             */
            for (int index = 0; index < 4; index++) {
                double maxWeight = -1;
                IntArrayList maxEdges = new IntArrayList();

                for (int edgeKey : neighborhoodWeights.keySet()) {
                    double[] weights = neighborhoodWeights.get(edgeKey);

                    if (weights[index] > maxWeight) {
                        maxWeight = weights[index];
                        maxEdges = new IntArrayList();
                        maxEdges.add(edgeKey);
                    } else if (weights[index] == maxWeight) {
                        maxEdges.add(edgeKey);
                    }
                }

                if (maxEdges.size() == 1 || index == 3) { // If there is no tie (cases 1, 2, 3, and 4) or there are ties in all weights (case 5)
                    System.out.println("MAX EDGE(S): " + maxEdges);
                    int nextEdgeId = maxEdges.getInt(0);

                    selectedEdges.push(nextEdgeId);
                    unusedEdgeKeys.remove(nextEdgeId);
                    break;
                }
            }

            System.out.println("CURRENT ORDERING: " + selectedEdges);
            System.out.println("UNUSED EDGE KEYS: " + unusedEdgeKeys);

        }

        System.out.println("ORDERING: " + selectedEdges);
    }
}
