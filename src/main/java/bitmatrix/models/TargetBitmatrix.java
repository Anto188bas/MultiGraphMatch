package bitmatrix.models;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import target_graph.graph.GraphPaths;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;

import java.util.BitSet;


public class TargetBitmatrix extends BitMatrix {
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> reversedTable; // btx_id -> src -> {dst, ...}
    // CONSTRUCTOR
    public TargetBitmatrix() {
        super();
        reversedTable = new Int2ObjectOpenHashMap<>();
    }

    // BITMATRIX EDGES SETTING
    private void edge_part_configuration(BitSet bit_mtx_row, int[] outColors, int[] inColors, int offset_1, int offset_2) {
        // OUT EDGE
        if (outColors != null) for (int color : outColors) bit_mtx_row.set(offset_1 + offset_2 + color);
        // IN EDGES
        if (inColors != null) for (int color : inColors) bit_mtx_row.set(offset_1 + color);
    }
    @Override
    public int add_src_dst_singleRow(int src, int dst, BitSet row) {
        int bitset_id = super.add_src_dst_singleRow(src, dst, row);

        Int2ObjectOpenHashMap<IntArrayList> bitsetIdMap;
        if (reversedTable.containsKey(bitset_id)) {
            bitsetIdMap = reversedTable.get(bitset_id);
        } else {
            bitsetIdMap = new Int2ObjectOpenHashMap<>();
            reversedTable.put(bitset_id, bitsetIdMap);
        }
        IntArrayList srcList;
        if(bitsetIdMap.containsKey(dst)) {
            srcList = bitsetIdMap.get(dst);
        } else {
            srcList = new IntArrayList();
            bitsetIdMap.put(dst, srcList);
        }
        srcList.add(src);

        return bitset_id;
    }


    // NEW SOLUTION (TABLE)
    public void createBitset(GraphPaths graphPaths, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager) {
        int numOfDifferentNodesLabels = nodesLabelsManager.getMapIntLabelToStringLabel().size();
        int numOfDifferentEdgesLabels = edgesLabelsManager.getMapIntLabelToStringLabel().size();
        int bitSetSize = setStartDirectedPosition(numOfDifferentNodesLabels, numOfDifferentEdgesLabels, true);

        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> mapKeyToEdgeList =  graphPaths.getMap_key_to_edge_list();

        mapKeyToEdgeList.int2ObjectEntrySet().forEach(entry -> {
            int n1 = entry.getIntKey();
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> n1Map = entry.getValue();

            n1Map.int2ObjectEntrySet().forEach(n1Entry -> {
                int n2 = n1Entry.getIntKey();

                // To avoid duplicates
                if((n1 < n2) || ( n1 > n2 && (!mapKeyToEdgeList.containsKey(n2) || (mapKeyToEdgeList.containsKey(n2) && !mapKeyToEdgeList.get(n2).containsKey(n1))))) {
                    int[] outColors = n1Entry.getValue().keySet().toIntArray();

                    int[] inColors = null;
                    if(mapKeyToEdgeList.containsKey(n2) && mapKeyToEdgeList.get(n2).containsKey(n1)) {
                        inColors = mapKeyToEdgeList.get(n2).get(n1).keySet().toIntArray();
                    }
                    // BIT SET
                    BitSet bit_mtx_row = new BitSet(bitSetSize);
                    // SRC LABELS CONFIGURATION.    0 TO len(NODE_LABELS) - 1
                    node_part_configuration(bit_mtx_row, nodesLabelsManager.getMapElementIdToLabelSet().get(n1), 0);
                    // DST LABELS CONFIGURATION
                    node_part_configuration(bit_mtx_row, nodesLabelsManager.getMapElementIdToLabelSet().get(n2), super.getStart_directed_pos()[3]);
                    // EDGE PART CONFIGURATION
                    edge_part_configuration(bit_mtx_row, outColors, inColors, numOfDifferentNodesLabels, numOfDifferentEdgesLabels);
                    // SRC-DST-ROW ASSOCIATION
                    add_src_dst_singleRow(n1, n2, bit_mtx_row);
                    add_src_dst_singleRow(n2, n1, super.getSpeculateRow(bit_mtx_row));
                }
            });
        });
    }

    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getReversedTable() {
        return reversedTable;
    }
}
