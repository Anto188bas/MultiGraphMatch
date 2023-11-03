package target_graph.graph;

import bitmatrix.models.TargetBitmatrix;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import it.unimi.dsi.fastutil.ints.*;
import matching.models.OutData;
import out.OutElaborationFiles;
import reading.FileManager;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;
import target_graph.managers.PropertiesManager;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.index.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class TargetUtils {
    public static void initNodeLabelsDegrees(Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees, int node) {
        if (mapNodeIdToLabelsDegrees.containsKey(node)) return;
        mapNodeIdToLabelsDegrees.put(node, new Int2IntOpenHashMap());
    }


    // ADD COLOR FOR AGGREGATION (IT IS USED ONLY FOR BITMATRIX)
    public static void add_color_for_aggregation(int src, int dst, int type, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation) {
        if (src_dst_aggregation.containsKey(dst) && src_dst_aggregation.get(dst).containsKey(src)) {
            IntOpenHashSet[] dir_colors = src_dst_aggregation.get(dst).get(src);
            if (dir_colors[1] == null) dir_colors[1] = new IntOpenHashSet();
            if (type != -1) dir_colors[1].add(type);
        } else {
            if (!src_dst_aggregation.containsKey(src)) src_dst_aggregation.put(src, new Int2ObjectOpenHashMap<>());
            Int2ObjectOpenHashMap<IntOpenHashSet[]> dsts_colors = src_dst_aggregation.get(src);
            if (!dsts_colors.containsKey(dst)) {
                dsts_colors.put(dst, new IntOpenHashSet[2]);
                dsts_colors.get(dst)[0] = new IntOpenHashSet();
            }
            dsts_colors.get(dst)[0].add(type);

        }
    }

    public static void increaseNodeLabelDegree(Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees, int nodeId, int labelId) {
        Int2IntOpenHashMap color_degrees = mapNodeIdToLabelsDegrees.get(nodeId);
        if (color_degrees.containsKey(labelId)) {
            color_degrees.replace(labelId, color_degrees.get(labelId) + 1);
        } else {
            color_degrees.put(labelId, 1);
        }
    }
}

@JsonFilter("targetFilter")
public class TargetGraph {
    private String idsColumnName;
    private String labelsColumnName;
    private int edgeSourceColumnNumber = 0;
    private int edgeDestinationColumnNumber = 1;
    private String edgeLabelsColumnName = "type";
    GraphPaths graphPaths;
    private NodesLabelsManager nodesLabelsManager;
    private EdgesLabelsManager edgesLabelsManager;
    private PropertiesManager nodesPropertiesManager;
    private PropertiesManager edgesPropertiesManager;

    private Table[] nodesTables;
    private Table[] edgesTables;
    private TargetBitmatrix targetBitmatrix;

    public TargetGraph() {}

    public TargetGraph(Table[] nodesTables, Table[] edgesTables, String idsColumnName, String labelsColumnName, OutElaborationFiles outData) {
        graphPaths = new GraphPaths();

        this.nodesLabelsManager = new NodesLabelsManager(0);
        this.edgesLabelsManager = new EdgesLabelsManager(0);

        this.nodesPropertiesManager = new PropertiesManager(idsColumnName);
        this.edgesPropertiesManager = new PropertiesManager(idsColumnName);

        this.nodesTables = nodesTables;
        this.edgesTables = edgesTables;

        this.idsColumnName = idsColumnName;
        this.labelsColumnName = labelsColumnName;

        System.out.println("Elaborating nodes...");
        // Nodes
        double startTime = System.nanoTime();
        for (int i = 0; i < nodesTables.length; i++) {
            Table currentTable     = nodesTables[i];
            List<String> properties = currentTable.columnNames()
                .stream()
                .filter(colName -> !colName.equals(this.idsColumnName) && !colName.equals(this.labelsColumnName))
                .toList();
            nodesPropertiesManager.addProperties(properties);

            // Properties
            for(String property : properties) {
                int propertyId    = nodesPropertiesManager.getMapPropertyStringToPropertyId().getInt(property);
                var currentColumn = currentTable.column(property);
                Index index       = null;

                switch(currentColumn.type().name()) {
                    case "INTEGER":
                        index = new IntIndex(currentTable.intColumn(property));

                        for(Object value: currentColumn.unique()) {
                            // IntArrayList idList = new IntArrayList((List<Integer>) currentTable.where(((IntIndex) index).get((Integer) value)).column("id").asList());
                           var idList = new IntOpenHashSet((List<Integer>) currentTable.where(((IntIndex) index).get((Integer) value)).column("id").asList());
                           nodesPropertiesManager.getMapPropertyIdToValues().get(propertyId).put(value, idList);
                        }

                        break;
                    case "FLOAT":
                        index = new FloatIndex(currentTable.floatColumn(property));

                        for(Object value: currentColumn.unique()) {
                            // IntArrayList idList = new IntArrayList((List<Integer>) currentTable.where(((FloatIndex) index).get((Float) value)).column("id").asList());
                            var idList = new IntOpenHashSet((List<Integer>) currentTable.where(((FloatIndex) index).get((Float) value)).column("id").asList());
                            nodesPropertiesManager.getMapPropertyIdToValues().get(propertyId).put(value, idList);
                        }

                        break;
                    case "DOUBLE":
                        index = new DoubleIndex(currentTable.doubleColumn(property));

                        for(Object value: currentColumn.unique()) {
                            // IntArrayList idList = new IntArrayList((List<Integer>) currentTable.where(((DoubleIndex) index).get((Double) value)).column("id").asList());
                            var idList = new IntOpenHashSet((List<Integer>) currentTable.where(((DoubleIndex) index).get((Double) value)).column("id").asList());
                            nodesPropertiesManager.getMapPropertyIdToValues().get(propertyId).put(value, idList);
                        }

                        break;
                    case "STRING":
                        index = new StringIndex(currentTable.stringColumn(property));

                        for(Object value: currentColumn.unique()) {
                            // IntArrayList idList = new IntArrayList((List<Integer>) currentTable.where(((StringIndex) index).get((String) value)).column("id").asList());
                            var idList = new IntOpenHashSet((List<Integer>) currentTable.where(((StringIndex) index).get((String) value)).column("id").asList());
                            nodesPropertiesManager.getMapPropertyIdToValues().get(propertyId).put(value, idList);
                        }
                        break;
                }
            }

            // Labels
            currentTable.forEach(row -> {
                int id = row.getInt(this.idsColumnName);
                String labelSetString = row.getString(this.labelsColumnName);

                nodesLabelsManager.addElement(id, labelSetString);
            });
        }
        outData.nodesElaborationTime = (System.nanoTime() - startTime) / Math.pow(10, 9);

        System.out.println("Elaborating edges...");
        // Edges
        startTime=System.nanoTime();
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees = new Int2ObjectOpenHashMap<>();

        AtomicInteger edgeIdCount = new AtomicInteger(0);
        AtomicInteger pairKeyCount = new AtomicInteger(0);

        Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapPairToKey = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> mapKeyToEdgeList = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> srcDstAggregation = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < edgesTables.length; i++) {
            Table currentTable = edgesTables[i];
            List<String> header = currentTable.columnNames();

            // Edges don't have an ID. We need to create the column.
            IntColumn edgeIdColumn = IntColumn.create(this.idsColumnName);

            // Do edges have labels?
            int labelsColumnNumber = this.getEdgeLabelsColumnId(header);

            String sourcesColumnName = header.get(this.edgeSourceColumnNumber);
            String destinationsColumnName = header.get(this.edgeDestinationColumnNumber);

            if (labelsColumnNumber != -1) { // Labels are defined
                // Properties
                List<String> properties = header.stream().filter(colName -> !colName.equals(this.edgeLabelsColumnName) && !colName.equals(sourcesColumnName) && !colName.equals(destinationsColumnName)).toList();
                edgesPropertiesManager.addProperties(properties);
                // Add all the edges
                currentTable.forEach(row -> addEdge(row, header, row.getString(header.get(labelsColumnNumber)), edgeIdCount, edgeIdColumn, mapKeyToEdgeList, pairKeyCount, mapNodeIdToLabelsDegrees, properties, srcDstAggregation));
                // We remove sources, destinations and label sets
                currentTable.removeColumns(header.get(this.edgeSourceColumnNumber), header.get(this.edgeDestinationColumnNumber), this.edgeLabelsColumnName);
            } else { // Labels aren't defined
                // Properties
                List<String> properties = header.stream().filter(colName -> !colName.equals(this.edgeLabelsColumnName) && !colName.equals(sourcesColumnName) && !colName.equals(destinationsColumnName)).toList();
                edgesPropertiesManager.addProperties(properties);
                // Add all the edges
                currentTable.forEach(row -> addEdge(row, header, "none", edgeIdCount, edgeIdColumn, mapKeyToEdgeList, pairKeyCount, mapNodeIdToLabelsDegrees, properties, srcDstAggregation));
                // We remove sources and destinations
                currentTable.removeColumns(header.get(this.edgeSourceColumnNumber), header.get(this.edgeDestinationColumnNumber));
            }
            // We add the edge ID column
            currentTable.addColumns(edgeIdColumn);
        }
        outData.edgesElaborationTime = (System.nanoTime() - startTime) / Math.pow(10, 9);

        this.graphPaths = new GraphPaths(mapKeyToEdgeList, mapNodeIdToLabelsDegrees);

        System.out.println("Creating the target Bit Matrix...");
        startTime = System.nanoTime();
        this.targetBitmatrix = new TargetBitmatrix();
        targetBitmatrix.createBitset(srcDstAggregation, this.getNodesLabelsManager(), this.getEdgesLabelsManager());
        outData.bitMatrixTime = (System.nanoTime() - startTime) / Math.pow(10,9);
    }

    public void write(String path) throws IOException {
        Path outDirectory = Paths.get(path);
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
        mapper.writer(targetFilters).writeValue(graphDirectory.toFile(), this);

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
        mapper.writer(filters).writeValue(Paths.get(matrixDirectory.toString(), "matrix.json").toFile(), this.getTargetBitmatrix());


        FileOutputStream f = new FileOutputStream(Paths.get(matrixDirectory.toString(), "bitsetList.ser").toFile());
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(this.getTargetBitmatrix().getBitmatrix());
        o.close();
        f.close();
    }

    public static TargetGraph read(String path) throws IOException, ClassNotFoundException {
        Path elaboratedDirectory = Paths.get(path, "elaborated");
        Path graphDirectory = Paths.get(elaboratedDirectory.toString(), "graph", "graph.json");
        Path nodesDirectory = Paths.get(elaboratedDirectory.toString(), "nodes");
        Path edgesDirectory = Paths.get(elaboratedDirectory.toString(), "edges");
        Path matrixDirectory = Paths.get(elaboratedDirectory.toString(), "matrix");

        ObjectMapper mapper = new ObjectMapper();
        // Target Graph
        TargetGraph graph = mapper.readValue(graphDirectory.toFile(), TargetGraph.class);

        // Tables
        graph.nodesTables = FileManager.files_reading(nodesDirectory.toString(), ',');
        graph.edgesTables = FileManager.files_reading(edgesDirectory.toString(), ',');

        // Bitmatrix
        TargetBitmatrix bitmatrix = mapper.readValue(Paths.get(matrixDirectory.toString(), "matrix.json").toFile(), TargetBitmatrix.class);
        FileInputStream fi = new FileInputStream(Paths.get(matrixDirectory.toString(), "bitsetList.ser").toFile());
        ObjectInputStream oi = new ObjectInputStream(fi);

        ArrayList<BitSet> bitsetList = (ArrayList<BitSet>) oi.readObject();
        bitmatrix.setBitmatrix(bitsetList);

        graph.setTargetBitmatrix(bitmatrix);

        return graph;
    }

    private int getEdgeLabelsColumnId(List<String> colNames) {
        for (int i = 0; i < colNames.size(); i++) {
            if (colNames.get(i).toLowerCase().contains(this.edgeLabelsColumnName)) {
                return i;
            }
        }

        return -1;
    }

    private void addEdge(Row row, List<String> header, String labelString, AtomicInteger edgeIdCount, IntColumn edgeIdColumn, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> mapKeyToEdgeList, AtomicInteger pairKeyCount, Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees, List<String> properties, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> srcDstAggregation) {
        // Source and Destination IDs
        int src = Integer.parseInt(row.getString(header.get(this.edgeSourceColumnNumber)));
        int dst = Integer.parseInt(row.getString(header.get(this.edgeDestinationColumnNumber)));

        // Edge ID
        int edgeId = edgeIdCount.getAndIncrement();
        edgeIdColumn.append(edgeId);

        // Edge label ID
        int labelId = edgesLabelsManager.addElement(edgeId, labelString);

        // This is used for the bit-matrix
        TargetUtils.add_color_for_aggregation(src, dst, labelId, srcDstAggregation);

        // This is used for the degree of each node for each label
        TargetUtils.initNodeLabelsDegrees(mapNodeIdToLabelsDegrees, src);
        TargetUtils.initNodeLabelsDegrees(mapNodeIdToLabelsDegrees, dst);

        // Each pair (src, dst) is used to get all the (directed) edges between src and dst.
        // These edges are grouped by their label.
        Int2ObjectOpenHashMap<IntArrayList> mapLabelToEdgeList;

        if (!mapKeyToEdgeList.containsKey(src)) mapKeyToEdgeList.put(src, new Int2ObjectOpenHashMap());

        if (!mapKeyToEdgeList.get(src).containsKey(dst)) {
            mapLabelToEdgeList = new Int2ObjectOpenHashMap<>();
            mapKeyToEdgeList.get(src).put(dst, mapLabelToEdgeList);
        } else {
            mapLabelToEdgeList = mapKeyToEdgeList.get(src).get(dst);
        }

        if (!mapLabelToEdgeList.containsKey(labelId)) mapLabelToEdgeList.put(labelId, new IntArrayList());
        mapLabelToEdgeList.get(labelId).add(edgeId);

        TargetUtils.increaseNodeLabelDegree(mapNodeIdToLabelsDegrees, src, labelId);
        TargetUtils.increaseNodeLabelDegree(mapNodeIdToLabelsDegrees, dst, labelId);

        // Properties
        edgesPropertiesManager.addElement(row, properties, edgeId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TARGET GRAPH\n\n");
        sb.append(nodesLabelsManager.toString()).append("\n");
        sb.append(edgesLabelsManager.toString()).append("\n");
        sb.append("NODES ").append(nodesPropertiesManager.toString());
        sb.append("EDGES ").append(edgesPropertiesManager.toString());

        return sb.toString();
    }
    // Getters and Setters
    public GraphPaths getGraphPaths() {
        return graphPaths;
    }

    public NodesLabelsManager getNodesLabelsManager() {
        return nodesLabelsManager;
    }

    public EdgesLabelsManager getEdgesLabelsManager() {
        return edgesLabelsManager;
    }

    public PropertiesManager getNodesPropertiesManager() {
        return nodesPropertiesManager;
    }

    public PropertiesManager getEdgesPropertiesManager() {
        return edgesPropertiesManager;
    }

    public String getIdsColumnName() {
        return idsColumnName;
    }

    public String getLabelsColumnName() {
        return labelsColumnName;
    }

    public int getEdgeSourceColumnNumber() {
        return edgeSourceColumnNumber;
    }

    public int getEdgeDestinationColumnNumber() {
        return edgeDestinationColumnNumber;
    }

    public String getEdgeLabelsColumnName() {
        return edgeLabelsColumnName;
    }

    public Table[] getNodesTables() {
        return nodesTables;
    }

    public Table[] getEdgesTables() {
        return edgesTables;
    }

    public TargetBitmatrix getTargetBitmatrix() {
        return targetBitmatrix;
    }

    public void setTargetBitmatrix(TargetBitmatrix targetBitmatrix) {
        this.targetBitmatrix = targetBitmatrix;
    }
}
