
import configuration.ElaborateTargetConfiguration;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.io.*;

public class ElaborateTarget {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // CONFIGURATION
        ElaborateTargetConfiguration configuration = new ElaborateTargetConfiguration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodesTables = FileManager.files_reading(configuration.nodesDirectory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edgesDirectory, ',');

        System.out.println("Elaborating target graph...");
        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels");

        targetGraph.write(configuration.outDirectory);
        System.out.println("Done!");
    }
}

