import condition.QueryConditionType;
import configuration.Configuration;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.controllers.WhereUtils;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Analysis {
    static String[] header = new String[]{"query", "#nodes", "#edges", "#conditions", "#nodes_with_conditions"};

    private static void initCSV(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        writer.write(String.join("\t", header));
        writer.newLine();
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        // CONFIGURATION
        Configuration configuration = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodesTables = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edges_main_directory, ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels");

        // QUERIES READING
        System.out.println("Reading queries...");
        List<String> queries = FileManager.query_reading(configuration);

        initCSV(configuration.out_file);

        queries.forEach(query_test -> {

            System.out.println(query_test);

            WhereConditionExtraction where_managing = new WhereConditionExtraction();
            where_managing.where_condition_extraction(query_test);

            if (where_managing.getWhere_string() != null) { // There are WHERE CONDITIONS
                Info info = new Info();

                where_managing.normal_form_computing();
                where_managing.buildSetWhereConditions();

                Int2ObjectOpenHashMap<ObjectArrayList<QueryCondition>> mapOrPropositionToConditionSet = where_managing.getMapOrPropositionToConditionSet();

                int numConditions = 0;
                if (mapOrPropositionToConditionSet.size() > 0) { // at least one OR
                    ObjectArrayList<Info> infos = new ObjectArrayList<>();

                    QueryStructure query_t = new QueryStructure(targetGraph);
                    query_t.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.of(where_managing));

                    for (int orIndex = 0; orIndex < mapOrPropositionToConditionSet.size(); orIndex++) {
                        query_t.clean();

                        ObjectArrayList<QueryCondition> simpleConditions = new ObjectArrayList<>();
                        ObjectArrayList<QueryCondition> complexConditions = new ObjectArrayList<>();

                        for (QueryCondition condition : mapOrPropositionToConditionSet.get(orIndex)) {
                            numConditions ++;

                            if (condition.getType() == QueryConditionType.SIMPLE) {
                                simpleConditions.add(condition);
                            } else {
                                complexConditions.add(condition);
                            }
                        }

                        QueryAnalyzer analyzer = new QueryAnalyzer(query_t, simpleConditions, complexConditions);
                        infos.add(analyzer.getInfo());
                    }

                    info.numNodes = infos.get(0).numNodes;
                    info.numEdges = infos.get(0).numEdges;
                    info.numConditions = numConditions;

                    ObjectArraySet<String> nodes = new ObjectArraySet<>();
                    for (Info i : infos) {
                        nodes.addAll(i.nodesWithConditions);
                    }

                    info.numNodesWithConditions = nodes.size();
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

                    QueryAnalyzer analyzer = new QueryAnalyzer(query_t, simpleConditions, complexConditions);
                    info = analyzer.getInfo();
                }

                try {
                    info.writeCSV(configuration.out_file, query_test);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        System.exit(0);
    }
}

class Info {
    public int numNodes;
    public int numEdges;
    public int numConditions;
    public int numNodesWithConditions;
    public ObjectArrayList<String> nodesWithConditions;
    public IntArrayList domainSizes;

    public Info() {
        numNodes = 0;
        numEdges = 0;
        numConditions = 0;
        numNodesWithConditions = 0;
        nodesWithConditions = new ObjectArrayList<>();
        domainSizes = new IntArrayList();
    }

    public void writeCSV(String path, String query) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        String result = query + "\t" + numNodes + "\t" + numEdges + "\t" + numConditions + "\t" + numNodesWithConditions;
        writer.write(result + "\n");
        writer.close();
    }
}

class QueryAnalyzer {
    QueryStructure query;
    ObjectArrayList<QueryCondition> simpleConditions;
    ObjectArrayList<QueryCondition> complexConditions;

    public QueryAnalyzer(QueryStructure query, ObjectArrayList<QueryCondition> simpleConditions, ObjectArrayList<QueryCondition> complexConditions) {
        this.query = query;
        this.simpleConditions = simpleConditions;
        this.complexConditions = complexConditions;
    }

    public Info getInfo() {
        Info info = new Info();

        // SIMPLE WHERE CONDITIONS
        if (simpleConditions.size() > 0) {
            WhereUtils.assignSimpleConditionsToNodesAndEdges(simpleConditions, query);
        }

        info.numNodes = query.getQuery_nodes().size();
        info.numEdges = query.getQuery_edges().size();
        info.numConditions = simpleConditions.size();
        info.numNodesWithConditions = (int) query.getQuery_nodes().values().stream().filter(node -> (node.getSimpleConditions().size() > 0 || node.getComplexConditions().size() > 0)).count();
        info.nodesWithConditions = new ObjectArrayList<>(query.getQuery_nodes().values().stream().filter(node -> (node.getSimpleConditions().size() > 0 || node.getComplexConditions().size() > 0)).map(node -> node.getNode_name()).collect(Collectors.toList()));
        info.domainSizes = new IntArrayList(query.getQuery_nodes().values().stream().mapToInt(node -> node.getWhereConditionsCompatibilityDomain().size()).toArray());

        return info;
    }
}