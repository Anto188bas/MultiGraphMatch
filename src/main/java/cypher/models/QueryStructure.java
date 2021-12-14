package cypher.models;

import cypher.controller.WhereConditionHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;
import scala.collection.Iterator;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import java.util.LinkedList;


public class QueryStructure {
    private final Int2ObjectOpenHashMap<QueryNode> query_nodes;
    private final Object2IntOpenHashMap<String>    node_name_idx;
    private final Int2ObjectOpenHashMap<QueryEdge> query_edges;
    private final QueryEdgeAggregation             query_pattern;

    public QueryStructure(){
        query_nodes   = new Int2ObjectOpenHashMap<>();
        node_name_idx = new Object2IntOpenHashMap<>();
        query_edges   = new Int2ObjectOpenHashMap<>();
        query_pattern = new QueryEdgeAggregation();
    }

    // PARSER FUNCTION
    public void parser(String query, NodesEdgesLabelsMaps label_type_map){
        CypherParser parser      = new CypherParser();
        Query query_obj          = (Query) parser.parse(query, null);
        if(!(query_obj.part() instanceof SingleQuery)) return;
        SingleQuery single_query = (SingleQuery) query_obj.part();
        // MATCHING AND RESULT ELABORATION
        Iterator<Clause> clauses = single_query.clauses().iterator();
        while (clauses.hasNext()){
            Clause clause = clauses.next();
            if(clause instanceof Match){
                match_handler(clause, label_type_map);
                Option<Where> where_conditions = ((Match) clause).where();
                if(!where_conditions.isDefined()) continue;
                Object conditions = WhereConditionHandler.where_condition_handler(where_conditions.get().expression());
                // TODO complete conditions
            }
            else if(clause instanceof Return){
                QueryReturn query_return = new QueryReturn();
                query_return.return_elaboration((Return) clause);
                // TODO implement me
            }
        }
    }

    // MATCH PART ELABORATION
    private void match_handler(Clause clause, NodesEdgesLabelsMaps label_type_map){
        Pattern pattern = ((Match) clause).pattern();
        Iterator<PatternPart> pattern_parts = pattern.patternParts().iterator();
        while (pattern_parts.hasNext()){
            PatternPart    pattern_part    = pattern_parts.next();
            // TODO manage named pattern
            String         named_pattern   = get_pattern_name(pattern_part);
            PatternElement pattern_element = pattern_part.element();
            if (pattern_element.isSingleNode())
                node_manager((NodePattern) pattern_element, label_type_map);
            else{
                LinkedList<Integer> nodes_ids = new LinkedList<>();
                LinkedList<Integer> edges_ids = new LinkedList<>();
                pattern_elaboration(pattern_element, nodes_ids, edges_ids, label_type_map);
                this.query_pattern.create_aggregation(nodes_ids, edges_ids, this.query_edges);

                for (int i = 0; i<edges_ids.size(); i++){
                    int edg_id = edges_ids.get(i);
                    // TODO fixed me
                    // int type = query_edges.get(edg_id).getEdge_label();
                    // query_pattern.put(nodes_ids.get(i), nodes_ids.get(i+1), edg_id, type);
                }
            }
        }
    }

    // GET PATTERN NAME IN EXISTS
    private String get_pattern_name(PatternPart pattern_part) {
        if (!(pattern_part instanceof NamedPatternPart)) return null;
        NamedPatternPart pattern = (NamedPatternPart) pattern_part;
        return pattern.variable().name();
    }

    // NODE CREATION
    private int node_manager(NodePattern nodePattern, NodesEdgesLabelsMaps label_type_map){
        Option<LogicalVariable> name = nodePattern.variable();
        // NODE MANE NOT DEFINED (:LABEL {PROPS}). SO, EACH ELEMENT IS A NEW NODE.
        if(!name.isDefined()){
            QueryNode node = new QueryNode(nodePattern, null);
            int id         = query_nodes.size();
            query_nodes.put(id, node);
            return id;
        }
        String name_str = name.get().name();
        // NODE HAS ALREADY BEEN CREATED
        if (node_name_idx.containsKey(name_str))
            return node_name_idx.getInt(name_str);
        // NODE HAVE TO BE CREATED
        QueryNode node  = new QueryNode(nodePattern, name_str, label_type_map);
        int id          = query_nodes.size();
        query_nodes.put(id, node);
        node_name_idx.put(name_str, id);
        return id;
    }

    // EDGE CREATION
    private int edge_manager(RelationshipPattern relationship, NodesEdgesLabelsMaps label_type_map){
        int id = query_edges.size();
        System.out.println(relationship);
        query_edges.put(id, new QueryEdge(relationship, label_type_map));
        return id;
    }

    // PATTERN ELABORATION
    private void pattern_elaboration(
        PatternElement       patternElement,
        LinkedList<Integer>  nodes_ids,
        LinkedList<Integer>  edges_ids,
        NodesEdgesLabelsMaps label_type_map
    ){
        RelationshipChain relationshipChain = (RelationshipChain) patternElement;
        // RIGHT ELEMENT
        nodes_ids.addFirst(node_manager(relationshipChain.rightNode(), label_type_map));
        edges_ids.addFirst(edge_manager(relationshipChain.relationship(), label_type_map));
        // LEFT ELEMENT
        PatternElement new_pattern = relationshipChain.element();
        if(new_pattern instanceof RelationshipChain)
           pattern_elaboration(new_pattern, nodes_ids, edges_ids, label_type_map);
        else if(new_pattern instanceof NodePattern)
            nodes_ids.addFirst(node_manager((NodePattern) new_pattern, label_type_map));
    }

    // GETTER
    public Int2ObjectOpenHashMap<QueryNode> getQuery_nodes()   {return query_nodes;  }
    public Object2IntOpenHashMap<String>    getNode_name_idx() {return node_name_idx;}
    public Int2ObjectOpenHashMap<QueryEdge> getQuery_edges()   {return query_edges;  }
    public QueryEdgeAggregation             getQuery_pattern() {return query_pattern;}

    // TO STRING

    @Override
    public String toString() {
        return "QueryStructure{"   + "\n"          +
                "query_nodes="     + query_nodes   + "\n" +
                ", node_name_idx=" + node_name_idx + "\n" +
                ", query_edges="   + query_edges   + "\n" +
                ", query_pattern=" + query_pattern + "\n" +
                '}';
    }
}
