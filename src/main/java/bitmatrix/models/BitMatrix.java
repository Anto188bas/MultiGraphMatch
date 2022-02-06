package bitmatrix.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
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


public abstract class BitMatrix {
    private final Table             table;
    private final ArrayList<BitSet> bitmatrix;
    private       IntIndex          bitmatrix_id_indexing;
    private       int[]             start_directed_pos;
    private       int[]             start_reverse_pos;

    public BitMatrix(){
        IntColumn src_col    = IntColumn.create("src");
        IntColumn dst_col    = IntColumn.create("dst");
        IntColumn bit_mtx_id = IntColumn.create("btx_id");
        table                = Table.create(src_col, dst_col, bit_mtx_id);
        bitmatrix            = new ArrayList<>();
    }

    // BITMATRIX NODES SETTING
    public void node_part_configuration(BitSet bit_mtx_row, int[] labels, int offset){
        Arrays.stream(labels).forEach(label -> {
            if (label != -1)
                bit_mtx_row.set(offset + label);
        });
    }

    // ADD BITSET IF NOT EXIST
    // QUERY VERSION
    public int add_bitset_if_not_exist(BitSet record){
        int i = 0;
        for (; i < this.bitmatrix.size(); i++){
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    public int add_bitset_if_not_exist(BitSet record, int last_idx){
        int i = 0;
        for (; i < this.bitmatrix.size(); i++){
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record))  return i;
            BitSet reverse = speculate_row(record, last_idx);
            if (sel_bitset.equals(reverse)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    // CONFIGURE START DIRECTED POSITION VECTOR
    public int set_start_directed_position(
            NodesEdgesLabelsMaps labels_map,
            boolean is_directed
    ){
        int nodes_label_size = labels_map.n_type_sz();
        int edges_label_size = labels_map.e_type_sz();
        int bit_set_size     = 2 * nodes_label_size + (is_directed ? 2:3) * edges_label_size;

        // TYPE -> 0 UNDIRECTED, 1 -> DIRECTED
        if (is_directed) {
            //  SRC       IN                         OUT                                  DST
            start_directed_pos = new int[] {
                    0, nodes_label_size, nodes_label_size + edges_label_size, nodes_label_size + 2 * edges_label_size, bit_set_size
            };
            this.start_reverse_pos = new int[] {
                    nodes_label_size + 2 * edges_label_size, nodes_label_size + edges_label_size, nodes_label_size, 0
            };
        }
        else {
            //  SRC       IN                         OUT                                 IN/OUT
            this.start_directed_pos = new int[] {
                    0, nodes_label_size, nodes_label_size +  edges_label_size, nodes_label_size + 2 * edges_label_size,
                    nodes_label_size + 3 * edges_label_size, bit_set_size
            };
            this.start_reverse_pos  = new int[] {
                    nodes_label_size + 3 * edges_label_size, nodes_label_size +  edges_label_size, nodes_label_size,
                    nodes_label_size + 2 * edges_label_size, 0
            };
        } return bit_set_size;
    }

    // SPECULATE ROW
    public BitSet speculate_row(BitSet origin_row, int last_idx) {
        BitSet new_row = new BitSet(getStart_directed_pos()[last_idx]);
        for (int i = 0; i < last_idx; i++) {
            final int shift = getStart_reverse_pos()[i];
            origin_row.get(getStart_directed_pos()[i], getStart_directed_pos()[i + 1])
                      .stream().forEach(index -> new_row.set(shift + index));
        } return new_row;
    }

    // GETTER
    public Table                    getTable() {return table;                }
    public ArrayList<BitSet>    getBitmatrix() {return bitmatrix;            }
    public IntIndex getBitmatrix_id_indexing() {return bitmatrix_id_indexing;}
    public int[]       getStart_directed_pos() {return start_directed_pos;   }
    public int[]        getStart_reverse_pos() {return start_reverse_pos;    }

    // SETTER
    public void setBitmatrix_id_indexing(IntIndex bitmatrix_id_indexing) {
        this.bitmatrix_id_indexing = bitmatrix_id_indexing;
    }

    // SET THE EDGE COLOR IN BITSET
    public void set_edge_color(BitSet bitSet, int type, int offset_1, int offset_2){
        if (type == 0) return;
        // OUT EDGE
        if (type > 0 )
            bitSet.set(offset_1 + offset_2 + type -1);
        // IN EDGES
        else
            bitSet.set(offset_1 + (-1) * type -1);
        // NOTE: -1 because the type edge start from 1
    }

    // ADD SRC-DST-ROW INTO THE TABLE
    // QUERY VERSION
    public void add_src_dst_row(int src, int dst, BitSet row) {
        int bitset_id = add_bitset_if_not_exist(row);
        // src - dst - bitset_id association
        ((IntColumn) table.column("src"   )).append(src);
        ((IntColumn) table.column("dst"   )).append(dst);
        ((IntColumn) table.column("btx_id")).append(bitset_id);
    }

    // TARGET VERSION
    public void add_src_dst_row(int src, int dst, BitSet row, int last_idx) {
        int bitset_id = add_bitset_if_not_exist(row, last_idx);
        // src - dst - bitset_id association
        ((IntColumn) table.column("src"   )).append(src);
        ((IntColumn) table.column("dst"   )).append(dst);
        ((IntColumn) table.column("btx_id")).append(bitset_id);
    }

    public void add_src_dst_row(int src, int dst, ArrayList<BitSet> rows) {
        for (BitSet row: rows)
            add_src_dst_row(src, dst, row);
    }

    // ABSTRACT METHODS
    // 1. FOR TARGET
    public abstract void create_bitset(
       NewEdgeAggregation edge_aggregation, NodesEdgesLabelsMaps labels_map,
       HashMap<String, GraphMacroNode> macro_nodes,
       Int2ObjectOpenHashMap<String> nodes_macro
    );

    // 1.b
    public abstract void create_bitset(
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_colors,
        NodesEdgesLabelsMaps labels_map,
        HashMap<String, GraphMacroNode> macro_nodes,
        Int2ObjectOpenHashMap<String> nodes_macro
    );

    // 2. FOR QUERY
    public abstract void create_bitset(
        QueryStructure query,
        NodesEdgesLabelsMaps labels_map
    );
}
