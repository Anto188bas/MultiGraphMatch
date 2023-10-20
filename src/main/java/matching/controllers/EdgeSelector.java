package matching.controllers;

import cypher.models.QueryEdge;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.MatchingData;
import org.javatuples.Triplet;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;

import java.util.ArrayList;


public class EdgeSelector {
    // NODE CHECK FOR BREAKING CONDITION
    public static boolean nodeCondCheck(int q_node, int t_node, MatchingData matchingData, IntArrayList[] nodes_symmetry) {
        boolean condCheck = true;
        IntArrayList condNode = nodes_symmetry[q_node];
        for (int i = 0; i < condNode.size(); i++) {
            int refTargetNode = matchingData.solution_nodes[condNode.getInt(i)];
            if (refTargetNode != -1 && t_node < refTargetNode) {
                condCheck = false;
                break;
            }
        }
        return condCheck;
    }

    // EDGE CHECK CONDITION
    public static boolean condCheckEdges(int q_edge, int t_edge, MatchingData matchingData, IntArrayList[] edge_symmetry, StateStructures states) {
        boolean condCheck = true;
        IntArrayList condEdge = edge_symmetry[q_edge];
        for (int i = 0; i < condEdge.size(); i++) {
            int refTargetEdge = matchingData.solution_edges[states.map_edge_to_state[condEdge.getInt(i)]];
            if (refTargetEdge != -1 && t_edge < refTargetEdge) {
                condCheck = false;
                break;
            }
        }
        return condCheck;
    }
    // =================================================================================================================
    // =================================================================================================================
    // 1A. TYPE VECTOR IS EMPTY
    public static void no_types_case(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, MatchingData matchingData, IntArrayList[] nodes_symmetry, IntArrayList listCandidates, int q_node, int[] cols, GraphPaths graphPaths) {
        // NOTE: cols_name has size 1 if the query is directed, otherwise 2. it contains the column name
        //       related to target nodes list that have to be matched.
        for (int i = 0; i < cols.length; i++) {
            int column = cols[i];
            candidates.forEach(triplet -> {
                int t_node = (column == 0 ? triplet.getValue0() : triplet.getValue1());
                if (!matchingData.matchedNodes.contains(t_node) && nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)) {
                    Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = triplet.getValue2();
                    for (IntArrayList edges : mapColorToEdges.values()) {
                        for (int idEdge : edges) {
                            // TODO WE WILL ANALYZE THE PROPERTIES AFTER THE BITMATRIX COMPATIBILITY
                            if (matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_node);
                        }
                    }
                }
            });
        }
    }

    // 1.A --> TODO: NEW SOLUTION 04/10/2023 [NO EDGE TYPE AND SRC-DSTs]
    public static void no_types_src(
         IntArrayList   candidates,     MatchingData matchingData,
         IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
         GraphPaths     graphPaths,
         int q_dst,
         int t_src
    ){
        for (int t_dst: candidates){
            if (!matchingData.matchedNodes.contains(t_dst) && nodeCondCheck(q_dst, t_dst, matchingData, nodes_symmetry)){
                for(IntArrayList color_edges: graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).values()){
                    for (int idEdge: color_edges){
                        if (matchingData.matchedEdges.contains(idEdge)) continue;
                        listCandidates.add(idEdge);
                        listCandidates.add(t_dst);
                    }
                }
            }
        }
    }


    // 1A. --> TODO: NEW SOLUTION 04/10/2023 [NO EDGE TYPE AND SRCs-DST]
    public static void no_types_dst(
         IntArrayList   candidates,     MatchingData matchingData,
         IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
         GraphPaths     graphPaths,
         int q_src,
         int t_dst
    ){
        for (int t_src: candidates){
            if (!matchingData.matchedNodes.contains(t_src) && nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)){
                for(IntArrayList color_edges: graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).values()){
                    for (int idEdge: color_edges){
                        if (matchingData.matchedEdges.contains(idEdge)) continue;
                        listCandidates.add(idEdge);
                        listCandidates.add(t_src);
                    }
                }
            }
        }
    }
    // =================================================================================================================
    // =================================================================================================================
    // 1B. TYPE VECTOR IS EMPTY (FIRST NODE)
    public static void no_type_case(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, int q_node, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodes_symmetry, IntArrayList listCandidates) {
        candidates.forEach(triple -> {
            int t_node = triple.getValue0();
            if (nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)) {
                Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges =triple.getValue2();
                for (IntArrayList edges : mapColorToEdges.values()) {
                    for (int idEdge : edges) {
                        listCandidates.add(idEdge);
                        listCandidates.add(t_node);
                        listCandidates.add(triple.getValue1().intValue());
                    }
                }
            }
        });
    }


    // 1B --> TODO: NEW SOLUTION 04/10/2023 [NO EDGE TYPE AND SRC-DST] FIRST NODE
    public static void no_types_first(
         MatchingData matchingData,   IntArrayList[] nodes_symmetry,
         IntArrayList listCandidates, GraphPaths     graphPaths,
         int q_src,
         int t_src,
         int t_dst
    ){
        if (!nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)) return;
        for (IntArrayList edges : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).values()){
            for (int idEdge : edges) {
                listCandidates.add(idEdge);
                listCandidates.add(t_dst);
                listCandidates.add(t_src);
            }

        }
    }
    // =================================================================================================================
    // =================================================================================================================
    // 2A. TYPE VECTOR IS SET
    public static void types_case(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, MatchingData matchingData, IntArrayList[] nodes_symmetry, IntArrayList listCandidates, int q_node, int[] cols, GraphPaths graphPaths, QueryEdge query_edge) {
        for (int i = 0; i < cols.length; i++) {
            int column = cols[i];
            candidates.forEach(triplet -> {
                int t_node = (column == 0 ? triplet.getValue0() : triplet.getValue1());
                if (!matchingData.matchedNodes.contains(t_node) && nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)) {
                    Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = triplet.getValue2();
                    for (int color : query_edge.getEdge_label()) {
                        if (mapColorToEdges.containsKey(color)) {
                            IntArrayList edges = mapColorToEdges.get(color);
                            for (int idEdge : edges) {
                                if (matchingData.matchedEdges.contains(idEdge)) continue;
                                listCandidates.add(idEdge);
                                listCandidates.add(t_node);
                            }
                        }
                    }
                }
            });
        }
    }


    // 2A --> TODO: NEW SOLUTION 04/10/2023 [SRC-DSTs]
    public static void types_src(
         IntArrayList   candidates,     MatchingData matchingData,
         IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
         GraphPaths     graphPaths,
         int q_dst,
         int t_src,
         int type
    ){
        for(int t_dst: candidates){
            if (!matchingData.matchedNodes.contains(t_dst) && nodeCondCheck(q_dst, t_dst, matchingData, nodes_symmetry)) {
                for(int idEdge: graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)){
                    if (matchingData.matchedEdges.contains(idEdge)) continue;
                    listCandidates.add(idEdge);
                    listCandidates.add(t_dst);
                }
            }
        }
    }

    // 2A --> TODO: NEW SOLUTION 04/10/2023 [SRC-DSTs MULTIPLE TYPE]
    public static void types_src(
            IntArrayList   candidates,     MatchingData matchingData,
            IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
            GraphPaths     graphPaths,
            int q_dst,
            int t_src,
            IntArrayList   types
    ){
        for(int t_dst: candidates){
            if (!matchingData.matchedNodes.contains(t_dst) && nodeCondCheck(q_dst, t_dst, matchingData, nodes_symmetry)) {
                for (int type: types) {
                    var types_edges = graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst);
                    if(types_edges.containsKey(type))
                        for (int idEdge : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)) {
                            if (matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_dst);
                        }
                }
            }
        }
    }

    // 2A --> TODO: NEW SOLUTION 04/10/2023 [SRCs-DST]
    public static void types_dst(
         IntArrayList   candidates,     MatchingData matchingData,
         IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
         GraphPaths     graphPaths,
         int q_src,
         int t_dst,
         int type
    ){
        for(int t_src: candidates){
            if (!matchingData.matchedNodes.contains(t_src) && nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)) {
                for(int idEdge: graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)){
                    if (matchingData.matchedEdges.contains(idEdge)) continue;
                    listCandidates.add(idEdge);
                    listCandidates.add(t_src);
                }
            }
        }
    }

    // 2A --> TODO: NEW SOLUTION 04/10/2023 [SRCs-DST MULTIPLE TYPE]
    public static void types_dst(
            IntArrayList   candidates,     MatchingData matchingData,
            IntArrayList[] nodes_symmetry, IntArrayList listCandidates,
            GraphPaths     graphPaths,
            int q_src,
            int t_dst,
            IntArrayList   types
    ){
        for(int t_src: candidates){
            if (!matchingData.matchedNodes.contains(t_src) && nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)) {
                for(int type: types) {
                    var types_edges = graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst);
                    if(types_edges.containsKey(type)){
                        for (int idEdge : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)) {
                            if (matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_src);
                        }
                    }
                }

            }
        }
    }
    // =================================================================================================================
    // =================================================================================================================
    // 2B. TYPE VECTOR IS SET (FIRST NODE)
    public static void types_case(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, int query_node, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodes_symmetry, IntArrayList listCandidates, QueryEdge query_edge, int t_src, int t_dst) {
        if (nodeCondCheck(query_node, t_src, matchingData, nodes_symmetry)) {
            candidates.forEach(triplet -> {
                Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = triplet.getValue2();
                for (int color : query_edge.getEdge_label()) {
                    if (mapColorToEdges.containsKey(color)) {
                        IntArrayList edges = mapColorToEdges.get(color);
                        for (int idEdge : edges) {
                            if (matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_src);
                            listCandidates.add(t_dst);
                        }
                    }
                }
            });
        }
    }

    // 2B --> TODO: NEW SOLUTION 04/10/2023 [SRC-DSTs FIRST NODE]
    public static void type_first(
         MatchingData matchingData,   IntArrayList[] nodes_symmetry,
         IntArrayList listCandidates, GraphPaths     graphPaths,
         int q_src,
         int t_src,
         int t_dst,
         int type
    ){
         if (nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)) {
             for(int idEdge: graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)){
                 if (matchingData.matchedEdges.contains(idEdge)) continue;
                 listCandidates.add(idEdge);
                 listCandidates.add(t_dst);
                 listCandidates.add(t_src);
             }
         }
    }

    public static void type_first(
            MatchingData matchingData,   IntArrayList[] nodes_symmetry,
            IntArrayList listCandidates, GraphPaths     graphPaths,
            int q_src,
            int t_src,
            int t_dst,
            IntArrayList types
    ){
        if (nodeCondCheck(q_src, t_src, matchingData, nodes_symmetry)) {
            for (int type: types) {
                var colors_edges = graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst);
                if (colors_edges.containsKey(type)) {
                    for (int idEdge : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).get(type)) {
                        if (matchingData.matchedEdges.contains(idEdge)) continue;
                        listCandidates.add(idEdge);
                        listCandidates.add(t_dst);
                        listCandidates.add(t_src);
                    }
                }
            }
        }
    }
    // =================================================================================================================
    // =================================================================================================================
    // 3A. BOTH MATCHED AND TYPE VECTOR IS UNSET
    public static void no_types_case_matched_nodes(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, MatchingData matchingData, IntArrayList[] edges_symmetry, IntArrayList listCandidates, GraphPaths graphPaths, StateStructures states, int q_edge) {
        candidates.forEach(triplet -> {
            Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = triplet.getValue2();
            for (IntArrayList edges : mapColorToEdges.values()) {
                for (int idEdge : edges) {
                    if (!matchingData.matchedEdges.contains(idEdge) && condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                        listCandidates.add(idEdge);
                }
            }
        });
    }

    // TODO NEW SOLUTION 04/10/2023 [SRC-DST]
    public static void no_types_matched_nodes(
         MatchingData matchingData,  IntArrayList[] edges_symmetry,
         StateStructures states   ,  GraphPaths     graphPaths,
         IntArrayList listCandidates,
         int t_src,
         int t_dst,
         int q_edge
    ){
         if (graphPaths.getMap_key_to_edge_list().get(t_src).containsKey(t_dst)){
             for (IntArrayList edges : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).values()) {
                 for (int idEdge : edges) {
                     if (!matchingData.matchedEdges.contains(idEdge) && condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                         listCandidates.add(idEdge);
                 }
             }
         }

    }
    // =================================================================================================================
    // =================================================================================================================
    // 4A. BOTH MATCHED AND TYPE VECTOR IS UNSET
    public static void type_case_matched_nodes(ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> candidates, MatchingData matchingData, IntArrayList[] edges_symmetry, IntArrayList listCandidates, GraphPaths graphPaths, StateStructures states, int q_edge, QueryEdge query_edge) {
        candidates.forEach(triplet -> {
            Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = triplet.getValue2();
            for (int color : query_edge.getEdge_label()) {
                if (mapColorToEdges.containsKey(color)) {
                    IntArrayList edges = mapColorToEdges.get(color);
                    for (int idEdge : edges) {
                        if (!matchingData.matchedEdges.contains(idEdge) && condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                            listCandidates.add(idEdge);
                    }
                }
            }
        });
    }

    // TODO NEW SOLUTION 04/10/2023 [SRC-DST]
    public static void types_matched_nodes(
            MatchingData    matchingData  ,  IntArrayList[] edges_symmetry,
            StateStructures states        ,  GraphPaths     graphPaths,
            IntArrayList    listCandidates,
            int t_src,
            int t_dst,
            int q_edge,
            IntArrayList types
    ){
        if (graphPaths.getMap_key_to_edge_list().get(t_src).containsKey(t_dst)){
            var colors_edges = graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst);
            for(int type: types) {
                if (colors_edges.containsKey(type)) {
                    for (IntArrayList edges : graphPaths.getMap_key_to_edge_list().get(t_src).get(t_dst).values()) {
                        for (int idEdge : edges) {
                            if (!matchingData.matchedEdges.contains(idEdge) && condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                                listCandidates.add(idEdge);
                        }
                    }
                }
            }
        }
    }
}
