package target_graph.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;


public class GraphPaths {
    // src | dst | key
    private final Table map_pair_to_key;
    // [key][color] => {edges}
    private final IntArrayList[][] map_key_to_edge_list;

    private final int num_pairs;
    private final int num_edge_colors;

    private final IntIndex src_index;
    private final IntIndex dst_index;

    public GraphPaths(Table map_pair_to_key, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list, int num_edge_colors) {
        this.num_pairs = map_pair_to_key.rowCount();
        this.num_edge_colors = num_edge_colors;
        this.map_pair_to_key = map_pair_to_key;
        this.map_key_to_edge_list = new IntArrayList[this.num_pairs][this.num_edge_colors];

        for(int i = 0; i < this.num_pairs; i++) {
            for(int j = 0; j < this.num_edge_colors; j++) {
                if(tmp_map_key_to_edge_list.get(i).containsKey(j)) {
                    map_key_to_edge_list[i][j] = tmp_map_key_to_edge_list.get(i).get(j).clone();
                } else {
                    map_key_to_edge_list[i][j] = new IntArrayList();
                }
            }
        }

        this.src_index = new IntIndex(this.map_pair_to_key.intColumn("src"));
        this.dst_index = new IntIndex(this.map_pair_to_key.intColumn("dst"));
    }

    public Table getMap_pair_to_key() {
        return map_pair_to_key;
    }

    public IntArrayList[][] getMap_key_to_edge_list() {
        return map_key_to_edge_list;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("map_pair_to_key\n");
        str.append(this.map_pair_to_key.toString() + '\n');
        str.append("map_key_to_edge_list\n");
        for(int i = 0; i < num_pairs; i++) {
            str.append("KEY: " + i + '\n');
            for (int j = 0; j < num_edge_colors; j++) {
                str.append("\tCOLOR: " + j + " => " + map_key_to_edge_list[i][j] + '\n');
            }
        }
        return str.toString();
    }
}
