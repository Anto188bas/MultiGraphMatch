package bitmatrix.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import target_graph.edges.NewEdgeAggregation;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;
import java.util.BitSet;
import java.util.HashMap;


public class TargetBitmatrix extends BitMatrix{
    // CONSTRUCTOR
    public TargetBitmatrix(){
        super();
    }

    // BITMATRIX EDGES SETTING
    private void edge_part_configuration(
        BitSet bit_mtx_row,
        Int2ObjectOpenHashMap<IntArrayList> type_edge,
        //  nodes_labels_num     edges_types_num
        int offset_1,            int offset_2
    ){
        IntIterator types_iter = type_edge.keySet().iterator();
        while (types_iter.hasNext())
            super.set_edge_color(bit_mtx_row, types_iter.nextInt(), offset_1, offset_2);
    }

    @Override
    public void create_bitset(
        NewEdgeAggregation              edge_aggregation,
        NodesEdgesLabelsMaps            labels_map,
        HashMap<String, GraphMacroNode> macro_nodes,
        Int2ObjectOpenHashMap<String>   nodes_macro
    ){
        int nodes_label_size = labels_map.n_type_sz();
        int edges_label_size = labels_map.e_type_sz();
        Table table          = super.getTable();
        edge_aggregation.getAggregateEdges().forEach((src, dest_list) -> {
             GraphMacroNode src_macro_node = macro_nodes.get(nodes_macro.get((int) src));
             dest_list.forEach((dest, edges_type) -> {
                  GraphMacroNode dst_macro_node = macro_nodes.get(nodes_macro.get((int) dest));
                  BitSet bit_mtx_row = new BitSet(2*(nodes_label_size + edges_label_size));
                  // SRC LABELS CONFIGURATION.    0 TO len(NODE_LABELS) - 1
                  node_part_configuration(bit_mtx_row, src_macro_node.get_macroNode_labels(), 0);
                  // IN/OUT TYPES CONFIGURATION
                  // IN  -> len(NODES_LABEL) TO len(NODES_LABEL) + len(EDGE_TYPES)
                  // OUT -> len(NODES_LABEL) + len(EDGE_TYPES) TO len(NODES_LABEL) + 2 * len(EDGE_TYPES)
                  edge_part_configuration(bit_mtx_row, edges_type, nodes_label_size, edges_label_size);
                  // DST LABELS CONFIGURATION
                  int offset = nodes_label_size + 2 * edges_label_size;
                  node_part_configuration(bit_mtx_row, dst_macro_node.get_macroNode_labels(), offset);
                  // SRC-DST-ROW ASSOCIATION
                  add_src_dst_row(src, dest, bit_mtx_row);
             });
        });
        // INDEXING CREATION
        super.setBitmatrix_id_indexing(new IntIndex(table.intColumn("btx_id")));
    }

    @Override
    public void create_bitset(QueryStructure query, NodesEdgesLabelsMaps labels_map) {}
}
