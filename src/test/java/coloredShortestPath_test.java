import ColoredShortestPath.DijkstraWithTreeSet;
import com.google.common.graph.*;
import com.google.gson.Gson;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class coloredShortestPath_test {
    public static void main(String[] args) {
        // CONFIGURATION
        Configuration configuration     = new Configuration(args);
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');

        // NODE ELABORATION
        HashMap<String, GraphMacroNode>          macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String>            nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables,"type", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);


        //loading the graph in the new graph structure using the old reading system
        MutableValueGraph<Integer, Integer> graph =  ValueGraphBuilder.directed().build();


        //it can be bypassed, used only for the labeling system
        for(int i = 0; i<nodes_macro.size(); i++)  //vertex generation
            graph.addNode(i);

        for(int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if(test != null) {
                var arr = test.keySet().toArray();
                for(var j: arr)
                    graph.putEdgeValue(i, (Integer) j, (int) src_dst_aggregation.get(i).get(j)[0].toArray()[0]);
            }
        }


        System.out.println(graph.nodes());


        //testing
        findAndPrintShortestPath(graph, 254,431, 1);
        findAndPrintShortestPath(graph, 254, 431,2);
        findAndPrintShortestPath(graph, 254, 431,3);
        findAndPrintShortestPath(graph, 254, 431,4);
        findAndPrintShortestPath(graph, 254, 431,5);
        findAndPrintShortestPath(graph, 254, 431,6);
        findAndPrintShortestPath(graph, 254, 431,7);
        findAndPrintShortestPath(graph, 254, 431,8);
        findAndPrintShortestPath(graph, 254, 431,9);
    }

    private static void findAndPrintShortestPath( ValueGraph<Integer, Integer> graph, Integer source, Integer target, int edgeColor) {
        Gson gson = new Gson();
        List<Integer> shortestPath = DijkstraWithTreeSet.findShortestPath(graph, source, target, edgeColor);
        System.out.println(gson.toJson(shortestPath));
        System.out.printf("shortestPath from %s to %s of color %s = %s %n", source, target, edgeColor, shortestPath);
    }

}
