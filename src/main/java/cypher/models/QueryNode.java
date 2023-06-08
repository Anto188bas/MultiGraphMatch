package cypher.models;

import cypher.controller.PropertiesUtility;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.opencypher.v9_0.expressions.*;
import scala.Option;
import target_graph.managers.NodesLabelsManager;
import utility.Utils;

import java.util.HashMap;


public class QueryNode {
    private String node_name;
    private final IntArrayList labels;
    private final HashMap<String, Object> properties;

    private HashMap<String, QueryCondition> simpleConditions;
    private HashMap<String, QueryCondition> complexConditions;

    // private IntArrayList whereConditionsCompatibilityDomain;
    private IntOpenHashSet whereConditionsCompatibilityDomain;


    public QueryNode(NodePattern node_pattern, String name, NodesLabelsManager nodesLabelsManager) {
        labels = new IntArrayList();
        properties = new HashMap<>();
        simpleConditions = new HashMap<>();
        complexConditions = new HashMap<>();
        node_name = name;
        configureNodeLabels(node_pattern, nodesLabelsManager);
        configure_node_properties(node_pattern);
    }

    public QueryNode(NodePattern node_pattern, NodesLabelsManager nodesLabelsManager) {
        this(node_pattern, null, nodesLabelsManager);
        node_name = configure_node_name(node_pattern);
    }

    // CONFIGURE NODE VARIABLE NAME
    private String configure_node_name(NodePattern nodePattern) {
        Option<LogicalVariable> name = nodePattern.variable();
        if (!name.isDefined()) return null;
        return name.get().name();
    }

    // SET NODE LABELS NAME
    private void configureNodeLabels(NodePattern nodePattern, NodesLabelsManager nodesLabelsManager) {
        var labelsNames = nodePattern.labels().iterator();
        while (labelsNames.hasNext()) {
            String labelString = labelsNames.next().name();
            labels.add(nodesLabelsManager.getMapStringLabelToIntLabel().getInt(labelString));
        }
    }

    // SET NODE PROPERTIES
    private void configure_node_properties(NodePattern nodePattern) {
        Option<Expression> node_properties = nodePattern.properties();
        PropertiesUtility.configure_properties(node_properties, properties);
    }

    // TODO handler the duplicate code
    // EQUIVALENT TO
    public boolean equivalent_to(QueryNode other_node) {
        // LABELS CHECK
        if (!labels.equals(other_node.getLabels())) return false;
        // PROPERTIES CHECK
        HashMap<String, Object> other_node_props = other_node.getProperties();
        if (properties.size() != other_node_props.size()) return false;
        // TODO check me
        for (String key : properties.keySet()) {
            if (!other_node_props.containsKey(key) || !properties.get(key).equals(other_node_props.get(key)))
                return false;
        }
        return true;
    }


    public void addSimpleCondition(QueryCondition condition, String condition_key) {
        this.simpleConditions.put(condition_key, condition);
    }

    public void addComplexCondition(QueryCondition condition, String condition_key) {
        this.complexConditions.put(condition_key, condition);
    }

    public void clean() {
        this.simpleConditions = new HashMap<>();
        this.complexConditions = new HashMap<>();
        // this.whereConditionsCompatibilityDomain = new IntArrayList();
        this.whereConditionsCompatibilityDomain = new IntOpenHashSet();
    }

    // GETTER METHOD
    public IntArrayList getLabels() {
        return labels;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public HashMap<String, QueryCondition> getSimpleConditions() {
        return simpleConditions;
    }

    public HashMap<String, QueryCondition> getComplexConditions() {
        return complexConditions;
    }

    /*
    public IntArrayList getWhereConditionsCompatibilityDomain() {
         return whereConditionsCompatibilityDomain;
    }
    */

    public IntOpenHashSet getWhereConditionsCompatibilityDomain() {
         return whereConditionsCompatibilityDomain;
    }

    public String getNode_name() {
        return node_name;
    }

    // SETTER
    /*
    public void setWhereConditionsCompatibilityDomain(IntArrayList whereConditionsCompatibilityDomain) {
        if (this.whereConditionsCompatibilityDomain == null || this.whereConditionsCompatibilityDomain.size() == 0)
            this.whereConditionsCompatibilityDomain = whereConditionsCompatibilityDomain;
        else {
            this.whereConditionsCompatibilityDomain = Utils.intArrayListIntersection(this.whereConditionsCompatibilityDomain, whereConditionsCompatibilityDomain);
        }
    }
    */


    public void setWhereConditionsCompatibilityDomain(IntOpenHashSet whereConditionsCompatibilityDomain) {
        if (this.whereConditionsCompatibilityDomain == null || this.whereConditionsCompatibilityDomain.size() == 0)
            this.whereConditionsCompatibilityDomain = whereConditionsCompatibilityDomain;
        else {
            // this.whereConditionsCompatibilityDomain = Utils.intArrayListIntersection(this.whereConditionsCompatibilityDomain, whereConditionsCompatibilityDomain);
            this.whereConditionsCompatibilityDomain = Utils.intArraySetIntersection(this.whereConditionsCompatibilityDomain, whereConditionsCompatibilityDomain);
        }
    }


    // TO STRING
    @Override
    public String toString() {
        return "QueryNode{" + "node_name='" + node_name + '\'' + ", labels=" + labels + ", properties=" + properties + ", simpleConditions=" + simpleConditions + ", complexConditions=" + complexConditions + '}';
    }
}
