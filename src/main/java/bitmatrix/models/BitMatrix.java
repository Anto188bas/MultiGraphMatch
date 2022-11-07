package bitmatrix.models;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.IntColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;


public abstract class BitMatrix {
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> table; // btx_id -> src -> {dst, ...}


    private final ArrayList<BitSet> bitmatrix;
    private int[] start_directed_pos;
    private int[] start_reverse_pos;

    public BitMatrix() {
        table = new Int2ObjectOpenHashMap<>();
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
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getTable() {
        return table;
    }

    public ArrayList<BitSet> getBitmatrix() {
        return bitmatrix;
    }

    public int[] getStart_directed_pos() {
        return start_directed_pos;
    }

    public int[] getStart_reverse_pos() {
        return start_reverse_pos;
    }


    // ADD SRC-DST-ROW INTO THE TABLE
    // TARGET VERSION
    public void add_src_dst_singleRow(int src, int dst, BitSet row) {
        int bitset_id = add_bitset_if_not_exist(row);
        // src - dst - bitset_id association
        Int2ObjectOpenHashMap<IntArrayList> bitsetIdMap;
        if (table.containsKey(bitset_id)) {
            bitsetIdMap = table.get(bitset_id);
        } else {
            bitsetIdMap = new Int2ObjectOpenHashMap<>();
            table.put(bitset_id, bitsetIdMap);
        }
        IntArrayList dstList;
        if(bitsetIdMap.containsKey(src)) {
            dstList = bitsetIdMap.get(src);
        } else {
            dstList = new IntArrayList();
            bitsetIdMap.put(src, dstList);
        }
        dstList.add(dst);
    }

    // QUERY VERSION
    public void add_src_dst_multipleRows(int src, int dst, ArrayList<BitSet> rows) {
        for (BitSet row : rows)
            add_src_dst_singleRow(src, dst, row);
    }
}
