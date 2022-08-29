package ordering;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class EdgeOrdering {
    private ObjectArraySet<NodesPair> selected_pairs;
    private ObjectArraySet<NodesPair> unselected_pairs;
    private Int2ObjectOpenHashMap<IntArraySet> map_endpoints_to_edges;
    private IntArrayList edges_ordering;
    private IntArrayList nodes_ordering;
    int state_index;

    /*******/
    private final QueryStructure query_structure;
    private int[] map_edge_to_state;
    private EdgeDirection[] map_edge_to_direction;
    private int[] map_state_to_edge;
    private int[] map_state_to_first_endpoint;
    private int[] map_state_to_second_endpoint;
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
        edges_ordering.addAll(current_edge_set);

        int firstEndpoint = query_pair_to_add.getFirstEndpoint().intValue();
        int secondEndpoint = query_pair_to_add.getSecondEndpoint().intValue();

        if (!nodes_ordering.contains(firstEndpoint)) {
            map_state_to_unmapped_nodes[state_index++] = firstEndpoint;
        } else if (!nodes_ordering.contains(secondEndpoint)) {
            map_state_to_unmapped_nodes[state_index++] = secondEndpoint;
        } else {
            map_state_to_unmapped_nodes[state_index++] = -1;
        }

        if (current_edge_set.size() > 1) {
            for (int i = 0; i < current_edge_set.size() - 1; i++) {
                map_state_to_unmapped_nodes[state_index++] = -1;
            }
        }

        if(!nodes_ordering.contains(firstEndpoint)) {
            nodes_ordering.add(firstEndpoint);
        }

        if(!nodes_ordering.contains(secondEndpoint)) {
            nodes_ordering.add(secondEndpoint);
        }
    }

    private void computePairsOrdering() {
        //************************************************************* PRE PROCESSING *************************************************************//
        edges_ordering = new IntArrayList();
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
        //******************************************************************************************************************************************//

        //*************************************************************** FIRST PAIR ***************************************************************//
        nodes_ordering = new IntArrayList();
        map_state_to_unmapped_nodes = new int[edge_keys.size()];

        state_index = 0;
        NodesPair query_pair_to_add = null;

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
        // We compute the score between the node and all its neighbours.
        Int2DoubleOpenHashMap map_pair_to_score = new Int2DoubleOpenHashMap();
        for (int node : max_nodes) {
            IntArraySet neighborhood = map_node_to_neighborhood.get(node);

            for (int neighbour : neighborhood) {
                NodesPair current_endpoints = new NodesPair(node, neighbour);
                int domain_size = map_id_to_pair.get(current_endpoints.getId().intValue()).getDomain_size();
                int neighbour_degree = nodes_degree.get(neighbour);

                Double score = OrderingUtils.computePairScore(node, neighbour, map_node_to_neighborhood, domain_size, neighbour_degree);
                map_pair_to_score.put(current_endpoints.getId().intValue(), score.doubleValue());
            }
        }

        // We search the pair having the maximum score
        double max_score = -1d;

        for (NodesPair pair : unselected_pairs) {
            double current_score = map_pair_to_score.get(pair.getId().intValue());

            if (current_score > max_score) {
                max_score = current_score;
                query_pair_to_add = pair;
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
                if (nodes_ordering.contains(pair.getFirstEndpoint().intValue()) && nodes_ordering.contains((pair.getSecondEndpoint().intValue()))) {
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
            } else { // If there aren't pairs with both endpoint matched, we select the next pair of the ordering using the score
                Int2DoubleOpenHashMap map_pair_to_simplified_score = new Int2DoubleOpenHashMap();

                for (NodesPair current_pair : ordered_pairs_neighborhood) {
                    int domain_size = map_id_to_pair.get(current_pair.getId().intValue()).getDomain_size();

                    int neighbour;
                    int node;
                    if(nodes_ordering.contains(current_pair.getFirstEndpoint().intValue())) {
                        neighbour = current_pair.getSecondEndpoint().intValue();
                        node = current_pair.getFirstEndpoint().intValue();
                    } else {
                        neighbour = current_pair.getFirstEndpoint().intValue();
                        node = current_pair.getSecondEndpoint().intValue();
                    }
                    int neighbour_degree = nodes_degree.get(neighbour);

                    // Score(u, v) = Jacc(u, v) * Deg(v) * 1/|Dom(u, v)|, where u is the node already added to the ordering and v is the other one.
                    double score = OrderingUtils.computePairScore(node, neighbour, map_node_to_neighborhood, domain_size, neighbour_degree);

                    map_pair_to_simplified_score.put(current_pair.getId().intValue(), score);
                }

                /*
                 Here we select the next pair of the ordering.
                 Criteria: pair with maximum score.
                 */

                ObjectArrayList<NodesPair> max_pairs = new ObjectArrayList<>();
                max_score = -1;

                for (NodesPair current_pair : ordered_pairs_neighborhood) {
                    double current_score = map_pair_to_simplified_score.get(current_pair.getId().intValue());

                    if (current_score > max_score) {
                        max_score = current_score;
                        max_pairs = new ObjectArrayList<>();
                        max_pairs.push(current_pair);
                    } else if (current_score == max_score) {
                        max_pairs.push(current_pair);
                    }

                }

                addPairToTheOrdering(max_pairs.get(0));
            }
        }
        //******************************************************************************************************************************************//
        pairs_ordering = selected_pairs;
        map_state_to_edge = edges_ordering.toIntArray();
        map_edge_to_state = this.getInverseMap(map_state_to_edge);

        map_state_to_first_endpoint = new int[edge_keys.size()];
        map_state_to_second_endpoint = new int[edge_keys.size()];
        map_edge_to_direction = new EdgeDirection[edge_keys.size()];


        int i = 0;
        for (int edge : edges_ordering) {
            NodesPair pair = map_edge_to_endpoints.get(edge);

            map_state_to_first_endpoint[i] = pair.getFirstEndpoint().intValue();
            map_state_to_second_endpoint[i] = pair.getSecondEndpoint().intValue();
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

    public int[] getMap_state_to_first_endpoint() {
        return map_state_to_first_endpoint;
    }

    public int[] getMap_state_to_second_endpoint() {
        return map_state_to_second_endpoint;
    }

    public int[] getMap_state_to_unmapped_nodes() {
        return map_state_to_unmapped_nodes;
    }

    public EdgeDirection[] getMap_edge_to_direction() {
        return map_edge_to_direction;
    }

    public IntArrayList getEdges_ordering() {
        return edges_ordering;
    }

    public IntArrayList getNodes_ordering() {
        return nodes_ordering;
    }
}
