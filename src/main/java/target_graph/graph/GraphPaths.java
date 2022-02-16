package target_graph.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;
import tech.tablesaw.selection.BitmapBackedSelection;
import tech.tablesaw.selection.Selection;


public class GraphPaths {
    // src | dst | key
    private final Table map_pair_to_key;
    // [key][color] => {edges}
    private final IntArrayList[][] map_key_to_edge_list;
    private final int num_pairs;
    private final int num_edge_colors;
    // INDEXING
    private final IntIndex src_index;
    private final IntIndex dst_index;

    public GraphPaths(
        Table map_pair_to_key,
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list,
        int num_edge_colors
    ) {
        this.num_pairs            = map_pair_to_key.rowCount();
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
        // INDEXING CREATION
        this.src_index = new IntIndex(this.map_pair_to_key.intColumn("src"));
        this.dst_index = new IntIndex(this.map_pair_to_key.intColumn("dst"));
    }

    // in SEARCHING DEFINITION
    private Selection in(IntColumn column_values, IntIndex index) {
        Selection selection  = new BitmapBackedSelection();
        IntArrayList values  = new IntArrayList();
        column_values.forEach(value -> {
            Selection sel = index.get(value);
            IntIterator sel_iter = sel.iterator();
            while (sel_iter.hasNext())
                values.add(sel_iter.nextInt());
        });
        IntListIterator var2 = values.iterator();
        while(var2.hasNext())
            selection.add(var2.nextInt());
        return selection;
    }

    private Selection inSRC(IntColumn column_values) {return in(column_values, this.src_index);}
    private Selection inDST(IntColumn column_values) {return in(column_values, this.dst_index);}


    // GETTER
    public Table            getMap_pair_to_key()      {return map_pair_to_key;}
    public IntArrayList[][] getMap_key_to_edge_list() {
        return map_key_to_edge_list;
    }
    // USING INDEX ON SINGLE COLUMN
    public Table            getBySRC(int value)       {return map_pair_to_key.where(src_index.get(value));}
    public Table            getByDST(int value)       {return map_pair_to_key.where(dst_index.get(value));}
    // USING INDEX ON BOTH COLUMN
    public Table            getBySRCandDSTs(int src, IntColumn dsts) {return map_pair_to_key.where(src_index.get(src).and(inDST(dsts)));}
    public Table            getByDSTandSRCs(int dst, IntColumn srcs) {return map_pair_to_key.where(src_index.get(dst).and(inDST(srcs)));}
    public Table            getBySRCandDST (int src, int dst)        {return map_pair_to_key.where(src_index.get(src).and(dst_index.get(dst)));}

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
