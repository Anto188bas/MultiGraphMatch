package matching.controllers;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import ordering.EdgeDirection;
import ordering.NodesPair;
import org.javatuples.Triplet;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import java.util.ArrayList;
import java.util.Arrays;


public class NewFindCandidates {
    // GLOBAL VARIABLE
    private static final int[] src_vet = new int[] {0};
    private static final int[] dst_vet = new int[] {1};
    private static final int[] src_dst = new int[] {0, 1};
    private static final int[] dst_src = new int[] {1, 0};


    // FIND CANDIDATE FOR SELECTED EDGES (FROM STATE 1 TO STATE N)
    public static IntArrayList find_candidates (
            GraphPaths           graphPaths,
            QueryStructure       query,
            int                  sel_state,
            IntArrayList[]       nodes_symmetry,
            IntArrayList[]       edges_symmetry,
            StateStructures      states,
            MatchingData         matchingData
    ) {
//        // PARAMETERS
        IntArrayList listCandidates    = new IntArrayList();

        int           edge_id           = states.map_state_to_edge[sel_state];
        NodesPair     edge_data         = query.getMap_edge_to_endpoints().get(edge_id);
        QueryEdge     queryEdge         = query.getQuery_edge(edge_id);
        EdgeDirection direction         = states.map_edge_to_direction[edge_id];
        IntArrayList  edge_type         = queryEdge.getEdge_label();

        // QUERY NODES
        int q_src                      = edge_data.getFirstEndpoint();
        int q_dst                      = edge_data.getSecondEndpoint();
        // TARGET NODES
        int t_src                      = matchingData.solution_nodes[q_src];
        int t_dst                      = matchingData.solution_nodes[q_dst];

        ArrayList<Triplet<Integer, Integer, Integer>> edges_submap  = new ArrayList<>();
        int[]         cols;
        // A. t_dst IS MATCHED
        if(t_src == -1) {
            IntArrayList compatible_list   = edge_data.getBySecondValue(t_dst);
            if(compatible_list != null) {
                // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
                if (direction == EdgeDirection.OUT) {          // src to match
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_dst, compatible_list));
                    cols = src_vet;
                }
                // 2. EDGE FROM q_dst TO q_src (INBOUND)
                else if (direction == EdgeDirection.IN) {    // dst to match
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_dst, compatible_list));
                    cols = dst_vet;
                }
                // 3. UNDIRECTED CASE          (BOTH)
                else {                        // 1. src to match and 2. dst to match
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_dst, compatible_list));
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_dst, compatible_list));
                    cols = src_dst;
                }
                // CANDIDATE CONFIGURATION
                if (edge_type.size() == 0)
                    NewEdgeSelector.no_types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_src, cols, graphPaths);
                else
                    NewEdgeSelector.types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_src, cols, graphPaths, queryEdge);
            }
        }

        // B. t_src IS MATCHED
        else if (t_dst == -1){
            IntArrayList compatible_subtable = edge_data.getByFirstValue(t_src);
            if(compatible_subtable != null) {

                // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
                if (direction == EdgeDirection.OUT) {
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_src, compatible_subtable));
                    cols = dst_vet;
                }
                // 2. EDGE FROM q_dst TO q_src (INBOUND)
                else if (direction == EdgeDirection.IN) {
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_src, compatible_subtable));
                    cols = src_vet;
                }
                // 3. UNDIRECTED CASE          (BOTH)
                else {
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_src, compatible_subtable));
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_src, compatible_subtable));
                    cols = dst_src;
                }
                // CANDIDATE CONFIGURATION
                if (edge_type.size() == 0)
                    NewEdgeSelector.no_types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_dst, cols, graphPaths);
                else
                    NewEdgeSelector.types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_dst, cols, graphPaths, queryEdge);
            }
        }
        else {
            // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
            if (direction == EdgeDirection.OUT) {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_src, t_dst);

                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            // 2. EDGE FROM q_dst TO q_src (INBOUND)
            else if (direction == EdgeDirection.IN) {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_dst, t_src);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            // 3. UNDIRECTED CASE          (BOTH)
            else {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_src, t_dst);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
                triplet = graphPaths.getBySRCandDST(t_dst, t_src);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            if (edge_type.size() == 0)
                NewEdgeSelector.no_types_case_matched_nodes(edges_submap, matchingData, edges_symmetry, listCandidates, graphPaths, states, edge_id);
            else
                NewEdgeSelector.type_case_matched_nodes(edges_submap, matchingData, edges_symmetry, listCandidates, graphPaths, states, edge_id, queryEdge);
        }
        return listCandidates;
    }


    //
    public static IntArrayList find_first_candidates(
            int q_src, int q_dst, int t_src, int t_dst, int edge_id,
            QueryStructure query,
            GraphPaths     graphPaths,
            MatchingData   matchingData,
            IntArrayList[] nodes_symmetry,
            StateStructures states
    ){
        IntArrayList listCandidates    = new IntArrayList();
        QueryEdge        queryEdge     = query.getQuery_edge(edge_id);
        EdgeDirection    direction     = states.map_edge_to_direction[edge_id];
        IntArrayList     edge_type     = queryEdge.getEdge_label();
        ArrayList<Triplet<Integer, Integer, Integer>> edges_submap  = new ArrayList<>();
        int q_node = q_src;

        // q_src = t_src AND q_dst = t_dst
        if (direction == EdgeDirection.OUT) {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
        }
        else if (direction == EdgeDirection.IN) {
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        }
        else {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        }

        if (edge_type.size() == 0)
            NewEdgeSelector.no_type_case(edges_submap, q_node, graphPaths, matchingData, nodes_symmetry, listCandidates);
        else
            NewEdgeSelector.types_case(edges_submap, q_node, graphPaths, matchingData, nodes_symmetry, listCandidates, queryEdge, t_src, t_dst);
        return listCandidates;
    }

}
