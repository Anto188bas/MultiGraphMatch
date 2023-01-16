
import condition.QueryConditionType;
import configuration.MatchingConfiguration;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.controllers.JustOrdering;
import matching.controllers.MatchingBase;
import matching.controllers.MatchingSimple;
import matching.controllers.MatchingWhere;
import matching.models.OutData;
import reading.FileManager;
import target_graph.graph.TargetGraph;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class OrderingTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // CONFIGURATION
        MatchingConfiguration configuration = new MatchingConfiguration(args);

        // PATH
        System.out.println("Reading target graph...");
        TargetGraph targetGraph = TargetGraph.read(configuration.targetDirectory);
        System.out.println("Done!");

        // QUERIES READING
        List<String> queries = FileManager.readQueries(configuration.queriesDirectory);
        final Duration tout = Duration.ofSeconds(configuration.timeout);
        queries.forEach(query_test -> {

            System.out.println(query_test);

            ExecutorService exec = Executors.newSingleThreadExecutor();
            final Future<Double> handler = exec.submit(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    double totalTime = 0d;
                    long numOccurrences = 0;

                    WhereConditionExtraction where_managing = new WhereConditionExtraction();
                    where_managing.where_condition_extraction(query_test);

                    if (where_managing.getWhere_string() != null) { // There are WHERE CONDITIONS
                        where_managing.normal_form_computing();
                        where_managing.buildSetWhereConditions();

                        Int2ObjectOpenHashMap<ObjectArrayList<QueryCondition>> mapOrPropositionToConditionSet = where_managing.getMapOrPropositionToConditionSet();

                        if (mapOrPropositionToConditionSet.size() > 0) { // Multi-Thread (at least one OR)
                            System.out.println("---- AT LEAST ONE OR PROPOSITION ----");
                            double time = System.currentTimeMillis();

                            QueryStructure query_t = new QueryStructure(targetGraph);
                            query_t.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), targetGraph.getNodesTables(), targetGraph.getEdgesTables(), Optional.of(where_managing));

                            for (int orIndex = 0; orIndex < mapOrPropositionToConditionSet.size(); orIndex++) {
                                System.out.println("\tOR PROPOSITION " + (orIndex + 1) + "/" + mapOrPropositionToConditionSet.size());
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
                                JustOrdering matchingMachine = new JustOrdering(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, targetGraph.getTargetBitmatrix(), simpleConditions);

                                matchingMachine.matching();
                            }

                            time = (System.currentTimeMillis() - time) / 1000;
                            totalTime = time;
                        } else { // Single-Thread (only AND)
                            System.out.println("---- NO OR PROPOSITIONS ----");
                            double time = System.currentTimeMillis();

                            QueryStructure query_t = new QueryStructure(targetGraph);
                            query_t.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), targetGraph.getNodesTables(), targetGraph.getEdgesTables(), Optional.of(where_managing));

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
                            JustOrdering matchingMachine = new JustOrdering(outData, query_t, false, false, Long.MAX_VALUE, targetGraph, targetGraph.getTargetBitmatrix(), simpleConditions);

                            matchingMachine.matching();

                            outData = matchingMachine.matching();
                            time = (System.currentTimeMillis() - time) / 1000;
                            totalTime = time;
                        }
                    } else {
                        System.err.println("ERROR: No Where Conditions!!!");
                        System.exit(-1);
                    }

                    return totalTime;
                }
            });
            try {
                handler.get(tout.getSeconds(), TimeUnit.SECONDS);
            }
            catch (Exception e) {
                handler.cancel(true);
                e.printStackTrace();
                System.err.println("timeout");
            }

            try {
                exec.shutdownNow();
                boolean res = exec.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        System.exit(0);
    }
}
