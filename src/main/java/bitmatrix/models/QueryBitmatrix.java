package bitmatrix.models;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class QueryBitmatrix extends BitMatrix {
    private boolean isDirected;

    // CONSTRUCTOR
    public QueryBitmatrix() {
        super();
    }

    // 1. EDGE BITs CONFIGURATION
    private void fill_edge_bits(QueryEdge edge, ArrayList<BitSet> bit_mtx_row, int offset, int[] src_label, int[] dst_label, int dst_start_pos) {
        IntArrayList types = edge.getEdge_label();
        int actual_size, i;
        // IF AN EDGE IS A VARIABLE PATH WE NEED TO SPLIT THE ROWs INTO TWO ONES
        // (n1:A)-[:*2..3]->(n2:B)
        // [1,0, in_part, out_part, 0,1] -->
        //    1. [1,0, in_part, out_part, 0,0]
        //    2. [0,0, in_part, out_part, 0,1]
        if (edge.getMax_deep() > 1) {
            actual_size = bit_mtx_row.size();
            for (i = 0; i < actual_size; i++) {
                BitSet row = bit_mtx_row.get(i);
                BitSet copy_row = (BitSet) row.clone();
                // RESET SRC BITs
                Arrays.stream(src_label).forEach(copy_row::clear);
                // RESET DST BITs
                Arrays.stream(dst_label).forEach(label -> row.clear(dst_start_pos + label));
                bit_mtx_row.add(copy_row);
            }
        }
        int types_size = types.size();
        if (types_size == 0) return;
        // EDGEs BIT SETTING
        actual_size = bit_mtx_row.size();
        for (i = 0; i < actual_size; i++) {
            BitSet origin_row = bit_mtx_row.get(i);
            // CASE WHERE TYPE IS CHARACTERIZED BY AN OR CONDITION
            // -[r:type_1|type_2]-> OR <-[r:type_1|type_2]-
            // NOTE: -1 BECAUSE THE TYPE IDX START FROM 1
            if (types_size > 1) {
                for (int k = 1; k < types_size; k++) {
                    BitSet new_row = (BitSet) origin_row.clone();
                    new_row.set(offset + types.getInt(k));  //-1);
                    bit_mtx_row.add(new_row);
                }
            }
            origin_row.set(offset + types.getInt(0)); // -1);
        }
    }

    // 2. SRC-DST EDGEs BITs CONFIGURATION
    private void edges_part_configuration(QueryStructure query, int dst_position, int[] src_labels, int[] dst_labels, ArrayList<BitSet> src_dst_edges, Int2ObjectOpenHashMap<IntArrayList> verse_edges) {
        verse_edges.int2ObjectEntrySet().fastForEach(dir_edges -> {
            // -1 INCOMING; 1 OUTGOING; 0 BOTH
            int idx = dir_edges.getIntKey() == -1 ? 1 : dir_edges.getIntKey() == 1 ? 2 : 3;
            for (int edge_id : dir_edges.getValue())
                fill_edge_bits(query.getQuery_edge(edge_id), src_dst_edges, getStart_directed_pos()[idx], src_labels, dst_labels, dst_position);
        });
    }

    // 3. SPECULATE ROWs CREATION
    // WE NEED TO CREATE A SEPARATE STRUCTURE BECAUSE THE REVERSE IMPLIES DST-SRC AND NOT SRC-DST
    private ArrayList<BitSet> speculate_rows_computing(ArrayList<BitSet> bit_mtx_row) {
        ArrayList<BitSet> speculate = new ArrayList<>();
        // INIT SPECULATE ROW
        int last_idx = getStart_directed_pos().length - 1;
        for (BitSet row : bit_mtx_row) {
            speculate.add(super.speculate_row(row, last_idx));
        }
        return speculate;
    }

    // BIT-MATRIX CONFIGURATION
    public void createBitset(QueryStructure query, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager) {
        isDirected = query.getQuery_pattern().getIn_out_edges().size() == 0;
        int bitSetSize = setStartDirectedPosition(nodesLabelsManager.getMapIntLabelToStringLabel().size(), edgesLabelsManager.getMapIntLabelToStringLabel().size(), isDirected);
        int dstPosition = getStart_directed_pos().length - 2;

        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> aggregate_edge = query.getQuery_pattern().aggregate_edge();
        aggregate_edge.forEach((src, dst_list) -> {
            int[] src_labels = query.getQuery_node(src).getLabels().toIntArray();
            dst_list.forEach((dst, verse_edges) -> {
                int[] dst_labels = query.getQuery_node(dst).getLabels().toIntArray();
                // LIST DUE TO OR CONDITION IN EDGE TYPE
                ArrayList<BitSet> src_dst_edges = new ArrayList<>();
                BitSet src_dst_aggreg = new BitSet(bitSetSize);
                // SRC AND DST LABELs CONFIGURATION
                node_part_configuration(src_dst_aggreg, src_labels, 0);
                // WE PUT -2 BECAUSE THE LAST POSITION IS RELATED TO BITSET SIZE
                node_part_configuration(src_dst_aggreg, dst_labels, getStart_directed_pos()[dstPosition]);
                src_dst_edges.add(src_dst_aggreg);
                // EDGES TYPEs ASSOCIATION
                edges_part_configuration(query, dstPosition, src_labels, dst_labels, src_dst_edges, verse_edges);
                ArrayList<BitSet> dst_src_edges = speculate_rows_computing(src_dst_edges);
                // SRC-DST-ROWs ASSOCIATION
                add_src_dst_multipleRows(src, dst, src_dst_edges);
                add_src_dst_multipleRows(dst, src, dst_src_edges);
            });
        });
    }

    // GETTER
    public boolean isDirected() {
        return isDirected;
    }
}