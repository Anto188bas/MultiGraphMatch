package algorithms;

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
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * This class translate the original graph structure to the formats used in the algorithms package
 *
 */
@SuppressWarnings("UnstableApiUsage")

public class UtilityGraph {
    private Graph<Integer, RelationshipEdge> jGraph;
    private MutableValueGraph<Integer, Integer> vGraph;

    private final int nPairs;
    private final int nEdgeColors;

    final private Int2ObjectOpenHashMap<String> nodes_macro;
    final private Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation;

    public UtilityGraph(String[] args) {
        Configuration configuration     = new Configuration(args);
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();
        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');


        HashMap<String, GraphMacroNode> macro_nodes  = new HashMap<>();

        nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables,"type", idx_label, macro_nodes, level_nodeId, nodes_macro
        );
        src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);
        assert graphPaths != null;
        nPairs = graphPaths.getNum_pairs();
        nEdgeColors = graphPaths.getNum_edge_colors();

        generateJGraph();
        generateVGraph();
    }


    private void generateJGraph(){
        jGraph = new SimpleDirectedGraph<>(RelationshipEdge.class);

        for(int i = 0; i<nodes_macro.size(); i++)  //vertex generation
            jGraph.addVertex(i);

        //edge generation
        for(int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if(test != null) {
                var arr = test.keySet().toArray();
                for(var j: arr)
                    //jGraph.addEdge(i, (Integer) j, new RelationshipEdge(src_dst_aggregation.get(i).get(j)[0]));
                    jGraph.addEdge(i, (int) j, new RelationshipEdge(test.get((int) j)[0]));
            }
        }
        System.out.println("...JGraph Generated...");
    }

    private void generateVGraph(){
        vGraph = ValueGraphBuilder.directed().build();

        for(int i=0;i<nodes_macro.size();i++)
            vGraph.addNode(i);

        for (int i = 0; i < nodes_macro.size(); i++) {
            var test = src_dst_aggregation.get(i);
            if (test != null) {
                var arr =  test.keySet().toArray();
                for (var j : arr)
                   //vGraph.putEdgeValue(i, (Integer) j, (int) src_dst_aggregation.get(i).get(j)[0].toArray()[0]);
                    vGraph.putEdgeValue(i, (int) j, (int) test.get((int) j)[0].toArray()[0]);
            }
        }
        System.out.println("...VGraph Generated...");
    }

    public Graph<Integer, RelationshipEdge> getJGraph() {
        return jGraph;
    }

    public MutableValueGraph<Integer, Integer> getVGraph() {
        return vGraph;
    }

    public int getNPairs() { return nPairs; }
    public int getNEdgeColors() {
        return nEdgeColors;
    }

    public HashMap<Integer, String> getNodeLabels(){
        HashMap<Integer, String> labels = new HashMap<>();
        for(int i=0;i<nodes_macro.size();i++)
            labels.put(i,nodes_macro.get(i));
        return  labels;
    }




}
