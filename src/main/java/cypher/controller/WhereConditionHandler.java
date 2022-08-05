package cypher.controller;

import cypher.models.QueryCondition;
import cypher.models.QueryEdge;
import cypher.models.QueryNode;
import cypher.models.WhereExpression;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.opencypher.v9_0.expressions.And;
import org.opencypher.v9_0.expressions.Expression;
import org.opencypher.v9_0.expressions.Or;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.util.Optional;

public class WhereConditionHandler {
    public static Object where_condition_handler(
            Expression expression, Table[] nodes, Table[] edges,
            Object2IntOpenHashMap<String> node_name,
            Object2IntOpenHashMap<String> edge_name,
            Int2ObjectOpenHashMap<QueryNode> query_nodes,
            Int2ObjectOpenHashMap<QueryEdge> query_edges,
            Optional<WhereConditionExtraction> where_managing
    ){
        // COMPLEX WHERE CONDITION
        if (expression instanceof And) {
            And and_cond = (And) expression;

            return new WhereExpression(
               and_cond.lhs(), and_cond.getClass().getSimpleName(), and_cond.rhs(),
               nodes, edges, node_name, edge_name, query_nodes, query_edges, where_managing
            );
        }
        else if(expression instanceof Or){
            Or or_cond   = (Or) expression;

            return new WhereExpression(
               or_cond.lhs(), or_cond.getClass().getSimpleName(), or_cond.rhs(),
               nodes, edges, node_name, edge_name, query_nodes, query_edges, where_managing
            );
        }
        // SINGLE CONDITION
        else {

            QueryCondition condition = new QueryCondition(
                    expression, nodes, edges, node_name, edge_name,
                    query_nodes, query_edges, where_managing
            );

            return condition;
        }
    }
}
