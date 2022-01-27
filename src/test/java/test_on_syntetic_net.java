import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import configuration.Configuration;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.controllers.MatchingProcedure;
import ordering.EdgeOrdering;
import reading.FileManager;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.edges.EdgeHandler;
import target_graph.edges.NewEdgeAggregation;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.io.IOException;
import java.util.*;


public class test_on_syntetic_net {
    public static void main(String[] args) throws IOException {
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
        NewEdgeAggregation graphEdge = new NewEdgeAggregation();
        EdgeHandler.createGraphEdge(edges_tables_properties, idx_label, graphEdge);

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(graphEdge, idx_label, macro_nodes, nodes_macro);

        // QUERIES READING
        System.out.println(idx_label.getIdxToLabelEdge());
        List<String> queries = FileManager.query_reading(configuration);
        queries.forEach(query -> {
            QueryStructure query_obj = new QueryStructure();
            query_obj.parser(query, idx_label);
            MatchingProcedure.matching(true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, query_obj, graphEdge);
        });

    }
}
