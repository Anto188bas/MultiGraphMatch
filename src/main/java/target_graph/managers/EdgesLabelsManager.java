package target_graph.managers;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class EdgesLabelsManager {
    private Object2IntOpenHashMap<String> mapStringLabelToIntLabel;
    private Int2ObjectOpenHashMap<String> mapIntLabelToStringLabel;
    private Int2IntOpenHashMap mapElementIdToLabel;
    private Integer offset;

    public EdgesLabelsManager() {}

    /**
     * Class constructor. This class is used to manage labels for edges.
     *
     * @param offset
     */
    public EdgesLabelsManager(int offset) {
        mapStringLabelToIntLabel = new Object2IntOpenHashMap<>();
        mapIntLabelToStringLabel = new Int2ObjectOpenHashMap<>();
        mapElementIdToLabel = new Int2IntOpenHashMap();

        this.offset = offset;
    }


    /**
     * Add the element to the manager. After that, it will be possible to retrieve the label of the element (starting from its id).
     *
     * @param elementId
     * @param labelString
     */
    public int addElement(int elementId, String labelString) {
        int intLabel = addLabelIfNotExists(labelString);
        mapElementIdToLabel.put(elementId, intLabel);

        return intLabel;
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
        sb.append("EDGES LABELS MANAGER\n");
        mapElementIdToLabel.forEach((key, value) -> sb.append(key).append("->").append(mapIntLabelToStringLabel.get(value.intValue())).append(", "));
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

    public Int2IntOpenHashMap getMapElementIdToLabel() {
        return mapElementIdToLabel;
    }

    public Integer getOffset() {
        return offset;
    }
}
