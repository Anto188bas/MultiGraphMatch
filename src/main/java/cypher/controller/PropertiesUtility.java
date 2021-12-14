package cypher.controller;

import org.opencypher.v9_0.expressions.*;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PropertiesUtility {
    // SET TYPE VALUE
    public static Object value_type(Expression value){
        // LONG AND INT
        if(value instanceof SignedDecimalIntegerLiteral || value instanceof IntegerLiteral)
            return ((IntegerLiteral) value).value();
        // FLOAT AND DOUBLE
        else if(value instanceof DecimalDoubleLiteral || value instanceof DoubleLiteral)
            return ((DoubleLiteral) value).value();
        // STRING
        else if(value instanceof StringLiteral)
            return ((StringLiteral) value).value();
        else
            return null;
    }

    // NO PROPERTY ARRAY SETTING
    public static void add_property(
        Tuple2<PropertyKeyName, Expression> record,
        HashMap<String, Object> properties
    ){
        String prop_name  = record._1.name();
        Expression value  = record._2;
        Object java_value = value_type(value);
        if (java_value == null) return;
        properties.put(prop_name, java_value);
    }

    // PROPERTY ARRAY SETTING
    public static void add_array_property(
        Tuple2<PropertyKeyName, Expression> record,
        HashMap<String, Object> properties
    ){
        List<Object> par_array   = new ArrayList<>();
        String prop_name         = record._1.name();
        Iterator<Expression> exp = ((ListLiteral) record._2).expressions().iterator();
        while (exp.hasNext()){
            Expression value  = exp.next();
            Object java_value = value_type(value);
            if(java_value == null) continue;
            par_array.add(java_value);
        }
        properties.put(prop_name, par_array);
    }

    // PROPERTY SETTING
    public static void configure_properties(
        Option<Expression> expression,
        HashMap<String, Object> properties
    ){
        if (!expression.isDefined()) return;
        MapExpression map_expr = (MapExpression) expression.get();
        var props_iter= map_expr.items().iterator();
        while (props_iter.hasNext()){
            var item = props_iter.next();
            if(item._2 instanceof ListLiteral)
                PropertiesUtility.add_array_property(item, properties);
            else
                PropertiesUtility.add_property(item, properties);
        }
    }
}
