package cypher.models;

import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.*;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.List;


public class QueryReturn {
    //
    private final List<String[]> properties_map;
    private final List<String[]> function_map;
    private final List<String[]> orderby;
    private boolean is_distinct;
    private boolean is_count;
    private long limit;


    // CONSTRUCTOR
    public QueryReturn() {
        properties_map = new ArrayList<>();
        function_map = new ArrayList<>();
        orderby = new ArrayList<>();
        is_count = false;
    }

    // SET PROPERTY DATA
    private String[] create_array(String... items) {
        String[] record = new String[items.length];
        System.arraycopy(items, 0, record, 0, items.length);
        return record;
    }

    // 1.
    private String[] set_property_data(Property property, String alias_or_orderby_type) {
        Variable variable = (Variable) property.map();
        //                                     (n1:Label {x:"value"})
        //                         n1                   x
        //                    element_name         property_name                   defined_alias
        return create_array(variable.name(), property.propertyKey().name(), alias_or_orderby_type);
    }

    // 2.
    private void set_property_data(Tuple2<PropertyKeyName, Expression> property, String alias_or_orderby_type) {
        Variable variable;
        String[] data;
        if (property._2 instanceof Property) {
            Property prop = (Property) property._2;
            variable = ((Variable) prop.map());
            //                                {name: n1.nm, --- } AS person
            //                        n1                 name                  nm                     person
            //                    node_name          property_name          json_field_name         defined_alias
            data = create_array(variable.name(), prop.propertyKey().name(), property._1.name(), alias_or_orderby_type);
        } else {
            variable = (Variable) property._2;
            //                                 {nodes:elab_node ---} AS network
            //                            elab_node               nodes                network
            data = create_array(variable.name(), "", property._1.name(), alias_or_orderby_type);
        }
        this.properties_map.add(data);
    }

    // 3.
    private void set_property_data(Variable name_pr, String alias_or_orderby_type) {
        //  RETURN n2 as node_2
        //                                              n2                     node_2
        this.properties_map.add(create_array(name_pr.name(), "", alias_or_orderby_type));
    }

    // PROPERTY-ALIAS ASSOCIATION
    private void create_prop_alias_array(Expression expression, String item_name) {
        // PROPERTY PART -> n1.name AS n1_name
        if (expression instanceof Property)
            this.properties_map.add(set_property_data((Property) expression, item_name));
            // JSON PART -> {name:n1.name, age: n1.age}
        else if (expression instanceof MapExpression) {
            MapExpression map_expression = (MapExpression) expression;
            var map_elements = map_expression.items().iterator();
            while (map_elements.hasNext()) set_property_data(map_elements.next(), item_name);
        }
        // FUNCTION PART -> nodes(p), relationships(p), ID(n), etc
        else if (expression instanceof FunctionInvocation) {
            FunctionInvocation func_exp = (FunctionInvocation) expression;
            if (func_exp.name().equalsIgnoreCase("count")) this.is_count = true;
            Iterator<Expression> args = func_exp.args().iterator();
            StringBuilder arguments = new StringBuilder(((Variable) args.next()).name());
            while (args.hasNext()) arguments.append(",").append(((Variable) args.next()).name());
            this.function_map.add(create_array(arguments.toString(), func_exp.name(), item_name));
        }
        // VARIABLE PART -> n1, r1, etc
        else if (expression instanceof Variable) set_property_data((Variable) expression, item_name);
    }

    // SET LIMIT VALUE
    private void limit_configuration(Return ret_obj) {
        Option<Limit> limit = ret_obj.limit();
        if (!limit.isDefined()) this.limit = 1000;
        else {
            SignedDecimalIntegerLiteral value = (SignedDecimalIntegerLiteral) limit.get().expression();
            this.limit = value.value();
        }
    }

    // SET ORDER BY IF EXISTS
    private void order_by_setting(Return ret_obj) {
        Option<OrderBy> order_by = ret_obj.orderBy();
        if (!order_by.isDefined()) return;
        Iterator<SortItem> sortItems = order_by.get().sortItems().iterator();
        while (sortItems.hasNext()) {
            SortItem item = sortItems.next();
            String order_type = item instanceof DescSortItem ? "DESC" : "ASC";
            this.orderby.add(set_property_data((Property) item.expression(), order_type));
        }
    }

    // RETURN OBJECT ELABORATION
    public void return_elaboration(Return ret_obj) {
        limit_configuration(ret_obj);
        order_by_setting(ret_obj);
        is_distinct = ret_obj.distinct();
        ReturnItemsDef return_item_def = ret_obj.returnItems();
        Iterator<ReturnItem> items = return_item_def.items().iterator();
        // TODO MANAGE THE NAMED MATCH CASE
        while (items.hasNext()) {
            ReturnItem item = items.next();
            create_prop_alias_array(item.expression(), item.name());
        }
    }

    // GETTER
    public List<String[]> getProperties_map() {
        return properties_map;
    }

    public List<String[]> getFunction_map() {
        return function_map;
    }

    public List<String[]> getOrderby() {
        return orderby;
    }

    public boolean isIs_distinct() {
        return is_distinct;
    }

    public long getLimit() {
        return limit;
    }

    public boolean isCount() {
        return is_count;
    }

    // TODO COMPLETE TO STRING

    @Override
    public String toString() {
        return "QueryReturn{" + "properties_map=" + properties_map + ", function_map=" + function_map + ", orderby=" + orderby + ", is_distinct=" + is_distinct + ", is_count=" + is_count + ", limit=" + limit + '}';
    }
}
