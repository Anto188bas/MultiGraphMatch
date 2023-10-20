
import configuration.ElaborateTargetConfiguration;
import out.OutElaborationFiles;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ElaborateTarget {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // CONFIGURATION
        ElaborateTargetConfiguration configuration = new ElaborateTargetConfiguration(args);
        OutElaborationFiles outData = new OutElaborationFiles();

        // PATH
        System.out.println("Reading target graph...");
        double startTime    = System.nanoTime();
        Table[] nodesTables = FileManager.files_reading(configuration.nodesDirectory, ',');
        Table[] edgesTables = FileManager.files_reading(configuration.edgesDirectory, ',');
        outData.fileReadingTime = (System.nanoTime() - startTime) / Math.pow(10,9);

        System.out.println("Elaborating target graph...");
        // TARGET GRAPH
        TargetGraph targetGraph = new TargetGraph(nodesTables, edgesTables, "id", "labels", outData);
        targetGraph.write(configuration.outDirectory);
        System.out.println("Done!");


        String[] elements    = configuration.outDirectory.split("/");
        elements[elements.length -1] = "";
        String savingPath    = String.join("/", elements) + "results/";
        Path savingRes       = Paths.get(savingPath);
        savingRes.toFile().mkdirs();

        savingPath            = savingPath + "elaboration_results.csv";
        File file             = new File(savingPath);
        boolean exists        = file.exists();
        BufferedWriter writer = new BufferedWriter(new FileWriter(savingPath, true));
        if(!exists)
            writer.write("Files Reading Time\tNodes Elaboration\tEdges Elaboration\tBitMatrix Elaboration\n");
        writer.write(outData.fileReadingTime+"\t"+outData.nodesElaborationTime+"\t"+outData.edgesElaborationTime+"\t"+outData.bitMatrixTime+"\n");
        writer.close();

    }
}

