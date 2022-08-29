package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeOrdering;
import ordering.NodesPair;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import utility.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class MatchingPath extends MatchingSimple {
    public static OutData matching(
            boolean justCount,
            boolean distinct,
            long numMaxOccs,
            NodesEdgesLabelsMaps labels_types_idx,
            TargetBitmatrix target_bitmatrix,
            QueryStructure query_obj,
            GraphPaths graphPaths,
            HashMap<String, GraphMacroNode> macro_nodes,
            Int2ObjectOpenHashMap<String> nodes_macro,
            Optional<WhereConditionExtraction> where_managing
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
        states.map_state_to_first_endpoint = edgeOrdering.getMap_state_to_first_endpoint();
        states.map_state_to_second_endpoint = edgeOrdering.getMap_state_to_second_endpoint();
        states.map_state_to_unmatched_node = edgeOrdering.getMap_state_to_unmapped_nodes();
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

        // START MATCHING PHASE
        int si = 0;
        // FIRST QUERY NODES
        outData.matching_time = System.currentTimeMillis();
        NodesPair first_compatibility = query_obj.getMap_edge_to_endpoints().get(states.map_state_to_edge[si]);
        int q_src = first_compatibility.getFirstEndpoint();
        int q_dst = first_compatibility.getSecondEndpoint();

        Utils.printDebugInfo(graphPaths, query_obj, states, edgeOrdering);

        outData.num_occurrences = matching_procedure(
                first_compatibility.getFirst_second(), matchingData, states, graphPaths,
                query_obj, nodes_symmetry, edges_symmetry, numQueryEdges, numMaxOccs,
                q_src, q_dst, justCount, distinct
        );
        report();
        return outData;
    }

    private static long matching_procedure(
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

        System.out.println(Arrays.toString(states.map_state_to_edge));

//        matchingData.solution_nodes[1] = 2;
//        System.out.println(PathsUtils.findPaths(0, query_obj, graphPaths,matchingData, nodes_symmetry, edges_symmetry, states));

        matchingData.solution_nodes[0] = 7;
        System.out.println(PathsUtils.findStartPaths(0, query_obj, graphPaths,matchingData, nodes_symmetry, states));


        return 0;
    }
}
