package ordering;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class EdgeOrdering {
    private ObjectArraySet<NodesPair> selected_pairs;
    private ObjectArraySet<NodesPair> unselected_pairs;
    private Int2ObjectOpenHashMap<IntArraySet> map_endpoints_to_edges;
    private IntArrayList edge_ordering;
    private IntArraySet ordered_nodes;
    int state_index;

    /*******/
    private final QueryStructure query_structure;
    private int[] map_edge_to_state;
    private EdgeDirection[] map_edge_to_direction;
    private int[] map_state_to_edge;
    private int[] map_state_to_src;
    private int[] map_state_to_dst;
    private int[] map_state_to_unmapped_nodes;
    private ObjectArraySet<NodesPair> pairs_ordering;

    public EdgeOrdering(QueryStructure query_structure) {
        this.query_structure = query_structure;

        this.computePairsOrdering();
    }

    private void addPairToTheOrdering(NodesPair query_pair_to_add) {
        unselected_pairs.remove(query_pair_to_add);
        selected_pairs.add(query_pair_to_add);
        IntArraySet current_edge_set = map_endpoints_to_edges.get(query_pair_to_add.getId().intValue());
        edge_ordering.addAll(current_edge_set);

        if (!ordered_nodes.contains(query_pair_to_add.getFirstEndpoint().intValue())) {
            map_state_to_unmapped_nodes[state_index++] = query_pair_to_add.getFirstEndpoint().intValue();
        } else if (!ordered_nodes.contains(query_pair_to_add.getSecondEndpoint().intValue())) {
            map_state_to_unmapped_nodes[state_index++] = query_pair_to_add.getSecondEndpoint().intValue();
        } else {
            map_state_to_unmapped_nodes[state_index++] = -1;
        }

        if (current_edge_set.size() > 1) {
            for (int i = 0; i < current_edge_set.size() - 1; i++) {
                map_state_to_unmapped_nodes[state_index++] = -1;
            }
        }

        ordered_nodes.add(query_pair_to_add.getFirstEndpoint().intValue());
        ordered_nodes.add(query_pair_to_add.getSecondEndpoint().intValue());
    }

    private void computePairsOrdering() {
        //************************************************************* PRE PROCESSING *************************************************************//
        edge_ordering = new IntArrayList();
        // NODES
        IntSet node_keys = query_structure.getQuery_nodes().keySet();

        // EDGES
        IntSet edge_keys = query_structure.getQuery_edges().clone().keySet(); // We clone query_structure.getQuery_edges() in order to preserve the original key set

        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_edges = query_structure.getQuery_pattern().getIn_edges();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> out_edges = query_structure.getQuery_pattern().getOut_edges();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_out_edges = query_structure.getQuery_pattern().getIn_out_edges();

        // PAIR OF NODES HAVING AT LEAST ONE EDGE
        selected_pairs = new ObjectArraySet<>();
        unselected_pairs = query_structure.getPairs().clone();

        // MAP EACH PAIR TO ITS EDGES
        map_endpoints_to_edges = query_structure.getMap_endpoints_to_edges();

        // MAP EACH EDGE TO ITS ENDPOINTS
        Int2ObjectOpenHashMap<NodesPair> map_edge_to_endpoints = query_structure.getMap_edge_to_endpoints();

        // MAP EACH NODE TO ITS NEIGHBORHOOD
        Int2ObjectOpenHashMap<IntArraySet> map_node_to_neighborhood = query_structure.getMap_node_to_neighborhood();

        // MAP EACH PAIR OF NODES TO ITS NEIGHBORHOOD
        Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>> map_pair_to_neighborhood = query_structure.getMap_pair_to_neighborhood();

        // MAP AN ID TO THE CORRESPONDING PAIR
        Int2ObjectOpenHashMap<NodesPair> map_id_to_pair = query_structure.getMap_id_to_pair();

        // DEGREE OF EACH NODE
        Int2IntOpenHashMap nodes_degree = OrderingUtils.computeNodesDegree(node_keys, in_edges, out_edges, in_out_edges);

        // MAP EACH PAIR TO THE CORRESPONDING DEGREE  d(u, v) = d(u) + d(v)
        Int2IntOpenHashMap map_pair_to_degree = OrderingUtils.computeEdgesDegree(unselected_pairs, nodes_degree);


        // MAP EACH PAIR TO THE CORRESPONDING WEIGHTED DEGREE  (1 - \frac{dom(p)}{domains_sum}) * \frac{deg(p)}{degrees_sum}
        Int2DoubleOpenHashMap map_pair_to_weighted_degree = new Int2DoubleOpenHashMap();

        int domains_sum = unselected_pairs.stream().map(NodesPair::getDomain_size).reduce(0, Integer::sum);
        int degrees_sum = nodes_degree.values().stream().reduce(0, Integer::sum);
        for(NodesPair pair: unselected_pairs) {
            double weighted_degree = (1 - (pair.getDomain_size()/domains_sum)) * (map_pair_to_degree.get(pair.getId().intValue()) / degrees_sum);
            map_pair_to_weighted_degree.put(pair.getId().intValue(), weighted_degree);
        }
        //******************************************************************************************************************************************//

        //*************************************************************** FIRST PAIR ***************************************************************//
        ordered_nodes = new IntArraySet();
        map_state_to_unmapped_nodes = new int[edge_keys.size()];
        IntArraySet current_edge_set;
        state_index = 0;
        NodesPair query_pair_to_add = null;

        int shortest_cycle_len = OrderingUtils.shortestCycleLen(query_structure);

        if(shortest_cycle_len < 4) { //If there is a triangle we use the Jaccard Similarity
            // We search the node with the higher degree
            IntArrayList max_nodes = new IntArrayList();
            int max_degree = 0;

            for (int key : node_keys) {
                int current_degree = nodes_degree.get(key);

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
                    int domain_size = map_id_to_pair.get(current_endpoints.getId().intValue()).getDomain_size();

                    Double jaccard_similarity = OrderingUtils.computeJaccardSimilarity(node, neighbour, map_node_to_neighborhood, domain_size);
                    map_pair_to_jaccard_similarity.put(current_endpoints.getId().intValue(), jaccard_similarity.doubleValue());
                }
            }

            // We search the pair having the maximum Jaccard Similarity
            double max_jaccard_similarity = -1d;

            for (NodesPair pair : unselected_pairs) {
                double current_jaccard_similarity = map_pair_to_jaccard_similarity.get(pair.getId().intValue());

                if (current_jaccard_similarity > max_jaccard_similarity) {
                    max_jaccard_similarity = current_jaccard_similarity;
                    query_pair_to_add = pair;
                }
            }

        } else { // If there isn't a triangle we use the weighted edge-degree
            double max_weighted_degree = -1d;

            for (NodesPair pair : unselected_pairs) {
                double current_weighted_degree = map_pair_to_weighted_degree.get(pair.getId().intValue());

                if (current_weighted_degree > max_weighted_degree) {
                    max_weighted_degree = current_weighted_degree;
                    query_pair_to_add = pair;
                }
            }
        }

        addPairToTheOrdering(query_pair_to_add);
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

            // Pairs having both endpoints matched
            ObjectArraySet<NodesPair> pairs_with_both_endpoints_matched = new ObjectArraySet<>();
            ordered_pairs_neighborhood.forEach((pair) -> {
                if (ordered_nodes.contains(pair.getFirstEndpoint().intValue()) && ordered_nodes.contains((pair.getSecondEndpoint().intValue()))) {
                    pairs_with_both_endpoints_matched.add(pair);
                }
            });

            // If there are pairs with both endpoint matched, we select the next pair of the ordering from these pairs
            if (pairs_with_both_endpoints_matched.size() > 0) {
                int minimum_domain_size = Integer.MAX_VALUE;
                NodesPair selected_pair = null;

                for (NodesPair pair : pairs_with_both_endpoints_matched) {
                    int domain_size = map_id_to_pair.get(pair.getId().intValue()).getDomain_size();
                    if (domain_size < minimum_domain_size) {
                        minimum_domain_size = domain_size;
                        selected_pair = pair;
                    }
                }

                addPairToTheOrdering(selected_pair);
            } else { // If there aren't pairs with both endpoint matched, we select the next pair of the ordering using OS
                // For each of these pairs, we compute OS's weight
                Int2DoubleOpenHashMap neighborhood_weights = new Int2DoubleOpenHashMap();

                for (NodesPair current_pair : ordered_pairs_neighborhood) {
                    ObjectArraySet<NodesPair> current_pair_neighborhood = map_pair_to_neighborhood.get(current_pair.getId().intValue()).clone(); // We clone the neighborhood because we modify it while we calculate weights

                    ObjectArraySet<NodesPair> os = new ObjectArraySet<>();

                    double w_os;

                    // OS (neighbour pairs already selected for the ordering)
                    for (NodesPair neighbour : current_pair_neighborhood) {
                        if (selected_pairs.contains(neighbour)) {
                            os.add(neighbour);
                        }
                    }

                    // Let's remove the OS pairs from the neighborhood. OS, NS and RS must be pairwise disjoint.
                    current_pair_neighborhood.removeAll(os); //// We use this IntArraySet because we can't remove elements during a for-each loop (tests have already been done)

                    w_os = OrderingUtils.computeSetWeight(os, map_id_to_pair);

                    neighborhood_weights.put(current_pair.getId().intValue(), w_os);
                }

                /*
                 Here we select the next pair of the ordering.
                 Criteria: pair with maximum w_os.
                 */

                double max_weight = -1;
                NodesPair max_pair = null;
                for (NodesPair current_pair : ordered_pairs_neighborhood) {
                    double weight = neighborhood_weights.get(current_pair.getId().intValue());

                    if (weight > max_weight) {
                        max_weight = weight;
                        max_pair = current_pair;
                    }

                }
                addPairToTheOrdering(max_pair);
            }

        }
        //******************************************************************************************************************************************//
        pairs_ordering = selected_pairs;
        map_state_to_edge = edge_ordering.toIntArray();
        map_edge_to_state = this.getInverseMap(map_state_to_edge);

        map_state_to_src = new int[edge_keys.size()];
        map_state_to_dst = new int[edge_keys.size()];
        map_edge_to_direction = new EdgeDirection[edge_keys.size()];


        int i = 0;
        for (int edge : edge_ordering) {
            NodesPair pair = map_edge_to_endpoints.get(edge);

            map_state_to_src[i] = pair.getFirstEndpoint().intValue();
            map_state_to_dst[i] = pair.getSecondEndpoint().intValue();
            map_edge_to_direction[edge] = query_structure.getDirection(pair.getFirstEndpoint(), pair.getSecondEndpoint(), edge);

            i++;
        }
    }

    // GETTER
    private int[] getInverseMap(int[] map) {
        int[] inverse = new int[map.length];

        for (int i = 0; i < map.length; i++) {
            inverse[map[i]] = i;
        }

        return inverse;
    }

    public ObjectArraySet<NodesPair> getPairs_ordering() {
        return pairs_ordering;
    }

    public int[] getMap_state_to_edge() {
        return map_state_to_edge;
    }

    public int[] getMap_edge_to_state() {
        return map_edge_to_state;
    }

    public int[] getMap_state_to_src() {
        return map_state_to_src;
    }

    public int[] getMap_state_to_dst() {
        return map_state_to_dst;
    }

    public int[] getMap_state_to_unmapped_nodes() {
        return map_state_to_unmapped_nodes;
    }

    public EdgeDirection[] getMap_edge_to_direction() {
        return map_edge_to_direction;
    }
}
