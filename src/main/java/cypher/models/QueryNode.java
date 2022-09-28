package cypher.models;

import cypher.controller.PropertiesUtility;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.opencypher.v9_0.expressions.*;
import scala.Option;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import utility.Utils;

import java.util.HashMap;


public class QueryNode {
    private String                          node_name;
    private final IntArrayList              labels;
    private final HashMap<String, Object>   properties;
    private HashMap<String, QueryCondition> conditions;
    private IntArraySet whereConditionsCompatibilityDomain;

    public QueryNode(NodePattern node_pattern, String name, NodesEdgesLabelsMaps label_type_map){
        labels     = new IntArrayList();
        properties = new HashMap<>();
        conditions = new HashMap<>();
        node_name  = name;
        configure_node_labels(node_pattern, label_type_map);
        configure_node_properties(node_pattern);
    }

    public QueryNode(NodePattern node_pattern, NodesEdgesLabelsMaps label_type_map){
        this(node_pattern, null, label_type_map);
        node_name  = configure_node_name(node_pattern);
    }

    // CONFIGURE NODE VARIABLE NAME
    private String configure_node_name(NodePattern nodePattern){
        Option<LogicalVariable> name = nodePattern.variable();
        if(!name.isDefined()) return null;
        return name.get().name();
    }

    // SET NODE LABELS NAME
    private void configure_node_labels(NodePattern nodePattern, NodesEdgesLabelsMaps label_type_map){
        var labels_names = nodePattern.labels().iterator();
        while (labels_names.hasNext()){
            String label_name = labels_names.next().name();
            labels.add(label_type_map.getLabelIdxNode(label_name));
        }
    }

    // SET NODE PROPERTIES
    private void configure_node_properties(NodePattern nodePattern){
        Option<Expression> node_properties = nodePattern.properties();
        PropertiesUtility.configure_properties(node_properties, properties);
    }

    // TODO handler the duplicate code
    // EQUIVALENT TO
    public boolean equivalent_to(QueryNode other_node) {
        // LABELS CHECK
        if (!labels.equals(other_node.getLabels()))         return false;
        // PROPERTIES CHECK
        HashMap<String, Object> other_node_props = other_node.getProperties();
        if (properties.size() != other_node_props.size())   return false;
        // TODO check me
        for (String key: properties.keySet()){
            if (
                 !other_node_props.containsKey(key) ||
                 !properties.get(key).equals(other_node_props.get(key))
            ) return false;
        }
        return true;
    }

    public void addCondition(QueryCondition condition, String condition_key) {
        this.conditions.put(condition_key, condition);
    }

    // GETTER METHOD
    public IntArrayList                    getLabels()     {return labels;    }
    public HashMap<String, Object>         getProperties() {return properties;}
    public HashMap<String, QueryCondition> getConditions() {return conditions;}

    public IntArraySet getWhereConditionsCompatibilityDomain() {
        return whereConditionsCompatibilityDomain;
    }

    // SETTER
    public void setWhereConditionsCompatibilityDomain(IntArraySet whereConditionsCompatibilityDomain) {
        if(this.whereConditionsCompatibilityDomain == null || this.whereConditionsCompatibilityDomain.size() == 0)
            this.whereConditionsCompatibilityDomain = whereConditionsCompatibilityDomain;
        else {
            this.whereConditionsCompatibilityDomain = Utils.intArraySetIntersection(this.whereConditionsCompatibilityDomain, whereConditionsCompatibilityDomain);
        }
    }

    // TO STRING
    @Override
    public String toString() {
        return "QueryNode{" +
                "node_name='" + node_name + '\'' +
                ", labels=" + labels +
                ", properties=" + properties +
                ", conditions=" + conditions +
                '}';
    }
}
