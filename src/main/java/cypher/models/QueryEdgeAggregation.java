package cypher.models;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.LinkedList;

public class QueryEdgeAggregation {
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_edges;
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> out_edges;
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> in_out_edges;

    public QueryEdgeAggregation(){
        in_edges     = new Int2ObjectOpenHashMap<>();
        out_edges    = new Int2ObjectOpenHashMap<>();
        in_out_edges = new Int2ObjectOpenHashMap<>();
    }

    // ADD NODES-EDGE ASSOCIATION INTO THE CONSIDERED STRUCTURE
    private void put_nodes_edge_association(
        int node_1, int node_2, int edge_id,
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> associations
    ){
        if (!associations.containsKey(node_1))
            associations.put(node_1, new Int2ObjectOpenHashMap<>());
        Int2ObjectOpenHashMap<IntArrayList> dst_list = associations.get(node_1);
        if (!dst_list.containsKey(node_2))
            dst_list.put(node_2, new IntArrayList());
        dst_list.get(node_2).add(edge_id);
    }

    // IN/OUT/BOTH HANDLER
    private void put_nodes_edge_association_based_on_dir(int node_1, int node_2, int edge_id, String dir){
        switch (dir) {
            case "OUTGOING" -> {
                put_nodes_edge_association(node_1, node_2, edge_id, out_edges);
                put_nodes_edge_association(node_2, node_1, edge_id, in_edges );
            }
            case "INCOMING" -> {
                put_nodes_edge_association(node_1, node_2, edge_id, in_edges );
                put_nodes_edge_association(node_2, node_1, edge_id, out_edges);
            }
            default -> {
                put_nodes_edge_association(node_1, node_2, edge_id, in_out_edges);
                put_nodes_edge_association(node_2, node_1, edge_id, in_out_edges);
            }
        }
    }

    // CREATE EDGE AGGREGATION
    public void create_aggregation(
        LinkedList<Integer>              nodes_ids,
        LinkedList<Integer>              edges_ids,
        Int2ObjectOpenHashMap<QueryEdge> query_edges
    ){
        for (int i = 0; i<edges_ids.size(); i++){
            int edg_id       = edges_ids.get(i);
            QueryEdge edge   = query_edges.get(edg_id);
            String edge_type = edge.getDirection();
            put_nodes_edge_association_based_on_dir(
               nodes_ids.get(i), nodes_ids.get(i+1),
               edg_id, edge_type
            );
        }
    }

    // GETTER
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getIn_edges()     {return in_edges;    }
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getOut_edges()    {return out_edges;   }
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getIn_out_edges() {return in_out_edges;}

    // IS FUNCTION
    public boolean  isIn(int node1, int node2) {return  in_edges.get(node1).containsKey(node2);}
    public boolean isOut(int node1, int node2) {return out_edges.get(node1).containsKey(node2);}
    public boolean isRev(int node1, int node2) {return in_out_edges.get(node1).containsKey(node2);}

    // GET NEIGHBOURS OF A NODES
    public IntArrayList get_node_neighbours(int node_1, int total_nodes){
        IntArrayList vNei = new IntArrayList();
        for (int node_2 = 0; node_2 < total_nodes; node_2++)
            if (isOut(node_1, node_2) || isIn(node_1, node_2) || isRev(node_1, node_2))
                vNei.add(node_2);
        return vNei;
    }

    // IN AND OUT AGGREGATION
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> aggregate_edge() {
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> aggregation = new Int2ObjectOpenHashMap<>();
        // ITERATION OVER SRC NODEs
        out_edges.int2ObjectEntrySet().fastForEach(src_record -> {
             int src = src_record.getIntKey();
             // ITERATION OVER DST NODEs
             src_record.getValue().int2ObjectEntrySet().fastForEach(dst_record -> {
                 int dst            = dst_record.getIntKey();
                 IntArrayList edges = dst_record.getValue();
                 // DST AND SRC ARE IN AGGREGATION. BUT DST IS A SOURCE NODE INSTEAD SRC A DESTINATION ONE. SO
                 // THESE EDGES WILL BE INCOMING ONES (-1)
                 if (aggregation.containsKey(dst) && aggregation.get(dst).containsKey(src))
                    aggregation.get(dst).get(src).put(-1, edges);
                 // SRC IS A SOURCE NODE, WHILE DST A DESTINATION ONE
                 else {
                     if (!aggregation.containsKey(src))
                         aggregation.put(src, new Int2ObjectOpenHashMap<>());
                     Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> src_dst_list = aggregation.get(src);
                     // WE ALREADY AGGREGATE ALL OUT EDGES FROM A NODE TO ANOTHER. THEREFORE, IF DST NOT IN LIST
                     // WE PUT BOTH DST AND THE ASSOCIATED EDGES
                     if (!src_dst_list.containsKey(dst)) {
                         Int2ObjectOpenHashMap<IntArrayList> dir_edge = new Int2ObjectOpenHashMap<>();
                         dir_edge.put(1, edges);
                         src_dst_list.put(dst, dir_edge);
                     }
                 }
             });
        });
        return aggregation;
    }
}
