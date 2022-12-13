package target_graph.managers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Arrays;

public class NodesLabelsManager {
    private Object2IntOpenHashMap<String> mapStringLabelToIntLabel;
    private Int2ObjectOpenHashMap<String> mapIntLabelToStringLabel;
    private Int2ObjectOpenHashMap<int[]> mapElementIdToLabelSet;
    private Integer offset;

    public NodesLabelsManager() {}

    /**
     * Class constructor. This class is used to manage labels for nodes.
     *
     * @param offset
     */
    public NodesLabelsManager(int offset) {
        mapStringLabelToIntLabel = new Object2IntOpenHashMap<>();
        mapIntLabelToStringLabel = new Int2ObjectOpenHashMap<>();
        mapElementIdToLabelSet = new Int2ObjectOpenHashMap<>();

        this.offset = offset;
    }

    /**
     * Each element can have one or more labels. Initially, each label set is a string. This method converts the label set from a string to an array of strings.
     *
     * @param labelSetString
     */
    public static String[] splitLabels(String labelSetString) {
        return labelSetString.split("[:|~]");
    }

    /**
     * Add the element to the manager. After that, it will be possible to retrieve the label set of the element (starting from its id).
     *
     * @param elementId
     * @param labelSetString
     */
    public void addElement(int elementId, String labelSetString) {
        String[] stringLabels = splitLabels(labelSetString);
        int[] intLabels = Arrays.stream(stringLabels).mapToInt(label -> addLabelIfNotExists(label)).toArray();
        mapElementIdToLabelSet.put(elementId, intLabels);
    }


    /**
     * Add the label to the manager (if it's not already present) and return its id.
     *
     * @param label
     * @return
     */
    private int addLabelIfNotExists(String label) {
        if (mapStringLabelToIntLabel.containsKey(label)) {
            return mapStringLabelToIntLabel.getInt(label);
        }

        int labelId = label.equals("none") ? offset - 1 : mapStringLabelToIntLabel.size() + offset;
        mapStringLabelToIntLabel.put(label, labelId);
        mapIntLabelToStringLabel.put(labelId, label);
        return labelId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NODES LABELS MANAGER\n");
        mapElementIdToLabelSet.forEach((key, value) -> sb.append(key).append("->").append(Arrays.toString(value)).append(", "));
        sb.append("\n");
        return sb.toString();
    }

    // Getter

    public Object2IntOpenHashMap<String> getMapStringLabelToIntLabel() {
        return mapStringLabelToIntLabel;
    }

    public Int2ObjectOpenHashMap<String> getMapIntLabelToStringLabel() {
        return mapIntLabelToStringLabel;
    }

    public Int2ObjectOpenHashMap<int[]> getMapElementIdToLabelSet() {
        return mapElementIdToLabelSet;
    }

    public int getOffset() {
        return offset;
    }

}
