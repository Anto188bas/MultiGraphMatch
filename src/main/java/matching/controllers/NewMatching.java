package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeOrdering;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import java.util.HashMap;

public class NewMatching {
    public static OutData outData;

    public static void report() {
        outData.matching_time = (System.currentTimeMillis() - outData.matching_time) / 1000;
        System.out.println("MATCHING REPORT:");
        System.out.println("\t-domain computing time: " + outData.domain_time);
        System.out.println("\t-ordering computing time: " + outData.ordering_time);
        System.out.println("\t-symmetry computing time: " + outData.symmetry_time);
        System.out.println("\t-matching computing time: " + outData.matching_time);
        System.out.println("\t-occurrences: " + outData.num_occurrences);
    }


    private static long matching_procedure(
            MatchingData matchingData,
            StateStructures states,
            GraphPaths graphPaths,
            QueryStructure query_obj,
            IntArrayList[] nodes_symmetry,
            IntArrayList[] edges_symmetry,
            int numQueryEdges, long numTotalOccs, long numMaxOccs,
            boolean justCount, boolean distinct
    ) {
        int si = 0;
        int psi = -1;
        int sip1;

        // FIRST QUERY NODES
        Int2ObjectOpenHashMap<IntArrayList> first_compatibility = query_obj.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]).getFirst_second();


        for (int first_target_node : first_compatibility.keySet()) {
            for (int second_target_node : first_compatibility.get(first_target_node)) {

                matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                        states.map_state_to_edge[0],
                        query_obj, graphPaths, states, first_target_node, second_target_node);


                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                    // STATE ZERO
                    matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                    matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                    matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
                    psi = si;
                    si++;
                    matchingData.setCandidates[si] = NewFindCandidates.find_candidates(
                            graphPaths, query_obj, si, nodes_symmetry, edges_symmetry, states, matchingData
                    );
                    matchingData.candidatesIT[si] = -1;
                    while (si > 0) {
                        // BACK TRACKING ON EDGES
                        if (psi >= si) {
                            matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                            matchingData.solution_edges[si] = -1;
                            // REMOVE THE NODE IF EXIST
                            int selected_candidate = states.map_state_to_mnode[si];
                            if (selected_candidate != -1) {
                                matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
                                matchingData.solution_nodes[selected_candidate] = -1;
                            }
                        }

                        // NEXT CANDIDATE
                        matchingData.candidatesIT[si]++;
                        boolean backtrack = matchingData.candidatesIT[si] == matchingData.setCandidates[si].size();

                        if (backtrack) {
                            psi = si;
                            si--;
                        }

                        // FORWARD TRACKING ON EDGES
                        else {
                            // SET NODE AND EDGE TO MATCH
                            matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
                            int node_to_match = states.map_state_to_mnode[si];
                            if (node_to_match != -1)
                                matchingData.solution_nodes[node_to_match] =
                                        matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                            // INCREASE OCCURRENCES
                            if (si == numQueryEdges - 1) {
                                //New occurrence found
                                numTotalOccs++;
                                if (!justCount || distinct) {
                                    // TODO implement me
                                }
                                if (numTotalOccs == numMaxOccs) {
                                    report();
                                    System.exit(0);
                                }
                                psi = si;
                            }
                            // GO AHEAD
                            else {
                                //Update auxiliary info
                                matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                                node_to_match = states.map_state_to_mnode[si];
                                if (node_to_match != -1) {
                                    matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
                                }
                                sip1 = si + 1;
                                matchingData.setCandidates[sip1] = NewFindCandidates.find_candidates(
                                        graphPaths, query_obj, sip1, nodes_symmetry, edges_symmetry, states, matchingData
                                );
                                matchingData.candidatesIT[sip1] = -1;
                                psi = si;
                                si = sip1;
                            }
                        }
                    }
                    // CLEANING OF STRUCTURES
                    matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                    matchingData.solution_edges[si] = -1;
                    matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
                    matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
                    matchingData.solution_nodes[0] = -1;
                    matchingData.solution_nodes[1] = -1;
                }
                matchingData.candidatesIT[0] = -1;
            }
        }

        return numTotalOccs;
    }


    private static boolean check_nodes_labels(QueryStructure query_object) {
        for (QueryNode node : query_object.getQuery_nodes().values()) {
            for (int label : node.getLabels())
                if (label == -1) return true;
        }
        return false;
    }

    public static OutData matching(
            boolean justCount,
            boolean distinct,
            long numMaxOccs,
            NodesEdgesLabelsMaps labels_types_idx,
            TargetBitmatrix target_bitmatrix,
            QueryStructure query_obj,
            GraphPaths graphPaths,
            HashMap<String, GraphMacroNode> macro_nodes,
            Int2ObjectOpenHashMap<String> nodes_macro
    ) {
        outData = new OutData();

        if (check_nodes_labels(query_obj)) {
            report();
            return outData;
        }

        // DOMAIN COMPUTING
        // QUERY BITMATRIX COMPUTING
        outData.domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.create_bitset(query_obj, labels_types_idx);
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query_obj.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility);
        outData.domain_time = (System.currentTimeMillis() - outData.domain_time) / 1000;


        // EDGE ORDERING AND STATE OBJECT CREATION
        outData.ordering_time = System.currentTimeMillis();
        EdgeOrdering edgeOrdering = new EdgeOrdering(query_obj);
        StateStructures states = new StateStructures();
        states.map_state_to_edge = edgeOrdering.getMap_state_to_edge();
        states.map_edge_to_state = edgeOrdering.getMap_edge_to_state();
        states.map_state_to_src = edgeOrdering.getMap_state_to_src();
        states.map_state_to_dst = edgeOrdering.getMap_state_to_dst();
        states.map_state_to_mnode = edgeOrdering.getMap_state_to_unmapped_nodes();
        states.map_edge_to_direction = edgeOrdering.getMap_edge_to_direction();
        outData.ordering_time = (System.currentTimeMillis() - outData.ordering_time) / 1000;

        // SYMMETRY CONDITION COMPUTING
        outData.symmetry_time = System.currentTimeMillis();
        IntArrayList[] nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query_obj);
        IntArrayList[] edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query_obj);
        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        int numQueryEdges = query_obj.getQuery_edges().size();

        // OTHER CONFIGURATION
        MatchingData matchingData = new MatchingData(query_obj);

        outData.matching_time = System.currentTimeMillis();

        outData.num_occurrences = matching_procedure(
                matchingData, states, graphPaths,
                query_obj, nodes_symmetry, edges_symmetry, numQueryEdges, outData.num_occurrences, numMaxOccs,
                justCount, distinct
        );
        report();
        return outData;
    }
}
