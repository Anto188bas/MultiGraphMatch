package matching.controllers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;


public class EdgeSelections {
    // 0. SET SELECTED EDGE AND NODE
    // - 1.
    private static void set_edge_node_id(IntArrayList edges, MatchingData matchingData, IntArrayList listCandidates, int t_node) {
        for(int idEdge: edges) {
            // TODO WE WILL ANALYZE THE PROPERTIES AFTER THE BITMATRIX COMPATIBILITY
            if(matchingData.matchedEdges.contains(idEdge)) continue;
            listCandidates.add(idEdge);
            listCandidates.add(t_node);
        }
    }

    // - 2.
    public static void set_edges_id(
        IntArrayList    edges,
        MatchingData    matchingData,
        int             q_edge,
        IntArrayList    listCandidates,
        IntArrayList[]  edges_symmetry,
        StateStructures states
    ){
        for(int idEdge : edges) {
            if(
                !matchingData.matchedEdges.contains(idEdge) &&
                FindCandidates.condCheckEdges(q_edge, idEdge, matchingData, edges_symmetry, states)
            )
                listCandidates.add(idEdge);
        }
    }

    // 1. TYPE VECTOR IS EMPTY AND DIRECTION IS BOTH, SO EVERY TARGET EDGE COULD BE A CANDIDATE
    public static void no_types_undirected_case(
            IntArrayList listCandidates, NewEdgeAggregation target_aggregation,
            Table candidates,            int                id_col,
            int                q_node,   MatchingData matchingData,
            IntArrayList[]     nodes_symmetry
    ) {
        if (candidates == null) return;
        for (Row row : candidates) {
            // WE ARE CONSIDERING THAT THE NODE OF POSITION id_col IS UNMATCHED
            int t_node = row.getInt(id_col);
            if (!matchingData.matchedNodes.contains(t_node) &&
                 FindCandidates.nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)) {
                 target_aggregation.getSrcDstAssociations(row.getInt(0), row.getInt(1))
                     .int2ObjectEntrySet().forEach(type_edges ->
                        // ALL EDGES ARE OK (BOTH DIRECTION)
                        set_edge_node_id(type_edges.getValue(), matchingData, listCandidates, t_node)
                     );
            }
        }
    }

    // 2. TYPE VECTOR IS EMPTY AND DIRECTION IS SET
    public static void no_types_directed_case(
            IntArrayList       listCandidates,  NewEdgeAggregation target_aggregation,
            Table              candidates,      int                id_col,
            int                q_node,          MatchingData       matchingData,
            IntArrayList[]     nodes_symmetry,  int                direction
    ) {
        if (candidates == null) return;
        for (Row row : candidates) {
            // WE ARE CONSIDERING THAT THE NODE OF POSITION id_col IS UNMATCHED
            int t_node = row.getInt(id_col);
            if (!matchingData.matchedNodes.contains(t_node) &&
                 FindCandidates.nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)) {
                 target_aggregation.getSrcDstAssociations(row.getInt(0), row.getInt(1))
                     .int2ObjectEntrySet().forEach(type_edges -> {
                         // ONLY EDGE WITH CONSIDERED DIRECTION (+1 OUT -1 IN)
                         if(Integer.signum(type_edges.getIntKey()) == direction)
                            set_edge_node_id(type_edges.getValue(), matchingData, listCandidates, t_node);
                     });
            }
        }
    }

    // 3. TYPE VECTOR IS SET
    public static void configured_types_case (
            IntArrayList       listCandidates,  NewEdgeAggregation target_aggregation,
            Table              candidates,      int                id_col,
            int                q_node,          MatchingData       matchingData,
            IntArrayList[]     nodes_symmetry,  IntArrayList types
    ) {
        if (candidates == null) return;
        for (Row row : candidates) {
            // WE ARE CONSIDERING THAT THE NODE OF POSITION id_col IS UNMATCHED
            int t_node = row.getInt(id_col);
            if (!matchingData.matchedNodes.contains(t_node) &&
                 FindCandidates.nodeCondCheck(q_node, t_node, matchingData, nodes_symmetry)){
                 int src = row.getInt(0); int dst = row.getInt(1);
                 Int2ObjectOpenHashMap<IntArrayList> edges = target_aggregation.getSrcDstAssociations(src, dst);
                 for(int i=0; i < types.size(); i++){
                     IntArrayList sel_edges = edges.get(types.getInt(i));
                     if(sel_edges == null) continue;
                     set_edge_node_id(sel_edges, matchingData, listCandidates, t_node);
                 }
            }
        }
    }


    // 4. CASE WHERE BOTH NODE ARE MATCHED
    public static void set_edge_candidate_both_nodes_matched(
         Int2ObjectOpenHashMap<IntArrayList> types_edges,
         IntArrayList                        types,
         MatchingData                        matchingData,
         StateStructures                     states,
         IntArrayList                        listCandidates,
         IntArrayList[]                      edges_symmetry,
         int edge_id, int codificated_direction
    ) {
        // NO TYPEs
        if (types.size() == 0) {
            // NO TYPEs AND UNDIRECTED CASE
            if (codificated_direction == 0) {
                types_edges.int2ObjectEntrySet().fastForEach(record ->
                   EdgeSelections.set_edges_id(record.getValue(), matchingData, edge_id, listCandidates, edges_symmetry, states)
                );
            }
            // NO TYPES AND DIRECTED CASE
            else{
                types_edges.int2ObjectEntrySet().fastForEach(record -> {
                    if(Integer.signum(record.getIntKey()) == codificated_direction)
                       EdgeSelections.set_edges_id(record.getValue(), matchingData, edge_id, listCandidates, edges_symmetry, states);
                });
            }
        }
        // TYPEs
        else {
            for(int i=0; i < types.size(); i++){
                IntArrayList sel_edges = types_edges.get(types.getInt(i));
                if(sel_edges == null) continue;
                EdgeSelections.set_edges_id(sel_edges, matchingData, edge_id, listCandidates, edges_symmetry, states);
            }
        }
    }
}