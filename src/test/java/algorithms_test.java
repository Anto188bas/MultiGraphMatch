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

import static java.lang.Thread.MAX_PRIORITY;

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


        //adding edge
        /*
        for(int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if(test != null) {
                int finalI = i;
                test.keySet().stream().forEach(j -> testGraph.addEdge(finalI, j, new RelationshipEdge("A")));
            }
        }
        */

        for(int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if(test != null) {
                var arr = test.keySet().toArray();
                for(var j: arr)
                    testGraph.addEdge(i, (Integer) j, new RelationshipEdge("A"));
            }
        }

        System.out.println(testGraph.edgeSet());


        //Multithreading algorithm testing
        Runnable runnableShortestPath =
                () -> {
                    Algorithms a = new Algorithms(testGraph);
                    try {
                        a.DijsktraShortestPath(4532,5632);
                        a.DijsktraAllShortestPath(0);
                        a.BellmanFordShortestPath(0,3);
                        a.BellmanFordAllShortestPath(0);
                        //a.FloydWarshallShortestPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };


        Runnable runnableCentrality =
                () -> {
                    Algorithms a = new Algorithms(testGraph);
                    try {
                        a.EigenVectorCentrality();
                        a.BetweennessCentrality();
                        a.ClosenessCentrality();
                        a.PageRankCentrality();
                        a.KatzCentrality();
                        a.ClusteringCoefficient();
                        a.AverageClusteringCoefficient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableClustering =
                () -> {
                    Algorithms a = new Algorithms(testGraph);
                    try {
                        a.LabelPropagationClustering();
                        a.KSpanningTreeClustering(3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableLinkPrediction =
                () -> {
                    Algorithms a = new Algorithms(testGraph);
                    try {
                        a.PreferentialAttachmentPrediction(0,1);
                        a.CommonNeighborsPrediction(0,1);
                        a.JaccardCoefficientPrediction(0,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Thread threadShortestPath = new Thread(runnableShortestPath);
        Thread threadCentrality = new Thread(runnableCentrality);
        Thread threadClustering = new Thread(runnableClustering);
        Thread threadLinkPrediction = new Thread(runnableLinkPrediction);
        threadShortestPath.setPriority(MAX_PRIORITY);

        threadShortestPath.start();
        threadCentrality.start();
        threadClustering.start();
        threadLinkPrediction.start();

        /*
        //algorithms implementation testing
        Algorithms a = new Algorithms(testGraph);

        //a.DijsktraShortestPath(4532,5632);
        //a.DijsktraAllShortestPath(0);
        //a.BellmanFordShortestPath(0,3);
        //a.BellmanFordAllShortestPath(0);
        //a.FloydWarshallShortestPath();

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
        */
        // TODO read edge labels




    }
}
