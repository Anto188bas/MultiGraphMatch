package bitmatrix.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
    public int add_bitset_if_not_exist(BitSet record){
        int i = 0;
        for (; i < this.bitmatrix.size(); i++){
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    // GETTER
    public Table             getTable()        {return table;                }
    public ArrayList<BitSet> getBitmatrix()    {return bitmatrix;            }
    public IntIndex getBitmatrix_id_indexing() {return bitmatrix_id_indexing;}

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
    public void add_src_dst_row(int src, int dst, BitSet row) {
        int bitset_id = add_bitset_if_not_exist(row);
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
    public abstract void create_bitset(
       NewEdgeAggregation edge_aggregation, NodesEdgesLabelsMaps labels_map,
       HashMap<String, GraphMacroNode> macro_nodes,
       Int2ObjectOpenHashMap<String> nodes_macro
    );

    public abstract void create_bitset(
        QueryStructure query,
        NodesEdgesLabelsMaps labels_map
    );


}
