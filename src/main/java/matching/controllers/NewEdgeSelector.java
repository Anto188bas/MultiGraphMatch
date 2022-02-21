package matching.controllers;

import cypher.models.QueryEdge;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import java.util.ArrayList;
import java.util.stream.Stream;


public class NewEdgeSelector {
    // NODE CHECK FOR BREAKING CONDITION
    public static boolean nodeCondCheck(int q_node, int t_node, MatchingData matchingData, IntArrayList[] nodes_symmetry) {
        boolean condCheck     = true;
        IntArrayList condNode = nodes_symmetry[q_node];
        for(int i=0; i<condNode.size(); i++) {
            int refTargetNode = matchingData.solution_nodes[condNode.getInt(i)];
            if (refTargetNode != -1 && t_node < refTargetNode){
                condCheck = false;
                break;
            }
        } return condCheck;
    }

    // EDGE CHECK CONDITION
    public static boolean condCheckEdges(int q_edge, int t_edge, MatchingData matchingData, IntArrayList[] edge_symmetry, StateStructures states) {
        boolean condCheck=true;
        IntArrayList condEdge = edge_symmetry[q_edge];
        for(int i=0; i<condEdge.size(); i++)
        {
            int refTargetEdge = matchingData.solution_edges[states.map_edge_to_state[condEdge.getInt(i)]];
            if(refTargetEdge!=-1 && t_edge < refTargetEdge) {
                condCheck = false;
                break;
            }
        }
        return condCheck;
    }

    // 1A. TYPE VECTOR IS EMPTY
    public static void no_types_case(
          ArrayList<Stream<Row>> candidates,
          MatchingData     matchingData,
          IntArrayList[]   nodes_symmetry,
          IntArrayList     listCandidates,
          int              q_node,
          String[]         cols_name,
          GraphPaths       graphPaths
    ) {
        // NOTE: cols_name has size 1 if the query is directed, otherwise 2. it contains the column name
        //       related to target nodes list that have to be matched.
        for(int i = 0; i < cols_name.length; i++){
            String column_name = cols_name[i];
            candidates.get(i).forEach(row -> {
            //for (Row row : candidates.get(i)) {
                int t_node = row.getInt(column_name);
                if (!matchingData.matchedNodes.contains(t_node) &&
                    nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)){
                    IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[row.getInt(2)];
                    for (IntArrayList edges: colors_edges) {
                        if (edges == null) continue;
                        for(int idEdge: edges) {
                            // TODO WE WILL ANALYZE THE PROPERTIES AFTER THE BITMATRIX COMPATIBILITY
                            if(matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_node);
                        }
                    }
                }
            });
        }
    }

    // 1B. TYPE VECTOR IS EMPTY (FIRST NODE)
    public static void no_type_case(
         ArrayList<Stream<Row>> candidates,
         int[]            query_nodes,
         GraphPaths       graphPaths,
         MatchingData     matchingData,
         IntArrayList[]   nodes_symmetry,
         IntArrayList     listCandidates
    ){
        for(int i = 0; i < query_nodes.length; i++) {
            final int ii = i;
            candidates.get(i).forEach(row->{
            //for (Row row : candidates.get(i)) {
                int t_node = row.getInt(0);
                if (nodeCondCheck(query_nodes[ii], t_node, matchingData, nodes_symmetry)){
                    IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[row.getInt(2)];
                    for (IntArrayList edges: colors_edges) {
                        if (edges == null) continue;
                        for(int idEdge: edges) {
                            listCandidates.add(idEdge);
                            listCandidates.add(t_node);
                            listCandidates.add(row.getInt(1));
                        }
                    }
                }
            });
        }
    }

    // 2A. TYPE VECTOR IS SET
    public static void types_case(
           ArrayList<Stream<Row>> candidates,
           MatchingData     matchingData,
           IntArrayList[]   nodes_symmetry,
           IntArrayList     listCandidates,
           int              q_node,
           String[]         cols_name,
           GraphPaths       graphPaths,
           QueryEdge        query_edge
    ) {
        for(int i = 0; i < cols_name.length; i++){
            String column_name = cols_name[i];
            candidates.get(i).forEach(row -> {
            //for (Row row : candidates.get(i)) {
                int t_node = row.getInt(column_name);
                if (!matchingData.matchedNodes.contains(t_node) &&
                    nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)){
                    IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[row.getInt(2)];
                    for(int color: query_edge.getEdge_label()) {
                        IntArrayList edges = colors_edges[color];
                        if (edges == null) continue;
                        for(int idEdge: edges) {
                            if(matchingData.matchedEdges.contains(idEdge)) continue;
                            listCandidates.add(idEdge);
                            listCandidates.add(t_node);
                        }
                    }
                }
            });
        }
    }

    // 2B. TYPE VECTOR IS SET (FIRST NODE)
    public static void types_case(
            ArrayList<Stream<Row>> candidates,
            int[]            query_nodes,
            GraphPaths       graphPaths,
            MatchingData     matchingData,
            IntArrayList[]   nodes_symmetry,
            IntArrayList     listCandidates,
            QueryEdge        query_edge,
            int              t_src,
            int              t_dst
    ){
        for(int i = 0; i < query_nodes.length; i++) {
            if (nodeCondCheck(query_nodes[i], t_src, matchingData, nodes_symmetry)){
                candidates.get(i).forEach(row -> {
                //for (Row row : candidates.get(i)) {
                    int key = row.getInt(2);
                    IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[key];
                    for(int color: query_edge.getEdge_label()) {
                        IntArrayList edges = colors_edges[color];
                        if (edges == null) continue;
                        for(int idEdge: edges) {
                            listCandidates.add(idEdge);
                            listCandidates.add(t_src);
                            listCandidates.add(t_dst);
                        }
                    }
                });
            }
        }
    }


    // 3A. BOTH MATCHED AND TYPE VECTOR IS UNSET
    public static void no_types_case_matched_nodes(
            ArrayList<Stream<Row>> candidates,
            MatchingData     matchingData,
            IntArrayList[]   edges_symmetry,
            IntArrayList     listCandidates,
            GraphPaths       graphPaths,
            StateStructures  states,
            int              q_edge
    ) {
        for (Stream<Row> candidate: candidates)
            candidate.forEach(row -> {
            //for (Row row : candidate) {
                IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[row.getInt(2)];
                for (IntArrayList edges : colors_edges) {
                    if (edges == null) continue;
                    for (int idEdge : edges) {
                        if (!matchingData.matchedEdges.contains(idEdge) &&
                                condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                            listCandidates.add(idEdge);
                    }
                }
            });
    }

    // 4A. BOTH MATCHED AND TYPE VECTOR IS UNSET
    public static void type_case_matched_nodes(
            ArrayList<Stream<Row>> candidates,
            MatchingData     matchingData,
            IntArrayList[]   edges_symmetry,
            IntArrayList     listCandidates,
            GraphPaths       graphPaths,
            StateStructures  states,
            int              q_edge,
            QueryEdge        query_edge
    ){
        for (Stream<Row> candidate: candidates) {
            candidate.forEach(row -> {
            //for (Row row : candidate) {
                IntArrayList[] colors_edges = graphPaths.getMap_key_to_edge_list()[row.getInt(2)];
                for (int color : query_edge.getEdge_label()) {
                    IntArrayList edges = colors_edges[color];
                    if (edges == null) continue;
                    for (int idEdge : edges) {
                        if (!matchingData.matchedEdges.contains(idEdge) &&
                            condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states))
                            listCandidates.add(idEdge);
                    }
                }
            });
        }
    }
}
