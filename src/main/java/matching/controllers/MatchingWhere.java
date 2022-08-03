package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.*;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeDirection;
import ordering.EdgeOrdering;
import ordering.NodesPair;
import scala.Int;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import javax.sound.midi.SysexMessage;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.IntConsumer;

public class MatchingWhere extends MatchingSimple {
    public static boolean doWhereCheck;


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
        // WHERE CONDITIONS
        doWhereCheck = true;
        boolean areThereConditions = true;
        IntArrayList setWhereConditions = where_managing.getSetWhereConditions();

        if (setWhereConditions.isEmpty()) {
            doWhereCheck = false;
            areThereConditions = false;
        }

        int[] setLogicalWhereConditions = new int[setWhereConditions.size()];
        IntArrayList numSatisfiedConditionsPerOrProposition = new IntArrayList();
        for(int i = 0; i < setWhereConditions.size(); i++) {
            numSatisfiedConditionsPerOrProposition.add(i, 0);
        }
        IntArrayList currPosWhereCond = new IntArrayList();
        IntArrayList currLogWhereCond = new IntArrayList();
        Int2IntOpenHashMap mapPropostionToNumConditions = where_managing.getMapPropositionToNumConditions();
        System.out.println("********************************************************************************");
        System.out.println("SET WHERE CONDITIONS: " + setWhereConditions);
        System.out.println("SET LOGICAL WHERE CONDITIONS: " + Arrays.toString(setLogicalWhereConditions));
        System.out.println("CUR POS WHERE CONDITIONS: " + currPosWhereCond);
        System.out.println("CUR LOG WHERE CONDITIONS: " + currLogWhereCond);
        System.out.println("********************************************************************************");


        // STATES
        int si = 0;
        int psi = -1;
        int sip1;

        // CANDIDATES FOR STATE 0 (src, dst, edge)
        for (int f_node : first_compatibility.keySet()) {
            for (int s_node : first_compatibility.get(f_node)) {
                matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                        q_src, q_dst, f_node, s_node, states.map_state_to_edge[0],
                        query_obj, graphPaths, matchingData, nodes_symmetry, states
                );

                System.out.println("FIRST CANIDATES: " + matchingData.setCandidates[0]);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                    // STATE ZERO
                    System.out.println("SI: " + si + "\tCANDIDATES IT SI: " + matchingData.candidatesIT[si] + "\tsetCandidates SI: " + matchingData.setCandidates[si]);
                    matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                    matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                    matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                    matchingData.matchedNodes.add(matchingData.solution_nodes[1]);

                    //Check where conditions
                    boolean whereCheckOk = true;
                    if (doWhereCheck)
                        whereCheckOk = checkWhereCond(query_obj, states, si, matchingData, setWhereConditions, setLogicalWhereConditions,
                                currPosWhereCond, currLogWhereCond, numSatisfiedConditionsPerOrProposition, mapPropostionToNumConditions);

                    if (whereCheckOk) {
                        psi = si;
                        si++;
                        System.out.println("WHERE CHECK OK FOR STATE "+si+", GO AHEAD");
                        matchingData.setCandidates[si] = NewFindCandidates.find_candidates(
                                graphPaths, query_obj, si, nodes_symmetry, edges_symmetry, states, matchingData
                        );
                        matchingData.candidatesIT[si] = -1;
                        System.out.println("\tSTATE: " + si + "\tPSI: " + psi + "\tCandidate Set " + (matchingData.setCandidates[si]) + "\titerator: " + matchingData.candidatesIT[si]);

                        while (si > 0) {
                            System.out.println("while si > 0 true");

                            // BACK TRACKING ON EDGES
                            if (psi >= si) {
                                System.out.println("\t psi >= si, BACKTRACKING");
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

                                            int newSatisfiedCount=numSatisfiedConditionsPerOrProposition.getInt(pos)-1;
                                            numSatisfiedConditionsPerOrProposition.set(pos, newSatisfiedCount);

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
                                System.out.println("BACKTRACK TO STATE " + (si -1) + "\tset cand size: " + matchingData.setCandidates[si].size() + "\tit: " + matchingData.candidatesIT[si]);
                                psi = si;
                                si--;
                            }

                            // FORWARD TRACKING ON EDGES
                            else { // GO AEHAD
                                System.out.println("GO AHED");
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
                                            currPosWhereCond, currLogWhereCond, numSatisfiedConditionsPerOrProposition, mapPropostionToNumConditions);

                                if (whereCheckOk) {
                                    System.out.println("whereCheckOk");
                                    // INCREASE OCCURRENCES
                                    if (si == numQueryEdges - 1) {
                                        System.out.println("\tOCCURRENCE FOUND!");
                                        System.out.println("\t\tSOLUTION NODES: " + Arrays.toString(matchingData.solution_nodes));

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
                                        System.out.println("\tGO AHEAD TO STATE " + (si + 1));
                                        //Update auxiliary info
                                        matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                                        node_to_match = states.map_state_to_mnode[si];
                                        if (node_to_match != -1) {
                                            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
                                        }
                                        sip1 = si + 1;
                                        System.out.println("\t\tSOLUTION NODES: " + Arrays.toString(matchingData.solution_nodes));
                                        System.out.println("FIND CANDIDATES FOR STATE " + sip1);
                                        matchingData.setCandidates[sip1] = NewFindCandidates.find_candidates(
                                                graphPaths, query_obj, sip1, nodes_symmetry, edges_symmetry, states, matchingData
                                        );
                                        matchingData.candidatesIT[sip1] = -1;
                                        psi = si;
                                        si = sip1;
                                        System.out.println("\t\tSTATE: " + si + "\tPSI: " + psi + "\tCandidate Set " + (matchingData.setCandidates[si]) + "\titerator: " + matchingData.candidatesIT[si]);

                                    }
                                } else {
                                    System.out.println("whereCheck not Ok (doWhereCheck: " + doWhereCheck);
                                }
                            }
                        }
                    }

                    if(areThereConditions)
                    {
                        for(int i=0;i<currPosWhereCond.size();i++)
                        {
                            int pos=currPosWhereCond.getInt(i);
                            int val=currLogWhereCond.getInt(i);
                            setLogicalWhereConditions[pos]=0;
                            if(val==1)
                            {
                                int newCount=setWhereConditions.getInt(pos)+1;
                                setWhereConditions.set(pos,newCount);

                                int newSatisfiedCount=numSatisfiedConditionsPerOrProposition.getInt(pos)-1;
                                numSatisfiedConditionsPerOrProposition.set(pos, newSatisfiedCount);
                                if(newCount>0)
                                    doWhereCheck=true;
                            }
                        }

                        currPosWhereCond.clear();
                        currLogWhereCond.clear();
                    }

                    // CLEANING OF STRUCTURES
                    System.out.println("CLEANING DATA STRUCTURES");
                    si = 0;
                    psi = - 1;
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

        System.out.println("NODES SIMMETRY: " + Arrays.toString(nodes_symmetry)) ;
        System.out.println("EDGES SIMMETRY: " + Arrays.toString(edges_symmetry)) ;


        //FIXME: symmetry conditions don't work as expected (RG)
//        IntArrayList[] nodes_symmetry = new IntArrayList[query_obj.getQuery_nodes().size()];
//        for(int i = 0; i < query_obj.getQuery_nodes().size(); i++) {
//            nodes_symmetry[i] = new IntArrayList();
//        }
//        IntArrayList[] edges_symmetry = new IntArrayList[query_obj.getQuery_edges().size()];
//        for(int i = 0; i < query_obj.getQuery_edges().size(); i++) {
//            edges_symmetry[i] = new IntArrayList();
//        }
        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;

        // QUERY INFORMATION
        int numQueryEdges = query_obj.getQuery_edges().size();

        // OTHER CONFIGURATION
        MatchingData matchingData = new MatchingData(query_obj);

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
                                          int[] setLogicalWhereConditions, IntArrayList currPosWhereCond, IntArrayList currLogWhereCond, IntArrayList numSatisfiedConditionsPerOrProposition,
                                          Int2IntOpenHashMap mapPropostionToNumConditions) {
        boolean whereCheckOk = false;
        System.out.println("CHECK WHERE COND FOR STATE " + si);
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


        if (si == 0) { // STATE 0, BOTH SRC AND DST MUST BE CHECKED
            int srcCand, dstCand;

            srcCand = matchingData.solution_nodes[0];
            dstCand = matchingData.solution_nodes[1];

            System.out.println("\tSRC CAND: " + srcCand + "\tDST CAND: " + dstCand);

            // SRC
            for (QueryCondition condition : querySrc.getConditions().values()) {
                int pos = condition.getOrPropositionPos();
                if (setLogicalWhereConditions[pos] == 0) {
                    boolean ans = checkQueryCondition(srcCand, condition);
                    currPosWhereCond.add(pos);
                    if (ans){
                        int newSatisfiedCount = numSatisfiedConditionsPerOrProposition.getInt(pos) + 1;
                        numSatisfiedConditionsPerOrProposition.set(pos, newSatisfiedCount);
                        currLogWhereCond.add(1);
                    }
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
                    if (ans){
                        int newSatisfiedCount = numSatisfiedConditionsPerOrProposition.getInt(pos) + 1;
                        numSatisfiedConditionsPerOrProposition.set(pos, newSatisfiedCount);
                        currLogWhereCond.add(1);
                    }
                    else
                        currLogWhereCond.add(-1);
                }
            }
        } else { // STATE > 0, ONLY THE NEW MATCHED NODE MUST BE CHECKED

            int matchedNodeID = states.map_state_to_mnode[si];
            if (matchedNodeID != -1) {
                QueryNode matchedNode = query_obj.getQuery_node(matchedNodeID);

                for (QueryCondition condition : matchedNode.getConditions().values()) {
                    int pos = condition.getOrPropositionPos();
                    if (setLogicalWhereConditions[pos] == 0) {
                        boolean ans = checkQueryCondition(matchedNodeID, condition);
                        currPosWhereCond.add(pos);
                        if (ans){
                            int newSatisfiedCount = numSatisfiedConditionsPerOrProposition.getInt(pos) + 1;
                            numSatisfiedConditionsPerOrProposition.set(pos, newSatisfiedCount);
                            currLogWhereCond.add(1);
                        }
                        else
                            currLogWhereCond.add(-1);
                    }
                }
            }


//            if (matchingData.solution_nodes[querySrcID] == -1) {
//                int srcCand = matchingData.solution_nodes[querySrcID];
//                querySrc = query_obj.getQuery_node(querySrcID);
//
//                for (QueryCondition condition : querySrc.getConditions().values()) {
//                    int pos = condition.getOrPropositionPos();
//                    if (setLogicalWhereConditions[pos] == 0) {
//                        // Verificare se si può verificare la condizione
//                        // CASO 1: si può verificare
//                        boolean ans = checkQueryCondition(srcCand, condition);
//                        currPosWhereCond.add(pos);
//                        if (ans)
//                            currLogWhereCond.add(1);
//                        else
//                            currLogWhereCond.add(-1);
//                        // CASO 2: non si può vericare
//                        // -> non succede nulla
//                        // Gestire stato 0
//                    }
//                }
//            }
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
        System.out.println("CHECK IF WHERE clause is TRUE");
        System.out.println("currPosWhereCond: " + currPosWhereCond);
        System.out.println("currLogWhereCond: " + currLogWhereCond);
        System.out.println("setWhereCond: " + setWhereConditions);

        for (int i = 0; i < currPosWhereCond.size(); i++) {
            int pos = currPosWhereCond.getInt(i);
            int val = currLogWhereCond.getInt(i);
            System.out.println("I: " + i + "\tPOS: " + pos + "\tVAL: " + val);
            if (val == -1)
                setLogicalWhereConditions[pos] = -1;
            else {
                int newCount=setWhereConditions.getInt(pos)-1;
                setWhereConditions.set(pos,newCount);
                System.out.println("newCount: " + newCount);
                System.out.println("updatedSetWhereCond: " + setWhereConditions);
                System.out.println("numSatisfiedCond: " + numSatisfiedConditionsPerOrProposition.getInt(pos));
                System.out.println("numCond: " + mapPropostionToNumConditions.get(pos));



                // ultima condizione della or corrente && #condizioni - #condizioni_non_verificabili - #numero_condizioni_verificate
                if(mapPropostionToNumConditions.get(pos) - numSatisfiedConditionsPerOrProposition.getInt(pos) - newCount == 0) {
                    whereCheckOk = true;
                    setLogicalWhereConditions[pos] = 1;
                }
                // Al posto di verificare newCount == 0, si potrebbe verificare
                // newCount - (condizioni non ancora verificate perché i nodi/archi non sono ancora stati matchati) == 0)
                // In questo caso, però, doWhereCheck deve essere true
                if (newCount == 0) {
//                    whereCheckOk = true;
//                    setLogicalWhereConditions[pos] = 1;
                    doWhereCheck=false;
                }
            }
        }


        System.out.println("whereCheckOk: " + whereCheckOk);
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
        System.out.println("\tRES CHECK ALL CONDITIONS: " + whereCheckOk);
        return whereCheckOk;
    }


    public static boolean checkQueryCondition(int targetElementID, QueryCondition condition) {
        //TODO: rewrite without if
        String operator = condition.getOperation();
        String propertyName = condition.getNode_param().getElementKey();
        Object expressionValue = condition.getExpr_value();
        Object candidateValue = condition.getConditionCheck().getProperty(targetElementID, propertyName);

        if(expressionValue instanceof NameValue) {
            System.out.println("WHERE VARIABILE, IMPLEMENTARE CONTROLLO");

            return true;
        }

        boolean negate = condition.isNegation();
        System.out.println("\tCondition: " + condition);
        System.out.println("\tProperty Name: " + propertyName + "\tExpression Value: " + expressionValue + "\tCandidate Value: " + candidateValue + "\tOPERATOR: " + operator + "\tNEGATE: " + negate);
        boolean res = condition.getConditionCheck().getComparison().comparison(candidateValue, expressionValue, operator);
        if (negate) {
            System.out.println("\t\tRES: " + !res);
            return !res;
        }
        System.out.println("\t\tRES: " + res);

        return res;
    }
}
