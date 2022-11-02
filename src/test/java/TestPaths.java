import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.controllers.MatchingPathSimple;
import matching.models.OutData;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.util.Optional;

public class TestPaths {

    public static void main(String[] args) {
        // PATH
        String root_dir = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/TestPaths";

        // TARGET READING
        Table[] nodesTables = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edgesTables = FileManager.files_reading(netw_path + "/edges", ',');

        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels");

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.createBitset(targetGraph.getSrcDstAggregation(), targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());

        // QUERY
        String query_test = "MATCH (n1:P)<-[:F*2..2]-(n0:P), (n0:P) -[:C]-> (n2:P) RETURN n0,n1,n2";
        System.out.println(query_test);

        QueryStructure query_obj = new QueryStructure();
        query_obj.parser(query_test, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager(), nodesTables, edgesTables, Optional.empty());

        OutData outData = new OutData();

        MatchingPathSimple matchingMachine = new MatchingPathSimple(outData, query_obj, true, false, Long.MAX_VALUE, targetGraph, target_bitmatrix, new ObjectArrayList<>());
        outData = matchingMachine.matching();

        double totalTime = outData.getTotalTime();
        long numOccurrences = outData.num_occurrences;
        System.out.println("FINAL NUMBER OF OCCURRENCES: " + numOccurrences + "\tTIME: " + totalTime);
    }
}
