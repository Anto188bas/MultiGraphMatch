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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class MainClass {
    public static void main(String[] args) throws IOException{
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();
        Configuration configuration     = new Configuration(args);

//        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt")), true));


        // PATH
        System.out.println("Reading target graph...");

        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');
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

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(src_dst_aggregation, idx_label, macro_nodes, nodes_macro);

        System.out.println(idx_label.getLabelToIdxNode().keySet());
        System.out.println(idx_label.getLabelToIdxEdge().keySet());

        // QUERIES READING
        List<String> queries = FileManager.query_reading(configuration);
        final Duration tout  = Duration.ofSeconds(configuration.timeout);
        queries.forEach(query_test -> {
            System.out.println(query_test);
            ExecutorService exec = Executors.newSingleThreadExecutor();
            final Future<Double> handler = exec.submit(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    WhereConditionExtraction where_managing = new WhereConditionExtraction();
                    where_managing.where_condition_extraction(query_test);
                    where_managing.normal_form_computing();
                    where_managing.buildSetWhereConditions();

                    QueryStructure query = new QueryStructure();
                    query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                    OutData outData = new OutData();

                    MatchingWhere matchingMachine = new MatchingWhere(outData, query,true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, Optional.of(where_managing));
                    outData = matchingMachine.matching();

                    // SAVING
                    if(configuration.out_file != null) {
                        try {
                            FileManager.saveIntoCSV(query_test, configuration.out_file, outData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return outData.domain_time + outData.matching_time + outData.ordering_time + outData.symmetry_time;
                }
            });
            try {handler.get(tout.getSeconds(), TimeUnit.SECONDS);}
            catch (Exception e) {
                handler.cancel(true);
                e.printStackTrace();
                System.err.println("timeout");
            }

            try {
                exec.shutdownNow();
                boolean res = exec.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {e.printStackTrace();}
        });

        System.exit(0);
    }
}
