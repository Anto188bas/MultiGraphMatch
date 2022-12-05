package target_graph.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;


public class GraphPaths {
    // src => dst => color => {edges}
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> map_key_to_edge_list;
    private final int num_pairs;
    private final int num_edge_colors;
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees;

    public GraphPaths() {
        this.map_key_to_edge_list = null;
        this.num_pairs = 0;
        this.num_edge_colors = 0;
        this.map_node_color_degrees = null;
    }

    public GraphPaths(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>>  map_key_to_edge_list, int num_edge_colors, int num_pairs, Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees) {
        this.num_pairs = num_pairs;
        this.num_edge_colors = num_edge_colors;
        this.map_key_to_edge_list = map_key_to_edge_list;
        this.map_node_color_degrees = map_node_color_degrees;
    }

    // GETTERS
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>>  getMap_key_to_edge_list() {
        return map_key_to_edge_list;
    }

    public Int2ObjectOpenHashMap<Int2IntOpenHashMap> getMap_node_color_degrees() {
        return map_node_color_degrees;
    }

    public ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> getBySRCandDSTs(int src, IntArrayList dsts) {
        ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> result = new ArrayList<>();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> src_map = this.map_key_to_edge_list.get(src);
        for (int dst : dsts) {
            if(src_map.containsKey(dst)) {
                result.add(new Triplet<>(src, dst, src_map.get(dst)));
            }
        }
        return result;
    }

    public ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> getByDSTandSRCs(int dst, IntArrayList srcs) {
        ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> result = new ArrayList<>();
        for (int src : srcs) {
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> src_map = this.map_key_to_edge_list.get(src);
            if(src_map.containsKey(dst)) {
                result.add(new Triplet<>(src, dst, src_map.get(dst)));
            }
        }
        return result;
    }


    public Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>> getBySRCandDST(int src, int dst) {
        Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>> result = null;
        if (this.map_key_to_edge_list.containsKey(src)) {
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> src_map = this.map_key_to_edge_list.get(src);
            if (src_map.containsKey(dst)) {
                result = new Triplet<>(src, dst, src_map.get(dst));
            }
        }
        return result;
    }

    /**
     * Given a source node, this method returns map <dst, {edge_id1, edge_id2, ...}> such that:
     * - dst is an out-neighbor of src;
     * - edge_id is the id of the edge between src and dst.
     * This method does not consider the edge color.
     *
     * @param src source node.
     */
    public Int2ObjectOpenHashMap<IntArraySet> getAdiacsBySrc(int src) {
        Int2ObjectOpenHashMap<IntArraySet> result = new Int2ObjectOpenHashMap<>();
        if (map_key_to_edge_list.containsKey(src)) {
            map_key_to_edge_list.get(src).forEach((dst, mapColorToEdges) -> {
                IntArraySet adiacs = new IntArraySet();
                for(IntArrayList edges: mapColorToEdges.values()) { // We don't consider the color
                    adiacs.addAll(edges);
                }

                result.put(dst.intValue(), adiacs);
            });
        }

        return result;
    }

    /**
     * Given a destination node, this method returns a map <src, {edge_id1, edge_id2, ...}> such that:
     * - src is an in-neighbor of dst;
     * - edge_id is the id of the edge between src and dst.
     * This method does not consider the edge color.
     *
     * @param dst destination node.
     */
    public Int2ObjectOpenHashMap<IntArraySet> getAdiacsByDst(int dst) {
        Int2ObjectOpenHashMap<IntArraySet> result = new Int2ObjectOpenHashMap<>();

        map_key_to_edge_list.forEach((src, outNeighbours) -> {
            IntArraySet adiacs = new IntArraySet();
            if (outNeighbours.containsKey(dst)) {
                Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = outNeighbours.get(dst);
                for(IntArrayList edges: mapColorToEdges.values()) { // We don't consider the color
                    adiacs.addAll(edges);
                }
            }
            result.put(src.intValue(), adiacs);
        });

        return result;
    }

    /**
     * Given a source node, this method returns a <dst, {edge_id1, edge_id2, ...}> such that:
     * - dst is an out-neighbor of src;
     * - the color of the edge is contained is colors.
     * - edge_id is the id of the edge between src and dst.
     *
     * @param src    source node.
     * @param colors list of colors.
     */
    public Int2ObjectOpenHashMap<IntArraySet> getAdiacsBySrcAndColors(int src, IntArrayList colors) {
        Int2ObjectOpenHashMap<IntArraySet> result = new Int2ObjectOpenHashMap<>();

        if (map_key_to_edge_list.containsKey(src)) {
            map_key_to_edge_list.get(src).forEach((dst, mapColorToEdges) -> {
                IntArraySet adiacs = new IntArraySet();
                for (int color : colors) {
                    if(mapColorToEdges.get(color) != null) {
                        adiacs.addAll(mapColorToEdges.get(color));
                    }
                }

                result.put(dst.intValue(), adiacs);
            });
        }

        return result;
    }

    /**
     * Given a destination node, this method returns <src, {edge_id1, edge_id2, ...}> such that:
     * - src is an in-neighbor of dst;
     * - the color of the edge is contained is colors.
     * - edge_id is the id of the edge between src and dst.
     *
     * @param dst    destination node.
     * @param colors list of colors.
     */
    public Int2ObjectOpenHashMap<IntArraySet> getAdiacsByDstAndColors(int dst, IntArrayList colors) {
        Int2ObjectOpenHashMap<IntArraySet> result = new Int2ObjectOpenHashMap<>();

        map_key_to_edge_list.forEach((src, outNeighbours) -> {
            IntArraySet adiacs = new IntArraySet();

            if (outNeighbours.containsKey(dst)) {
                Int2ObjectOpenHashMap<IntArrayList> mapColorToEdges = outNeighbours.get(dst);
                for (int color : colors) {
                    if(mapColorToEdges.get(color) != null) {
                        adiacs.addAll(mapColorToEdges.get(color));
                    }
                }
                result.put(src.intValue(), adiacs);
            }
        });

        return result;
    }


    // PRINTING
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("map_key_to_edge_list\n");
        for(int src: map_key_to_edge_list.keySet()) {
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> dstMap = map_key_to_edge_list.get(src);
            for(int dst: dstMap.keySet()) {
                str.append("( " + src + ", " + dst + " ) ->");
                Int2ObjectOpenHashMap<IntArrayList> colorMap = dstMap.get(dst);
                for(int color: colorMap.keySet()) {
                    str.append(" c" + color + " -> " + colorMap.get(color) + ", ");
                }
                str.append("\n");
            }
        }
        return str.toString();
    }
}
