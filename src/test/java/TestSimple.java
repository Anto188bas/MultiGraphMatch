import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.controllers.MatchingSimple;
import matching.models.OutData;
import out.OutElaborationFiles;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.util.Optional;

public class TestSimple {
    public static void main(String[] args) {
        String[] queries = new String[]{"MATCH (n0:P)-[r0:F]->(n1:P), (n0:P)-[r1:C]->(n2:P), (n1:P)-[r2:F]->(n2:C) RETURN n0, n1, n2",};
        OutElaborationFiles outDataFile = new OutElaborationFiles();

        // PATH
        System.out.println("Reading target graph...");

        // PATH
        String root_dir = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/Test";


        // TARGET READING
        Table[] nodesTables = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edgesTables = FileManager.files_reading(netw_path + "/edges", ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels", outDataFile);

        // QUERIES READING
        for (int i = 0; i < queries.length; i++) {
            String query_test = queries[i];
            System.out.println(query_test);

            QueryStructure query = new QueryStructure(targetGraph);
            query.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.empty());

            OutData outData = new OutData();
            MatchingSimple matchingMachine = new MatchingSimple(outData, query, true, false, Long.MAX_VALUE, targetGraph, targetGraph.getTargetBitmatrix(), new ObjectArrayList<>());
            outData = matchingMachine.matching();

            double totalTime = outData.getTotalTime();
            long numOccurrences = outData.num_occurrences;
            System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
        }

        System.exit(0);
    }
}
