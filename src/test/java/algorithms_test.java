import ColoredShortestPath.ColorShortestPath;
import algorithms.Algorithms;
import algorithms.RelationshipEdge;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import configuration.Configuration;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.MAX_PRIORITY;

public class algorithms_test {
    public static void main(String[] args){

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



    /*******************************************************************************************************************************************/

         //NEW STRUCTURE
        int nPairs = graphPaths.getNum_pairs();
        int nEdgeColors = graphPaths.getNum_edge_colors();
        //System.out.println(nPairs);
        //System.out.println(nEdgeColors);

    /*
        var mat = graphPaths.getMap_key_to_edge_list();


        for(int i=0; i<nPairs; i++){
            System.out.println();
            for(int j=0; j<nEdgeColors; j++){
                System.out.println("Pair_id: "+i+", color_id: "+j+": "+mat[i][j]);
            }
        }

        //NEW STRUCTURE
        //old and new color corresponds

        System.out.println("Pair_id: "+994949+", color_id: "+0+", edge label: "+mat[994949][0]);
        System.out.println("Pair_id: "+994949+", color_id: "+1+", edge label: "+mat[994949][1]);
        System.out.println("Pair_id: "+994949+", color_id: "+2+", edge label: "+mat[994949][2]);
        System.out.println("Pair_id: "+994949+", color_id: "+3+", edge label: "+mat[994949][3]);
        System.out.println("Pair_id: "+994949+", color_id: "+4+", edge label: "+mat[994949][4]);
        System.out.println("Pair_id: "+994949+", color_id: "+5+", edge label: "+mat[994949][5]);
        System.out.println("Pair_id: "+994949+", color_id: "+6+", edge label: "+mat[994949][6]);
        System.out.println("Pair_id: "+994949+", color_id: "+7+", edge label: "+mat[994949][7]);
        System.out.println("Pair_id: "+994949+", color_id: "+8+", edge label: "+mat[994949][8]);
        System.out.println("Pair_id: "+994949+", color_id: "+9+", edge label: "+mat[994949][9]);

        int pair_id = graphPaths.getMap_pair_to_key().get(9984).get(9999);
        System.out.println("src: 9984, dst: 9999: pair_id: "+pair_id);
        */  //NEW STRUCTURES


        //OLD STRUCTURES ALGORITHMS
        Graph<Integer, RelationshipEdge> testGraph = new SimpleDirectedGraph<>(RelationshipEdge.class);

        for(int i = 0; i<nodes_macro.size(); i++)  //vertex generation
            testGraph.addVertex(i);

        //edge generation
        for(int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if(test != null) {
                var arr = test.keySet().toArray();
                for(var j: arr)
                    testGraph.addEdge(i, (Integer) j, new RelationshipEdge(src_dst_aggregation.get(i).get(j)[0]));
            }
        }

        testGraph.vertexSet().forEach(i -> System.out.println(i+": "+nodes_macro.get(i)));  //vertex label printing
        System.out.println(testGraph.edgeSet());


    /*****************************************************************************************************************************************/


        MutableValueGraph<Integer, Integer> graph = ValueGraphBuilder.directed().build();

        for (int i = 0; i < nodes_macro.size(); i++)  //vertex generation
            graph.addNode(i);

        for (int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if (test != null) {
                var arr = test.keySet().toArray();
                for (var j : arr)
                    graph.putEdgeValue(i, (Integer) j, (int) src_dst_aggregation.get(i).get(j)[0].toArray()[0]);
            }
        }

        //System.out.println(graph.nodes());
        //System.out.println(graph.edges());

    /*****************************************************************************************************************************************/



        //Multithreading algorithm testing
        Runnable runnableColoredShortestPath =
                () -> {
                    ColorShortestPath csp = new ColorShortestPath(graph, nEdgeColors);
                    try {
                        csp.ColoredShortestPath(254, 431, 2);
                        csp.AllColoredShortestPath(254, 431);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableShortestPath =
                () -> {
                    Algorithms a = new Algorithms(testGraph);
                    try {
                        a.DijsktraShortestPath(254,431);
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


        Thread threadColoredShortestPath = new Thread(runnableColoredShortestPath);
        Thread threadShortestPath = new Thread(runnableShortestPath);
        Thread threadCentrality = new Thread(runnableCentrality);
        Thread threadClustering = new Thread(runnableClustering);
        Thread threadLinkPrediction = new Thread(runnableLinkPrediction);
        threadShortestPath.setPriority(MAX_PRIORITY);

        threadColoredShortestPath.start();
        threadShortestPath.start();
        threadCentrality.start();
        threadClustering.start();
        threadLinkPrediction.start();

    }

}
