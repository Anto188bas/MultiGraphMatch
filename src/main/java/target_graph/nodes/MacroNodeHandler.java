package target_graph.nodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.StringIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MacroNodeHandler {
    private static int add_macro_node_id_to_level(
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId,
        int    level,
        String nodeId,
        int    max_deep_level
    ){
        if(!level_nodeId.containsKey(level)){
           level_nodeId.put(level, new ArrayList<>());
           max_deep_level = Math.max(max_deep_level, level);
        }
        level_nodeId.get(level).add(nodeId);
        return max_deep_level;
    }

    private static void create_node_id_macro_id_association(
         Int2ObjectOpenHashMap<String> node_id_macro_id,
         Table  table,
         String label_col_name
    ){
         table.selectColumns("id", label_col_name).forEach(row ->
             node_id_macro_id.put(row.getInt("id"), row.getString(label_col_name))
         );
    }

    public static int graph_macro_node_creation(
        Table[]                                  tables,
        String                                   col_2_index,
        NodesEdgesLabelsMaps idx_label,
        HashMap<String, GraphMacroNode>          macro_nodes,
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId,
        Int2ObjectOpenHashMap<String>            nodes_macro
    ){
        if(tables == null) return 0;
        AtomicInteger max_deep_level = new AtomicInteger(0);
        for(int i = 0; i < tables.length; i++){
            Table table              = tables[i];
            StringIndex labels_index = new StringIndex(table.stringColumn(col_2_index));
            final int pos = i;
            create_node_id_macro_id_association(nodes_macro, table, col_2_index);
            table.column(col_2_index).asSet().forEach(element -> {
                 String labels_str           = String.valueOf(element);
                 int[]  labels               = idx_label.stringVectorToIntOne(labels_str);
                 Table  sub_table            = table.
                    where(labels_index.get(labels_str)).
                    removeColumns(col_2_index);
                 NodesProperties nodes_props = new NodesProperties(sub_table);
                 if(macro_nodes.containsKey(labels_str))
                    macro_nodes.get(labels_str).set_properties_table(pos, nodes_props);
                 else{
                    GraphMacroNode  macro_node = new GraphMacroNode(
                          labels, tables.length,
                          pos, nodes_props
                    );
                    macro_nodes.put(labels_str, macro_node);
                    max_deep_level.set(
                       add_macro_node_id_to_level(
                          level_nodeId,
                          labels.length,
                          labels_str,
                          max_deep_level.get()
                       )
                    );
                 }
            });
            tables[i] = null;
        }
        return max_deep_level.get();
    }
}
