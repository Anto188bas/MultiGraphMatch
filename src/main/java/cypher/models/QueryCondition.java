package cypher.models;
import cypher.controller.PropositionStatus;
import cypher.controller.PropertiesUtility;
import cypher.controller.TypeConditionSelection;
import cypher.controller.WhereConditionExtraction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.opencypher.v9_0.expressions.*;
import scala.collection.Iterator;
import tech.tablesaw.api.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


// TODO HAVE TO BE MORE GENERIC (NODE/EDGE)
public class QueryCondition {
    private NameValue node_param;
    private String    operation;
    private boolean   negation;
    private Object    expr_value;
    private TypeConditionSelection conditionCheck;
    private final HashMap<String, String> associations;
    private int orPropositionPos;
    private int andChainPos;

    private String conditionKey;

    private PropositionStatus status;

    // CONSTRUCTOR
    public QueryCondition(
        Expression expression, Table[] nodes, Table[] edges,
        Object2IntOpenHashMap<String> node_name,
        Object2IntOpenHashMap<String> edge_name,
        Int2ObjectOpenHashMap<QueryNode> query_nodes,
        Int2ObjectOpenHashMap<QueryEdge> query_edges,
        Optional<WhereConditionExtraction> where_managing
        ){
        negation     = false;
        associations = new HashMap<>();
        associations.put("Equals",              "=");
        associations.put("GreaterThan",         ">");
        associations.put("LessThan",            "<");
        associations.put("GreaterThanOrEqual", ">=");
        associations.put("LessThanOrEqual",    "<=");
        conditions_init(expression, nodes, edges, node_name, edge_name, query_nodes, query_edges, where_managing);
    }


    // CONDITIONS INITIALIZATION
    private void conditions_init(
        Expression expression, Table[] nodes, Table[] edges,
        Object2IntOpenHashMap<String> node_name,
        Object2IntOpenHashMap<String> edge_name,
        Int2ObjectOpenHashMap<QueryNode> query_nodes,
        Int2ObjectOpenHashMap<QueryEdge> query_edges,
        Optional<WhereConditionExtraction> where_managing
        ){
        if (expression instanceof Not || expression instanceof NotEquals || expression instanceof IsNotNull) {
            negation = true;
            if (expression instanceof Not)
                expression = ((Not) expression).rhs();
        }
        this.operation                 = operator_updating(expression.getClass().getSimpleName());
        var lh_rh       = expression.arguments().toList();
        Expression lh                  = lh_rh.head();
        Expression rh                  = lh_rh.last();
        // IS NULL; IS NOT NULL
        if (lh != rh) {
            if (lh instanceof Property) {
                Property property = (Property) lh;
                Variable nodeName = (Variable) property.map();
                this.node_param   = new NameValue(nodeName.name(), property.propertyKey().name());
                if (rh instanceof Property) {
                    property = (Property) rh;
                    nodeName = (Variable) property.map();
                    this.expr_value = new NameValue(nodeName.name(), property.propertyKey().name());
                } else set_property_value(rh);
            } else {
                Property property = (Property) rh;
                Variable nodeName = (Variable) property.map();
                this.node_param = new NameValue(nodeName.name(), property.propertyKey().name());
                set_property_value(lh);
            }
        } else {
            Property property = (Property) lh;
            Variable nodeName = (Variable) property.map();
            this.node_param   = new NameValue(nodeName.name(), property.propertyKey().name());
        }
        conditionCheck = new TypeConditionSelection();
        expr_value     = conditionCheck.inferTypeCondition(nodes, edges, node_name, edge_name, this.node_param, expr_value);
        this.conditionKey = generate_condition_key();


        this.orPropositionPos = where_managing.get().getMap_condition_to_orPropositionPos().getInt(this.conditionKey);
        this.andChainPos = where_managing.get().getMapConditionToAndChainPos().getInt(this.conditionKey);
        this.status = PropositionStatus.NOT_EVALUATED;

        where_managing.get().getMapOrPropositionToConditionSet().get(this.orPropositionPos).put(this.andChainPos, this);
        where_managing.get().getMapOrPropositionToConditionSet().get(this.orPropositionPos).put(this.andChainPos, this);

        where_managing.get().getQueryConditions().add(this);
    }


    // PROPERTY DATA SET VALUE
    private void set_property_value(Expression value){
        if(value instanceof ListLiteral){
            Iterator<Expression> values = value.arguments().iterator();
            List<Object> par_array      = new ArrayList<>();
            while (values.hasNext()){
                Object java_value       = PropertiesUtility.value_type(values.next());
                if(java_value == null) continue;
                par_array.add(java_value);
            }
            expr_value = par_array;
        }
        else {
            Object java_value = PropertiesUtility.value_type(value);
            if(java_value == null) return;
            expr_value = PropertiesUtility.value_type(value);
        }
    }


    // UPDATE OPERATOR
    private String operator_updating(String operator){
        switch (operator.toLowerCase()){
            case "notequals":
            case "isnotnull":
            case "isnull":
                return "Equals";
            default:
                return operator;
        }
    }

    // GENERATE CONDITION KEY
    public String generate_condition_key(){
        String condition_key = node_param.toString();
        if(this.associations.containsKey(this.operation))
            condition_key += (this.isNegation() ? "!" : "") + associations.get(operation);
        else
            condition_key = (isNegation() ? "NOT " : "") + condition_key + " " + operation + " ";
        condition_key += expr_value.toString();
        return condition_key;
    }

    /**
     * Assign the condition to a node or to an edge depending on the ordering.
     */
    public void assign(QueryStructure queryStructure, IntArrayList nodesOrdering, IntArrayList edgesOrdering) {
        Object2IntOpenHashMap<String> mapNodeNameToID = queryStructure.getMap_node_name_to_idx();
        Object2IntOpenHashMap<String> mapEdgeNameToID = queryStructure.getMap_edge_name_to_idx();

        if(this.expr_value instanceof NameValue) { // COMPLEX QUERY CONDITION
            String firstName = this.node_param.getElementName();
            String secondName = ((NameValue) this.expr_value).getElementName();
            if (mapNodeNameToID.containsKey(firstName) && mapNodeNameToID.containsKey(secondName)) { // CONDITION ON NODES
                int firstId = mapNodeNameToID.getInt(firstName);
                int secondId = mapNodeNameToID.getInt(secondName);

                // Here we assign the condition to the node that comes after in the ordering
                if(nodesOrdering.indexOf(firstId) > nodesOrdering.indexOf(secondId)) {
                    queryStructure.getQuery_nodes().get(firstId).addCondition(this, this.conditionKey);
                } else {
                    queryStructure.getQuery_nodes().get(secondId).addCondition(this, this.conditionKey);
                }
            } else if(mapEdgeNameToID.containsKey(firstName) && mapEdgeNameToID.containsKey(secondName)) { // CONDITION ON EDGES
                int firstId = mapEdgeNameToID.getInt(firstName);
                int secondId = mapEdgeNameToID.getInt(secondName);

                // Here we assign the condition to the edge that comes first in the ordering
                if(edgesOrdering.indexOf(firstId) < edgesOrdering.indexOf(secondId)) {
                    queryStructure.getQuery_edges().get(firstId).addCondition(this, this.conditionKey);
                } else {
                    queryStructure.getQuery_edges().get(secondId).addCondition(this, this.conditionKey);
                }
            } else {
                System.err.println("This kind of condition is not handled!");
                System.exit(-1);
            }
        } else { // SIMPLE QUERY CONDITION
            if (mapNodeNameToID.containsKey(this.node_param.getElementName())) { // CONDITION ON NODE
                queryStructure.getQuery_nodes().get(mapNodeNameToID.getInt(node_param.getElementName())).addCondition(this, this.conditionKey);
            } else { // CONDITION ON EDGE
                queryStructure.getQuery_edges().get(mapEdgeNameToID.getInt(node_param.getElementName())).addCondition(this, this.conditionKey);
            }
        }
    }


    // GETTER
    public NameValue getNode_param()   {return node_param;}
    public String    getOperation()    {return operation;}
    public Object    getExpr_value()   {return expr_value;}
    public boolean   isNegation()      {return negation;}
    public TypeConditionSelection getConditionCheck()   {return conditionCheck;}
    public int getOrPropositionPos()                    {return orPropositionPos;}
    public int getAndChainPos()                         {return andChainPos;}

    public PropositionStatus getStatus() {
        return status;
    }
    // SETTER

    public void setStatus(PropositionStatus status) {
        this.status = status;
    }



    // TO STRING

    @Override
    public String toString() {
        return "QueryCondition{" +
                "node_param=" + node_param +
                ", operation='" + operation + '\'' +
                ", negation=" + negation +
                ", expr_value=" + expr_value +
                ", orPropositionPos=" + orPropositionPos +
                '}';
    }
}
