package matching.controllers;

import bitmatrix.models.TargetBitmatrix;
import cypher.controller.PropositionStatus;
import cypher.models.*;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.NodesPair;

import target_graph.graph.TargetGraph;


public class MatchingWhere extends MatchingBase {
    public boolean doWhereCheck;
    public ObjectArrayList<QueryCondition> simpleConditions;
    public ObjectArrayList<QueryCondition> complexConditions;


    public MatchingWhere(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix, ObjectArrayList<QueryCondition> simpleConditions, ObjectArrayList<QueryCondition> complexConditions) {
        super(outData, query, justCount, distinct, numMaxOccs, targetGraph, target_bitmatrix);
        this.simpleConditions = simpleConditions;
        this.complexConditions = complexConditions;
    }

    public OutData matching() {
        if (check_nodes_labels()) {
            report();
            return outData;
        }

        // SIMPLE WHERE CONDITIONS
        if (simpleConditions.size() > 0) {
            WhereUtils.assignSimpleConditionsToNodesAndEdges(simpleConditions, query);
            // DOMAINS
            computeFilteredCompatibilityDomains();
        } else {
            // DOMAINS
            computeCompatibilityDomains();
        }

        // ORDERING
        computeOrdering();

        // COMPLEX WHERE CONDITIONS ORDERING
        WhereUtils.assignComplexConditionsToNodesAndEdges(complexConditions, query, edgeOrdering.getNodes_ordering(), edgeOrdering.getEdges_ordering());

        // SYMMETRY CONDITIONS
        computeSymmetryConditions();

        // QUERY INFORMATION
        numQueryEdges = query.getQuery_edges().size();

        // MATCHING DATA
        matchingData = new MatchingData(query);

        //DEBUG INFO
//        Utils.printDebugInfo(graphPaths, query, states, edgeOrdering);

        // MATCHING
        outData.matching_time = System.currentTimeMillis();
        matching_procedure();
        report();
        return outData;
    }

    private void matching_procedure() {
        // NEW PART
        IntOpenHashSet edge_types_lens = new IntOpenHashSet();
        this.query.getQuery_edges().forEach((id, edge) -> edge_types_lens.add(edge.getEdge_label().size()));
        FindCandidateParent findCandidate = null;
        if(edge_types_lens.size() == 1) {
            int value = edge_types_lens.stream().findFirst().get();
            findCandidate = switch (value) {
                case 0  -> new FindCandidatesNoTypes();
                case 1  -> new FindCandidateSingleType();
                default -> new FindCandidateMultipleType();
            };
        }
        else findCandidate = new FindCandidateGeneral();


        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
        int q_src = firstPair.getFirstEndpoint();
        int q_dst = firstPair.getSecondEndpoint();

        // WHERE CONDITIONS
        doWhereCheck = true;
        initializeConditions();

        for (int f_node : firstPair.getFirst_second().keySet()) {
            for (int s_node : firstPair.getFirst_second().get(f_node)) {
                updateCandidatesForStateZero(q_src, q_dst, f_node, s_node, findCandidate);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                    // STATE ZERO
                    startFromStateZero();
                    updateSolutionNodesAndEdgeForStateZero();

                    if (areWhereConditionsVerified()) {
                        updateMatchingInfoForStateZero();
                        goAhead();
                        updateCandidatesForStateGraterThanZero(findCandidate);

                        while (si > 0) {
                            // BACK TRACKING ON EDGES
                            if (psi >= si) {
                                removeMatchingInfoForStateGraterThanZero();

                                // RESET CONDITIONS FOR THE PREVIOUS MATCHED STATE
                                resetConditionsForState(si); // N.B. we reset the conditions for the previous state
                            }

                            // NEXT CANDIDATE
                            matchingData.candidatesIT[si]++;

                            if (shouldBacktrack()) { // BACKTRACKING
                                backtrack();
                            } else {  // FORWARD TRACKING ON EDGES
                                // SET NODE AND EDGE TO MATCH
                                updateSolutionNodesAndEdgeForStateGreaterThanZero();

                                if (areWhereConditionsVerified()) {
                                    updateMatchingInfoForStateGreaterThanZero();
                                    if (lastStateReached()) { // INCREASE OCCURRENCES
                                        // New occurrence found
                                        newOccurrenceFound();
                                    } else { // GO AHEAD
                                        goAhead();
                                        updateCandidatesForStateGraterThanZero(findCandidate);
                                    }
                                } else {
                                    psi = si;
                                }
                            }
                        }
                    }
                    //WHERE CHECK FAILED OR NO MORE CANDIDATES
                    initializeConditions();

                    // CLEANING OF STRUCTURES
                    removeMatchingInfoForStateZero();
                }
            }
        }
    }

    private boolean checkWhereCond() {
        //Check edge conditions
        int edgeCand = matchingData.solution_edges[si];
        int queryEdgeId = states.map_state_to_edge[si];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getComplexConditions().values()) {
            if (WhereUtils.checkQueryCondition(edgeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                condition.setStatus(PropositionStatus.SUCCEEDED);
            } else {
                condition.setStatus(PropositionStatus.FAILED);
                return false;
            }
        }

        //Check node conditions
        int querySrcID = states.map_state_to_first_endpoint[si];
        int queryDstID = states.map_state_to_second_endpoint[si];

        QueryNode querySrc = query.getQuery_node(querySrcID);
        QueryNode queryDst = query.getQuery_node(queryDstID);


        if (si == 0) { // STATE 0, BOTH SRC AND DST MUST BE CHECKED
            int srcCand, dstCand;

            srcCand = matchingData.solution_nodes[states.map_state_to_first_endpoint[0]];
            dstCand = matchingData.solution_nodes[states.map_state_to_first_endpoint[0]];

            // SRC
            for (QueryCondition condition : querySrc.getComplexConditions().values()) {
                if (WhereUtils.checkQueryCondition(srcCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.SUCCEEDED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                    return false;
                }
            }

            // DST
            for (QueryCondition condition : queryDst.getComplexConditions().values()) {
                if (WhereUtils.checkQueryCondition(dstCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.SUCCEEDED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                    return false;
                }
            }
        } else { // STATE > 0, ONLY THE NEW MATCHED NODE MUST BE CHECKED
            int nodeToMatch = states.map_state_to_unmatched_node[si];

            if (nodeToMatch != -1) {
                QueryNode matchedNode = query.getQuery_node(nodeToMatch);
                int nodeCand = matchingData.solution_nodes[nodeToMatch];
                for (QueryCondition condition : matchedNode.getComplexConditions().values()) {
                    if (WhereUtils.checkQueryCondition(nodeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                        condition.setStatus(PropositionStatus.SUCCEEDED);
                    } else {
                        condition.setStatus(PropositionStatus.FAILED);
                        return false;
                    }
                }
            }
        }

        //Check pattern conditions
        //TODO: IMPLEMENT

        //WHERE check
        return evaluateAllConditions();
    }

    public boolean evaluateAllConditions() {
        boolean allConditionsVerified = true;
        boolean canBeTrue = true;

        for (QueryCondition condition : this.complexConditions) {
            switch (condition.getStatus()) {
                case NOT_EVALUATED -> {
                    allConditionsVerified = false;
                }

                case FAILED -> {
                    allConditionsVerified = false;
                    canBeTrue = false;
                }

                // otherwise is SUCCEEDED
            }
        }

        if (allConditionsVerified) {
            doWhereCheck = false;
        } else {
            doWhereCheck = true;
        }

        return canBeTrue;
    }

    public void initializeConditions() {
        // for each condition
        this.complexConditions.forEach(condition -> {
            condition.setStatus(PropositionStatus.NOT_EVALUATED);
        });

        doWhereCheck = true;
    }

    public void resetConditionsForState(int state) {
        // STATE > 0

        //Reset edge conditions
        int queryEdgeId = states.map_state_to_edge[state];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getComplexConditions().values()) {
            condition.setStatus(PropositionStatus.NOT_EVALUATED);
        }

        // Reset node conditions
        int nodeToMatch = states.map_state_to_unmatched_node[state];
        if (nodeToMatch != -1) {
            QueryNode matchedNode = query.getQuery_node(nodeToMatch);

            for (QueryCondition condition : matchedNode.getComplexConditions().values()) {
                condition.setStatus(PropositionStatus.NOT_EVALUATED);
            }
        }

        evaluateAllConditions();
    }

    public boolean areWhereConditionsVerified() {
        if (doWhereCheck) {
            return checkWhereCond();
        }
        return true;
    }
}
