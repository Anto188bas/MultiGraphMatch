import bitmatrix.models.TargetBitmatrix;
import condition.QueryConditionType;
import configuration.Configuration;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.controllers.MatchingBase;
import matching.controllers.MatchingSimple;
import matching.controllers.MatchingWhere;
import matching.models.OutData;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class MainClass {
    public static void main(String[] args) throws IOException {
        // CONFIGURATION
        Configuration configuration = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodesTables = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edges_main_directory, ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels");

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.createBitset(targetGraph.getSrcDstAggregation(), targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());

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
                            double time = System.currentTimeMillis();

                            QueryStructure query_t = new QueryStructure(targetGraph);
                            query_t.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.of(where_managing));

                            for (int orIndex = 0; orIndex < mapOrPropositionToConditionSet.size(); orIndex++) {
                                query_t.clean();

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
                                    matchingMachine = new MatchingSimple(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions);
                                } else { // Complex conditions
                                    matchingMachine = new MatchingWhere(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions, complexConditions);
                                }
                                matchingMachine.matching();
                                sharedMemory.add(outData.occurrences);
                            }

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
                            QueryStructure query_t = new QueryStructure(targetGraph);
                            query_t.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.of(where_managing));

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
                                matchingMachine = new MatchingSimple(outData, query_t, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions);
                            } else { // Complex conditions
                                matchingMachine = new MatchingWhere(outData, query_t, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions, complexConditions);
                            }

                            outData = matchingMachine.matching();

                            totalTime = outData.getTotalTime();
                            numOccurrences = outData.num_occurrences;
                            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
                        }
                    } else { // No WHERE CONDITIONS
                        QueryStructure query = new QueryStructure(targetGraph);
                        query.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.empty());

                        OutData outData = new OutData();
                        MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, new ObjectArrayList<>());
                        outData = matchingMachine.matching();

                        totalTime = outData.getTotalTime();
                        numOccurrences = outData.num_occurrences;
                        System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
                    }

                    // SAVING
                    if (configuration.out_file != null) {
                        try {
                            FileManager.saveToCSV(query_test, configuration.out_file, totalTime, numOccurrences);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return totalTime;
                }
            });
            try {
                handler.get(tout.getSeconds(), TimeUnit.SECONDS);
            } catch (Exception e) {
                handler.cancel(true);
                System.err.println("timeout");
                System.exit(-1);
            }
        });


        System.exit(0);
    }
}
