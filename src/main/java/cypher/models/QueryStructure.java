package cypher.models;

import cypher.controller.WhereConditionHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import ordering.NodesPair;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;
import scala.collection.Iterator;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class QueryStructure {
    private final Int2ObjectOpenHashMap<QueryNode>                  query_nodes;
    private final Object2IntOpenHashMap<String>                     node_name_idx;
    private final Int2ObjectOpenHashMap<QueryEdge>                  query_edges;
    private final QueryEdgeAggregation                              query_pattern;
    private final ObjectArraySet<NodesPair>                         pairs;
    private final Int2ObjectOpenHashMap<IntArraySet>                map_endpoints_to_edges;
    private final Int2ObjectOpenHashMap<NodesPair>                  map_edge_to_endpoints;
    private final Int2ObjectOpenHashMap<IntArraySet>                map_node_to_neighborhood;
    private final Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>>  map_pair_to_neighborhood;

    public QueryStructure(){
        query_nodes                 = new Int2ObjectOpenHashMap<>();
        node_name_idx               = new Object2IntOpenHashMap<>();
        query_edges                 = new Int2ObjectOpenHashMap<>();
        query_pattern               = new QueryEdgeAggregation();
        pairs                       = new ObjectArraySet<>();
        map_endpoints_to_edges      = new Int2ObjectOpenHashMap<>();
        map_edge_to_endpoints       = new Int2ObjectOpenHashMap<>();
        map_node_to_neighborhood    = new Int2ObjectOpenHashMap<>();
        map_pair_to_neighborhood    = new Int2ObjectOpenHashMap<>();
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
            }
        }
        pairs_elaboration();
        neighborhoods_elaboration();
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
        //System.out.println(relationship);
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

    private void pairs_elaboration() {
        for (int edge_key : query_edges.keySet()) {
            NodesPair endpoints = getEdgeEndpoints(query_pattern.getOut_edges(), edge_key);

            if (endpoints == null) { // Undirected edge
                endpoints = getEdgeEndpoints(query_pattern.getIn_out_edges(), edge_key);
            } // Else is a directed edge

            if(map_endpoints_to_edges.containsKey(endpoints.getId().intValue())) {
                map_endpoints_to_edges.get(endpoints.getId().intValue()).add(edge_key);
            } else {
                IntArraySet edge_set = new IntArraySet();
                edge_set.add(edge_key);
                map_endpoints_to_edges.put(endpoints.getId().intValue(), edge_set);
            }

            map_edge_to_endpoints.put(edge_key, endpoints);

            pairs.add(endpoints);
        }
    }

    private void neighborhoods_elaboration() {
        // MAP EACH NODE TO ITS NEIGHBORHOOD
        for (int node : query_nodes.keySet()) {
            IntArraySet node_neighborhood = getNodeNeighborhood(node, query_pattern.getIn_edges(), query_pattern.getOut_edges(), query_pattern.getIn_out_edges());
            map_node_to_neighborhood.put(node, node_neighborhood);
        }

        // MAP EACH PAIR OF NODES TO ITS NEIGHBORHOOD
        for (NodesPair pair : pairs) {
            ObjectArraySet<NodesPair> pair_neighborhood = getPairNeighborhood(pair, map_node_to_neighborhood);
            map_pair_to_neighborhood.put(pair.getId().intValue(), pair_neighborhood);
        }
    }

    public static NodesPair getEdgeEndpoints(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges, int edgeId) {
        AtomicReference<NodesPair> endpoints = new AtomicReference<>();

        edges.int2ObjectEntrySet().fastForEach(record -> {
            int first_endpoint = record.getIntKey();

            record.getValue().int2ObjectEntrySet().fastForEach(sub_record -> {
                int second_endpoint = sub_record.getIntKey();

                for(int _edge_id: sub_record.getValue()) {
                    if (_edge_id == edgeId) {
                        endpoints.set(new NodesPair(first_endpoint, second_endpoint));  // Endpoints are lexicographically ordered
                    }
                }
            });
        });

        return endpoints.get();
    }

    public static ObjectArraySet<NodesPair> getPairNeighborhood(NodesPair pair, Int2ObjectOpenHashMap<IntArraySet> map_node_to_neighborhood) {
        ObjectArraySet<NodesPair> neighborhood = new ObjectArraySet<>();

        int node = pair.getFirstEndpoint().intValue();

        for (int neighbour : map_node_to_neighborhood.get(node)) {
            neighborhood.add(new NodesPair(node, neighbour));
        }

        node = pair.getSecondEndpoint().intValue();

        for (int neighbour : map_node_to_neighborhood.get(node)) {
            neighborhood.add(new NodesPair(node, neighbour));
        }

        return neighborhood;
    }

    public static IntArraySet getNodeNeighborhood(int nodeKey, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> outEdges, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> inOutEdges) {
        IntArraySet neighborhood = new IntArraySet();

        if (inEdges.containsKey(nodeKey)) {
            neighborhood.addAll(inEdges.get(nodeKey).keySet());
        }

        if (outEdges.containsKey(nodeKey)) {
            neighborhood.addAll(outEdges.get(nodeKey).keySet());
        }

        if (inOutEdges.containsKey(nodeKey)) {
            neighborhood.addAll(inOutEdges.get(nodeKey).keySet());
        }

        return neighborhood;
    }

    // NODES EQUIVALENT TO
    public boolean nodes_equivalent_to(int node_1, int node_2) {
        return query_nodes.get(node_1).equivalent_to(query_nodes.get(node_2));
    }

    // EDGES EQUIVALENT TO
    public boolean edges_equivalent_to(IntArrayList pe1List, IntArrayList pe2List) {
        if (pe1List.size() != pe2List.size()) return false;
        IntArrayList pe2List_copy = pe2List.clone();
        for (int edge_1_id: pe1List) {
            QueryEdge e1        = query_edges.get(edge_1_id);
            int sel_edge_id     = -1;
            for (int edge_2_id: pe2List_copy){
                QueryEdge e2    = query_edges.get(edge_2_id);
                if (e1.equivalent_to(e2)) {
                    sel_edge_id = edge_2_id ;
                    break;
                }
            }
            if (sel_edge_id == -1) return false;
            pe2List_copy.rem(sel_edge_id);
        }
        return true;
    }

    // NODES PAIRS COMPATIBILITIES
    public boolean nodes_pairs_compatibilities (
            int node_1,   int node_2,
            int f_node_1, int f_node_2,
            Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> relationships
    ){
        if(relationships.isEmpty() || relationships.get(node_1) == null || relationships.get(f_node_1) == null) return false;
        IntArrayList edges_set_1 = relationships.get(node_1).get(node_2);
        IntArrayList edges_set_2 = relationships.get(f_node_1).get(f_node_2);

        return (edges_set_1 == null && edges_set_2 != null) ||
               (edges_set_1 != null && edges_set_2 == null) ||
               (edges_set_1 != null && !edges_equivalent_to(edges_set_1, edges_set_1));
    }

    public void domains_elaboration(Table query_bitmatrix_table, Table target_bitmatrix_table, Int2ObjectOpenHashMap<IntArrayList> compatibility) {
        IntIndex query_src_index = new IntIndex(query_bitmatrix_table.intColumn("src"));
        IntIndex query_dst_index = new IntIndex(query_bitmatrix_table.intColumn("dst"));
        IntIndex target_id_index = new IntIndex(target_bitmatrix_table.intColumn("btx_id"));

        for(NodesPair pair: pairs) {
            int src = pair.getFirstEndpoint();
            int dst = pair.getSecondEndpoint();

            Table domain_table = Table.create()
                .addColumns(IntColumn.create("first"))
                .addColumns(IntColumn.create("second"));

            for(Row query_row: query_bitmatrix_table.where(query_src_index.get(src).and(query_dst_index.get(dst)))) {
                int directed_id = query_row.getInt("btx_id");

                for (int target_id : compatibility.get(directed_id)) {
                    for (Row target_row : target_bitmatrix_table.where(target_id_index.get(target_id))) {
                        int t_src = target_row.getInt("src");
                        int t_dst = target_row.getInt("dst");

                        IntColumn src_column = IntColumn.create("first", new int[]{t_src});
                        IntColumn dst_column = IntColumn.create("second", new int[]{t_dst});
                        domain_table.append(Table.create().addColumns(src_column, dst_column));
                    }
                }
            }

            for(Row query_row: query_bitmatrix_table.where(query_src_index.get(dst).and(query_dst_index.get(src)))) {
                int reversed_id = query_row.getInt("btx_id");

                for (int target_id : compatibility.get(reversed_id)) {
                    for (Row target_row : target_bitmatrix_table.where(target_id_index.get(target_id))) {
                        int t_src = target_row.getInt("src");
                        int t_dst = target_row.getInt("dst");

                        IntColumn src_column = IntColumn.create("first", new int[]{t_dst});
                        IntColumn dst_column = IntColumn.create("second", new int[]{t_src});
                        domain_table.append(Table.create().addColumns(src_column, dst_column));
                    }
                }
            }

            pair.setCompatibility_domain(domain_table);
        }
    }


    // GETTER
    public Int2ObjectOpenHashMap<QueryNode>                 getQuery_nodes()                {return query_nodes;          }
    public QueryNode                                        getQuery_node(int node)         {return query_nodes.get(node);}
    public Object2IntOpenHashMap<String>                    getNode_name_idx()              {return node_name_idx;        }
    public Int2ObjectOpenHashMap<QueryEdge>                 getQuery_edges()                {return query_edges;          }
    public QueryEdge                                        getQuery_edge(int edge)         {return query_edges.get(edge);}
    public QueryEdgeAggregation                             getQuery_pattern()              {return query_pattern;        }
    public boolean                                          isIn(int node1, int node2)      {return query_pattern.isIn(node1,  node2);}
    public boolean                                          isOut(int node1, int node2)     {return query_pattern.isOut(node1, node2);}
    public boolean                                          isRev(int node1, int node2)     {return query_pattern.isRev(node1, node2);}
    public IntArrayList                                     get_node_neighbours(int node)   {return query_pattern.get_node_neighbours(node, query_nodes.size());}
    public ObjectArraySet<NodesPair>                        getPairs()                      {return pairs;}
    public Int2ObjectOpenHashMap<IntArraySet>               getMap_endpoints_to_edges()     {return map_endpoints_to_edges;}
    public Int2ObjectOpenHashMap<NodesPair>                 getMap_edge_to_endpoints()      {return map_edge_to_endpoints;}
    public Int2ObjectOpenHashMap<IntArraySet>               getMap_node_to_neighborhood()   {return map_node_to_neighborhood;}
    public Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>> getMap_pair_to_neighborhood()   {return map_pair_to_neighborhood;}

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
