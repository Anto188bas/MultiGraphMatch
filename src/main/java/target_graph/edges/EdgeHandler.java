package target_graph.edges;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import target_graph.graph.GraphPaths;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static boolean isTypeSpecified(String col_name){
        return col_name.toLowerCase().contains("type");
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
            if(type != -1)
               dir_colors[1].add(type);
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
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>>  src_dst_aggregation
    ){
        int src  = str2int(row, header.get(0));
        int dst  = str2int(row, header.get(1));
        int type = idx_label.createEdgeLabelIdx(type_str); // COLORE
        int edge_key = count.getAndIncrement();
        edgeId.append(edge_key);
        add_color_for_aggregation(src, dst, type, src_dst_aggregation);

        int key;

        if(!map_pair_to_key.containsKey(src)) {
            map_pair_to_key.put(src, new Int2IntOpenHashMap());
        }

        if(!map_pair_to_key.get(src).containsKey(dst)) {
            key = key_count.getAndIncrement();
            map_pair_to_key.get(src).put(dst, key);
            map_key_to_edge_list.put(key, new Int2ObjectOpenHashMap<>());
        } else {
            key = map_pair_to_key.get(src).get(dst);
        }

        Int2ObjectOpenHashMap<IntArrayList> map_color_to_edge_list = map_key_to_edge_list.get(key);

        if(!map_color_to_edge_list.containsKey(type)) {
            map_color_to_edge_list.put(type, new IntArrayList());
        }

        map_color_to_edge_list.get(type).add(edge_key);
    }

    public static void createGraphEdge(
            Table[] tables,
            NodesEdgesLabelsMaps idx_label,
            ParentAggregation src_dst_edges
    ){
        if(tables == null) return;
        AtomicInteger pCount      = new AtomicInteger(0);
        for(Table table: tables){
            List<String>  header  = table.columnNames();
            IntColumn     edgeId  = IntColumn.create("edge_id");
            // TYPE SPECIFIED
            if (isTypeSpecified(header.get(2))){
                table.forEach(row ->
                        add_edge_in_list(
                                row, header, row.getString(header.get(2)),
                                src_dst_edges, idx_label, pCount, edgeId)
                );
                table.removeColumns(header.get(0), header.get(1), header.get(2));
            }
            // MISSING TYPE
            else {
                table.forEach(row ->
                        add_edge_in_list(
                                row, header, "none",
                                src_dst_edges, idx_label, pCount, edgeId)
                );
                table.removeColumns(header.get(0), header.get(1));
            }
            table.addColumns(edgeId);
        }
    }

    public static GraphPaths createGraphPaths(
            Table[] tables,
            NodesEdgesLabelsMaps idx_label,
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation
    ){
        if(tables == null) return null;
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> tmp_map_key_to_edge_list = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> tmp_map_pair_to_key = new Int2ObjectOpenHashMap<>();

        AtomicInteger edge_id_count   = new AtomicInteger(0);
        AtomicInteger pair_key_count  = new AtomicInteger(0);

        for(Table table: tables){
            List<String>  header  = table.columnNames();
            // TYPE SPECIFIED
            IntColumn     edgeId  = IntColumn.create("edge_id");
            if (isTypeSpecified(header.get(2))){
                table.forEach(row ->
                    new_add_edge_in_list(
                        row, header, row.getString(header.get(2)), idx_label,
                        edge_id_count, edgeId, tmp_map_pair_to_key, tmp_map_key_to_edge_list,
                        pair_key_count, src_dst_aggregation
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
                       pair_key_count, src_dst_aggregation
                    )
                );
                table.removeColumns(header.get(0), header.get(1));
            }
            table.addColumns(edgeId);
        }
        return new GraphPaths(tmp_map_pair_to_key, tmp_map_key_to_edge_list,idx_label.getLabelToIdxEdge().size(), pair_key_count.get());
    }
}
