package cypher.models;

import cypher.controller.PropertiesUtility;
import org.opencypher.v9_0.expressions.*;
import scala.collection.Iterator;
import java.util.ArrayList;
import java.util.List;


public class QueryCondition {
    private String  node_name;
    private String  property_key;
    private String  operation;
    private boolean negation;
    // VALUE COULD ALSO BE ANOTHER QUERY CONDITION
    private Object  expr_value;

    // CONSTRUCTOR
    public QueryCondition(Expression expression){
        negation = false;
        conditions_init(expression);
    }

    // CONDITIONS INITIALIZATION
    private void conditions_init(Expression expression){
        if (expression instanceof Not){
            negation = true;
            expression = ((Not) expression).rhs();
        }
        // TODO regex condition managing
        this.operation                 = expression.getClass().getSimpleName();
        Iterator<Expression> arguments = expression.arguments().iterator();
        while (arguments.hasNext()){
            Expression sub_expression  = arguments.next();
            if(sub_expression instanceof Property)
                property_configuration((Property) sub_expression);
            else
                set_property_value(sub_expression);
        }
    }

    // PROPERTY DATA ELABORATION
    private void property_configuration(Property property_data){
        property_key = property_data.propertyKey().name();
        node_name    = ((Variable) property_data.map()).name();
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

    // GETTER
    public String  getNode_name()    {return node_name;   }
    public String  getProperty_key() {return property_key;}
    public String  getOperation()    {return operation;   }
    public Object  getExpr_value()   {return expr_value;  }
    public boolean isNegation()      {return negation;    }

    // TO STRING

    @Override
    public String toString() {
        return "QueryCondition{"   +
                "node_name='"      + node_name    + '\'' +
                ", property_key='" + property_key + '\'' +
                ", operation='"    + operation    + '\'' +
                ", negation="      + negation     +
                ", expr_value="    + expr_value   +
                '}';
    }
}
