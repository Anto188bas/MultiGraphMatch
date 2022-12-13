
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import configuration.ElaborateTargetConfiguration;
import reading.FileManager;
import target_graph.graph.TargetGraph;
import tech.tablesaw.api.Table;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

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

        Path outDirectory = Paths.get(configuration.outDirectory);
        Path elaboratedDirectory = Paths.get(outDirectory.toString(), "elaborated");
        Path graphDirectory = Paths.get(elaboratedDirectory.toString(), "graph");
        Path nodesDirectory = Paths.get(elaboratedDirectory.toString(), "nodes");
        Path edgesDirectory = Paths.get(elaboratedDirectory.toString(), "edges");
        Path matrixDirectory = Paths.get(elaboratedDirectory.toString(), "matrix");

        if (elaboratedDirectory.toFile().exists() && elaboratedDirectory.toFile().isDirectory()) {
            System.out.println("Elaborated directory already exists, deleting it...");
            Files.walk(elaboratedDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        elaboratedDirectory.toFile().mkdirs();
        graphDirectory.toFile().mkdirs();
        nodesDirectory.toFile().mkdirs();
        edgesDirectory.toFile().mkdirs();
        matrixDirectory.toFile().mkdirs();

        System.out.println("Writing target graph to " + graphDirectory + "...");
        graphDirectory = Paths.get(graphDirectory.toString(), "graph.json");

        SimpleBeanPropertyFilter targetFilter = SimpleBeanPropertyFilter
                .serializeAllExcept("targetBitmatrix", "nodesTables", "edgesTables");
        FilterProvider targetFilters = new SimpleFilterProvider()
                .addFilter("targetFilter", targetFilter);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writer(targetFilters).writeValue(graphDirectory.toFile(), targetGraph);

        System.out.println("Writing nodes to " + nodesDirectory + "...");
        for(Table table: nodesTables) {
            table.write().csv(Paths.get(nodesDirectory.toString(), table.name()).toString());
        }

        System.out.println("Writing edges to " + edgesDirectory + "...");
        for(Table table: edgesTables) {
            table.write().csv(Paths.get(edgesDirectory.toString(), table.name()).toString());
        }

        System.out.println("Writing matrix to " + matrixDirectory + "...");
        SimpleBeanPropertyFilter matrixFilter = SimpleBeanPropertyFilter
                .serializeAllExcept("bitmatrix");
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("matrixFilter", matrixFilter);
        mapper.writer(filters).writeValue(Paths.get(matrixDirectory.toString(), "matrix.json").toFile(), targetGraph.getTargetBitmatrix());


        FileOutputStream f = new FileOutputStream(Paths.get(matrixDirectory.toString(), "bitsetList.ser").toFile());
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(targetGraph.getTargetBitmatrix().getBitmatrix());
        o.close();
        f.close();

        TargetGraph prova = TargetGraph.read(configuration.outDirectory);
        System.out.println(prova);

//
//        ArrayList<BitSet> prova = null;
//
//        FileInputStream fi = new FileInputStream(Paths.get(matrixDirectory.toString(), "matrix.ser").toFile());
//        ObjectInputStream oi = new ObjectInputStream(fi);
//
//        prova = (ArrayList<BitSet>) oi.readObject();
//        System.out.println(prova);


//
//
//
//        TargetGraph car = mapper.readValue(graphDirectory.toFile(), TargetGraph.class);
//        System.out.println("PRIMA: " + targetGraph);
//        System.out.println("DOPO: " + car);

    }
}

