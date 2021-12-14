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
                put_nodes_edge_association(node_2, node_1, edge_id, in_edges);
            }
            case "INCOMING" -> {
                put_nodes_edge_association(node_1, node_2, edge_id, in_edges);
                put_nodes_edge_association(node_2, node_1, edge_id, out_edges);
            }
            default -> put_nodes_edge_association(node_1, node_2, edge_id, in_out_edges);
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

    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getIn_edges()     {return in_edges;    }
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getOut_edges()    {return out_edges;   }
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> getIn_out_edges() {return in_out_edges;}
}
