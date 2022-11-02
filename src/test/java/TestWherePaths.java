import bitmatrix.models.TargetBitmatrix;
import condition.QueryConditionType;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.controllers.*;
import matching.models.OutData;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.graph.TargetGraph;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class TestWherePaths {
    public static void main(String[] args) {
        // PATH
        String root_dir  = System.getProperty("user.dir");
//        String netw_path = root_dir + "/Networks/Person";
        String netw_path = root_dir + "/Networks/TestPaths";

        // TARGET READING
        Table[] nodesTables = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edgesTables = FileManager.files_reading(netw_path + "/edges", ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id","labels");

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.createBitset(targetGraph.getSrcDstAggregation(), targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());

        // QUERY
//        String query_test           = "MATCH (n1:Person)-[r1:college]->(n2:Person), (n3:Person) -[r2:college]-> (n2:Person) WHERE (n1.name <> n2.name AND NOT n1.name IN [\"Antonio\", \"Paolo\"]) OR (n2.name <> \"Franco\" AND n1.age > 18) RETURN n1,n2,n3";
//        String query_test           = "MATCH (n1:Person)-[r1:college]->(n2:Person), (n3:Person) -[r2:college]-> (n2:Person) WHERE (n2.name <> \"Franco\" AND n1.age > 18) RETURN n1,n2,n3";

//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name <> \"Franco\" AND n1.age > 18 AND n3.age >= 25) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name <> \"Franco\" AND n1.age > 18) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name = \"Leonardo\" AND n1.age = 25) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name = \"PIPPO\" AND n1.age = 250) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name <> \"Franco\" AND n1.age > 18 AND n3.age >= 25) RETURN n0,n1,n2,n3";

//        String query_test           = "MATCH (n1:P)<-[:F*2..2]-(n0:P), (n0:P) -[:C]-> (n2:P) WHERE (n0.name = \"Leonardo\" AND n0.age = 30 AND n1.name = \"Giuseppe\") RETURN n0,n1";

        String query_test           = "MATCH (n1:P)<-[:F*1..2]-(n0:P), (n0:P) -[:C]-> (n2:P) WHERE (n0.name = \"Leonardo\" AND n0.age = 30 AND n1.name = \"Giuseppe\") OR (n0.name = \"Leonardo\" AND n1.name = \"Leonardo\") RETURN n0,n1";

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

                QueryStructure query_t = new QueryStructure();
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
                        matchingMachine = new MatchingPathSimple(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions);
                    } else { // Complex conditions
                        matchingMachine = new MatchingPathWhere(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions, complexConditions);
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
                QueryStructure query_t = new QueryStructure();
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
                    matchingMachine = new MatchingPathSimple(outData, query_t, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions);
                } else { // Complex conditions
                    matchingMachine = new MatchingPathWhere(outData, query_t, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, simpleConditions, complexConditions);
                }

                outData = matchingMachine.matching();

                totalTime = outData.getTotalTime();
                numOccurrences = outData.num_occurrences;
                System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
            }
        } else { // No WHERE CONDITIONS
            QueryStructure query = new QueryStructure();
            query.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(),nodesTables, edgesTables, Optional.empty());

            OutData outData = new OutData();
            MatchingPathSimple matchingMachine = new MatchingPathSimple(outData, query, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, new ObjectArrayList<>());
            outData = matchingMachine.matching();

            totalTime = outData.getTotalTime();
            numOccurrences = outData.num_occurrences;
            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
        }
    }
}
