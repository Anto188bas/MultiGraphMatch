package cypher.models;


import cypher.controller.WhereConditionExtraction;
import cypher.controller.WhereConditionHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.opencypher.v9_0.expressions.Expression;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.util.Optional;

public class WhereExpression {
    // left_expression and right one could be: QueryCondition or WhereExpression
    private final Object  left_expression;
    private final String  conj_operator;
    private final Object  right_expression;

    // CONSTRUCTOR
    public WhereExpression(
            Expression lft, String cj_op, Expression rft, Table[] nodes, Table[] edges,
            Object2IntOpenHashMap<String>      node_name,
            Object2IntOpenHashMap<String>      edge_name,
            Int2ObjectOpenHashMap<QueryNode>   query_nodes,
            Int2ObjectOpenHashMap<QueryEdge>   query_edges,
            Optional<WhereConditionExtraction> where_managing,
            NodesLabelsManager nodesLabelsManager,
            EdgesLabelsManager edgesLabelsManager
    ){
        left_expression  = WhereConditionHandler.handleWhereCondition(lft, nodes, edges, node_name, edge_name, query_nodes, query_edges, where_managing, nodesLabelsManager, edgesLabelsManager);
        conj_operator    = cj_op;
        right_expression = WhereConditionHandler.handleWhereCondition(rft, nodes, edges, node_name, edge_name, query_nodes, query_edges, where_managing, nodesLabelsManager, edgesLabelsManager);
    }

    // GETTER
    public Object getLeft_expression()  {return left_expression; }
    public String getConj_operator()    {return conj_operator;   }
    public Object getRight_expression() {return right_expression;}

    // TOSTRING

    @Override
    public String toString() {
        return "WhereExpression{"     +
                "left_expression="    + left_expression +
                ", conj_operator='"   + conj_operator + '\'' +
                ", right_expression=" + right_expression +
                '}';
    }
}
