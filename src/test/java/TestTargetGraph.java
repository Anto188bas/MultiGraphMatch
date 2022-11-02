import configuration.Configuration;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

public class TestTargetGraph {
    public static void main(String[] args) {
        // CONFIGURATION
        Configuration configuration = new Configuration(args);

        // PATH
        System.out.println("Reading target graph...");

        Table[] nodesTables = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edges_main_directory, ',');


        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id","labels");

        System.out.println(targetGraph);
    }
}
