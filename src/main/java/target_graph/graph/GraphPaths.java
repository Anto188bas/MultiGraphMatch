package target_graph.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;


public class GraphPaths {
    // src => dst => key
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_pair_to_key;
    // [key][color] => {edges}
    private final IntArrayList[][] map_key_to_edge_list;
    private final int num_pairs;
    private final int num_edge_colors;
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees;

    public GraphPaths() {
        this.map_pair_to_key = new Int2ObjectOpenHashMap<>();
        this.map_key_to_edge_list = new IntArrayList[0][0];
        this.num_pairs = 0;
        this.num_edge_colors = 0;
        this.map_node_color_degrees = new Int2ObjectOpenHashMap<>();
    }

    public GraphPaths(Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_pair_to_key, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list, int num_edge_colors, int num_pairs, Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees) {
        this.num_pairs = num_pairs;
        this.num_edge_colors = num_edge_colors;
        this.map_pair_to_key = map_pair_to_key;
        this.map_key_to_edge_list = new IntArrayList[this.num_pairs][this.num_edge_colors];
        this.map_node_color_degrees = map_node_color_degrees;


        // TABLE POPULATION
        for (int i = 0; i < this.num_pairs; i++) {
            for (int j = 0; j < this.num_edge_colors; j++) {
                if (tmp_map_key_to_edge_list.get(i).containsKey(j))
                    map_key_to_edge_list[i][j] = tmp_map_key_to_edge_list.get(i).get(j).clone();
                else map_key_to_edge_list[i][j] = new IntArrayList();
            }
        }
    }

    // GETTER
    public Int2ObjectOpenHashMap<Int2IntOpenHashMap> getMap_pair_to_key() {
        return map_pair_to_key;
    }

    public IntArrayList[][] getMap_key_to_edge_list() {
        return map_key_to_edge_list;
    }

    public Int2ObjectOpenHashMap<Int2IntOpenHashMap> getMap_node_color_degrees() {
        return map_node_color_degrees;
    }

    public ArrayList<Triplet<Integer, Integer, Integer>> getBySRCandDSTs(int src, IntArrayList dsts) {
        ArrayList<Triplet<Integer, Integer, Integer>> result = new ArrayList<>();
        Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
        for (int dst : dsts)
            result.add(new Triplet<>(src, dst, src_map.get(dst)));
        return result;
    }

    public ArrayList<Triplet<Integer, Integer, Integer>> getByDSTandSRCs(int dst, IntArrayList srcs) {
        ArrayList<Triplet<Integer, Integer, Integer>> result = new ArrayList<>();
        for (int src : srcs) {
            Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
            result.add(new Triplet<>(src, dst, src_map.get(dst)));
        }
        return result;
    }


    public Triplet<Integer, Integer, Integer> getBySRCandDST(int src, int dst) {
        Triplet<Integer, Integer, Integer> result = null;
        if (this.map_pair_to_key.containsKey(src)) {
            Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
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
        if (map_pair_to_key.containsKey(src)) {
            map_pair_to_key.get(src).forEach((dst, pairKey) -> {
                IntArraySet adiacs = new IntArraySet();
                for (int color = 0; color < map_key_to_edge_list[pairKey.intValue()].length; color++) {
                    for (int edge : map_key_to_edge_list[pairKey.intValue()][color]) {
                        adiacs.add(edge);
                    }
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

        map_pair_to_key.forEach((src, outNeighbours) -> {
            IntArraySet adiacs = new IntArraySet();
            if (outNeighbours.containsKey(dst)) {
                int pairKey = outNeighbours.get(dst);
                for (int color = 0; color < map_key_to_edge_list[pairKey].length; color++) {
                    for (int edge : map_key_to_edge_list[pairKey][color]) {
                        adiacs.add(edge);
                    }
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

        if (map_pair_to_key.containsKey(src)) {
            map_pair_to_key.get(src).forEach((dst, pairKey) -> {
                IntArraySet adiacs = new IntArraySet();
                for (int color : colors) {
                    for (int edge : map_key_to_edge_list[pairKey.intValue()][color]) {
                        adiacs.add(edge);
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

        map_pair_to_key.forEach((src, outNeighbours) -> {
            IntArraySet adiacs = new IntArraySet();

            if (outNeighbours.containsKey(dst)) {
                int pairKey = outNeighbours.get(dst);
                for (int color : colors) {
                    for (int edge : map_key_to_edge_list[pairKey][color]) {
                        adiacs.add(edge);
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
        str.append("map_pair_to_key\n");
        str.append(this.map_pair_to_key.toString()).append('\n');
        str.append("map_key_to_edge_list\n");
        for (int i = 0; i < num_pairs; i++) {
            str.append("KEY: ").append(i).append('\n');
            for (int j = 0; j < num_edge_colors; j++) {
                str.append("\tCOLOR: ").append(j).append(" => ").append(map_key_to_edge_list[i][j]).append('\n');
            }
        }
        return str.toString();
    }
}
