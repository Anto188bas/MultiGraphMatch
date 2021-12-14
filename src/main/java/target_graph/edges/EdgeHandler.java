package target_graph.edges;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
}
