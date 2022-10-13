import bitmatrix.models.TargetBitmatrix;
import condition.QueryConditionType;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestHybridWhere {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // PATH
        String root_dir  = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/Test";


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

//        String query_test           = "MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n0:P)-[r2:F]->(n2:P) WHERE (n1.name = \"FILIPPO\" AND n0.age > n1.age) OR (n0.name = \"Luca\" AND n1.name = \"Alessia\") RETURN n0, n1, n2";
        String query_test           = "MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n0:P)-[r2:F]->(n2:P) WHERE n1.name = \"FILIPPO\" AND n0.age > n1.age RETURN n0, n1, n2";
//        String query_test           = "MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n0:P)-[r2:F]->(n2:P) WHERE (n1.name = \"FILIPPO\" AND n0.age > n1.age) OR (n1.name = \"CIAO\") RETURN n0, n1, n2";

        double totalTime;
        long numOccurrences;

        WhereConditionExtraction where_managing = new WhereConditionExtraction();
        where_managing.where_condition_extraction(query_test);

        if(where_managing.getWhere_string() != null) { // There are WHERE CONDITIONS
            where_managing.normal_form_computing();
            where_managing.buildSetWhereConditions();

            Int2ObjectOpenHashMap<ObjectArrayList<QueryCondition>> mapOrPropositionToConditionSet = where_managing.getMapOrPropositionToConditionSet();

            if(mapOrPropositionToConditionSet.size() > 1) { // Multi-Thread (at least one OR)
                double time = System.currentTimeMillis();

                System.out.println("MultiThread");

                QueryStructure query = new QueryStructure();
                query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                ExecutorService pool = Executors.newCachedThreadPool();
                ArrayList<Runnable> runnableArrayList = new ArrayList<>();
                ObjectArrayList<ObjectArraySet<String>> sharedMemory = new ObjectArrayList<>();


                for(int orIndex: mapOrPropositionToConditionSet.keySet()) {
                    ObjectArrayList<QueryCondition> simpleConditions = new ObjectArrayList<>();
                    ObjectArrayList<QueryCondition> complexConditions = new ObjectArrayList<>();

                    for(QueryCondition condition: mapOrPropositionToConditionSet.get(orIndex)) {
                        if(condition.getType() == QueryConditionType.SIMPLE) {
                            simpleConditions.add(condition);
                        } else {
                            complexConditions.add(condition);
                        }
                    }

                    if(complexConditions.size() == 0) { // No complex conditions
                        System.out.println("No complex conditions");

                        QueryStructure query_t = new QueryStructure();
                        query_t.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                        OutData outData = new OutData();

                        MatchingSimple matchingMachine = new MatchingSimple(outData, query_t, false, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions);
                        MatchingBaseTask matchingTask = new MatchingBaseTask(orIndex, sharedMemory, matchingMachine);
                        runnableArrayList.add(matchingTask);

                    } else { // Complex conditions
                        System.out.println("Complex conditions");

                        QueryStructure query_t = new QueryStructure();
                        query_t.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                        OutData outData = new OutData();

                        MatchingWhere matchingMachine = new MatchingWhere(outData, query_t, false, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions, complexConditions);
                        MatchingBaseTask matchingTask = new MatchingBaseTask(orIndex, sharedMemory, matchingMachine);
                        runnableArrayList.add(matchingTask);
                    }
                }

                for(Runnable runnable: runnableArrayList) {
                    pool.execute(runnable);
                }

                pool.shutdown();
                pool.awaitTermination(1800, TimeUnit.SECONDS);

                ObjectArraySet<String> finalOccurrences = new ObjectArraySet<>();
                for(ObjectArraySet<String> occurrences: sharedMemory) {
                    finalOccurrences.addAll(occurrences);
                }

                time = (System.currentTimeMillis() - time) / 1000;
                totalTime = time;
                numOccurrences = finalOccurrences.size();
                System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences +"\tTIME: " + totalTime);

            } else { // Single-Thread (only AND)
                System.out.println("SingleThread");

                QueryStructure query = new QueryStructure();
                query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

                ObjectArrayList<QueryCondition> simpleConditions = new ObjectArrayList<>();
                ObjectArrayList<QueryCondition> complexConditions = new ObjectArrayList<>();

                for(QueryCondition condition: mapOrPropositionToConditionSet.get(0)) {
                    if(condition.getType() == QueryConditionType.SIMPLE) {
                        simpleConditions.add(condition);
                    } else {
                        complexConditions.add(condition);
                    }
                }

                if(complexConditions.size() == 0) { // No complex conditions
                    System.out.println("No complex conditions");

                    OutData outData = new OutData();

                    MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions);
                    outData = matchingMachine.matching();

                    totalTime = outData.getTotalTime();
                    numOccurrences = outData.num_occurrences;
                    System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences +"\tTIME: " + totalTime);

                } else { // Complex conditions
                    System.out.println("Complex conditions");

                    OutData outData = new OutData();
                    MatchingWhere matchingMachine = new MatchingWhere(outData, query, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, simpleConditions, complexConditions);
                    outData = matchingMachine.matching();

                    totalTime = outData.getTotalTime();
                    numOccurrences = outData.num_occurrences;
                    System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences +"\tTIME: " + totalTime);
                }
            }
        } else { // No WHERE CONDITIONS
            QueryStructure query = new QueryStructure();
            query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.empty());

            OutData outData = new OutData();
            MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, idx_label, target_bitmatrix, graphPaths, macro_nodes, nodes_macro, new ObjectArrayList<>());
            outData = matchingMachine.matching();

            totalTime = outData.getTotalTime();
            numOccurrences = outData.num_occurrences;
            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences +"\tTIME: " + totalTime);
        }
    }
}
