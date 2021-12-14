package target_graph.bitmatrix;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import target_graph.edges.NewEdgeAggregation;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;


public class BitmatrixStructure {
    private final Table             table;
    private final ArrayList<BitSet> bitmatrix;
    private       IntIndex          bitmatrix_id_indexing;

    public BitmatrixStructure(){
        IntColumn src_col    = IntColumn.create("src");
        IntColumn dst_col    = IntColumn.create("dst");
        IntColumn bit_mtx_id = IntColumn.create("btx_id");
        table                = Table.create(src_col, dst_col, bit_mtx_id);
        bitmatrix            = new ArrayList<>();
    }

    // BITMATRIX NODES SETTING
    private void node_part_configuration(BitSet bit_mtx_row, int[] labels, int offset){
        Arrays.stream(labels).forEach(label -> {
            if (label != -1)
                bit_mtx_row.set(offset + label);
        });
    }

    // BITMATRIX EDGES SETTING
    private void edge_part_configuration(
        BitSet bit_mtx_row,
        Int2ObjectOpenHashMap<IntArrayList> type_edge,
        //  nodes_labels_num     edges_types_num
        int offset_1,            int offset_2
    ){
        IntIterator types_iter = type_edge.keySet().iterator();
        while (types_iter.hasNext()){
            int type = types_iter.nextInt();
            // EDGE WITHOUT LABEL
            if (type == 0) continue;
            // OUT EDGE
            if (type > 0 )
                bit_mtx_row.set(offset_1 + offset_2 + type -1);
            // IN EDGES
            else {
                bit_mtx_row.set(offset_1 + (-1) * type -1);
                System.out.println(type);
            }
            // NOTE: -1 because the type edge start from 1
        }
    }

    // ADD BITSET IF NOT EXIST
    private int add_bitset_if_not_exist(BitSet record){
        int i = 0;
        for (; i < this.bitmatrix.size(); i++){
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    public void create_bitset(
        NewEdgeAggregation              edge_aggregation,
        NodesEdgesLabelsMaps            labels_map,
        HashMap<String, GraphMacroNode> macro_nodes,
        Int2ObjectOpenHashMap<String>   nodes_macro
    ){
        int nodes_label_size = labels_map.n_type_sz();
        int edges_label_size = labels_map.e_type_sz();
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
                  int bitset_id = add_bitset_if_not_exist(bit_mtx_row);
                  // src - dst - bitset_id association
                  ((IntColumn) table.column("src")).append(src);
                  ((IntColumn) table.column("dst")).append(dest);
                  ((IntColumn) table.column("btx_id")).append(bitset_id);
             });
        });
        // INDEXING CREATION
        bitmatrix_id_indexing = new IntIndex(table.intColumn("btx_id"));
    }

    // GETTER
    public Table             getTable()     {return table;    }
    public ArrayList<BitSet> getBitmatrix() {return bitmatrix;}

    // TODO implement query check
    // TODO implement some where condition
}
