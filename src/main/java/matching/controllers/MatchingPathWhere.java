//package matching.controllers;
//
//import bitmatrix.models.TargetBitmatrix;
//import cypher.controller.PropositionStatus;
//import cypher.models.QueryCondition;
//import cypher.models.QueryEdge;
//import cypher.models.QueryNode;
//import cypher.models.QueryStructure;
//
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import matching.models.OutData;
//import matching.models.PathsMatchingData;
//import ordering.NodesPair;
//
//import target_graph.graph.TargetGraph;
//
//public class MatchingPathWhere extends MatchingPathSimple {
//    public boolean doWhereCheck;
//    public ObjectArrayList<QueryCondition> simpleConditions;
//    public ObjectArrayList<QueryCondition> complexConditions;
//
//
//    public MatchingPathWhere(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix, ObjectArrayList<QueryCondition> simpleConditions, ObjectArrayList<QueryCondition> complexConditions) {
//        super(outData, query, justCount, distinct, numMaxOccs, targetGraph, target_bitmatrix, simpleConditions);
//        this.simpleConditions = simpleConditions;
//        this.complexConditions = complexConditions;
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
//        // ORDERING
//        computeOrdering();
//
//        // WHERE CONDITIONS ORDERING
//        WhereUtils.assignComplexConditionsToNodesAndEdges(complexConditions, query, edgeOrdering.getNodes_ordering(), edgeOrdering.getEdges_ordering());
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
//        // WHERE CONDITIONS
//        doWhereCheck = true;
//        initializeConditions();
//
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
//                if (areWhereConditionsVerified()) {
//                    updateMatchingInfoForStateZero();
//                    goAhead();
//                    updateCandidatesForStateGraterThanZero();
//
//                    while (si > 0) {
//                        // BACK TRACKING ON EDGES
//                        if (psi >= si) {
//                            removeMatchingInfoForStateGraterThanZero();
//
//                            // RESET CONDITIONS FOR THE PREVIOUS MATCHED STATE
//                            resetConditionsForState(si); // N.B. we reset the conditions for the previous state
//                        }
//
//                        // NEXT CANDIDATE
//                        matchingData.candidatesIT[si]++;
//
//                        if (shouldBacktrack()) { // BACKTRACKING
//                            backtrack();
//                        } else {  // FORWARD TRACKING ON EDGES
//                            // SET NODE AND EDGE TO MATCH
//                            updateSolutionNodesAndEdgeForStateGreaterThanZero();
//                            updateMatchingInfoForStateGreaterThanZero(); // TODO: check the position (it could be after goAhead)
//
//                            if (areWhereConditionsVerified()) {
//                                if (lastStateReached()) { // INCREASE OCCURRENCES
//                                    // New occurrence found
//                                    newOccurrenceFound();
//                                } else { // GO AHEAD
//                                    goAhead();
//                                    updateCandidatesForStateGraterThanZero();
//                                }
//                            } else {
//                                psi = si;
//                            }
//                        }
//                    }
//                }
//                //WHERE CHECK FAILED OR NO MORE CANDIDATES
//                initializeConditions();
//
//                // CLEANING OF STRUCTURES
//                removeMatchingInfoForStateZero();
//            }
//
//        }
//        return numTotalOccs;
//    }
//
//    private boolean checkWhereCond() {
//        //Check edge conditions
//        int edgeCand = matchingData.solution_edges[si];
//        int queryEdgeId = states.map_state_to_edge[si];
//        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);
//
//        for (QueryCondition condition : queryEdge.getComplexConditions().values()) {
//            if (WhereUtils.checkQueryCondition(edgeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
//                condition.setStatus(PropositionStatus.SUCCEEDED);
//            } else {
//                condition.setStatus(PropositionStatus.FAILED);
//            }
//        }
//
//        //Check node conditions
//        int querySrcID = states.map_state_to_first_endpoint[si];
//        int queryDstID = states.map_state_to_second_endpoint[si];
//
//        QueryNode querySrc = query.getQuery_node(querySrcID);
//        QueryNode queryDst = query.getQuery_node(queryDstID);
//
//
//        if (si == 0) { // STATE 0, BOTH SRC AND DST MUST BE CHECKED
//            int srcCand, dstCand;
//
//            srcCand = matchingData.solution_nodes[states.map_state_to_first_endpoint[0]];
//            dstCand = matchingData.solution_nodes[states.map_state_to_second_endpoint[0]];
//
//            // SRC
//            for (QueryCondition condition : querySrc.getComplexConditions().values()) {
//                if (WhereUtils.checkQueryCondition(srcCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
//                    condition.setStatus(PropositionStatus.SUCCEEDED);
//                } else {
//                    condition.setStatus(PropositionStatus.FAILED);
//                }
//            }
//
//            // DST
//            for (QueryCondition condition : queryDst.getComplexConditions().values()) {
//                if (WhereUtils.checkQueryCondition(dstCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
//                    condition.setStatus(PropositionStatus.SUCCEEDED);
//                } else {
//                    condition.setStatus(PropositionStatus.FAILED);
//                }
//            }
//        } else { // STATE > 0, ONLY THE NEW MATCHED NODE MUST BE CHECKED
//            int nodeToMatch = states.map_state_to_unmatched_node[si];
//
//            if (nodeToMatch != -1) {
//                QueryNode matchedNode = query.getQuery_node(nodeToMatch);
//                int nodeCand = matchingData.solution_nodes[nodeToMatch];
//                for (QueryCondition condition : matchedNode.getComplexConditions().values()) {
//                    if (WhereUtils.checkQueryCondition(nodeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
//                        condition.setStatus(PropositionStatus.SUCCEEDED);
//                    } else {
//                        condition.setStatus(PropositionStatus.FAILED);
//                    }
//                }
//            }
//        }
//
//        //Check pattern conditions
//        //TODO: IMPLEMENT
//
//        //WHERE check
//        return evaluateAllConditions();
//    }
//
//    public boolean evaluateAllConditions() {
//        boolean allConditionsVerified = true;
//        boolean canBeTrue = true;
//
//        for (QueryCondition condition : this.complexConditions) {
//            switch (condition.getStatus()) {
//                case NOT_EVALUATED -> {
//                    allConditionsVerified = false;
//                }
//
//                case FAILED -> {
//                    allConditionsVerified = false;
//                    canBeTrue = false;
//                }
//
//                // otherwise is SUCCEEDED
//            }
//        }
//
//        if (allConditionsVerified) {
//            doWhereCheck = false;
//        }
//
//        return canBeTrue;
//    }
//
//    public void initializeConditions() {
//        // for each condition
//        this.complexConditions.forEach(condition -> {
//            condition.setStatus(PropositionStatus.NOT_EVALUATED);
//        });
//
//        doWhereCheck = true;
//    }
//
//    public void resetConditionsForState(int state) {
//        // STATE > 0
//
//        //Reset edge conditions
//        int queryEdgeId = states.map_state_to_edge[state];
//        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);
//
//        for (QueryCondition condition : queryEdge.getComplexConditions().values()) {
//            condition.setStatus(PropositionStatus.NOT_EVALUATED);
//        }
//
//        // Reset node conditions
//        int nodeToMatch = states.map_state_to_unmatched_node[state];
//        if (nodeToMatch != -1) {
//            QueryNode matchedNode = query.getQuery_node(nodeToMatch);
//
//            for (QueryCondition condition : matchedNode.getComplexConditions().values()) {
//                condition.setStatus(PropositionStatus.NOT_EVALUATED);
//            }
//        }
//
//        evaluateAllConditions();
//    }
//
//    public boolean areWhereConditionsVerified() {
//        if (doWhereCheck) {
//            return checkWhereCond();
//        }
//        return true;
//    }
//}
