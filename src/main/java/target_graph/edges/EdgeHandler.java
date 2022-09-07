package target_graph.edges;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.opencypher.v9_0.expressions.In;
import target_graph.graph.GraphPaths;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.StringIndex;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.tablesaw.aggregate.AggregateFunctions.count;
import static tech.tablesaw.aggregate.AggregateFunctions.mean;


public class EdgeHandler {
    // ADD EDGE WITHOUT TYPE
    private static void addEdge(Int2ObjectOpenHashMap<Edge> edges, int src, int dst, int id){
        edges.put(id, new Edge(src, dst));
    }
    // ADD EDGE WITH TYPE
    private static void addEdge(Int2ObjectOpenHashMap<Edge> edges, int src, int dst, int id, String type, NodesEdgesLabelsMaps idx_label){
        int type_int = idx_label.createEdgeLabelIdx(type);
        edges.put(id, new Edge(src, dst, type_int));
    }

    // CHECK IF TYPE IS SPECIFIED
    private static int isTypeSpecified(List<String> col_names){
        for(int i=0; i<col_names.size(); i++)
            if(col_names.get(i).toLowerCase().contains("type"))
                return i;
        return -1;
    }

    // STR 2 INT
    private static int str2int(Row row, String col_name){
        return Integer.parseInt(row.getString(col_name));
    }

    // ADD EDGE IN LIST (OLD SOLUTION)
    private static void add_edge_in_list(
            Row                  row,
            List<String>         header,
            String               type_str,
            ParentAggregation    edges,
            NodesEdgesLabelsMaps idx_label,
            AtomicInteger        count,
            IntColumn            edgeId
    ){
        // TODO make a string - id association
        int src  = str2int(row, header.get(0));
        int dst  = str2int(row, header.get(1));
        int type = idx_label.createEdgeLabelIdx(type_str);
        edges.put(src, dst, type, count.getAndIncrement());
        edgeId.append(count.get());
    }

    // ADD COLOR FOR AGGREGATION (IT IS USED ONLY FOR BITMATRIX)
    private static void add_color_for_aggregation(
        int src, int dst, int type,
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation
    ) {
        if(src_dst_aggregation.containsKey(dst) && src_dst_aggregation.get(dst).containsKey(src)){
            IntOpenHashSet[] dir_colors = src_dst_aggregation.get(dst).get(src);
            if (dir_colors[1] == null) dir_colors[1] = new IntOpenHashSet();
            if(type != -1) dir_colors[1].add(type);
        }
        else {
            if (!src_dst_aggregation.containsKey(src))
                src_dst_aggregation.put(src, new Int2ObjectOpenHashMap<>());
            Int2ObjectOpenHashMap<IntOpenHashSet[]> dsts_colors = src_dst_aggregation.get(src);
            if (!dsts_colors.containsKey(dst)) {
                dsts_colors.put(dst, new IntOpenHashSet[2]);
                dsts_colors.get(dst)[0] = new IntOpenHashSet();
            }
            dsts_colors.get(dst)[0].add(type);

        }
    }

    //
    private static void node_color_degrees_init(Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees, int node){
        if(map_node_color_degrees.containsKey(node)) return;
        map_node_color_degrees.put(node, new Int2IntOpenHashMap());
    }

    private static void node_color_degrees_increase(
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees, int node, int color
    ){
        Int2IntOpenHashMap color_degrees = map_node_color_degrees.get(node);
        if(color_degrees.containsKey(color))
           color_degrees.replace(color, color_degrees.get(color) + 1);
        else
            color_degrees.put(color, 1);
    }

    // ADD EDGE IN LIST (NEW SOLUTION)
    private static void new_add_edge_in_list(
            Row                                                             row,
            List<String>                                                    header,
            String                                                          type_str,
            NodesEdgesLabelsMaps                                            idx_label,
            AtomicInteger                                                   count,
            IntColumn                                                       edgeId,
            Int2ObjectOpenHashMap<Int2IntOpenHashMap>                       map_pair_to_key,
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>      map_key_to_edge_list,
            AtomicInteger                                                   key_count,
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>>  src_dst_aggregation,
            Int2ObjectOpenHashMap<Int2IntOpenHashMap>                       map_node_color_degrees
    ){
        int src  = str2int(row, header.get(0));
        int dst  = str2int(row, header.get(1));
        int type = idx_label.createEdgeLabelIdx(type_str); // COLORE
        int edge_key = count.getAndIncrement();
        edgeId.append(edge_key);
        add_color_for_aggregation(src, dst, type, src_dst_aggregation);
        int key;
        if(!map_pair_to_key.containsKey(src))
            map_pair_to_key.put(src, new Int2IntOpenHashMap());

        //node-color-degrees configuration
        node_color_degrees_init(map_node_color_degrees, src);
        node_color_degrees_init(map_node_color_degrees, dst);

        if(!map_pair_to_key.get(src).containsKey(dst)) {
            key = key_count.getAndIncrement();
            map_pair_to_key.get(src).put(dst, key);
            map_key_to_edge_list.put(key, new Int2ObjectOpenHashMap<>());
        } else {
            key = map_pair_to_key.get(src).get(dst);
        }

        Int2ObjectOpenHashMap<IntArrayList> map_color_to_edge_list = map_key_to_edge_list.get(key);
        if(!map_color_to_edge_list.containsKey(type))
            map_color_to_edge_list.put(type, new IntArrayList());
        map_color_to_edge_list.get(type).add(edge_key);

        node_color_degrees_increase(map_node_color_degrees, src, type);
        node_color_degrees_increase(map_node_color_degrees, dst, type);
    }

    public static GraphPaths createGraphPaths(
            Table[] tables,
            NodesEdgesLabelsMaps idx_label,
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation
    ){
        if(tables == null) return null;
        int num_of_source = Arrays
            .stream(tables)
            .map(table -> table.column(0).unique().size())
            .reduce(0, Integer::sum);
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> tmp_map_pair_to_key = new Int2ObjectOpenHashMap<>(num_of_source);

        AtomicInteger edge_id_count   = new AtomicInteger(0);
        AtomicInteger pair_key_count  = new AtomicInteger(0);
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees = new Int2ObjectOpenHashMap<>();
        
        for(Table table: tables){
            List<String>  header  = table.columnNames();
            // TYPE SPECIFIED
            IntColumn     edgeId  = IntColumn.create("edge_id");
            int           idx     = isTypeSpecified(header);

            if (idx != -1){
                table.forEach(row ->
                    new_add_edge_in_list(
                        row, header, row.getString(header.get(idx)), idx_label,
                        edge_id_count, edgeId, tmp_map_pair_to_key, tmp_map_key_to_edge_list,
                        pair_key_count, src_dst_aggregation, map_node_color_degrees
                    )
                );
                table.removeColumns(header.get(0), header.get(1), header.get(2));
            }
            // MISSING TYPE
            else {
                table.forEach(row ->
                    new_add_edge_in_list(
                       row, header, "none", idx_label, edge_id_count,
                       edgeId, tmp_map_pair_to_key, tmp_map_key_to_edge_list,
                       pair_key_count, src_dst_aggregation, map_node_color_degrees
                    )
                );
                table.removeColumns(header.get(0), header.get(1));
            }
            table.addColumns(edgeId);
        }
        return new GraphPaths(
             tmp_map_pair_to_key, tmp_map_key_to_edge_list,
             idx_label.getLabelToIdxEdge().size(), pair_key_count.get(),
             map_node_color_degrees
        );
    }
}
