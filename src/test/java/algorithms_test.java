import algorithms.*;
import configuration.Configuration;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class algorithms_test {
    public static void main(String[] args) throws IOException {

        // CONFIGURATION
        Configuration configuration     = new Configuration(args);
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');


        // NODE ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String> nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables,"type", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);



        //create a test graph reading from the hashmap
        Graph<Integer, RelationshipEdge> testGraph = new SimpleDirectedGraph<>(RelationshipEdge.class);

        for(int i = 0; i<nodes_macro.size(); i++)  //vertex generation
            testGraph.addVertex(i);

        //id = id_vertex -> get -> label
        testGraph.vertexSet().stream()                                              //vertex label printing
                .forEach(i -> System.out.println(i+": "+nodes_macro.get(i)));


        testGraph.addEdge(0, 1, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(1, 3, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(0, 1, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(1, 3, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(3, 2, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(2, 0, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(4, 3, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(54, 3, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(123, 542, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(3,0, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(654, 756, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(123, 234, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(6532, 115, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(7345, 3655, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(342, 456, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(887, 432, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(777, 666, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(5345, 4234, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(5353, 334, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(4345, 5424, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(5, 5435, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(889, 242, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(5345, 43, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(54, 43, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(65, 2352, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(0, 8896, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(4, 66, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(5, 6457, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(54, 5436, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(657, 654, new RelationshipEdge("FRIEND"));
        testGraph.addEdge(444, 55, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(5445, 444, new RelationshipEdge("RELATIVE"));
        testGraph.addEdge(6623, 333, new RelationshipEdge("ACQUAINTANCE"));
        testGraph.addEdge(2356, 2345, new RelationshipEdge("FRIEND"));


        //algorithms implementation testing
        Algorithms a = new Algorithms(testGraph);

        a.DijsktraShortestPath(0,3);
        a.DijsktraAllShortestPath(0);
        a.BellmanFordShortestPath(0,3);
        a.BellmanFordAllShortestPath(0);
        a.FloydWarshallShortestPath();

        a.EigenVectorCentrality();
        a.BetweennessCentrality();
        a.ClosenessCentrality();
        a.PageRankCentrality();
        a.KatzCentrality();
        a.ClusteringCoefficient();
        a.AverageClusteringCoefficient();

        a.LabelPropagationClustering();
        a.KSpanningTreeClustering(3);

        a.PreferentialAttachmentPrediction(0,1);
        a.CommonNeighborsPrediction(0,1);
        a.JaccardCoefficientPrediction(0,1);



    /*
        System.out.println(nodes_macro.get(0));  //label of vertex 0
        System.out.println(nodes_macro.get(1));


        var map = src_dst_aggregation.get(5673);
        System.out.println(src_dst_aggregation.get(5673).int2ObjectEntrySet());  //key -> src = ritorna la lista delle destination


        // TODO read edge and edge labels

        System.out.println(Arrays.toString(map.get(7029)));
        System.out.println(Arrays.toString(map.get(9997)));
        System.out.println(Arrays.toString(map.get(6496)));
    */

    }
}
