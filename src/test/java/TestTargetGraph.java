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
import java.util.Optional;

public class TestTargetGraph {
    public static void main(String[] args) {
        // CONFIGURATION
        Configuration configuration = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodesTables = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edges_main_directory, ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id","labels");
        System.out.println(targetGraph);

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.createBitset(targetGraph.getGraphPaths(), targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());

        // QUERY
        double totalTime;
        long numOccurrences;
        String query_test = "MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n0:P)-[r2:F]->(n2:P) WHERE (n1.name = \"FILIPPO\" AND n0.age > n1.age) OR (n0.name = \"Luca\" AND n1.name = \"Alessia\") RETURN n0, n1, n2";
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
            query.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(),nodesTables, edgesTables, Optional.empty());

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
    }
}
