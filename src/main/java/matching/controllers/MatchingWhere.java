package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.PropositionStatus;
import cypher.controller.WhereConditionExtraction;
import cypher.models.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeOrdering;
import ordering.NodesPair;
import simmetry_condition.SymmetryCondition;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.HashMap;
import java.util.Optional;

public class MatchingWhere extends MatchingBase {
    public boolean doWhereCheck;
    public boolean atLeastOnePropositionVerified;
    public boolean allPropositionsFailed;
    public boolean areThereConditions;
    public WhereConditionExtraction whereHandler;
    public IntArrayList setWhereConditions;

    public MatchingWhere(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, NodesEdgesLabelsMaps labels_types_idx, TargetBitmatrix target_bitmatrix, GraphPaths graphPaths, HashMap<String, GraphMacroNode> macro_nodes, Int2ObjectOpenHashMap<String> nodes_macro, Optional<WhereConditionExtraction> where_managing) {
        super(outData, query, justCount, distinct, numMaxOccs, labels_types_idx, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, where_managing);
        whereHandler = where_managing.get();
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
        states.map_state_to_edge = edgeOrdering.getMap_state_to_edge();
        states.map_edge_to_state = edgeOrdering.getMap_edge_to_state();
        states.map_state_to_first_endpoint = edgeOrdering.getMap_state_to_first_endpoint();
        states.map_state_to_second_endpoint = edgeOrdering.getMap_state_to_second_endpoint();
        states.map_state_to_unmatched_node = edgeOrdering.getMap_state_to_unmapped_nodes();
        states.map_edge_to_direction = edgeOrdering.getMap_edge_to_direction();
        outData.ordering_time = (System.currentTimeMillis() - outData.ordering_time) / 1000;

        // WHERE CONDITIONS ORDERING
        where_managing.get().assignConditionsToNodesAndEdges(query, edgeOrdering.getNodes_ordering(), edgeOrdering.getEdges_ordering());

        // SYMMETRY CONDITIONS COMPUTING
        outData.symmetry_time = System.currentTimeMillis();
        nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query);
        edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query);

        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        numQueryEdges = query.getQuery_edges().size();

        // MATCHING DATA
        matchingData = new MatchingData(query);

        //DEBUG INFO
//        Utils.printDebugInfo(graphPaths, query, states, edgeOrdering);

        // MATCHING
        outData.num_occurrences = matching_procedure();
        report();
        return outData;
    }

    private long matching_procedure() {
        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
        int q_src = firstPair.getFirstEndpoint();
        int q_dst = firstPair.getSecondEndpoint();

        // WHERE CONDITIONS
        doWhereCheck = true;
        areThereConditions = true;
        setWhereConditions = whereHandler.getSetWhereConditions();

        if (setWhereConditions.isEmpty()) {
            doWhereCheck = false;
            areThereConditions = false;
        } else {
            initializeConditions();
        }

        for (int f_node: firstPair.getFirst_second().keySet()) {
            for (int s_node: firstPair.getFirst_second().get(f_node)) {
                updateCandidatesForStateZero(q_src, q_dst, f_node, s_node);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() -1) {
                    // STATE ZERO
                    startFromStateZero();
                    updateSolutionNodesAndEdgeForStateZero();

                    if(areWhereConditionsVerified()) {
                        updateMatchingInfoForStateZero();
                        goAhead();
                        updateCandidatesForStateGraterThanZero();

                        while (si > 0) {
                            // BACK TRACKING ON EDGES
                            if (psi >= si) {
                                removeMatchingInfoForStateGraterThanZero();

                                // RESET CONDITIONS FOR THE PREVIOUS MATCHED STATE
                                if(areThereConditions) {
                                    resetConditionsForState(psi); // N.B. we reset the conditions for the previous state
                                }
                            }

                            // NEXT CANDIDATE
                            matchingData.candidatesIT[si]++;

                            if (shouldBacktrack()) { // BACKTRACKING
                                backtrack();
                            } else {  // FORWARD TRACKING ON EDGES
                                // SET NODE AND EDGE TO MATCH
                                updateSolutionNodesAndEdgeForStateGreaterThanZero();

                                if(areWhereConditionsVerified()) {
                                    updateMatchingInfoForStateGreaterThanZero();
                                    if (lastStateReached()) { // INCREASE OCCURRENCES
                                        // New occurrence found
                                        newOccurrenceFound();
                                    } else { // GO AHEAD
                                        goAhead();
                                        updateCandidatesForStateGraterThanZero();
                                    }
                                } else {
                                    psi = si;
                                }
                            }
                        }
                    }
                    //WHERE CHECK FAILED OR NO MORE CANDIDATES
                    if(areThereConditions) {
                        initializeConditions();
                    }
                    // CLEANING OF STRUCTURES
                    removeMatchingInfoForStateZero();
                }
            }
        }
        return numTotalOccs;
    }

    private boolean checkWhereCond() {
        //Check edge conditions
        int edgeCand = matchingData.solution_edges[si];
        int queryEdgeId = states.map_state_to_edge[si];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getConditions().values()) {
            if (checkQueryCondition(edgeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                condition.setStatus(PropositionStatus.EVALUATED);
            } else {
                condition.setStatus(PropositionStatus.FAILED);
            }
        }

        //Check node conditions
        int querySrcID = states.map_state_to_first_endpoint[si];
        int queryDstID = states.map_state_to_second_endpoint[si];

        QueryNode querySrc = query.getQuery_node(querySrcID);
        QueryNode queryDst = query.getQuery_node(queryDstID);


        if (si == 0) { // STATE 0, BOTH SRC AND DST MUST BE CHECKED
            int srcCand, dstCand;

            srcCand = matchingData.solution_nodes[0];
            dstCand = matchingData.solution_nodes[1];

            // SRC
            for (QueryCondition condition : querySrc.getConditions().values()) {
                if (checkQueryCondition(srcCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.EVALUATED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                }
            }

            // DST
            for (QueryCondition condition : queryDst.getConditions().values()) {
                if (checkQueryCondition(dstCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.EVALUATED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                }
            }
        } else { // STATE > 0, ONLY THE NEW MATCHED NODE MUST BE CHECKED
            int nodeToMatch = states.map_state_to_unmatched_node[si];

            if (nodeToMatch != -1) {
                QueryNode matchedNode = query.getQuery_node(nodeToMatch);
                int nodeCand = matchingData.solution_nodes[nodeToMatch];
                for (QueryCondition condition : matchedNode.getConditions().values()) {
                    if (checkQueryCondition(nodeCand, condition, query, matchingData.solution_nodes, matchingData.solution_edges)) {
                        condition.setStatus(PropositionStatus.EVALUATED);
                    } else {
                        condition.setStatus(PropositionStatus.FAILED);
                    }
                }
            }
        }

        //Check pattern conditions
        //TODO: IMPLEMENT

        //WHERE check
        return evaluateAllConditions();
    }
    public void removeMatchingInfoForStateGraterThanZero() {
        matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
        matchingData.solution_edges[si] = -1;
        // REMOVE THE NODE IF EXIST
        int selected_candidate = states.map_state_to_unmatched_node[si];
        if (selected_candidate != -1) {
            matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
            matchingData.solution_nodes[selected_candidate] = -1;
        }
    }

    public void removeMatchingInfoForStateZero() {
        matchingData.matchedEdges.remove(matchingData.solution_edges[0]);
        matchingData.solution_edges[0] = -1;
        matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
        matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
        matchingData.solution_nodes[0] = -1;
        matchingData.solution_nodes[1] = -1;
    }
    public void updateSolutionNodesAndEdgeForStateZero() {
        matchingData.solution_edges[0] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
    }
    public void updateMatchingInfoForStateZero() {
        matchingData.matchedEdges.add(matchingData.solution_edges[0]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
    }

    public void updateMatchingInfoForStateGreaterThanZero() {
        matchingData.matchedEdges.add(matchingData.solution_edges[si]);
        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1) {
            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
        }
    }

    public void updateSolutionNodesAndEdgeForStateGreaterThanZero() {
        matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1)
            matchingData.solution_nodes[node_to_match] =
                    matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
    }


    public void updateCandidatesForStateZero(int q_src, int q_dst, int f_node, int s_node) {
        matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                q_src, q_dst, f_node, s_node, states.map_state_to_edge[0],
                query, graphPaths, matchingData, nodes_symmetry, states
        );
        matchingData.candidatesIT[0] = -1;
    }

    public void updateCandidatesForStateGraterThanZero() {
        matchingData.setCandidates[si] = NewFindCandidates.find_candidates(
                graphPaths, query, si, nodes_symmetry, edges_symmetry, states, matchingData
        );
        matchingData.candidatesIT[si] = -1;
    }


    public boolean evaluateAllConditions() {
        atLeastOnePropositionVerified = false;
        allPropositionsFailed = true;

        // for each or proposition
        for(int i = 0; i < whereHandler.getSetWhereConditions().size(); i++) {
            // for each condition in the current or proposition
            Int2ObjectOpenHashMap<QueryCondition> conditionSet = whereHandler.getMapOrPropositionToConditionSet().get(i);

            boolean allConditionsVerified = true;
            boolean canBeTrue = true;

            for(QueryCondition condition: conditionSet.values()) {
                switch (condition.getStatus()) {
                    case NOT_EVALUATED -> {
                        allConditionsVerified = false;
                    }

                    case FAILED -> {
                        allConditionsVerified = false;
                        canBeTrue = false;
                    }

                    // otherwise is evaluated
                }
            }

            if(allConditionsVerified) { // COMPLETELY EVALUATED
                whereHandler.getMapOrPropositionToStatus().put(i, PropositionStatus.EVALUATED);
                atLeastOnePropositionVerified = true;
                allPropositionsFailed = false;
            } else if(canBeTrue) { // NOT COMPLETELY EVALUATED (other conditions must be checked)
                whereHandler.getMapOrPropositionToStatus().put(i, PropositionStatus.NOT_EVALUATED);
                allPropositionsFailed = false;
            } else { // FAILED
                whereHandler.getMapOrPropositionToStatus().put(i, PropositionStatus.FAILED);
            }
        }

        if(atLeastOnePropositionVerified) { // One OR proposition is verified, we don't need other controls
            doWhereCheck = false;
            return true;
        } else if(!allPropositionsFailed) { // No OR proposition is verified, we need other controls
            return true;
        }
        // All OR propositions are FALSE, we must backtrack
        return false;
    }

    public void initializeConditions() {
        atLeastOnePropositionVerified = false;
        allPropositionsFailed = false;

        // for each or proposition
        for(int i = 0; i < whereHandler.getSetWhereConditions().size(); i++) {
            // for each condition in the current or proposition
            Int2ObjectOpenHashMap<QueryCondition> conditionSet = whereHandler.getMapOrPropositionToConditionSet().get(i);

            for(QueryCondition condition: conditionSet.values()) {
                condition.setStatus(PropositionStatus.NOT_EVALUATED);
            }
        }

        doWhereCheck = true;
    }

    public void resetConditionsForState(int state) {
        // STATE > 0

        //Reset edge conditions
        int queryEdgeId = states.map_state_to_edge[state];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getConditions().values()) {
            condition.setStatus(PropositionStatus.NOT_EVALUATED);
        }

        // Reset node conditions
        int nodeToMatch = states.map_state_to_unmatched_node[state];
        if (nodeToMatch != -1) {
            QueryNode matchedNode = query.getQuery_node(nodeToMatch);

            for (QueryCondition condition : matchedNode.getConditions().values()) {
                condition.setStatus(PropositionStatus.NOT_EVALUATED);
            }
        }

        evaluateAllConditions();
    }

    public static boolean checkQueryCondition(int targetElementID, QueryCondition condition, QueryStructure queryStructure, int[] solution_nodes, int[] solution_edges) {
        //TODO: rewrite without if
        String operator = condition.getOperation();
        String elementName = condition.getNode_param().getElementName();
        String propertyName = condition.getNode_param().getElementKey();
        Object expressionValue = condition.getExpr_value();

        boolean res;

        if(expressionValue instanceof NameValue) { // COMPLEX CONDITION
            String secondElementName = ((NameValue) expressionValue).getElementName();
            String secondPropertyName = ((NameValue) expressionValue).getElementKey();

            Object2IntOpenHashMap<String> mapNodeNameToID = queryStructure.getMap_node_name_to_idx();
            Object2IntOpenHashMap<String> mapEdgeNameToID = queryStructure.getMap_edge_name_to_idx();

            int firstID, secondID, firstCandidateID, secondCandidateID;
            if (mapNodeNameToID.containsKey(elementName) && mapNodeNameToID.containsKey(secondElementName)) { // CONDITION ON NODES
                firstID = mapNodeNameToID.getInt(elementName);
                secondID = mapNodeNameToID.getInt(secondElementName);

                firstCandidateID = solution_nodes[firstID];
                secondCandidateID = solution_nodes[secondID];
            } else {  // CONDITION ON EDGES  -> mapEdgeNameToID.containsKey(elementName) && mapEdgeNameToID.containsKey(secondElementName)
                firstID = mapEdgeNameToID.getInt(elementName);
                secondID = mapEdgeNameToID.getInt(secondElementName);

                firstCandidateID = solution_edges[firstID];
                secondCandidateID = solution_edges[secondID];
            }

            Object firstCandidateValue = condition.getConditionCheck().getProperty(firstCandidateID, propertyName);
            Object secondCandidateValue = condition.getConditionCheck().getProperty(secondCandidateID, secondPropertyName);

            res = condition.getConditionCheck().getComparison().comparison(firstCandidateValue, secondCandidateValue, operator);
        } else { // SIMPLE CONDITION
            Object candidateValue = condition.getConditionCheck().getProperty(targetElementID, propertyName);
            res = condition.getConditionCheck().getComparison().comparison(candidateValue, expressionValue, operator);
        }

        boolean negate = condition.isNegation();

        if (negate) {
            return !res;
        }

        return res;
    }

    public  boolean areWhereConditionsVerified() {
        if (doWhereCheck) {
            return checkWhereCond();
        }
        return true;
    }






}
