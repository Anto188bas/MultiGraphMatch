import bitmatrix.models.TargetBitmatrix;
import configuration.Configuration;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import matching.controllers.MatchingSimple;
import matching.controllers.MatchingWhere;
import matching.models.OutData;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class TestSimple {
    public static void main(String[] args) throws IOException {
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label = new NodesEdgesLabelsMaps();

        String[] queries = new String[]{
                "MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n1:P)-[r2:F]->(n2:C) WHERE n1.name = \"FILIPPO\" RETURN n0, n1, n2",
        };

        // PATH
        System.out.println("Reading target graph...");

        // PATH
        String root_dir = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/Test";


        // TARGET READING
        Table[] nodes_tables = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edges_tables_properties = FileManager.files_reading(netw_path + "/edges", ',');

        System.out.println("Elaborating nodes...");

        // NODES ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes = new HashMap<>();
        Int2ObjectOpenHashMap<String> nodes_macro = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables.clone(), "labels", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        System.out.println("Elaborating edges...");
        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(src_dst_aggregation, idx_label, macro_nodes, nodes_macro);

        System.out.println(idx_label.getLabelToIdxNode().keySet());
        System.out.println(idx_label.getLabelToIdxEdge().keySet());

        // QUERIES READING
        for (int i = 0; i < queries.length; i++) {
            String query_test = queries[i];
            System.out.println(query_test);

            WhereConditionExtraction where_managing = new WhereConditionExtraction();
            where_managing.where_condition_extraction(query_test);

            Optional<WhereConditionExtraction> optionalWhereConditionExtraction;

            if(where_managing.getWhere_string() != null) {
                where_managing.normal_form_computing();
                where_managing.buildSetWhereConditions();

                optionalWhereConditionExtraction = Optional.of(where_managing);
            } else {
                optionalWhereConditionExtraction = Optional.empty();
            }

            QueryStructure query = new QueryStructure();
            query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, optionalWhereConditionExtraction);

            OutData outData = new OutData();

            MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, optionalWhereConditionExtraction);
            outData = matchingMachine.matching();
        }

        System.exit(0);
    }
}
