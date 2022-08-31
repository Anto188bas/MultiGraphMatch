package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.OutData;
import matching.models.PathsMatchingData;
import ordering.EdgeOrdering;
import ordering.NodesPair;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import utility.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class MatchingPath extends MatchingBase {

    public PathsMatchingData matchingData;

    public MatchingPath(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, NodesEdgesLabelsMaps labels_types_idx, TargetBitmatrix target_bitmatrix, GraphPaths graphPaths, HashMap<String, GraphMacroNode> macro_nodes, Int2ObjectOpenHashMap<String> nodes_macro, Optional<WhereConditionExtraction> where_managing) {
        super(outData, query, justCount, distinct, numMaxOccs, labels_types_idx, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, where_managing);
    }

    public OutData matching() {
        outData = new OutData();

        if (check_nodes_labels()) {
            report();
            return outData;
        }

        // DOMAIN COMPUTING
        // QUERY BITMATRIX COMPUTING
        outData.domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.create_bitset(query, labels_types_idx);
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility, graphPaths.getMap_node_color_degrees());
        outData.domain_time = (System.currentTimeMillis() - outData.domain_time) / 1000;

        // EDGE ORDERING AND STATE OBJECT CREATION
        outData.ordering_time = System.currentTimeMillis();
        edgeOrdering = new EdgeOrdering(query);
        states = new StateStructures();
        states.map_state_to_edge = edgeOrdering.getMap_state_to_edge();
        states.map_edge_to_state = edgeOrdering.getMap_edge_to_state();
        states.map_state_to_first_endpoint = edgeOrdering.getMap_state_to_first_endpoint();
        states.map_state_to_second_endpoint = edgeOrdering.getMap_state_to_second_endpoint();
        states.map_state_to_unmatched_node = edgeOrdering.getMap_state_to_unmapped_nodes();
        states.map_edge_to_direction = edgeOrdering.getMap_edge_to_direction();
        outData.ordering_time = (System.currentTimeMillis() - outData.ordering_time) / 1000;

        // SYMMETRY CONDITION COMPUTING
        outData.symmetry_time = System.currentTimeMillis();
        nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query);
        edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query);

        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        numQueryEdges = query.getQuery_edges().size();

        // MATCHING DATA
        matchingData = new PathsMatchingData(query);

        // FIRST QUERY NODE
        outData.matching_time = System.currentTimeMillis();

        //DEBUG INFO
        Utils.printDebugInfo(graphPaths, query, states, edgeOrdering);

        // MATCHING
        outData.num_occurrences = matching_procedure();
        report();
        return outData;
    }

    private long matching_procedure() {
//        matchingData.solution_nodes[1] = 2;
//        System.out.println(PathsUtils.findPaths(0, query_obj, graphPaths,matchingData, nodes_symmetry, edges_symmetry, states));

//        matchingData.solution_nodes[0] = 7;
//        System.out.println(PathsUtils.findStartPaths(0, query, graphPaths,matchingData, nodes_symmetry, states));


        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
        int q_src = firstPair.getFirstEndpoint();
        int q_dst = firstPair.getSecondEndpoint();


        for (int f_node : query.getMap_node_to_domain().get(q_src)) {
            updateCandidatesForStateZero(q_src, q_dst, f_node, -1);

            while (matchingData.candidatesIT[0] < matchingData.setCandidatesPaths[0].size() - 1) {
                // STATE ZERO
                startFromStateZero();
                updateSolutionNodesAndEdgeForStateZero();

                updateMatchingInfoForStateZero();
                goAhead();
                updateCandidatesForStateGraterThanZero();

                while (si > 0) {
                    // BACK TRACKING ON EDGES
                    if (psi >= si) {
                        removeMatchingInfoForStateGraterThanZero();
                    }

                    // NEXT CANDIDATE
                    matchingData.candidatesIT[si]++;

                    if (shouldBacktrack()) { // BACKTRACKING
                        backtrack();
                    } else {  // FORWARD TRACKING ON EDGES
                        // SET NODE AND EDGE TO MATCH
                        updateSolutionNodesAndEdgeForStateGreaterThanZero();
                        updateMatchingInfoForStateGreaterThanZero(); // TODO: check the position (it could be after goAhead)

                        if (lastStateReached()) { // INCREASE OCCURRENCES
                            // New occurrence found
                            newOccurrenceFound();
                            System.out.println("SolutionNodes: " + Arrays.toString(matchingData.solution_nodes) + "\tSolutionPaths: " + Arrays.toString(matchingData.solutionPaths));
                        } else { // GO AHEAD
                            goAhead();
                            updateCandidatesForStateGraterThanZero();
                        }
                    }
                }

                // CLEANING OF STRUCTURES
                removeMatchingInfoForStateZero();
            }

        }
        return numTotalOccs;
    }

    @Override
    public boolean shouldBacktrack() {
        return (matchingData.candidatesIT[si] == matchingData.setCandidatesPaths[si].size());
    }

    public void updateAuxiliaryInfo() {
        for (int i = 0; i < matchingData.solutionPaths[si].size(); i++) {
            if (i % 2 == 0) {
                matchingData.matchedEdges.add(matchingData.solutionPaths[si].getInt(i));
            } else {
                matchingData.matchedNodes.add(matchingData.solutionPaths[si].getInt(i));
            }
        }
    }

    public void removeAuxiliaryInfo() {
        for (int i = 0; i < matchingData.solutionPaths[si].size(); i++) {
            if (i % 2 == 0) {
                matchingData.matchedEdges.remove(matchingData.solutionPaths[si].getInt(i));
            } else {
                matchingData.matchedNodes.remove(matchingData.solutionPaths[si].getInt(i));
            }
        }

        matchingData.solutionPaths[si] = null;
    }

    public void updateCandidatesForStateZero(int q_src, int q_dst, int f_node, int s_node) {
        matchingData.setCandidatesPaths[0] = PathsUtils.findStartPaths(f_node, query, graphPaths, matchingData, nodes_symmetry, states);
        matchingData.candidatesIT[0] = -1;
    }

    public void updateSolutionNodesAndEdgeForStateZero() {
        IntArrayList candidatesPaths = matchingData.setCandidatesPaths[0].get(++matchingData.candidatesIT[0]);
        int listSize = candidatesPaths.size();

        matchingData.solutionPaths[0] = new IntArrayList(candidatesPaths.subList(0, listSize - 2));
        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = candidatesPaths.getInt(listSize - 2);
        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = candidatesPaths.getInt(listSize - 1);
    }

    public void updateMatchingInfoForStateZero() {
        updateAuxiliaryInfo();

        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
    }


    public void removeMatchingInfoForStateZero() {
        removeAuxiliaryInfo();

        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = -1;
        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = -1;
    }
    public void updateCandidatesForStateGraterThanZero() {
        matchingData.setCandidatesPaths[si] = PathsUtils.findPaths(si, query, graphPaths, matchingData, nodes_symmetry, edges_symmetry, states);
        matchingData.candidatesIT[si] = -1;
    }

    public void removeMatchingInfoForStateGraterThanZero() {
        removeAuxiliaryInfo();

        // REMOVE THE NODE IF EXIST
        int selected_candidate = states.map_state_to_unmatched_node[this.si];
        if (selected_candidate != -1) {
            matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
            matchingData.solution_nodes[selected_candidate] = -1;
        }
    }

    public void updateSolutionNodesAndEdgeForStateGreaterThanZero() {
        IntArrayList candidatesPaths = matchingData.setCandidatesPaths[0].get(matchingData.candidatesIT[0]);
        int listSize = candidatesPaths.size();

        matchingData.solutionPaths[si] = new IntArrayList(candidatesPaths.subList(0, listSize -1));
        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1)
            matchingData.solution_nodes[node_to_match] = candidatesPaths.getInt(listSize - 1);
    }

    public void updateMatchingInfoForStateGreaterThanZero() {
        updateAuxiliaryInfo();

        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1) {
            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
        }
    }
}
