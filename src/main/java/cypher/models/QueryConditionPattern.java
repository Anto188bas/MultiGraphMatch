package cypher.models;
import cypher.controller.WhereConditionExtraction;
import  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import  org.opencypher.v9_0.expressions.*;
import  scala.Option;
import  target_graph.propeties_idx.NodesEdgesLabelsMaps;
import  java.util.LinkedList;
import java.util.Optional;


public class QueryConditionPattern {
    private boolean                                is_negation;
    private final Int2ObjectOpenHashMap<QueryNode> query_node;
    private final Int2ObjectOpenHashMap<QueryEdge> query_edge;
    private final Object2IntOpenHashMap<String>    node_name_idx;
    private final Object2IntOpenHashMap<String>    edge_name_idx;
    private final Int2ObjectOpenHashMap<int[]>     edge_nodes;


    public QueryConditionPattern(
        Expression                         expression,
        NodesEdgesLabelsMaps               label_type_map,
        Optional<WhereConditionExtraction> where_managing
    ){
        query_node    = new Int2ObjectOpenHashMap<>();
        query_edge    = new Int2ObjectOpenHashMap<>();
        node_name_idx = new Object2IntOpenHashMap<>();
        edge_name_idx = new Object2IntOpenHashMap<>();
        edge_nodes    = new Int2ObjectOpenHashMap<>();
        pattern_condition_init(expression, label_type_map);
        where_managing.ifPresent(whereConditionExtraction -> whereConditionExtraction.getQueryPatternCondition().add(this));
    }

    // METHODS
    // 1.
    private int create_node_id(NodePattern node, NodesEdgesLabelsMaps label_type_map) {
        Option<LogicalVariable> name = node.variable();
        if(!name.isDefined()) {
            QueryNode node_obj = new QueryNode(node, null);
            int id             = query_node.size();
            query_node.put(id, node_obj);
            return id;
        }
        String name_str = name.get().name();
        // NODE HAS ALREADY BEEN CREATED
        if (node_name_idx.containsKey(name_str)) return node_name_idx.getInt(name_str);
        // NODE HAVE TO BE CREATED
        QueryNode node_obj  = new QueryNode(node, name_str, label_type_map);
        int id              = query_node.size();
        query_node.put(id, node_obj);
        node_name_idx.put(name_str, id);
        return id;
    }

    // 2.
    private int create_edge_id(RelationshipPattern relationship, NodesEdgesLabelsMaps label_type_map){
        int id = query_edge.size();
        query_edge.put(id, new QueryEdge(relationship, label_type_map));
        if(query_edge.get(id).getEdge_name() == null) query_edge.get(id).setEdge_name(id);
        edge_name_idx.put(query_edge.get(id).getEdge_name(), id);
        return id;
    }

    // 3.
    private void pattern_elaboration(
        RelationshipChain   relationship, NodesEdgesLabelsMaps label_type_map,
        LinkedList<Integer> id_nodes,     LinkedList<Integer>  id_edges
    ){
        // RIGHT NODE
        id_nodes.addFirst(create_node_id(relationship.rightNode(),    label_type_map));
        // RELATIONSHIP
        id_edges.addFirst(create_edge_id(relationship.relationship(), label_type_map));
        // LEFT ELEMENT
        PatternElement tmp = relationship.element();
        if (tmp instanceof RelationshipChain)
            pattern_elaboration((RelationshipChain) tmp, label_type_map, id_nodes, id_edges);
        else id_nodes.addFirst(create_node_id((NodePattern) tmp, label_type_map));
    }

    // 4.
    private void pattern_condition_init(Expression expression, NodesEdgesLabelsMaps label_type_map){
        LinkedList<Integer>  id_nodes  = new LinkedList<>();
        LinkedList<Integer>  id_edges  = new LinkedList<>();

        // CHECK IF 'NOT' IN EXPRESSION
        if (expression instanceof Not) {
            is_negation = true;
            expression  = ((Not) expression).rhs();
        }

        // PROCESSING
        if (expression instanceof PatternExpression) {
            RelationshipsPattern patExpr = ((PatternExpression) expression).pattern();
            var prodIter  = patExpr.productIterator();
            while (prodIter.hasNext())
                pattern_elaboration((RelationshipChain) prodIter.next(), label_type_map, id_nodes, id_edges);
            for(int i=0; i<id_nodes.size() - 2; i++){
                int id_edge = id_edges.get(i);
                edge_nodes.put(id_edge, new int[2]);
                edge_nodes.get(id_edge)[0] = id_nodes.get(i);
                edge_nodes.get(id_edge)[1] = id_nodes.get(i+1);
            }
        }
        // TODO ADD LAST CASE NOT((n1)-[r1]->(n2), (n2)-[r2]->(n3))
    }


    // GETTER
    public boolean                          isIs_negation()    {return is_negation;   }
    public Int2ObjectOpenHashMap<QueryNode> getQuery_node()    {return query_node;    }
    public Int2ObjectOpenHashMap<QueryEdge> getQuery_edge()    {return query_edge;    }
    public Object2IntOpenHashMap<String>    getNode_name_idx() {return node_name_idx; }
    public Object2IntOpenHashMap<String>    getEdge_name_idx() {return edge_name_idx; }
    public Int2ObjectOpenHashMap<int[]>     getEdgeNodes()     {return edge_nodes;    }
}
