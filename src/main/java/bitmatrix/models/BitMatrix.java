package bitmatrix.models;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;


public abstract class BitMatrix {
    private final Table table;

    // TODO NEW (IT SHOULD BE REPLACE table)

    private final ArrayList<BitSet> bitmatrix;
    private IntIndex bitmatrix_id_indexing;
    private int[] start_directed_pos;
    private int[] start_reverse_pos;

    public BitMatrix() {
        IntColumn src_col = IntColumn.create("src");
        IntColumn dst_col = IntColumn.create("dst");
        IntColumn bit_mtx_id = IntColumn.create("btx_id");
        table = Table.create(src_col, dst_col, bit_mtx_id);
        bitmatrix = new ArrayList<>();
    }

    // BITMATRIX NODES SETTING
    public void node_part_configuration(BitSet bit_mtx_row, int[] labels, int offset) {
        Arrays.stream(labels).forEach(label -> {
            if (label != -1) bit_mtx_row.set(offset + label);
        });
    }

    // ADD BITSET IF NOT EXIST
    // QUERY VERSION
    public int add_bitset_if_not_exist(BitSet record) {
        int i = 0;
        for (; i < this.bitmatrix.size(); i++) {
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    public int add_bitset_if_not_exist(BitSet record, int last_idx) {
        int i = 0;
        for (; i < this.bitmatrix.size(); i++) {
            BitSet sel_bitset = bitmatrix.get(i);
            if (sel_bitset.equals(record)) return i;
            BitSet reverse = speculate_row(record, last_idx);
            if (sel_bitset.equals(reverse)) return i;
        }
        bitmatrix.add(record);
        return i;
    }

    // CONFIGURE START DIRECTED POSITION VECTOR
    public int setStartDirectedPosition(int numOfDifferentNodesLabels, int numOfDifferentEdgesLabels, boolean isDirected) {
        int bitSetSize = 2 * numOfDifferentNodesLabels + (isDirected ? 2 : 3) * numOfDifferentEdgesLabels;

        // TYPE -> 0 UNDIRECTED, 1 -> DIRECTED
        if (isDirected) {
            //  SRC       IN                         OUT                                  DST
            start_directed_pos = new int[]{0, numOfDifferentNodesLabels, numOfDifferentNodesLabels + numOfDifferentEdgesLabels, numOfDifferentNodesLabels + 2 * numOfDifferentEdgesLabels, bitSetSize};
            this.start_reverse_pos = new int[]{numOfDifferentNodesLabels + 2 * numOfDifferentEdgesLabels, numOfDifferentNodesLabels + numOfDifferentEdgesLabels, numOfDifferentNodesLabels, 0};
        } else {
            //  SRC       IN                         OUT                                 IN/OUT
            this.start_directed_pos = new int[]{0, numOfDifferentNodesLabels, numOfDifferentNodesLabels + numOfDifferentEdgesLabels, numOfDifferentNodesLabels + 2 * numOfDifferentEdgesLabels, numOfDifferentNodesLabels + 3 * numOfDifferentEdgesLabels, bitSetSize};
            this.start_reverse_pos = new int[]{numOfDifferentNodesLabels + 3 * numOfDifferentEdgesLabels, numOfDifferentNodesLabels + numOfDifferentEdgesLabels, numOfDifferentNodesLabels, numOfDifferentNodesLabels + 2 * numOfDifferentEdgesLabels, 0};
        }
        return bitSetSize;
    }

    // SPECULATE ROW
    public BitSet speculate_row(BitSet origin_row, int last_idx) {
        BitSet new_row = new BitSet(getStart_directed_pos()[last_idx]);
        for (int i = 0; i < last_idx; i++) {
            final int shift = getStart_reverse_pos()[i];
            origin_row.get(getStart_directed_pos()[i], getStart_directed_pos()[i + 1]).stream().forEach(index -> new_row.set(shift + index));
        }
        return new_row;
    }

    // GETTER
    public Table getTable() {
        return table;
    }

    public ArrayList<BitSet> getBitmatrix() {
        return bitmatrix;
    }

    public IntIndex getBitmatrix_id_indexing() {
        return bitmatrix_id_indexing;
    }

    public int[] getStart_directed_pos() {
        return start_directed_pos;
    }

    public int[] getStart_reverse_pos() {
        return start_reverse_pos;
    }

    // SETTER
    public void setBitmatrix_id_indexing(IntIndex bitmatrix_id_indexing) {
        this.bitmatrix_id_indexing = bitmatrix_id_indexing;
    }


    // ADD SRC-DST-ROW INTO THE TABLE
    // QUERY VERSION
    public void add_src_dst_row(int src, int dst, BitSet row) {
        int bitset_id = add_bitset_if_not_exist(row);
        // src - dst - bitset_id association
        ((IntColumn) table.column("src")).append(src);
        ((IntColumn) table.column("dst")).append(dst);
        ((IntColumn) table.column("btx_id")).append(bitset_id);
    }

    // TARGET VERSION
    public void add_src_dst_row(int src, int dst, ArrayList<BitSet> rows) {
        for (BitSet row : rows)
            add_src_dst_row(src, dst, row);
    }
}
