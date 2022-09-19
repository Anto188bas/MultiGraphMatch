import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import matching.controllers.MatchingPath;
import matching.models.OutData;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class TestPaths {

    public static void main(String[] args) {
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // PATH
        String root_dir  = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/TestPaths";


        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edges_tables_properties = FileManager.files_reading(netw_path + "/edges", ',');

        // NODE ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String> nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables.clone(),"labels", idx_label, macro_nodes, level_nodeId, nodes_macro
        );
        // FIXME: I added ".clone()" to nodes_tables because it seems that "graph_macro_node_creation" changes the tables. (RG)

        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);


        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(src_dst_aggregation, idx_label, macro_nodes, nodes_macro);

        System.out.println(idx_label.getLabelToIdxNode().keySet());
        System.out.println(idx_label.getLabelToIdxEdge().keySet());

        // QUERY
        String query_test           = "MATCH (n1:P)<-[:F*2..2]-(n0:P), (n0:P) -[:C]-> (n2:P) RETURN n0,n1,n2";

        QueryStructure query_obj = new QueryStructure();
        query_obj.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.empty());

        OutData outData = new OutData();

        MatchingPath matchingMachine = new MatchingPath(outData, query_obj,true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, Optional.empty());
        outData = matchingMachine.matching();
    }
}
