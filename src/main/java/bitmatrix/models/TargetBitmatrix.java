package bitmatrix.models;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;

import java.util.BitSet;


public class TargetBitmatrix extends BitMatrix {
    // CONSTRUCTOR
    public TargetBitmatrix() {
        super();
    }

    // BITMATRIX EDGES SETTING
    private void edge_part_configuration(BitSet bit_mtx_row, IntOpenHashSet[] dir_colors, int offset_1, int offset_2) {
        // OUT EDGE
        if (dir_colors[0] != null) for (int color : dir_colors[0]) bit_mtx_row.set(offset_1 + offset_2 + color);
        // IN EDGES
        if (dir_colors[1] != null) for (int color : dir_colors[1]) bit_mtx_row.set(offset_1 + color);
    }


    // NEW SOLUTION (TABLE)
    public void createBitset(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_colors, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager) {
        int numOfDifferentNodesLabels = nodesLabelsManager.getMapIntLabelToStringLabel().size();
        int numOfDifferentEdgesLabels = edgesLabelsManager.getMapIntLabelToStringLabel().size();
        int bitSetSize = setStartDirectedPosition(numOfDifferentNodesLabels, numOfDifferentEdgesLabels, false);

        src_dst_colors.int2ObjectEntrySet().fastForEach(src_dsts -> {
            // SELECTED SRC NODE
            int src = src_dsts.getIntKey();
            src_dsts.getValue().int2ObjectEntrySet().fastForEach(dst_colors -> {
                // SELECTED DST NODE
                int dst = dst_colors.getIntKey();
                BitSet bit_mtx_row = new BitSet(bitSetSize);
                // SRC LABELS CONFIGURATION.    0 TO len(NODE_LABELS) - 1
                node_part_configuration(bit_mtx_row, nodesLabelsManager.getMapElementIdToLabelSet().get(src), 0);
                // DST LABELS CONFIGURATION
                node_part_configuration(bit_mtx_row, nodesLabelsManager.getMapElementIdToLabelSet().get(dst), super.getStart_directed_pos()[3]);
                // EDGE PART CONFIGURATION
                edge_part_configuration(bit_mtx_row, dst_colors.getValue(), numOfDifferentNodesLabels, numOfDifferentEdgesLabels);
                // SRC-DST-ROW ASSOCIATION
                add_src_dst_singleRow(src_dsts.getIntKey(), dst_colors.getIntKey(), bit_mtx_row);
            });
        });
    }
}
