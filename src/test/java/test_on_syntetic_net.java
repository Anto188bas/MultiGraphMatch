import bitmatrix.models.TargetBitmatrix;
import configuration.Configuration;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import matching.controllers.NewMatching;
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
import java.util.*;
import java.util.concurrent.*;


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
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);


        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(src_dst_aggregation, idx_label, macro_nodes, nodes_macro);


        // QUERIES READING
        List<String> queries = FileManager.query_reading(configuration);
        final Duration tout  = Duration.ofSeconds(configuration.timeout);
        queries.forEach(query -> {
            System.out.println(query);
            ExecutorService exec = Executors.newSingleThreadExecutor();
            final Future<Double> handler = exec.submit(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    QueryStructure query_obj = new QueryStructure();
                    query_obj.parser(query, idx_label);

                    // MATCHING
                    OutData outData = NewMatching.matching(
                        true, false, Long.MAX_VALUE, idx_label, target_bitmatrix,
                        query_obj, graphPaths, macro_nodes, nodes_macro
                    );

                    // SAVING
                    if(configuration.out_file != null) {
                        try {
                            FileManager.saveIntoCSV(query, configuration.out_file, outData);
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
