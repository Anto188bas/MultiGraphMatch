package target_graph.graph;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;
import target_graph.managers.PropertiesManager;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.List;
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

public class TargetGraph {
    private final String idsColumnName;
    private final String labelsColumnName;
    private final int edgeSourceColumnNumber = 0;
    private final int edgeDestinationColumnNumber = 1;
    private final String edgeLabelsColumnName = "type";

    GraphPaths graphPaths;


    private NodesLabelsManager nodesLabelsManager;
    private EdgesLabelsManager edgesLabelsManager;
    private PropertiesManager nodesPropertiesManager;
    private PropertiesManager edgesPropertiesManager;

    private final Table[] nodesTables;
    private final Table[] edgesTables;

    // TODO: remove it!
    private Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> srcDstAggregation;

    public TargetGraph(Table[] nodesTables, Table[] edgesTables, String idsColumnName, String labelsColumnName) {
        graphPaths = new GraphPaths();

        this.nodesLabelsManager = new NodesLabelsManager(0);
        this.edgesLabelsManager = new EdgesLabelsManager(0);

        this.nodesPropertiesManager = new PropertiesManager(idsColumnName);
        this.edgesPropertiesManager = new PropertiesManager(idsColumnName);

        this.nodesTables = nodesTables;
        this.edgesTables = edgesTables;

        this.idsColumnName = idsColumnName;
        this.labelsColumnName = labelsColumnName;


        // Nodes
        for (int i = 0; i < nodesTables.length; i++) {
            Table currentTable = nodesTables[i];

            List<String> properties = currentTable.columnNames().stream().filter(colName -> !colName.equals(this.idsColumnName) && !colName.equals(this.labelsColumnName)).toList();

            nodesPropertiesManager.addProperties(properties);

            currentTable.forEach(row -> {
                int id = row.getInt(this.idsColumnName);
                String labelSetString = row.getString(this.labelsColumnName);

                nodesLabelsManager.addElement(id, labelSetString);
                nodesPropertiesManager.addElement(row, properties, -1);
            });
        }

        // Edges
        Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees = new Int2ObjectOpenHashMap<>();

        AtomicInteger edgeIdCount = new AtomicInteger(0);
        AtomicInteger pairKeyCount = new AtomicInteger(0);

        Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapPairToKey = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> mapKeyToEdgeList = new Int2ObjectOpenHashMap<>();

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
                currentTable.forEach(row -> addEdge(row, header, row.getString(header.get(labelsColumnNumber)), edgeIdCount, edgeIdColumn, mapPairToKey, mapKeyToEdgeList, pairKeyCount, srcDstAggregation, mapNodeIdToLabelsDegrees, properties));
                // We remove sources, destinations and label sets
                currentTable.removeColumns(header.get(this.edgeSourceColumnNumber), header.get(this.edgeDestinationColumnNumber), this.edgeLabelsColumnName);
            } else { // Labels aren't defined
                // Properties
                List<String> properties = header.stream().filter(colName -> !colName.equals(this.edgeLabelsColumnName) && !colName.equals(sourcesColumnName) && !colName.equals(destinationsColumnName)).toList();
                edgesPropertiesManager.addProperties(properties);
                // Add all the edges
                currentTable.forEach(row -> addEdge(row, header, "none", edgeIdCount, edgeIdColumn, mapPairToKey, mapKeyToEdgeList, pairKeyCount, srcDstAggregation, mapNodeIdToLabelsDegrees, properties));
                // We remove sources and destinations
                currentTable.removeColumns(header.get(this.edgeSourceColumnNumber), header.get(this.edgeDestinationColumnNumber));
            }
            // We add the edge ID column
            currentTable.addColumns(edgeIdColumn);
        }

        this.srcDstAggregation = srcDstAggregation;
        this.graphPaths = new GraphPaths(mapPairToKey, mapKeyToEdgeList, this.edgesLabelsManager.getMapIntLabelToStringLabel().size() ,mapKeyToEdgeList.size(), mapNodeIdToLabelsDegrees);
    }

    private int getEdgeLabelsColumnId(List<String> colNames) {
        for (int i = 0; i < colNames.size(); i++) {
            if (colNames.get(i).toLowerCase().contains(this.edgeLabelsColumnName)) {
                return i;
            }
        }

        return -1;
    }

    private void addEdge(Row row, List<String> header, String labelString, AtomicInteger edgeIdCount, IntColumn edgeIdColumn, Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapPairToKey, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> mapKeyToEdgeList, AtomicInteger pairKeyCount, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> srcDstAggregation, Int2ObjectOpenHashMap<Int2IntOpenHashMap> mapNodeIdToLabelsDegrees, List<String> properties) {
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

        // Each pair (src, dst) is identified by a key.
        // We use this key to get all the (directed) edges between src and dst.
        // These edges are grouped by their label.
        int pairKey;

        if (!mapPairToKey.containsKey(src)) mapPairToKey.put(src, new Int2IntOpenHashMap());

        if (!mapPairToKey.get(src).containsKey(dst)) {
            pairKey = pairKeyCount.getAndIncrement();
            mapPairToKey.get(src).put(dst, pairKey);
            mapKeyToEdgeList.put(pairKey, new Int2ObjectOpenHashMap<>());
        } else {
            pairKey = mapPairToKey.get(src).get(dst);
        }

        Int2ObjectOpenHashMap<IntArrayList> mapLabelToEdgeList = mapKeyToEdgeList.get(pairKey);
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
    // Getter

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

    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> getSrcDstAggregation() {
        return srcDstAggregation;
    }

}
