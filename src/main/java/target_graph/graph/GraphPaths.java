package target_graph.graph;

import it.unimi.dsi.fastutil.ints.*;
import org.javatuples.Triplet;

import java.util.ArrayList;


public class GraphPaths {
    // src => dst => key
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_pair_to_key;
    // [key][color] => {edges}
    private final IntArrayList[][] map_key_to_edge_list;
    private final int num_pairs;
    private final int num_edge_colors;

    public GraphPaths(
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_pair_to_key,
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list,
        int num_edge_colors,
        int num_pairs
    ) {
        this.num_pairs            = num_pairs;
        this.num_edge_colors      = num_edge_colors;
        this.map_pair_to_key      = map_pair_to_key;
        this.map_key_to_edge_list = new IntArrayList[this.num_pairs][this.num_edge_colors];

        // TABLE POPULATION
        for(int i = 0; i < this.num_pairs; i++) {
            for(int j = 0; j < this.num_edge_colors; j++) {
                if(tmp_map_key_to_edge_list.get(i).containsKey(j))
                    map_key_to_edge_list[i][j] = tmp_map_key_to_edge_list.get(i).get(j).clone();
                else
                    map_key_to_edge_list[i][j] = new IntArrayList();
            }
        }
    }

    // GETTER
    public Int2ObjectOpenHashMap<Int2IntOpenHashMap>    getMap_pair_to_key()      {return map_pair_to_key;}
    public IntArrayList[][]                             getMap_key_to_edge_list() {return map_key_to_edge_list;}

    public ArrayList<Triplet<Integer, Integer, Integer>> getBySRCandDSTs(int src, IntArrayList dsts) {
        ArrayList<Triplet<Integer, Integer, Integer>> result = new ArrayList<>();
        Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
        for (int dst: dsts)
            result.add(new Triplet<>(src, dst, src_map.get(dst)));
        return result;
    }

    public ArrayList<Triplet<Integer, Integer, Integer>> getByDSTandSRCs(int dst, IntArrayList srcs) {
        ArrayList<Triplet<Integer, Integer, Integer>> result = new ArrayList<>();
        for (int src: srcs) {
            Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
            result.add(new Triplet<>(src, dst, src_map.get(dst)));
        }
        return result;
    }


    public Triplet<Integer, Integer, Integer> getBySRCandDST (int src, int dst) {
        Triplet<Integer, Integer, Integer> result = null;
        if(this.map_pair_to_key.containsKey(src)) {
            Int2IntOpenHashMap src_map = this.map_pair_to_key.get(src);
            if(src_map.containsKey(dst)) {
                result = new Triplet<>(src, dst, src_map.get(dst));
            }
        }
        return result;
    }


    // PRINTING
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("map_pair_to_key\n");
        str.append(this.map_pair_to_key.toString()).append('\n');
        str.append("map_key_to_edge_list\n");
        for(int i = 0; i < num_pairs; i++) {
            str.append("KEY: ").append(i).append('\n');
            for (int j = 0; j < num_edge_colors; j++) {
                str.append("\tCOLOR: ").append(j).append(" => ").append(map_key_to_edge_list[i][j]).append('\n');
            }
        }
        return str.toString();
    }
}
