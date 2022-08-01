package cypher.controller;
import condition.*;
import cypher.models.NameValue;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

import java.util.ArrayList;


public class TypeConditionSelection {
    private Table      selectedTable;
    private Comparison comparison;

    private Object2ObjectOpenHashMap<String, Object> map_property_name_to_index;

    private IntIndex idIndex;

    public Object inferTypeCondition(
        Table[] nodes, Table[] edges,
        Object2IntOpenHashMap<String> node_name,
        Object2IntOpenHashMap<String> edge_name,
        NameValue leftElement,
        Object    rightElement
    ) {
        // CHECK IF THE ELEMENT IS A NODE OR AN EDGE
        //   1. NODE
        Object tableSelection;
        if (node_name.containsKey(leftElement.getElementName())) {
            tableSelection = tableSelection(leftElement.getElementKey(), nodes, rightElement);
            // ID INDEX
            this.idIndex = new IntIndex(this.selectedTable.intColumn("id"));
        }
        //   2. EDGES
        else {
            tableSelection = tableSelection(leftElement.getElementKey(), edges, rightElement);
        }

        return tableSelection;
    }

    @SuppressWarnings("unchecked")
    private Object tableSelection(
         String  property_name,
         Table[] tables,
         Object  rightElement
    ) {
        // TABLE SELECTION
        for(Table table: tables) {
            if(!table.containsColumn(property_name)) continue;
            selectedTable = table;
            break;
        }



        // COLUMN TYPE SELECTION
        // System.out.println(selectedTable.column(property_name));
        String columnType = selectedTable.column(property_name).type().name();
        switch (columnType) {
            // INTEGER ELABORATION
            case "INTEGER":
                if (rightElement instanceof ArrayList) {
                    comparison = new ArrayIntCheck();
                    ArrayList<Integer> element = new ArrayList<>();
                    ((ArrayList<String>) rightElement).forEach(str -> element.add(Integer.parseInt(str)));
                    return element;
                }
               else {
                   comparison  = new IntegerComparison();
                   return rightElement instanceof NameValue ? rightElement : Integer.parseInt(rightElement.toString());
               }
            // FLOAT ELABORATION
            case "FLOAT":
                if (rightElement instanceof ArrayList) {
                    comparison = new ArrayFloatCheck();
                    ArrayList<Float> element = new ArrayList<>();
                    ((ArrayList<String>) rightElement).forEach(str -> element.add(Float.parseFloat(str)));
                    return element;
                }
                else {
                    comparison  = new FloatComparison();
                    return rightElement instanceof NameValue ? rightElement : Float.parseFloat(rightElement.toString());
                }
            // DOUBLE ELABORATION
            case "DOUBLE":
                if (rightElement instanceof ArrayList) {
                    comparison = new ArrayDoubleCheck();
                    ArrayList<Double> element = new ArrayList<>();
                    ((ArrayList<String>) rightElement).forEach(str -> element.add(Double.parseDouble(str)));
                    return element;
                }
                else {
                    comparison  = new DoubleComparison();
                    return rightElement instanceof NameValue ? rightElement : Double.parseDouble(rightElement.toString());
                }
            // STRING ELABORATION
            default:
                if (rightElement instanceof ArrayList) {
                    comparison = new ArrayStringCheck();
                    return rightElement;
                }
                else {
                    comparison  = new StringComparison();
                    return rightElement instanceof NameValue ? rightElement : rightElement.toString();
                }
        }
    }

    protected String getColumnType(String propertyName) {
        return selectedTable.column(propertyName).type().name();
    }


    public Object getProperty(int id, String propertyName) {
        int columnIndex = this.selectedTable.columnIndex(propertyName);
        return this.selectedTable.where(this.idIndex.get(id)).get(0, columnIndex);
    }

    public Object getLabels(int id) {
        int columnIndex = this.selectedTable.columnIndex("labels");
        return this.selectedTable.where(this.idIndex.get(id)).get(0, columnIndex);
    }

    public Table getSelectedTable()   {return selectedTable;}
    public Comparison getComparison() {return comparison;}
}
