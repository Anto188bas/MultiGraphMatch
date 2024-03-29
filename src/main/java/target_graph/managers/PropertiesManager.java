package target_graph.managers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import tech.tablesaw.api.Row;

import java.util.List;

public class PropertiesManager  {
    private String idsColumnName;
    private ObjectArraySet<String> propertySet;
    private Object2IntOpenHashMap<String> mapPropertyStringToPropertyId;
    private Int2ObjectOpenHashMap<String> mapPropertyIdToPropertyString;

    // private Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Object, IntArrayList>> mapPropertyIdToValues;
    private Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Object, IntOpenHashSet>> mapPropertyIdToValues;

    public PropertiesManager() {}

    public PropertiesManager(String idsColumnName) {
        this.idsColumnName = idsColumnName;
        propertySet = new ObjectArraySet<>();
        mapPropertyStringToPropertyId = new Object2IntOpenHashMap<>();
        mapPropertyIdToPropertyString = new Int2ObjectOpenHashMap<>();

        mapPropertyIdToValues = new Int2ObjectOpenHashMap<>();
    }

    public void addProperties(List<String> properties) {
        properties.forEach(property -> {
            if (!propertySet.contains(property)) {
                propertySet.add(property);
                int id = propertySet.size() - 1;
                mapPropertyStringToPropertyId.put(property, id);
                mapPropertyIdToPropertyString.put(id, property);

                mapPropertyIdToValues.put(id, new Object2ObjectOpenHashMap<>());
            }
        });
    }

    /**
     * We use this function both for nodes and edges.
     * When we call this function for nodes, we can retrieve its id from the row (so, we pass -1 as id).
     * When we call this function for edges, we can't retrieve its id from the row (so, we pass the id as parameter).
     *
     * @param elementRow
     * @param properties
     * @param id
     */
    public void addElement(Row elementRow, List<String> properties, int id) {
        int elementId;
        if (id == -1) {
            elementId = elementRow.getInt(idsColumnName);
        } else {
            elementId = id;
        }

        properties.forEach(property -> {
            int propertyId = mapPropertyStringToPropertyId.getInt(property);

            Object value = elementRow.getObject(property);

            // Object2ObjectOpenHashMap<Object,IntArrayList> mapValueToElementIds = mapPropertyIdToValues.get(propertyId);
            var mapValueToElementIds = mapPropertyIdToValues.get(propertyId);

            if (!mapValueToElementIds.containsKey(value)) {
                // mapValueToElementIds.put(value, new IntArrayList());
                mapValueToElementIds.put(value, new IntOpenHashSet());
            }

            mapValueToElementIds.get(value).add(elementId);
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PROPERTIES MANAGER\n");
        sb.append("Properties: ").append(propertySet).append("\n");
        mapPropertyStringToPropertyId.forEach((property, id) -> {
            sb.append("\t").append(property).append("\n");
            mapPropertyIdToValues.get(id.intValue()).forEach((value, elementIds) -> {
                sb.append("\t\t").append(value).append(": ").append(elementIds).append("\n");
            });
        });
        return sb.toString();
    }

    // Getter

    public ObjectArraySet<String> getPropertySet() {
        return propertySet;
    }

    public Object2IntOpenHashMap<String> getMapPropertyStringToPropertyId() {
        return mapPropertyStringToPropertyId;
    }

    public Int2ObjectOpenHashMap<String> getMapPropertyIdToPropertyString() {
        return mapPropertyIdToPropertyString;
    }

    /*
    public Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Object, IntArrayList>> getMapPropertyIdToValues() {
        return mapPropertyIdToValues;
    }
    */


    public Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Object, IntOpenHashSet>> getMapPropertyIdToValues() {
        return mapPropertyIdToValues;
    }


    public String getIdsColumnName() {
        return idsColumnName;
    }
}
