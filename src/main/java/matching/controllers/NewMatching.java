package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.HashMap;

public class NewMatching {
    public static int     numTotalOccs;
    public static double  domain_time;
    public static double  ordering_stime;
    public static double  symmetry_condition;
    public static double  matching_time;


    public static void matching (
            boolean                         justCount,
            boolean                         distinct,
            long                            numMaxOccs,
            NodesEdgesLabelsMaps            labels_types_idx,
            TargetBitmatrix                 target_bitmatrix,
            QueryStructure                  query_obj,
            GraphPaths                      graphPaths,
            HashMap<String, GraphMacroNode> macro_nodes,
            Int2ObjectOpenHashMap<String>   nodes_macro
    ) {
        // MATCHING DATA
        numTotalOccs = 0;

        // DOMAIN COMPUTING
        // QUERY BITMATRIX COMPUTING
        domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.create_bitset(query_obj, labels_types_idx);
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query_obj.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility);
        domain_time = (System.currentTimeMillis() - domain_time) / 1000;

        // EDGE ORDERING AND STATE OBJECT CREATION
        ordering_stime            = System.currentTimeMillis();

    }
}
