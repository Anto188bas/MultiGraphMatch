import configuration.Configuration;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import reading.FileManager;
import bitmatrix.models.TargetBitmatrix;
import target_graph.edges.EdgeHandler;
import target_graph.edges.NewEdgeAggregation;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.util.*;


public class MainClass {
    public static void main(String[] args){
        Configuration config                                  = new Configuration(args);
        Table[] nodes_tables                                  = FileManager.files_reading(config.nodes_main_directory, ',');
        Table[] edges_tables_properties                       = FileManager.files_reading(config.edges_main_directory, ',');
        NodesEdgesLabelsMaps idx_label                        = new NodesEdgesLabelsMaps();

        // NODES ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes           = new HashMap<>();
        Int2ObjectOpenHashMap<String>   nodes_macro           = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();

        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
            nodes_tables,"label", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        // EDGES ELABORATION
        NewEdgeAggregation graphEdge = new NewEdgeAggregation();
        EdgeHandler.createGraphEdge(edges_tables_properties, idx_label, graphEdge);
        System.out.println(idx_label.getIdxToLabelEdge());
        System.out.println(idx_label.getIdxToLabelNode());
        // graphEdge.print_out_edges();
        //System.out.println(GraphLayout.parseInstance(graphEdge).toFootprint());

        // BITMATRIX
        TargetBitmatrix bitmatrix = new TargetBitmatrix();
        bitmatrix.create_bitset(graphEdge, idx_label, macro_nodes, nodes_macro);


        // TESTING
        // String query = "MATCH (n:Label:Table:Graph {prop: 'value', age: 15, date:[10.4, 20.6]}) WHERE n.prop < 50 AND n.prop > 20 AND NOT n.prop = 40 RETURN n";
        // String query = "MATCH p=(n1:Label:Table:Graph {prop: 'value', age: 15, date:[10.4, 20.6]})-[r1:best|tmp]->(n2:Table)-[rf:introduction *2..3 {number_of_citation:4}]-(n3:Graph) WHERE (n1.prop = 10.4 AND n1.age <= 10) OR n1.age IS NOT NULL  RETURN n1.prop AS c1";

        // QueryStructure parsed_query = new QueryStructure();
        // parsed_query.parser(query, idx_label);
        // System.out.println(parsed_query.toString());
    }
}
