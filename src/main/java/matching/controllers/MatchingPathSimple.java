//package matching.controllers;
//
//import bitmatrix.models.TargetBitmatrix;
//import cypher.models.QueryCondition;
//import cypher.models.QueryStructure;
//import it.unimi.dsi.fastutil.ints.IntArrayList;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import matching.models.OutData;
//import matching.models.PathsMatchingData;
//import ordering.NodesPair;
//import target_graph.graph.TargetGraph;
//
//import java.util.Arrays;
//
//
//public class MatchingPathSimple extends MatchingBase {
//
//    public PathsMatchingData matchingData;
//    ObjectArrayList<QueryCondition> simpleConditions;
//
//    public MatchingPathSimple(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix, ObjectArrayList<QueryCondition> simpleConditions) {
//        super(outData, query, justCount, distinct, numMaxOccs, targetGraph, target_bitmatrix);
//        this.simpleConditions = simpleConditions;
//        this.matchingData = new PathsMatchingData(query);
//    }
//
//    public OutData matching() {
//        if (check_nodes_labels()) {
//            report();
//            return outData;
//        }
//
//        // SIMPLE WHERE CONDITIONS
//        if (simpleConditions.size() > 0) {
//            WhereUtils.assignSimpleConditionsToNodesAndEdges(simpleConditions, query);
//
//            // DOMAINS
//            computeFilteredCompatibilityDomains();
//        } else {
//            // DOMAINS
//            computeCompatibilityDomains();
//        }
//
//        // EDGE ORDERING AND STATE OBJECT CREATION
//        computeOrdering();
//
//        // SYMMETRY CONDITIONS
//        computeSymmetryConditions();
//
//        // QUERY INFORMATION
//        numQueryEdges = query.getQuery_edges().size();
//
//        // MATCHING DATA
//        matchingData = new PathsMatchingData(query);
//
//        // FIRST QUERY NODE
//        outData.matching_time = System.currentTimeMillis();
//
//        //DEBUG INFO
////        Utils.printDebugInfo(graphPaths, query, states, edgeOrdering);
//
//        // MATCHING
//        outData.num_occurrences = matching_procedure();
//        report();
//        return outData;
//    }
//
//    private long matching_procedure() {
//        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
//        int q_src = firstPair.getFirstEndpoint();
//        int q_dst = firstPair.getSecondEndpoint();
//
//
//        for (int f_node : query.getMap_node_to_domain().get(q_src)) {
//            updateCandidatesForStateZero(q_src, q_dst, f_node, -1);
//
//            while (matchingData.candidatesIT[0] < matchingData.setCandidatesPaths[0].size() - 1) {
//                // STATE ZERO
//                startFromStateZero();
//                updateSolutionNodesAndEdgeForStateZero();
//
//                updateMatchingInfoForStateZero();
//                goAhead();
//                updateCandidatesForStateGraterThanZero();
//
//                while (si > 0) {
//                    // BACK TRACKING ON EDGES
//                    if (psi >= si) {
//                        removeMatchingInfoForStateGraterThanZero();
//                    }
//
//                    // NEXT CANDIDATE
//                    matchingData.candidatesIT[si]++;
//
//                    if (shouldBacktrack()) { // BACKTRACKING
//                        backtrack();
//                    } else {  // FORWARD TRACKING ON EDGES
//                        // SET NODE AND EDGE TO MATCH
//                        updateSolutionNodesAndEdgeForStateGreaterThanZero();
//                        updateMatchingInfoForStateGreaterThanZero(); // TODO: check the position (it could be after goAhead)
//
//                        if (lastStateReached()) { // INCREASE OCCURRENCES
//                            // New occurrence found
//                            newOccurrenceFound();
//                        } else { // GO AHEAD
//                            goAhead();
//                            updateCandidatesForStateGraterThanZero();
//                        }
//                    }
//                }
//
//                // CLEANING OF STRUCTURES
//                removeMatchingInfoForStateZero();
//            }
//
//        }
//        return numTotalOccs;
//    }
//
//    @Override
//    public boolean shouldBacktrack() {
//        return (matchingData.candidatesIT[si] == matchingData.setCandidatesPaths[si].size());
//    }
//
//    public void updateAuxiliaryInfo() {
//        for (int i = 0; i < matchingData.solutionPaths[si].size(); i++) {
//            if (i % 2 == 0) {
//                matchingData.matchedEdges.add(matchingData.solutionPaths[si].getInt(i));
//            } else {
//                matchingData.matchedNodes.add(matchingData.solutionPaths[si].getInt(i));
//            }
//        }
//    }
//
//    public void removeAuxiliaryInfo() {
//        for (int i = 0; i < matchingData.solutionPaths[si].size(); i++) {
//            if (i % 2 == 0) {
//                matchingData.matchedEdges.remove(matchingData.solutionPaths[si].getInt(i));
//            } else {
//                matchingData.matchedNodes.remove(matchingData.solutionPaths[si].getInt(i));
//            }
//        }
//
//        matchingData.solutionPaths[si] = null;
//    }
//
//    public void updateCandidatesForStateZero(int q_src, int q_dst, int f_node, int s_node) {
//        matchingData.setCandidatesPaths[0] = PathsUtils.findStartPaths(f_node, query, targetGraph.getGraphPaths(), matchingData, nodes_symmetry, states);
//        matchingData.candidatesIT[0] = -1;
//    }
//
//    public void updateSolutionNodesAndEdgeForStateZero() {
//        IntArrayList candidatesPaths = matchingData.setCandidatesPaths[0].get(++matchingData.candidatesIT[0]);
//        int listSize = candidatesPaths.size();
//
//        matchingData.solutionPaths[0] = new IntArrayList(candidatesPaths.subList(0, listSize - 2));
//        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = candidatesPaths.getInt(listSize - 2);
//        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = candidatesPaths.getInt(listSize - 1);
//    }
//
//    public void updateMatchingInfoForStateZero() {
//        updateAuxiliaryInfo();
//
//        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
//        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
//    }
//
//
//    public void removeMatchingInfoForStateZero() {
//        removeAuxiliaryInfo();
//
//        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
//        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
//        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = -1;
//        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = -1;
//    }
//
//    public void updateCandidatesForStateGraterThanZero() {
//        matchingData.setCandidatesPaths[si] = PathsUtils.findPaths(si, query, targetGraph.getGraphPaths(), matchingData, nodes_symmetry, edges_symmetry, states);
//        matchingData.candidatesIT[si] = -1;
//    }
//
//    public void removeMatchingInfoForStateGraterThanZero() {
//        removeAuxiliaryInfo();
//
//        // REMOVE THE NODE IF EXIST
//        int selected_candidate = states.map_state_to_unmatched_node[si];
//        if (selected_candidate != -1) {
//            matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
//            matchingData.solution_nodes[selected_candidate] = -1;
//        }
//    }
//
//    public void updateSolutionNodesAndEdgeForStateGreaterThanZero() {
//        IntArrayList candidatesPaths = matchingData.setCandidatesPaths[si].get(matchingData.candidatesIT[si]);
//        int listSize = candidatesPaths.size();
//
//        matchingData.solutionPaths[si] = new IntArrayList(candidatesPaths.subList(0, listSize - 1));
//        int node_to_match = states.map_state_to_unmatched_node[si];
//        if (node_to_match != -1) matchingData.solution_nodes[node_to_match] = candidatesPaths.getInt(listSize - 1);
//    }
//
//    public void updateMatchingInfoForStateGreaterThanZero() {
//        updateAuxiliaryInfo();
//
//        int node_to_match = states.map_state_to_unmatched_node[si];
//        if (node_to_match != -1) {
//            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
//        }
//    }
//
//    public void newOccurrenceFound() {
//        numTotalOccs++;
//        if (!justCount || distinct) {
//            outData.occurrences.add(matchingData.getSolutionPathsString());
//        }
//        if (numTotalOccs == numMaxOccs) {
//            report();
//            System.exit(0);
//        }
//        psi = si;
//    }
//}
