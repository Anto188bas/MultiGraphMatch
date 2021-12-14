package target_graph.edges;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import javax.swing.plaf.PanelUI;

public class NewEdgeAggregation extends ParentAggregation{
    //                 SRC id as key       DST id as key         type as key          edges id
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> aggregateEdges;

    // CONSTRUCTOR
    public NewEdgeAggregation() {
        aggregateEdges = new Int2ObjectOpenHashMap<>();
    }

    // ADD SRC - DST ASSOCIATIONS
    private void add_type(Int2ObjectOpenHashMap<IntArrayList> types_edges, int dir, int type, int edge) {
        int new_type = dir * type;
        if(!types_edges.containsKey(new_type))
           types_edges.put(new_type, new IntArrayList());
        types_edges.get(new_type).add(edge);
    }

    public void put(int src, int dst, int type, int edge) {
        // CHECK IF DST IS A SOURCE NODE AND SRC A DESTINATION ONE
        if (aggregateEdges.containsKey(dst) && aggregateEdges.get(dst).containsKey(src)){
            add_type(aggregateEdges.get(dst).get(src), -1, type, edge);
            return;
        }
        // THERE ARE NO DST-SRC ASSOCIATIONS
        if (!aggregateEdges.containsKey(src))
            aggregateEdges.put(src, new Int2ObjectOpenHashMap<>());
        if (!aggregateEdges.get(src).containsKey(dst))
            aggregateEdges.get(src).put(dst, new Int2ObjectOpenHashMap<>());
        add_type(aggregateEdges.get(src).get(dst), 1, type, edge);
    }

    // GETTER
    // 1. WHOLE ASSOCIATIONS
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> getAggregateEdges() {
        return aggregateEdges;
    }

    // 2. SRC ASSOCIATIONS
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getSrcAssociations(int src) {
        return aggregateEdges.get(src);
    }

    // 3. SRC - DST ASSOCIATIONS
    public Int2ObjectOpenHashMap<IntArrayList> getSrcDstAssociations(int src, int dst) {
        return aggregateEdges.get(src).get(dst);
    }

    // 4. SRC - DST - Type
    public IntArrayList getSrcDstTypeAssociations(int src, int dst, int type) {
        return aggregateEdges.get(src).get(dst).get(type);
    }

    // TOTAL NUMBER OF ASSOCIATIONS
}
