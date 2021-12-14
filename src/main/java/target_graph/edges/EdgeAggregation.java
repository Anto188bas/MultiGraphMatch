package target_graph.edges;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EdgeAggregation extends ParentAggregation {
    // src -> dst -> InOutEdges
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<InOutEdges>> aggregateEdges;

    // CONSTRUCTOR
    public EdgeAggregation(){
        aggregateEdges = new Int2ObjectOpenHashMap<>();
    }

    // PUT METHODS
    public void put(int src, int dst, int type, int edge_id){
        // First, we verify if there is a pair (dst, src)
        if(aggregateEdges.containsKey(dst) && aggregateEdges.get(dst).containsKey(src)){
            aggregateEdges.get(dst).get(src).add_in_edge_id(type, edge_id);
            return;
        }
        if(!aggregateEdges.containsKey(src))
            aggregateEdges.put(src, new Int2ObjectOpenHashMap<>());
        Int2ObjectOpenHashMap<InOutEdges> src_dst_list = aggregateEdges.get(src);
        if(!src_dst_list.containsKey(dst))
            src_dst_list.put(dst, new InOutEdges());
        InOutEdges src_dst = src_dst_list.get(dst);
        src_dst.add_out_edge_id(type, edge_id);
    }

    // GET
    // GET SRC-DST TYPES
    public IntSet get_out_src_dst_types(int src, int dst){
        return aggregateEdges.get(src).get(dst).get_out_edges_type();
    }
    public IntSet get_in_src_dst_types(int src, int dst){
        return aggregateEdges.get(src).get(dst).get_in_edges_type();
    }

    // GET SRC-DST EDGES
    // OUT
    public Int2ObjectOpenHashMap<IntArrayList> get_out_src_dst_edges(int src, int dst){
        return aggregateEdges.get(src).get(dst).get_out_edges_type_id();
    }
    // IN
    public Int2ObjectOpenHashMap<IntArrayList> get_in_src_dst_edges(int src, int dst){
        return aggregateEdges.get(src).get(dst).get_in_edges_type_id();
    }

    // GET SRC-DST EDGES BASED ON A SPECIFIED TYPE
    // OUT
    public IntArrayList get_out_src_dst_type_edges(int src, int dst, int type){
        return aggregateEdges.get(src).get(dst).get_out_type_edges(type);
    }
    // IN
    public IntArrayList get_in_src_dst_type_edges(int src, int dst, int type){
        return aggregateEdges.get(src).get(dst).get_in_type_edges(type);
    }

    // GET ALL EDGES
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<InOutEdges>> getAggregateEdges() {
        return aggregateEdges;
    }


    // PRINTING
    public void print_out_edges(){
        aggregateEdges.forEach((src, dst_lst) ->
            dst_lst.forEach((dst, in_out_edges) -> {
                System.out.println("src: " + src + ", dst: " + dst);
                in_out_edges.get_out_edges_type_id().forEach((type, edges_id)->
                    System.out.println("\ttype: " + type + ", edges: " + edges_id)
                );
            })
        );
    }

    // TO STRING


    @Override
    public String toString() {
        return "EdgeAggregation{\n" +
                "aggregateEdges="   + aggregateEdges + "\n" +
                '}';
    }

    // INNER CLASS
    private static class InOutEdges{
        // out: type -> [eo_id_1,..., eo_id_n]
        // in : type -> [ei_id_1,..., ei_id_n]
        private final Int2ObjectOpenHashMap<IntArrayList> out_type_edges;
        private final Int2ObjectOpenHashMap<IntArrayList> in_type_edges;

        // CONSTRUCTOR
        private InOutEdges(){
            out_type_edges = new Int2ObjectOpenHashMap<>();
            in_type_edges  = new Int2ObjectOpenHashMap<>();
        }

        // METHODS
        // 1. ADD EDGES ID
        private void add_edge_id(Int2ObjectOpenHashMap<IntArrayList> edges, int type, int edge_id){
            if(!edges.containsKey(type))
                edges.put(type, new IntArrayList());
            edges.get(type).add(edge_id);
        }
        // OUT
        private void add_out_edge_id(int type, int edge_id){add_edge_id(out_type_edges, type, edge_id);}
        // IN
        private void add_in_edge_id(int type, int edge_id) {add_edge_id(in_type_edges, type, edge_id);}

        // 2. GET EDGES TYPE
        // OUT
        private IntSet get_out_edges_type(){return out_type_edges.keySet();}
        // IN
        private IntSet get_in_edges_type() {return in_type_edges.keySet(); }

        // 3. GET EDGES TYPES AND IDS
        // OUT
        private Int2ObjectOpenHashMap<IntArrayList> get_out_edges_type_id(){return out_type_edges;}
        // IN
        private Int2ObjectOpenHashMap<IntArrayList> get_in_edges_type_id() {return in_type_edges;}

        // 4. GET EDGES IDS RELATED TO A SPECIFIED TYPE
        // OUT
        private IntArrayList get_out_type_edges(int type){return out_type_edges.get(type);}
        // IN
        private IntArrayList get_in_type_edges(int type) {return in_type_edges.get(type);}

        @Override
        public String toString() {
            return "InOutEdges{\n"       +
                    "out_type_edges="    + out_type_edges + "\n" +
                    ", in_type_edges="   + in_type_edges  + "\n" +
                    '}';
        }
    }
}
