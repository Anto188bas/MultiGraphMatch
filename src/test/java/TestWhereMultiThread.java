import bitmatrix.models.TargetBitmatrix;
import condition.QueryConditionType;
import configuration.Configuration;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.controllers.MatchingBase;
import matching.controllers.MatchingBaseTask;
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

public class TestWhereMultiThread {
    public static void main(String[] args) throws IOException {
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label = new NodesEdgesLabelsMaps();
        Configuration configuration = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodes_tables = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');

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

        // QUERIES READING
        List<String> queries = FileManager.query_reading(configuration);
        final Duration tout = Duration.ofSeconds(configuration.timeout);
        queries.forEach(query_test -> {

            System.out.println(query_test);

            ExecutorService exec = Executors.newSingleThreadExecutor();
            final Future<Double> handler = exec.submit(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    double totalTime;
                    long numOccurrences;

                    WhereConditionExtraction where_managing = new WhereConditionExtraction();
                    where_managing.where_condition_extraction(query_test);

                    if (where_managing.getWhere_string() != null) { // There are WHERE CONDITIONS
                        where_managing.normal_form_computing();
                        where_managing.buildSetWhereConditions();

                        Int2ObjectOpenHashMap<ObjectArrayList<QueryCondition>> mapOrPropositionToConditionSet = where_managing.getMapOrPropositionToConditionSet();

                        if (mapOrPropositionToConditionSet.size() > 0) { // Multi-Thread (at least one OR)
                            ObjectArrayList<ObjectArraySet<String>> sharedMemory = new ObjectArrayList<>();

                            ExecutorService pool = Executors.newCachedThreadPool();
                            ArrayList<Runnable> runnableArrayList = new ArrayList<>();

                            QueryStructure query = new QueryStructure();
                            query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                            for (int orIndex = 0; orIndex < mapOrPropositionToConditionSet.size(); orIndex++) {
                                QueryStructure query_t = new QueryStructure();
                                query_t.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                                ObjectArrayList<QueryCondition> simpleConditions = new ObjectArrayList<>();
                                ObjectArrayList<QueryCondition> complexConditions = new ObjectArrayList<>();

                                for (QueryCondition condition : mapOrPropositionToConditionSet.get(orIndex)) {
                                    if (condition.getType() == QueryConditionType.SIMPLE) {
                                        simpleConditions.add(condition);
                                    } else {
                                        complexConditions.add(condition);
                                    }
                                }

                                OutData outData = new OutData();
                                MatchingBase matchingMachine;
                                if (complexConditions.size() == 0) { // No complex conditions
                                    matchingMachine = new MatchingSimple(outData, query_t, false, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions);
                                } else { // Complex conditions
                                    matchingMachine = new MatchingWhere(outData, query_t, false, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions, complexConditions);
                                }

                                MatchingBaseTask matchingTask = new MatchingBaseTask(orIndex, sharedMemory, matchingMachine);
                                runnableArrayList.add(matchingTask);
                            }

                            double time = System.currentTimeMillis();
                            for(Runnable runnable: runnableArrayList) {
                                pool.execute(runnable);
                            }

                            pool.shutdown();
                            pool.awaitTermination(1800, TimeUnit.SECONDS);

                            // Union of the occurrences sets
                            ObjectArraySet<String> finalOccurrences = new ObjectArraySet<>();
                            for (ObjectArraySet<String> occurrences : sharedMemory) {
                                finalOccurrences.addAll(occurrences);
                            }

                            time = (System.currentTimeMillis() - time) / 1000;
                            totalTime = time;
                            numOccurrences = finalOccurrences.size();
                            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
                        } else { // Single-Thread (only AND)
                            QueryStructure query_t = new QueryStructure();
                            query_t.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                            int orIndex = 0;

                            ObjectArrayList<QueryCondition> simpleConditions = new ObjectArrayList<>();
                            ObjectArrayList<QueryCondition> complexConditions = new ObjectArrayList<>();

                            for (QueryCondition condition : mapOrPropositionToConditionSet.get(orIndex)) {
                                if (condition.getType() == QueryConditionType.SIMPLE) {
                                    simpleConditions.add(condition);
                                } else {
                                    complexConditions.add(condition);
                                }
                            }

                            OutData outData = new OutData();
                            MatchingBase matchingMachine;
                            if (complexConditions.size() == 0) { // No complex conditions
                                matchingMachine = new MatchingSimple(outData, query_t, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions);

                            } else { // Complex conditions
                                matchingMachine = new MatchingWhere(outData, query_t, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions, complexConditions);
                            }

                            outData = matchingMachine.matching();

                            totalTime = outData.getTotalTime();
                            numOccurrences = outData.num_occurrences;
                            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
                        }
                    } else { // No WHERE CONDITIONS
                        QueryStructure query = new QueryStructure();
                        query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.empty());

                        OutData outData = new OutData();
                        MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, new ObjectArrayList<>());
                        outData = matchingMachine.matching();

                        totalTime = outData.getTotalTime();
                        numOccurrences = outData.num_occurrences;
                        System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
                    }

                    // SAVING
                    if (configuration.out_file != null) {
                        try {
                            FileManager.saveIntoCSV_NEW(query_test, configuration.out_file, totalTime, numOccurrences);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return totalTime;
                }
            });
            try {handler.get(tout.getSeconds(), TimeUnit.SECONDS);}
            catch (Exception e) {
                handler.cancel(true);
                System.err.println("timeout");
                System.exit(-1);
            }
        });


        System.exit(0);
    }
}
