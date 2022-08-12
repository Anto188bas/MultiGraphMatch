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
import ordering.EdgeDirection;
import ordering.EdgeOrdering;
import ordering.NodesPair;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.IntConsumer;

public class MatchingWhere extends MatchingSimple {
    public static boolean doWhereCheck;
    public static boolean atLeastOnePropositionVerified;
    public static boolean allPropositionsFailed;
    public static boolean areThereConditions;
    public static int si;
    public static int psi;
    public static long numTotalOccs;

    private static long matching_procedure(
            WhereConditionExtraction where_managing,
            Int2ObjectOpenHashMap<IntArrayList> first_compatibility,
            MatchingData matchingData,
            StateStructures states,
            GraphPaths graphPaths,
            QueryStructure query_obj,
            IntArrayList[] nodes_symmetry,
            IntArrayList[] edges_symmetry,
            int numQueryEdges, long numMaxOccs,
            int q_src, int q_dst,
            boolean justCount, boolean distinct
    ) {
        numTotalOccs = 0;

        // WHERE CONDITIONS
        doWhereCheck = true;
        areThereConditions = true;
        IntArrayList setWhereConditions = where_managing.getSetWhereConditions();

        if (setWhereConditions.isEmpty()) {
            doWhereCheck = false;
            areThereConditions = false;
        } else {
            initializeConditions(where_managing);
        }

        for (int f_node: first_compatibility.keySet()) {
            for (int s_node: first_compatibility.get(f_node)) {
                updateCandidatesForStateZero(matchingData, states, q_src, q_dst, f_node, s_node, query_obj,nodes_symmetry,graphPaths);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() -1) {
                    // STATE ZERO
                    startFromStateZero();
                    updateSolutionNodesAndEdgeForStateZero(matchingData, states);

                    if(areWhereConditionsVerified(matchingData, states, query_obj, setWhereConditions, where_managing)) {
                        updateMatchingInfoForStateZero(matchingData);
                        goAhead();
                        updateCandidatesForStateGraterThanZero(matchingData, states, query_obj, nodes_symmetry, edges_symmetry, graphPaths);

                        while (si > 0) {
                            // BACK TRACKING ON EDGES
                            if (psi >= si) {
                                removeMatchingInfoForStateGraterThanZero(si, states, matchingData);

                                // RESET CONDITIONS FOR THE PREVIOUS MATCHED STATE
                                if(areThereConditions) {
                                    resetConditionsForState(where_managing, psi, matchingData, states, query_obj);
                                }
                            }

                            // NEXT CANDIDATE
                            matchingData.candidatesIT[si]++;

                            if (shouldBacktrack(matchingData)) { // BACKTRACKING
                                backtrack();
                            } else {  // FORWARD TRACKING ON EDGES
                                // SET NODE AND EDGE TO MATCH
                                updateSolutionNodesAndEdgeForStateGreaterThanZero(matchingData, states);

                                if(areWhereConditionsVerified(matchingData, states, query_obj, setWhereConditions, where_managing)) {
                                    updateMatchingInfoForStateGreaterThanZero(matchingData, states);
                                    if (lastStateReached(numQueryEdges)) { // INCREASE OCCURRENCES
                                        // New occurrence found
                                        newOccurrenceFound(numMaxOccs, justCount, distinct);
                                    } else { // GO AHEAD
                                        goAhead();
                                        updateCandidatesForStateGraterThanZero(matchingData, states, query_obj, nodes_symmetry, edges_symmetry, graphPaths);
                                    }
                                } else {
                                    psi = si;
                                }
                            }
                        }
                    }
                    //WHERE CHECK FAILED OR NO MORE CANDIDATES
                    if(areThereConditions) {
                        initializeConditions(where_managing);
                    }
                    // CLEANING OF STRUCTURES
                    removeMatchingInfoForStateZero(matchingData);
                }
            }
        }
        return numTotalOccs;
    }



    public static OutData matching(
            WhereConditionExtraction where_managing,
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
        query_obj.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility, graphPaths.getMap_node_color_degrees());
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

        // SYMMETRY CONDITIONS ORDERING
        where_managing.assignConditionsToNodesAndEdges(query_obj, edgeOrdering.getNodes_ordering(), edgeOrdering.getEdges_ordering());

        // SYMMETRY CONDITION COMPUTING
        outData.symmetry_time = System.currentTimeMillis();
        IntArrayList[] nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query_obj);
        IntArrayList[] edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query_obj);

        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        int numQueryEdges = query_obj.getQuery_edges().size();

        // OTHER CONFIGURATION
        MatchingData matchingData = new MatchingData(query_obj);

        // START MATCHING PHASE
        int si = 0;
        // FIRST QUERY NODES
        outData.matching_time = System.currentTimeMillis();
        NodesPair first_compatibility = query_obj.getMap_edge_to_endpoints().get(states.map_state_to_edge[si]);
        int q_src = first_compatibility.getFirstEndpoint();
        int q_dst = first_compatibility.getSecondEndpoint();

        outData.num_occurrences = matching_procedure(
                where_managing, first_compatibility.getFirst_second(), matchingData, states, graphPaths,
                query_obj, nodes_symmetry, edges_symmetry, numQueryEdges, numMaxOccs,
                q_src, q_dst, justCount, distinct
        );
        report();
        return outData;
    }

    private static boolean checkWhereCond(QueryStructure query_obj, StateStructures states, int si, MatchingData matchingData, WhereConditionExtraction where_managing) {
        //Check edge conditions
        int edgeCand = matchingData.solution_edges[si];
        int queryEdgeId = states.map_state_to_edge[si];
        QueryEdge queryEdge = query_obj.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getConditions().values()) {
            if (checkQueryCondition(edgeCand, condition, query_obj, matchingData.solution_nodes, matchingData.solution_edges)) {
                condition.setStatus(PropositionStatus.EVALUATED);
            } else {
                condition.setStatus(PropositionStatus.FAILED);
            }
        }

        //Check node conditions
        int querySrcID = states.map_state_to_src[si];
        int queryDstID = states.map_state_to_dst[si];

        QueryNode querySrc = query_obj.getQuery_node(querySrcID);
        QueryNode queryDst = query_obj.getQuery_node(queryDstID);


        if (si == 0) { // STATE 0, BOTH SRC AND DST MUST BE CHECKED
            int srcCand, dstCand;

            srcCand = matchingData.solution_nodes[0];
            dstCand = matchingData.solution_nodes[1];

            // SRC
            for (QueryCondition condition : querySrc.getConditions().values()) {
                if (checkQueryCondition(srcCand, condition, query_obj, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.EVALUATED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                }
            }

            // DST
            for (QueryCondition condition : queryDst.getConditions().values()) {
                if (checkQueryCondition(dstCand, condition, query_obj, matchingData.solution_nodes, matchingData.solution_edges)) {
                    condition.setStatus(PropositionStatus.EVALUATED);
                } else {
                    condition.setStatus(PropositionStatus.FAILED);
                }
            }
        } else { // STATE > 0, ONLY THE NEW MATCHED NODE MUST BE CHECKED
            int nodeToMatch = states.map_state_to_mnode[si];

            if (nodeToMatch != -1) {
                QueryNode matchedNode = query_obj.getQuery_node(nodeToMatch);
                int nodeCand = matchingData.solution_nodes[nodeToMatch];
                for (QueryCondition condition : matchedNode.getConditions().values()) {
                    if (checkQueryCondition(nodeCand, condition, query_obj, matchingData.solution_nodes, matchingData.solution_edges)) {
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
        return evaluateAllConditions(where_managing);
    }
    private static void removeMatchingInfoForStateGraterThanZero(int state, StateStructures states, MatchingData matchingData) {
        matchingData.matchedEdges.remove(matchingData.solution_edges[state]);
        matchingData.solution_edges[state] = -1;
        // REMOVE THE NODE IF EXIST
        int selected_candidate = states.map_state_to_mnode[si];
        if (selected_candidate != -1) {
            matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
            matchingData.solution_nodes[selected_candidate] = -1;
        }
    }

    public static void removeMatchingInfoForStateZero(MatchingData matchingData) {
        matchingData.matchedEdges.remove(matchingData.solution_edges[0]);
        matchingData.solution_edges[0] = -1;
        matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
        matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
        matchingData.solution_nodes[0] = -1;
        matchingData.solution_nodes[1] = -1;
    }
    public static void updateSolutionNodesAndEdgeForStateZero(MatchingData matchingData, StateStructures states) {
        matchingData.solution_edges[0] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_src[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_dst[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
    }
    public static void updateMatchingInfoForStateZero(MatchingData matchingData) {
        matchingData.matchedEdges.add(matchingData.solution_edges[0]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
    }

    public static void updateMatchingInfoForStateGreaterThanZero(MatchingData matchingData, StateStructures states) {
        matchingData.matchedEdges.add(matchingData.solution_edges[si]);
        int node_to_match = states.map_state_to_mnode[si];
        if (node_to_match != -1) {
            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
        }
    }

    public static void updateSolutionNodesAndEdgeForStateGreaterThanZero(MatchingData matchingData, StateStructures states) {
        matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
        int node_to_match = states.map_state_to_mnode[si];
        if (node_to_match != -1)
            matchingData.solution_nodes[node_to_match] =
                    matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
    }


    public static void updateCandidatesForStateZero(MatchingData matchingData, StateStructures states, int q_src, int q_dst, int f_node, int s_node, QueryStructure query_obj, IntArrayList[] nodes_symmetry, GraphPaths graphPaths) {
        matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                q_src, q_dst, f_node, s_node, states.map_state_to_edge[0],
                query_obj, graphPaths, matchingData, nodes_symmetry, states
        );
        matchingData.candidatesIT[0] = -1;
    }

    public static void updateCandidatesForStateGraterThanZero(MatchingData matchingData, StateStructures states,QueryStructure query_obj, IntArrayList[] nodes_symmetry, IntArrayList[] edges_symmetry, GraphPaths graphPaths) {
        matchingData.setCandidates[si] = NewFindCandidates.find_candidates(
                graphPaths, query_obj, si, nodes_symmetry, edges_symmetry, states, matchingData
        );
        matchingData.candidatesIT[si] = -1;
    }

    public static void goAhead() {
        psi = si;
        si++;
    }

    public static void backtrack() {
        psi = si;
        si--;
    }

    public static void startFromStateZero() {
        si = 0;
        psi = -1;
    }

    public static boolean shouldBacktrack(MatchingData matchingData) {
        return (matchingData.candidatesIT[si] == matchingData.setCandidates[si].size());
    }

    public static boolean evaluateAllConditions(WhereConditionExtraction where_managing) {
        atLeastOnePropositionVerified = false;
        allPropositionsFailed = true;

        // for each or proposition
        for(int i = 0; i < where_managing.getSetWhereConditions().size(); i++) {
            // for each condition in the current or proposition
            Int2ObjectOpenHashMap<QueryCondition> conditionSet = where_managing.getMapOrPropositionToConditionSet().get(i);

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
                where_managing.getMapOrPropositionToStatus().put(i, PropositionStatus.EVALUATED);
                atLeastOnePropositionVerified = true;
                allPropositionsFailed = false;
            } else if(canBeTrue) { // NOT COMPLETELY EVALUATED (other conditions must be checked)
                where_managing.getMapOrPropositionToStatus().put(i, PropositionStatus.NOT_EVALUATED);
                allPropositionsFailed = false;
            } else { // FAILED
                where_managing.getMapOrPropositionToStatus().put(i, PropositionStatus.FAILED);
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

    public static void initializeConditions(WhereConditionExtraction where_managing) {
        atLeastOnePropositionVerified = false;
        allPropositionsFailed = false;

        // for each or proposition
        for(int i = 0; i < where_managing.getSetWhereConditions().size(); i++) {
            // for each condition in the current or proposition
            Int2ObjectOpenHashMap<QueryCondition> conditionSet = where_managing.getMapOrPropositionToConditionSet().get(i);

            for(QueryCondition condition: conditionSet.values()) {
                condition.setStatus(PropositionStatus.NOT_EVALUATED);
            }
        }

        doWhereCheck = true;
    }

    public static void resetConditionsForState(WhereConditionExtraction where_managing, int state, MatchingData matchingData, StateStructures states, QueryStructure query_obj) {
        // STATE > 0

        //Reset edge conditions
        int queryEdgeId = states.map_state_to_edge[state];
        QueryEdge queryEdge = query_obj.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getConditions().values()) {
            condition.setStatus(PropositionStatus.NOT_EVALUATED);
        }

        // Reset node conditions
        int nodeToMatch = states.map_state_to_mnode[state];
        if (nodeToMatch != -1) {
            QueryNode matchedNode = query_obj.getQuery_node(nodeToMatch);

            for (QueryCondition condition : matchedNode.getConditions().values()) {
                condition.setStatus(PropositionStatus.NOT_EVALUATED);
            }
        }

        evaluateAllConditions(where_managing);
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

    public static boolean areWhereConditionsVerified(MatchingData matchingData, StateStructures states, QueryStructure query_obj, IntArrayList setWhereConditions, WhereConditionExtraction where_managing) {
        if (doWhereCheck) {
            return checkWhereCond(query_obj, states, si, matchingData, where_managing);
        }
        return true;
    }

    public static boolean lastStateReached(int numQueryEdges) {
        return si == numQueryEdges - 1;
    }

    public static void newOccurrenceFound(long numMaxOccs, boolean justCount, boolean distinct) {
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

    public static void printDebugInfo(GraphPaths graphPaths, QueryStructure query_obj, StateStructures states, EdgeOrdering edgeOrdering) {
        /**
         * LOG
         */

        System.out.println("TARGET GRAPH");
        graphPaths.getMap_pair_to_key().forEach((src, map) -> {
            map.forEach((dst, key) -> {
                System.out.print("(SRC: " + src +", DST: " + dst + ") -> {");
                IntArrayList[] edgeList = graphPaths.getMap_key_to_edge_list()[key];
                for(int color = 0; color < edgeList.length; color++) {
                    int finalColor = color;
                    edgeList[color].forEach((IntConsumer) (edge) -> {
                        System.out.print("( " + edge + ":C" + finalColor + "), " );
                    });
                }
                System.out.print("}\n");
            });
        });

        System.out.println("QUERY NODES");
        query_obj.getQuery_nodes().forEach((id, node) -> {
            System.out.println("ID: " + id + "-> " + node);
        });

        System.out.println("QUERY EDGES");
        query_obj.getQuery_pattern().getOut_edges().forEach((key, list) -> {
            System.out.println(key + "->" + list);
        });

        System.out.println("DOMAINS");
        query_obj.getPairs().forEach((pair) -> {
            System.out.print("P: " + pair + "\tDOMAIN (FS): ");
            pair.getFirst_second().forEach((key, list) -> {
                for (int dst : list) {
                    System.out.print("[" + key + ", " + dst + "], ");
                }

            });

            System.out.print("\tDOMAIN (SF): ");

            pair.getSecond_first().forEach((key, list) -> {
                for (int dst : list) {
                    System.out.print("[" + key + ", " + dst + "], ");
                }
            });
            System.out.print("\n");
        });

        System.out.println("PARIS ORDERING");
        System.out.println(edgeOrdering.getPairs_ordering());

        System.out.println("ORDERING DETAILS");
        for (int i = 0; i < states.map_state_to_src.length; i++) {
            int edge = states.map_state_to_edge[i];
            int src = states.map_state_to_src[i];
            int dst = states.map_state_to_dst[i];
            int matchedNode = states.map_state_to_mnode[i];
            EdgeDirection direction = states.map_edge_to_direction[i];
            System.out.println("STATE: " + i + "\tSRC: " + src + "\tDST: " + dst + "\tEDGE: " + edge + "\tDIRECTION: " + direction + "\tMATCHED_NODE: " + matchedNode);
        }
    }

    public static void printSymmetryConditions(IntArrayList[] nodes_symmetry, IntArrayList[] edges_symmetry) {
        System.out.println("NODES SIMMETRY: " + Arrays.toString(nodes_symmetry)) ;
        System.out.println("EDGES SIMMETRY: " + Arrays.toString(edges_symmetry)) ;
    }
}
