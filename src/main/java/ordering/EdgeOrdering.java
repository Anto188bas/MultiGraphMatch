package ordering;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class EdgeOrdering {
    public static int[] computePairsOrdering(QueryStructure query_structure, Int2ObjectOpenHashMap<Int2IntOpenHashMap> aggregate_domain) {
        //************************************************************* PRE PROCESSING *************************************************************//
        IntArrayList edge_ordering = new IntArrayList();
        // NODES
        IntSet node_keys = query_structure.getQuery_nodes().keySet();

        // EDGES
        IntSet edge_keys = query_structure.getQuery_edges().clone().keySet(); // We clone query_structure.getQuery_edges() in order to preserve the original key set

        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_edges = query_structure.getQuery_pattern().getIn_edges();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> out_edges = query_structure.getQuery_pattern().getOut_edges();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_out_edges = query_structure.getQuery_pattern().getIn_out_edges();

        // PAIR OF NODES HAVING AT LEAST ONE EDGE
        ObjectArrayList<NodesPair> selected_pairs = new ObjectArrayList<>();
        ObjectArrayList<NodesPair> unselected_pairs = new ObjectArrayList<>();
        Int2ObjectOpenHashMap<IntArraySet> map_endpoints_to_edges = new Int2ObjectOpenHashMap<>();

        for (int edge_key : edge_keys) {
            NodesPair endpoints = OrderingUtils.getEdgeEndpoints(out_edges, edge_key);

            if (endpoints == null) { // Undirected edge
                endpoints = OrderingUtils.getEdgeEndpoints(in_out_edges, edge_key);
            } // Else is a directed edge

            if(map_endpoints_to_edges.containsKey(endpoints.getId().intValue())) {
                map_endpoints_to_edges.get(endpoints.getId().intValue()).add(edge_key);
            } else {
                IntArraySet edge_set = new IntArraySet();
                edge_set.add(edge_key);
                map_endpoints_to_edges.put(endpoints.getId().intValue(), edge_set);
            }

            if (!unselected_pairs.contains(endpoints)) {
                unselected_pairs.push(endpoints);
            }
        }

        // DOMAINS CARDINALITY
        Int2IntOpenHashMap domains_cardinality = new Int2IntOpenHashMap();

        aggregate_domain.int2ObjectEntrySet().fastForEach(record -> {
            int src = record.getIntKey();
            record.getValue().int2IntEntrySet().fastForEach(sub_record -> {
                int dst = sub_record.getIntKey();

                NodesPair pair = new NodesPair(src, dst);
                domains_cardinality.put(pair.getId().intValue(), sub_record.getIntValue());
            });
        });

        // MAP EACH NODE TO ITS NEIGHBORHOOD
        Int2ObjectOpenHashMap<IntArraySet> map_node_to_neighborhood = new Int2ObjectOpenHashMap<>();
        for (int node : node_keys) {
            IntArraySet node_neighborhood = OrderingUtils.getNodeNeighborhood(node, in_edges, out_edges, in_out_edges);
            map_node_to_neighborhood.put(node, node_neighborhood);
        }

        // MAP EACH PAIR OD NODES TO ITS NEIGHBORHOOD
        Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>> map_pair_to_neighborhood = new Int2ObjectOpenHashMap<>();

        for (NodesPair pair : unselected_pairs) {
            ObjectArraySet<NodesPair> pair_neighborhood = OrderingUtils.getPairNeighborhood(pair, map_node_to_neighborhood);
            map_pair_to_neighborhood.put(pair.getId().intValue(), pair_neighborhood);
        }
        //******************************************************************************************************************************************//

        //*************************************************************** FIRST PAIR ***************************************************************//
        // First pair of the ordering
        Int2IntOpenHashMap degrees = OrderingUtils.computeDegrees(node_keys, in_edges, out_edges, in_out_edges);

        // We search the node with the higher degree
        IntArrayList max_nodes = new IntArrayList();
        int max_degree = 0;

        for (int key : node_keys) {
            int current_degree = degrees.get(key);

            if (current_degree > max_degree) {
                max_degree = current_degree;
                max_nodes = new IntArrayList();
                max_nodes.push(key);
            } else if (current_degree == max_degree) {
                max_nodes.push(key);
            }
        }


        // There can be multiple nodes with the same max degree.
        // For each node having max-degree we consider his neighborhood.
        // We compute the Jaccard similarity between the node and all his neighbours.
        Int2DoubleOpenHashMap map_pair_to_jaccard_similarity = new Int2DoubleOpenHashMap();
        for (int node : max_nodes) {
            IntArraySet neighborhood = map_node_to_neighborhood.get(node);

            for (int neighbour : neighborhood) {
                NodesPair current_endpoints = new NodesPair(node, neighbour);
                int domain_size = domains_cardinality.get(current_endpoints.getId().intValue());

                Double jaccard_similarity = OrderingUtils.computeJaccardSimilarity(node, neighbour, map_node_to_neighborhood, domain_size);
                map_pair_to_jaccard_similarity.put(current_endpoints.getId().intValue(), jaccard_similarity.doubleValue());
            }
        }

        // We search the pair having the maximum Jaccard Similarity
        NodesPair max_query_pair = new NodesPair();
        double max_jaccard_similarity = -1d;

        for (NodesPair pair : unselected_pairs) {
            double current_jaccard_similarity = map_pair_to_jaccard_similarity.get(pair.getId().intValue());

            if (current_jaccard_similarity > max_jaccard_similarity) {
                max_jaccard_similarity = current_jaccard_similarity;
                max_query_pair = pair;
            }
        }

        selected_pairs.push(max_query_pair);
        unselected_pairs.remove(max_query_pair);
        edge_ordering.addAll(map_endpoints_to_edges.get(max_query_pair.getId().intValue()));
        //******************************************************************************************************************************************//

        //************************************************************* RESIDUAL PAIRS *************************************************************//
        // Residual pairs ordering
        while (!unselected_pairs.isEmpty()) {
            ObjectArraySet<NodesPair> ordered_pairs_neighborhood = new ObjectArraySet<>();
            // We build the neighborhood of the already selected pairs
            selected_pairs.forEach(pair ->
                    map_pair_to_neighborhood.get(pair.getId().intValue()).forEach(neighbour_pair -> {
                        // We don't consider pairs already selected
                        if (!selected_pairs.contains(neighbour_pair)) {
                            ordered_pairs_neighborhood.add(neighbour_pair);
                        }
                    }));

            // For each of these pairs, we compute four weights
            Int2ObjectOpenHashMap<double[]> neighborhood_weights = new Int2ObjectOpenHashMap<>();

            for (NodesPair current_pair : ordered_pairs_neighborhood) {
                ObjectArraySet<NodesPair> current_pair_neighborhood = map_pair_to_neighborhood.get(current_pair.getId().intValue()).clone(); // We clone the neighborhood because we modify it while we calculate weights

                ObjectArraySet<NodesPair> os = new ObjectArraySet<>();
                ObjectArraySet<NodesPair> ns = new ObjectArraySet<>();
                ObjectArraySet<NodesPair> rs = new ObjectArraySet<>();

                // weights[0] = w_os
                // weights[1] = w_ns
                // weights[2] = w_rs
                // weights[3] = domain cardinality reciprocal
                double[] weights = new double[4];

                Double w_os, w_ns, w_rs;

                // OS (neighbour pairs already selected for the ordering)
                for (NodesPair neighbour : current_pair_neighborhood) {
                    if (selected_pairs.contains(neighbour)) {
                        os.add(neighbour);
                    }
                }

                // Let's remove the OS pairs from the neighborhood. OS, NS and RS must be pairwise disjoint.
                current_pair_neighborhood.removeAll(os); //// We use this IntArraySet because we can't remove elements during a for-each loop (tests have already been done)

                w_os = OrderingUtils.computeSetWeight(os, domains_cardinality);
                weights[0] = w_os;

                // NS (neighbour pairs having at least a common node with at least one pair into OS)
                for (NodesPair neighbour : current_pair_neighborhood) {
                    for (NodesPair os_pair : os) {
                        if (neighbour.hasCommonNodes(os_pair)) {
                            ns.add(neighbour);
                            break;
                        }
                    }
                }

                // Let's remove the NS pairs from the neighborhood. OS, NS and RS must be pairwise disjoint.
                current_pair_neighborhood.removeAll(ns);

                w_ns = OrderingUtils.computeSetWeight(ns, domains_cardinality);
                weights[1] = w_ns;

                // RS (neighbour pairs having no common nodes with pairs into OS and NS)
                for (NodesPair neighbour : current_pair_neighborhood) {
                    boolean commonNodes = false;

                    // Searching common nodes between current pair and pairs into OS
                    for (NodesPair os_pair : os) {

                        if (neighbour.hasCommonNodes(os_pair)) {
                            ns.add(neighbour);
                            break;
                        }
                    }

                    // Searching for common nodes between the current pair and pairs into NS.
                    // We don't need to execute this piece of code if we have already found common nodes.
                    if (!commonNodes) {
                        for (NodesPair ns_pair : ns) {
                            if (neighbour.hasCommonNodes(ns_pair)) {
                                ns.add(neighbour);
                                break;
                            }
                        }
                    }

                    // If there are no common nodes, we can add current pair to RS
                    if (!commonNodes) {
                        rs.add(neighbour);
                    }
                }

                // Let's remove the RS pairs from the neighborhood. OS, NS and RS must be pairwise disjoint
                current_pair_neighborhood.removeAll(rs);

                w_rs = OrderingUtils.computeSetWeight(rs, domains_cardinality);
                weights[2] = w_rs;

                // Domain size reciprocal
                int domain_size = domains_cardinality.get(current_pair.getId().intValue());
                weights[3] = 1d / domain_size;

                neighborhood_weights.put(current_pair.getId().intValue(), weights);
            }

            /*
             Here we select the next pair of the ordering.
             Criteria:
             1. pair with maximum w_os;
             2. in case of a tie, pair with maximum w_ns;
             3. in case of a tie, pair with maximum w_rs;
             4. in case of a tie, pair with maximum domain cardinality reciprocal
             5. in case of a tie, first pair between pairs having maximum domain cardinality reciprocal
             */
            for (int index = 0; index < 4; index++) {
                double max_weight = -1;
                ObjectArrayList<NodesPair> max_pairs = new ObjectArrayList<>();

                for (NodesPair current_pair : ordered_pairs_neighborhood) {
                    double[] weights = neighborhood_weights.get(current_pair.getId().intValue());

                    if (weights[index] > max_weight) {
                        max_weight = weights[index];
                        max_pairs = new ObjectArrayList<>();
                        max_pairs.add(current_pair);
                    } else if (weights[index] == max_weight) {
                        max_pairs.add(current_pair);
                    }

                }
                if (max_pairs.size() == 1 || index == 3) { // If there is no tie (cases 1, 2, 3, and 4) or there are ties in all weights (case 5)
                    unselected_pairs.remove(max_pairs.get(0));
                    selected_pairs.push(max_pairs.get(0));
                    edge_ordering.addAll(map_endpoints_to_edges.get(max_pairs.get(0).getId().intValue()));

                    break;
                }
            }

        }
        //******************************************************************************************************************************************//
        System.out.println("PAIRS: ");

        for (NodesPair pair : selected_pairs) {
            System.out.println("\tPAIR: {" + pair + "} \tEDGES: " + map_endpoints_to_edges.get(pair.getId().intValue()));
        }

        System.out.println("EDGES:\n\t" + edge_ordering);

        return edge_ordering.toIntArray();
    }

    public static int[] getInverseMap(int[] map) {
        int[] inverse = new int[map.length];

        for(int i = 0; i < map.length; i++) {
            inverse[map[i]] = i;
        }

        return inverse;
    }
}
