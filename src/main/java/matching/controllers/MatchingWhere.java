package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryEdge;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

import java.util.HashMap;

public class MatchingWhere extends MatchingSimple {
    protected static long matching_procedure(
            WhereConditionExtraction where_managing,
            Int2ObjectOpenHashMap<IntArrayList> first_compatibility,
            MatchingData matchingData,
            StateStructures states,
            GraphPaths graphPaths,
            QueryStructure query_obj,
            IntArrayList[] nodes_symmetry,
            IntArrayList[] edges_symmetry,
            int numQueryEdges, long numTotalOccs, long numMaxOccs,
            int q_src, int q_dst,
            boolean justCount, boolean distinct
    ) {
        System.out.println("WHERE MATCHING PROCEDURE");
        // WHERE CONDITIONS
        boolean doWhereCheck = true;
        boolean areThereConditions = true;
        IntArrayList setWhereConditions = where_managing.getSetWhereConditions();

        System.out.println("SET WHERE CONDITIONS: " + setWhereConditions);
        if (setWhereConditions.isEmpty()) {
            doWhereCheck = false;
            areThereConditions = false;
        }
        int[] setLogicalWhereConditions = new int[setWhereConditions.size()];
        IntArrayList currPosWhereCond = new IntArrayList();
        IntArrayList currLogWhereCond = new IntArrayList();

        // STATES
        int si = 0;
        int psi = -1;
        int sip1;

        for (int f_node : first_compatibility.keySet()) {
            for (int s_node : first_compatibility.get(f_node)) {
                matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                        q_src, q_dst, f_node, s_node, states.map_state_to_edge[0],
                        query_obj, graphPaths, matchingData, nodes_symmetry, states
                );

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

                    //Check where conditions
                    boolean whereCheckOk = true;
                    if (doWhereCheck)
                        whereCheckOk = checkWhereCond(query_obj, states, si, matchingData, setWhereConditions, setLogicalWhereConditions,
                                currPosWhereCond, currLogWhereCond);
                    if(whereCheckOk) {
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

                                // WHERE CONDITIONS MANAGEMENT
                                if (areThereConditions) {
                                    for (int i = 0; i < currPosWhereCond.size(); i++) {
                                        int pos = currPosWhereCond.getInt(i);
                                        int val = currLogWhereCond.getInt(i);
                                        setLogicalWhereConditions[pos] = 0;
                                        if (val == 1) {
                                            int newCount = setWhereConditions.getInt(pos) + 1;
                                            setWhereConditions.set(pos, newCount);
                                            if (newCount > 0)
                                                doWhereCheck = true;
                                        }
                                    }
                                    currPosWhereCond.clear();
                                    currLogWhereCond.clear();
                                }
                            }

                            // NEXT CANDIDATE
                            matchingData.candidatesIT[si]++;
                            boolean backtrack = matchingData.candidatesIT[si] == matchingData.setCandidates[si].size();

                            // BACKTRACK OR GO AHEAD
                            if (backtrack) { // BACKTRACK
                                psi = si;
                                si--;
                            }

                            // FORWARD TRACKING ON EDGES
                            else { // GO AEHAD
                                // SET NODE AND EDGE TO MATCH
                                matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
                                int node_to_match = states.map_state_to_mnode[si];
                                if (node_to_match != -1)
                                    matchingData.solution_nodes[node_to_match] =
                                            matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);

                                //Check where conditions
                                whereCheckOk = true;
                                if (doWhereCheck)
                                    whereCheckOk = checkWhereCond(query_obj, states, si, matchingData, setWhereConditions, setLogicalWhereConditions,
                                            currPosWhereCond, currLogWhereCond);

                                if (whereCheckOk) {
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


        // SYMMETRY CONDITION COMPUTING
        outData.symmetry_time = System.currentTimeMillis();
        IntArrayList[] nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query_obj);
        IntArrayList[] edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query_obj);
        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        int numQueryEdges = query_obj.getQuery_edges().size();

        // OTHER CONFIGURATION
        MatchingData matchingData = new MatchingData(query_obj);

        /**
         * LOG
         */
        System.out.println("QUERY NODES");
        query_obj.getQuery_nodes().forEach((id, node) -> {
            System.out.println("ID: " + id + "-> " +node);
        });

        System.out.println("QUERY EDGES");
        query_obj.getQuery_pattern().getOut_edges().forEach((key, list) -> {
            System.out.println(key + "->" + list);
        });

        System.out.println("PARIS ORDERING");
        System.out.println(edgeOrdering.getPairs_ordering());

        System.out.println("ORDERING DETAILS");
        for(int i = 0; i < states.map_state_to_src.length; i++) {
            int edge = states.map_state_to_edge[i];
            int src = states.map_state_to_src[i];
            int dst = states.map_state_to_dst[i];
            EdgeDirection direction = states.map_edge_to_direction[i];
            System.out.println("STATE: " + i + "\tSRC: " + src + "\tDST: " + dst + "\tEDGE: " + edge + "\tDIRECTION: " + direction);
        }




        // START MATCHING PHASE
        int si = 0;
        // FIRST QUERY NODES
        outData.matching_time = System.currentTimeMillis();
        NodesPair first_compatibility = query_obj.getMap_edge_to_endpoints().get(states.map_state_to_edge[si]);
        int q_src = first_compatibility.getFirstEndpoint();
        int q_dst = first_compatibility.getSecondEndpoint();

        outData.num_occurrences = matching_procedure(
                where_managing, first_compatibility.getFirst_second(), matchingData, states, graphPaths,
                query_obj, nodes_symmetry, edges_symmetry, numQueryEdges, outData.num_occurrences, numMaxOccs,
                q_src, q_dst, justCount, distinct
        );
        report();
        return outData;
    }


    private static boolean checkWhereCond(QueryStructure query_obj, StateStructures states, int si, MatchingData matchingData, IntArrayList setWhereConditions,
                                          int[] setLogicalWhereConditions, IntArrayList currPosWhereCond, IntArrayList currLogWhereCond) {

        System.out.println("CHECK WHERE COND");
        boolean whereCheckOk = false;

        //Check edge conditions
        int edgeCand = matchingData.solution_edges[si];
        int queryEdgeId = states.map_state_to_edge[si];
        QueryEdge queryEdge = query_obj.getQuery_edge(queryEdgeId);

        for (QueryCondition condition : queryEdge.getConditions().values()) {
            int pos = condition.getOrPropositionPos();
            if (setLogicalWhereConditions[pos] == 0) {
                boolean ans = checkQueryCondition(edgeCand, condition);

                currPosWhereCond.add(pos);
                if (ans)
                    currLogWhereCond.add(1);
                else
                    currLogWhereCond.add(-1);
            }
        }


        //Check node conditions
        int querySrcID = states.map_state_to_src[si];
        int queryDstID = states.map_state_to_dst[si];

        QueryNode querySrc = query_obj.getQuery_node(querySrcID);
        QueryNode queryDst = query_obj.getQuery_node(queryDstID);


        if (si == 0) {
            int srcCand, dstCand;
            if(states.map_edge_to_direction[queryEdgeId] == EdgeDirection.OUT) {
                srcCand = matchingData.solution_nodes[0];
                dstCand = matchingData.solution_nodes[1];
            } else {
                srcCand = matchingData.solution_nodes[1];
                dstCand = matchingData.solution_nodes[0];
            }

            // SRC
            for (QueryCondition condition : querySrc.getConditions().values()) {
                int pos = condition.getOrPropositionPos();
                if (setLogicalWhereConditions[pos] == 0) {
                    boolean ans = checkQueryCondition(srcCand, condition);
                    currPosWhereCond.add(pos);
                    if (ans)
                        currLogWhereCond.add(1);
                    else
                        currLogWhereCond.add(-1);
                }
            }

            // DST
            for (QueryCondition condition : queryDst.getConditions().values()) {

                int pos = condition.getOrPropositionPos();
                if (setLogicalWhereConditions[pos] == 0) {
                    boolean ans = checkQueryCondition(dstCand, condition);
                    currPosWhereCond.add(pos);
                    if (ans)
                        currLogWhereCond.add(1);
                    else
                        currLogWhereCond.add(-1);
                }
            }
        } else {
            //TODO: ask to Giovanni why we only check the source node
            querySrcID = states.map_state_to_src[si];
            if (matchingData.solution_nodes[querySrcID] == -1) {
                int srcCand = matchingData.solution_nodes[querySrcID];
                querySrc = query_obj.getQuery_node(querySrcID);

                for (QueryCondition condition : querySrc.getConditions().values()) {
                    int pos = condition.getOrPropositionPos();
                    if (setLogicalWhereConditions[pos] == 0) {
                        boolean ans = checkQueryCondition(srcCand, condition);
                        currPosWhereCond.add(pos);
                        if (ans)
                            currLogWhereCond.add(1);
                        else
                            currLogWhereCond.add(-1);
                    }
                }
            }
        }

        //Check pattern conditions
        //TODO: IMPLEMENTARE
//        ObjectArrayList<PatternCondition> listPatternConds=queryGraph.getListPatternConditions();
//        ObjectArrayList<QueryNode> queryNodes=queryGraph.getNodes();
//        for(PatternCondition cond : listPatternConds)
//        {
//            ObjectArrayList<QueryNode> listPathNodes=cond.getPathNodes();
//            IntArrayList listPathNodeIds=cond.getPathNodesIds();
//            int[] solPathNodeIds=new int[listPathNodes.size()];
//            //System.out.println("si: "+si+" -- solutionCurr: "+matchingData.solution_nodes[si]);
//            int i;
//            for(i=0;i<listPathNodes.size();i++)
//            {
//                int idNode=listPathNodeIds.getInt(i);
//                if(idNode<queryNodes.size())
//                {
//                    //System.out.println(nodeName);
//                    int nodeState=mama.map_node_to_state[idNode];
//                    //System.out.println("solState: "+matchingData.solution_nodes[nodeState]);
//                    if(matchingData.solution_nodes[nodeState]==-1)
//                        break;
//                    else
//                        solPathNodeIds[i]=matchingData.solution_nodes[nodeState];
//                }
//                else
//                    solPathNodeIds[i]=-1;
//            }
//            if(i==listPathNodes.size())
//            {
//                int pos=cond.getOrPropositionPos();
//                if(setLogicalWhereConditions[pos]==0)
//                {
//                    boolean ans=targetGraph.checkPatternCondition(cond,solPathNodeIds);
//                    currPosWhereCond.add(pos);
//                    if(ans)
//                        currLogWhereCond.add(1);
//                    else
//                        currLogWhereCond.add(-1);
//                }
//            }
//        }

        //System.out.println(currPosWhereCond);
        //System.out.println(currLogWhereCond);
        //Check if WHERE clause is true
        for (int i = 0; i < currPosWhereCond.size(); i++) {
            int pos = currPosWhereCond.getInt(i);
            int val = currLogWhereCond.getInt(i);
            if (val == -1)
                setLogicalWhereConditions[pos] = -1;
            else {
                int newCount = setWhereConditions.getInt(pos) - 1;
                setWhereConditions.set(pos, newCount);
                if (newCount == 0) {
                    whereCheckOk = true;
                    setLogicalWhereConditions[pos] = 1;
                    //doWhereCheck=false;
                }
            }
        }
        //System.out.println(whereCheckOk);
        //Restore info if WHERE clause is not set to true
        if (!whereCheckOk) {
            for (int i = 0; i < currPosWhereCond.size(); i++) {
                int pos = currPosWhereCond.getInt(i);
                int val = currLogWhereCond.getInt(i);
                setLogicalWhereConditions[pos] = 0;
                if (val == 1) {
                    int newCount = setWhereConditions.getInt(pos) + 1;
                    setWhereConditions.set(pos, newCount);
                }
            }
            currPosWhereCond.clear();
            currLogWhereCond.clear();
        }
        return whereCheckOk;
    }

    public static boolean checkQueryCondition(int targetElementID, QueryCondition condition) {
        String operator = condition.getOperation();
        String propertyName = condition.getNode_param().getElementKey();
        Object expressionValue = condition.getExpr_value();
        Object candidateValue = condition.getConditionCheck().getProperty(targetElementID, propertyName);
        System.out.println("Property Name: " + propertyName + "\tExpression Value: " + expressionValue + "\tCandidate Value: " + candidateValue + "\tOPERATOR: " + operator);
        return condition.getConditionCheck().getComparison().comparison(expressionValue, candidateValue, operator);
    }
}
