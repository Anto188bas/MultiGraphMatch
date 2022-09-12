import configuration.Configuration;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.util.ArrayList;
import java.util.HashMap;

public class reactome_test {
    public static void main(String[] args){
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();
        Configuration configuration     = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, '\t');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, '\t');
        System.out.println("NODES DIRECTORY: " + configuration.nodes_main_directory);
        System.out.println("EDGES DIRECTORY: " + configuration.edges_main_directory);

        System.out.println("Elaborating nodes...");

        // NODES ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String> nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables.clone(),"labels", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        System.out.println("Elaborating edges...");
        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);

        System.out.println("Done!");
    }
}
