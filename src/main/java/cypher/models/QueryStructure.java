package cypher.models;

import cypher.controller.WhereConditionExtraction;
import cypher.controller.WhereConditionHandler;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import ordering.EdgeDirection;
import ordering.NodesPair;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;
import scala.collection.Iterator;
import target_graph.graph.TargetGraph;
import target_graph.managers.EdgesLabelsManager;
import target_graph.managers.NodesLabelsManager;
import tech.tablesaw.api.Table;
import utility.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class QueryStructure {
    private final Int2ObjectOpenHashMap<QueryNode> query_nodes;
    private final Object2IntOpenHashMap<String> map_node_name_to_idx;
    private final Int2ObjectOpenHashMap<QueryEdge> query_edges;
    private final Object2IntOpenHashMap<String> map_edge_name_to_idx;
    private final QueryEdgeAggregation query_pattern;
    private final ObjectArraySet<NodesPair> pairs;
    private final Int2ObjectOpenHashMap<NodesPair> map_id_to_pair;
    private final Int2ObjectOpenHashMap<IntArraySet> map_endpoints_to_edges;
    private final Int2ObjectOpenHashMap<NodesPair> map_edge_to_endpoints;
    private final Int2ObjectOpenHashMap<IntArraySet> map_node_to_neighborhood;
    private final Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>> map_pair_to_neighborhood;
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap> map_node_color_degrees;
    private final Int2ObjectOpenHashMap<IntArrayList> map_node_to_domain;
    private final TargetGraph targetGraph;

    public QueryStructure(TargetGraph targetGraph) {
        query_nodes = new Int2ObjectOpenHashMap<>();
        map_node_name_to_idx = new Object2IntOpenHashMap<>();
        query_edges = new Int2ObjectOpenHashMap<>();
        map_edge_name_to_idx = new Object2IntOpenHashMap<>();
        query_pattern = new QueryEdgeAggregation();
        pairs = new ObjectArraySet<>();
        map_id_to_pair = new Int2ObjectOpenHashMap<>();
        map_endpoints_to_edges = new Int2ObjectOpenHashMap<>();
        map_edge_to_endpoints = new Int2ObjectOpenHashMap<>();
        map_node_to_neighborhood = new Int2ObjectOpenHashMap<>();
        map_pair_to_neighborhood = new Int2ObjectOpenHashMap<>();
        map_node_color_degrees = new Int2ObjectOpenHashMap<>();
        map_node_to_domain = new Int2ObjectOpenHashMap<>();
        this.targetGraph = targetGraph;
    }

    // PARSER FUNCTION
    public void parser(String query, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager, Table[] nodes, Table[] edges, Optional<WhereConditionExtraction> where_managing) {
        CypherParser parser = new CypherParser();
        Query query_obj = (Query) parser.parse(query, null);
        if (!(query_obj.part() instanceof SingleQuery)) return;
        SingleQuery single_query = (SingleQuery) query_obj.part();
        // MATCHING AND RESULT ELABORATION
        Iterator<Clause> clauses = single_query.clauses().iterator();

        while (clauses.hasNext()) {
            Clause clause = clauses.next();
            if (clause instanceof Match) {
                matchHandler(clause, nodesLabelsManager, edgesLabelsManager);
                Option<Where> where_conditions = ((Match) clause).where();
                if (!where_conditions.isDefined()) continue;
                WhereConditionHandler.handleWhereCondition(where_conditions.get().expression(), targetGraph, nodes, edges, map_node_name_to_idx, map_edge_name_to_idx, query_nodes, query_edges, where_managing, nodesLabelsManager, edgesLabelsManager);
            } else if (clause instanceof Return) {
                QueryReturn query_return = new QueryReturn();
                query_return.return_elaboration((Return) clause);
                // TODO implement me
            }
        }
    }

    // MATCH PART ELABORATION
    private void matchHandler(Clause clause, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager) {
        Pattern pattern = ((Match) clause).pattern();
        Iterator<PatternPart> pattern_parts = pattern.patternParts().iterator();
        while (pattern_parts.hasNext()) {
            PatternPart pattern_part = pattern_parts.next();
            // TODO manage named pattern
            String named_pattern = get_pattern_name(pattern_part);
            PatternElement pattern_element = pattern_part.element();
            if (pattern_element.isSingleNode()) nodeManager((NodePattern) pattern_element, nodesLabelsManager);
            else {
                LinkedList<Integer> nodes_ids = new LinkedList<>();
                LinkedList<Integer> edges_ids = new LinkedList<>();
                patternElaboration(pattern_element, nodes_ids, edges_ids, nodesLabelsManager, edgesLabelsManager);
                this.query_pattern.create_aggregation(nodes_ids, edges_ids, this.query_edges);
            }
        }
        buildNodesPairs();
        map_id_to_pair_elaboration();
        elaborateNeighborhoods();
    }

    public void clean() {
        this.query_nodes.forEach((index, node) -> {
            node.clean();
        });

        this.query_edges.forEach((index, edge) -> {
            edge.clean();
        });

        this.pairs.forEach((pair) -> {
            pair.clean();
        });
    }

    // GET PATTERN NAME IN EXISTS
    private String get_pattern_name(PatternPart pattern_part) {
        if (!(pattern_part instanceof NamedPatternPart)) return null;
        NamedPatternPart pattern = (NamedPatternPart) pattern_part;
        return pattern.variable().name();
    }

    public Int2ObjectOpenHashMap<Int2IntOpenHashMap> getMap_node_color_degrees() {
        return map_node_color_degrees;
    }

    // NODE CREATION
    private int nodeManager(NodePattern nodePattern, NodesLabelsManager nodesLabelsManager) {
        Option<LogicalVariable> name = nodePattern.variable();
        // NODE MANE NOT DEFINED (:LABEL {PROPS}). SO, EACH ELEMENT IS A NEW NODE.
        if (!name.isDefined()) {
            QueryNode node = new QueryNode(nodePattern, null);
            int id = query_nodes.size();
            query_nodes.put(id, node);
            return id;
        }
        String name_str = name.get().name();
        // NODE HAS ALREADY BEEN CREATED
        if (map_node_name_to_idx.containsKey(name_str)) return map_node_name_to_idx.getInt(name_str);
        // NODE HAVE TO BE CREATED
        QueryNode node = new QueryNode(nodePattern, name_str, nodesLabelsManager);
        int id = query_nodes.size();
        query_nodes.put(id, node);
        map_node_name_to_idx.put(name_str, id);
        return id;
    }

    // EDGE CREATION
    private int edgeManager(RelationshipPattern relationship, EdgesLabelsManager edgesLabelsManager) {
        int id = query_edges.size();
        query_edges.put(id, new QueryEdge(relationship, edgesLabelsManager));
        if (query_edges.get(id).getEdge_name() == null) query_edges.get(id).setEdge_name(id);
        map_edge_name_to_idx.put(query_edges.get(id).getEdge_name(), id);
        return id;
    }

    // PATTERN ELABORATION
    private void patternElaboration(PatternElement patternElement, LinkedList<Integer> nodes_ids, LinkedList<Integer> edges_ids, NodesLabelsManager nodesLabelsManager, EdgesLabelsManager edgesLabelsManager) {
        RelationshipChain relationshipChain = (RelationshipChain) patternElement;
        // RIGHT ELEMENT
        nodes_ids.addFirst(nodeManager(relationshipChain.rightNode(), nodesLabelsManager));
        edges_ids.addFirst(edgeManager(relationshipChain.relationship(), edgesLabelsManager));
        // LEFT ELEMENT
        PatternElement new_pattern = relationshipChain.element();
        if (new_pattern instanceof RelationshipChain)
            patternElaboration(new_pattern, nodes_ids, edges_ids, nodesLabelsManager, edgesLabelsManager);
        else if (new_pattern instanceof NodePattern)
            nodes_ids.addFirst(nodeManager((NodePattern) new_pattern, nodesLabelsManager));
    }


    private void node_color_degrees_init(int node) {
        if (map_node_color_degrees.containsKey(node)) return;
        map_node_color_degrees.put(node, new Int2IntOpenHashMap());
    }

    private void node_color_degrees_increase(int node, int color) {
        Int2IntOpenHashMap color_degrees = map_node_color_degrees.get(node);
        if (color_degrees.containsKey(color)) color_degrees.replace(color, color_degrees.get(color) + 1);
        else color_degrees.put(color, 1);
    }

    private void buildNodesPairs() {
        for (int edge_key : query_edges.keySet()) {
            NodesPair endpoints = getEdgeEndpoints(query_pattern.getOut_edges(), edge_key);

            if (endpoints == null) { // Undirected edge
                endpoints = getEdgeEndpoints(query_pattern.getIn_out_edges(), edge_key);
            } // Else is a directed edge

            if (map_endpoints_to_edges.containsKey(endpoints.getId().intValue())) {
                map_endpoints_to_edges.get(endpoints.getId().intValue()).add(edge_key);
            } else {
                IntArraySet edge_set = new IntArraySet();
                edge_set.add(edge_key);
                map_endpoints_to_edges.put(endpoints.getId().intValue(), edge_set);
            }
            map_edge_to_endpoints.put(edge_key, endpoints);
            pairs.add(endpoints);
            node_color_degrees_init(endpoints.getFirstEndpoint());
            node_color_degrees_init(endpoints.getSecondEndpoint());
            for (int type : query_edges.get(edge_key).getEdge_label()) {
                node_color_degrees_increase(endpoints.getFirstEndpoint(), type);
                node_color_degrees_increase(endpoints.getSecondEndpoint(), type);
            }
        }
    }

    private void elaborateNeighborhoods() {
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

    private void map_id_to_pair_elaboration() {
        for (NodesPair pair : pairs) {
            this.map_id_to_pair.put(pair.getId().intValue(), pair);
        }
    }

    public static NodesPair getEdgeEndpoints(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> edges, int edgeId) {
        AtomicReference<NodesPair> endpoints = new AtomicReference<>();

        edges.int2ObjectEntrySet().fastForEach(record -> {
            int first_endpoint = record.getIntKey();

            record.getValue().int2ObjectEntrySet().fastForEach(sub_record -> {
                int second_endpoint = sub_record.getIntKey();

                for (int _edge_id : sub_record.getValue()) {
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
        for (int edge_1_id : pe1List) {
            QueryEdge e1 = query_edges.get(edge_1_id);
            int sel_edge_id = -1;
            for (int edge_2_id : pe2List_copy) {
                QueryEdge e2 = query_edges.get(edge_2_id);
                if (e1.equivalent_to(e2)) {
                    sel_edge_id = edge_2_id;
                    break;
                }
            }
            if (sel_edge_id == -1) return false;
            pe2List_copy.rem(sel_edge_id);
        }
        return true;
    }

    // NODES PAIRS COMPATIBILITIES
    public boolean node_pairs_are_not_compatible(int node_1, int node_2, int f_node_1, int f_node_2, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> relationships) {
        if ((relationships.isEmpty()) || (relationships.get(node_1) == null && relationships.get(f_node_1) == null)) {
            return false;
        }

        if ((relationships.get(node_1) == null) || (relationships.get(f_node_1) == null)) {
            return true;
        }


        IntArrayList edges_set_1 = relationships.get(node_1).get(node_2);
        IntArrayList edges_set_2 = relationships.get(f_node_1).get(f_node_2);

        return (edges_set_1 == null && edges_set_2 != null) || (edges_set_1 != null && edges_set_2 == null) || (edges_set_1 != null && !edges_equivalent_to(edges_set_1, edges_set_2));
    }


    private boolean degree_comparison(int t_src, int q_src, int t_dst, int q_dst, Int2ObjectOpenHashMap<Int2IntOpenHashMap> t_map_node_color_degrees) {
        // SRC
        Int2IntOpenHashMap color_degrees = map_node_color_degrees.get(q_src);
        Int2IntOpenHashMap target_color_degrees = t_map_node_color_degrees.get(t_src);

        for (int type : color_degrees.keySet()) {
            int q_src_degree = color_degrees.get(type);
            int t_src_degree = target_color_degrees.getOrDefault(type, 0);
            if (q_src_degree > t_src_degree) return false;
        }

        // DST
        color_degrees = map_node_color_degrees.get(q_dst);
        target_color_degrees = t_map_node_color_degrees.get(t_dst);
        for (int type : color_degrees.keySet()) {
            int q_dst_degree = color_degrees.get(type);
            int t_dst_degree = target_color_degrees.getOrDefault(type, 0);
            if (q_dst_degree > t_dst_degree) return false;
        }

        return true;
    }

    private IntArrayList getQueryBtxIdList(int c1, int c2, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> query_bitmatrix_table) {
        // TODO: modify the query bitmatrix in order to get the id from a double map (src, dst) -> id
        IntArrayList queryBtxIdList = new IntArrayList();
        query_bitmatrix_table.forEach((id, srcMap) -> {
            srcMap.forEach((src, dstList) -> {
                if (src == c1) {
                    for (int dst : dstList) {
                        if (dst == c2) {
                            queryBtxIdList.add(id.intValue());
                        }
                    }
                }
            });
        });

        if (queryBtxIdList.size() == 0) {
            System.err.println("ERROR BTX ID QUERY");
            System.exit(0);
        }

        return queryBtxIdList;
    }

    public void populateCandidateSets(Int2ObjectOpenHashMap<IntArrayList> first_second, Int2ObjectOpenHashMap<IntArrayList> second_first, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees, int firstTargetNode, IntArrayList secondTargetNodeList, int firstQueryNode, int secondQueryNode) {
        IntIterator iterator = secondTargetNodeList.iterator();
        while (iterator.hasNext()) {
            int secondTargetNode = iterator.nextInt();

            // DEGREE CHECK
            if (!degree_comparison(firstTargetNode, firstQueryNode, secondTargetNode, secondQueryNode, target_map_node_color_degrees))
                continue;

            if (!first_second.containsKey(firstTargetNode)) first_second.put(firstTargetNode, new IntArrayList());
            if (!second_first.containsKey(secondTargetNode)) second_first.put(secondTargetNode, new IntArrayList());
            first_second.get(firstTargetNode).add(secondTargetNode);
            second_first.get(secondTargetNode).add(firstTargetNode);
        }
    }

    public void populateCandidateSets(Int2ObjectOpenHashMap<IntArrayList> first_second, Int2ObjectOpenHashMap<IntArrayList> second_first, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees, int firstTargetNode, IntSet secondTargetNodeList, int firstQueryNode, int secondQueryNode) {
        IntIterator iterator = secondTargetNodeList.iterator();
        while (iterator.hasNext()) {
            int secondTargetNode = iterator.nextInt();

            // DEGREE CHECK
            if (!degree_comparison(firstTargetNode, firstQueryNode, secondTargetNode, secondQueryNode, target_map_node_color_degrees))
                continue;

            if (!first_second.containsKey(firstTargetNode)) first_second.put(firstTargetNode, new IntArrayList());
            if (!second_first.containsKey(secondTargetNode)) second_first.put(secondTargetNode, new IntArrayList());
            first_second.get(firstTargetNode).add(secondTargetNode);
            second_first.get(secondTargetNode).add(firstTargetNode);
        }
    }

    public void populateCandidateSets(Int2ObjectOpenHashMap<IntArrayList> first_second, Int2ObjectOpenHashMap<IntArrayList> second_first, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees, IntArrayList firstTargetNodeList, int secondTargetNode, int firstQueryNode, int secondQueryNode) {
        IntIterator iterator = firstTargetNodeList.iterator();
        while (iterator.hasNext()) {
            int firstTargetNode = iterator.nextInt();

            // DEGREE CHECK
            if (!degree_comparison(firstTargetNode, firstQueryNode, secondTargetNode, secondQueryNode, target_map_node_color_degrees))
                continue;

            if (!first_second.containsKey(firstTargetNode)) first_second.put(firstTargetNode, new IntArrayList());
            if (!second_first.containsKey(secondTargetNode)) second_first.put(secondTargetNode, new IntArrayList());
            first_second.get(firstTargetNode).add(secondTargetNode);
            second_first.get(secondTargetNode).add(firstTargetNode);
        }
    }

    private void domain_population(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> query_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> target_bitmatrix_table, Int2ObjectOpenHashMap<IntArrayList> compatibility, Int2ObjectOpenHashMap<IntArrayList> first_second, Int2ObjectOpenHashMap<IntArrayList> second_first, int c1, int c2, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees) {
        IntArrayList queryBtxIdList = getQueryBtxIdList(c1, c2, query_bitmatrix_table);

        for(int query_btx_id: queryBtxIdList) {
            for (int targetId : compatibility.get(query_btx_id)) {
                Int2ObjectOpenHashMap<IntArrayList> srcMap = target_bitmatrix_table.get(targetId);
                srcMap.forEach((src, dstMap) -> {
                    populateCandidateSets(first_second, second_first, target_map_node_color_degrees, src, dstMap, c1, c2);
                });
            }
        }
    }

    private void filtered_domain_population(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> query_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> target_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> reversed_target_bitmatrix_table, Int2ObjectOpenHashMap<IntArrayList> compatibility, Int2ObjectOpenHashMap<IntArrayList> first_second, Int2ObjectOpenHashMap<IntArrayList> second_first, int c1, int c2, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees) {
        IntArrayList queryBtxIdList = getQueryBtxIdList(c1, c2, query_bitmatrix_table);
        HashMap<String, QueryCondition> c1Conditions = this.query_nodes.get(c1).getSimpleConditions();
        HashMap<String, QueryCondition> c2Conditions = this.query_nodes.get(c2).getSimpleConditions();

        // Case 1: |Conditions(c1)| > 0 && |Conditions(c2)| = 0
        if (c1Conditions.size() > 0 && c2Conditions.size() == 0) {
            for(int query_btx_id: queryBtxIdList) {
                for (int targetId : compatibility.get(query_btx_id)) {
                    IntCollection srcKeySet = Utils.intersection(target_bitmatrix_table.get(targetId).keySet(), this.query_nodes.get(c1).getWhereConditionsCompatibilityDomain());
                    Int2ObjectOpenHashMap<IntArrayList> srcMap = target_bitmatrix_table.get(targetId);

                    for (int src : srcKeySet) {
                        IntArrayList dstMap = srcMap.get(src);
                        populateCandidateSets(first_second, second_first, target_map_node_color_degrees, src, dstMap, c1, c2);
                    }
                }
            }
        }
        // Case 2: |Conditions(c1)| = 0 && |Conditions(c2)| > 0
        else if (c1Conditions.size() == 0 && c2Conditions.size() > 0) {
            for(int query_btx_id: queryBtxIdList) {
                for (int targetId : compatibility.get(query_btx_id)) {
                    IntCollection dstKeySet = Utils.intersection(reversed_target_bitmatrix_table.get(targetId).keySet(), this.query_nodes.get(c2).getWhereConditionsCompatibilityDomain());
                    Int2ObjectOpenHashMap<IntArrayList> dstMap = reversed_target_bitmatrix_table.get(targetId);

                    for (int dst : dstKeySet) {
                       IntArrayList srcMap = dstMap.get(dst);
                       populateCandidateSets(first_second, second_first, target_map_node_color_degrees, srcMap, dst, c1, c2);
                    }
                }
            }
        }
        // Case 3: |Conditions(c1)| > 0 && |Conditions(c2)| > 0
        else if (c1Conditions.size() > 0 && c2Conditions.size() > 0) {
            for(int query_btx_id: queryBtxIdList) {
                for (int targetId : compatibility.get(query_btx_id)) {
                    IntCollection srcKeySet = Utils.intersection(target_bitmatrix_table.get(targetId).keySet(), this.query_nodes.get(c1).getWhereConditionsCompatibilityDomain());
                    Int2ObjectOpenHashMap<IntArrayList> srcMap = target_bitmatrix_table.get(targetId);

                    for (int src : srcKeySet) {
                        IntArrayList dstMap = srcMap.get(src);
                        IntCollection tmpDstMap = Utils.intersection(dstMap, this.query_nodes.get(c2).getWhereConditionsCompatibilityDomain());
                        populateCandidateSets(first_second, second_first, target_map_node_color_degrees, src, new IntArrayList(tmpDstMap), c1, c2);
                    }
                }
            }
        }
        // Case 4: |Conditions(c1)| = 0 && |Conditions(c2)| = 0
        else {
            for(int query_btx_id: queryBtxIdList) {
                for (int targetId : compatibility.get(query_btx_id)) {
                    Int2ObjectOpenHashMap<IntArrayList> srcMap = target_bitmatrix_table.get(targetId);
                    srcMap.forEach((src, dstMap) -> {
                        populateCandidateSets(first_second, second_first, target_map_node_color_degrees, src, dstMap, c1, c2);
                    });
                }
            }
        }
    }

    public void domains_elaboration(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> query_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> target_bitmatrix_table, Int2ObjectOpenHashMap<IntArrayList> compatibility, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees) {
        for (NodesPair pair : pairs) {
            int src = pair.getFirstEndpoint();
            int dst = pair.getSecondEndpoint();

            Int2ObjectOpenHashMap<IntArrayList> first_second = new Int2ObjectOpenHashMap<>();
            Int2ObjectOpenHashMap<IntArrayList> second_first = new Int2ObjectOpenHashMap<>();

            // DIRECTED POPULATION
            domain_population(query_bitmatrix_table, target_bitmatrix_table, compatibility, first_second, second_first, src, dst, target_map_node_color_degrees);

            pair.setCompatibilityDomain(first_second, second_first);
        }
        //TODO: compute nodes domains only if there are paths
//        computeNodesDomains();
    }

    public void filtered_domains_elaboration(Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> query_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> target_bitmatrix_table, Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> reversed_target_bitmatrix_table, Int2ObjectOpenHashMap<IntArrayList> compatibility, Int2ObjectOpenHashMap<Int2IntOpenHashMap> target_map_node_color_degrees) {
        for (NodesPair pair : pairs) {
            int src = pair.getFirstEndpoint();
            int dst = pair.getSecondEndpoint();

            Int2ObjectOpenHashMap<IntArrayList> first_second = new Int2ObjectOpenHashMap<>();
            Int2ObjectOpenHashMap<IntArrayList> second_first = new Int2ObjectOpenHashMap<>();

            // DIRECTED POPULATION
            filtered_domain_population(query_bitmatrix_table, target_bitmatrix_table, reversed_target_bitmatrix_table, compatibility, first_second, second_first, src, dst, target_map_node_color_degrees);
            pair.setCompatibilityDomain(first_second, second_first);
        }
        //TODO: compute nodes domains only if there are paths
//        computeNodesDomains();
    }

    /*
    protected void computeNodesDomains() {
        for (int nodeID : query_nodes.keySet()) {
            IntArrayList domain = new IntArrayList();

            QueryNode node = this.getQuery_nodes().get(nodeID);
            // If there are simple conditions, we use the compatibility domains filtered with the conditions
            if (node.getSimpleConditions().size() > 0) {
                domain = node.getWhereConditionsCompatibilityDomain();
            } else {
                for (NodesPair pair : pairs) {
                    if (pair.getFirstEndpoint() == nodeID) {
                        domain.addAll(pair.getFirst_second().keySet());
                    } else if (pair.getSecondEndpoint() == nodeID) {
                        domain.addAll(pair.getSecond_first().keySet());
                    }
                }
            }
            map_node_to_domain.put(nodeID, domain);
        }
    }
    */

    public EdgeDirection getDirection(int src, int dst, int edgeId) {
        QueryEdge edge = this.query_edges.get(edgeId);
        if (edge.getDirection() == "BOTH") {
            return EdgeDirection.BOTH;
        } else {
            if (this.query_pattern.getOut_edges().containsKey(src) && this.query_pattern.getOut_edges().get(src).containsKey(dst) && this.query_pattern.getOut_edges().get(src).get(dst).contains(edgeId)) {
                return EdgeDirection.OUT;
            } else {
                return EdgeDirection.IN;
            }
        }
    }


    // GETTER
    public Int2ObjectOpenHashMap<QueryNode> getQuery_nodes() {
        return query_nodes;
    }

    public QueryNode getQuery_node(int node) {
        return query_nodes.get(node);
    }

    public Object2IntOpenHashMap<String> getMap_node_name_to_idx() {
        return map_node_name_to_idx;
    }

    public Object2IntOpenHashMap<String> getMap_edge_name_to_idx() {
        return map_edge_name_to_idx;
    }

    public Int2ObjectOpenHashMap<QueryEdge> getQuery_edges() {
        return query_edges;
    }

    public QueryEdge getQuery_edge(int edge) {
        return query_edges.get(edge);
    }

    public QueryEdgeAggregation getQuery_pattern() {
        return query_pattern;
    }

    public boolean isIn(int node1, int node2) {
        return query_pattern.isIn(node1, node2);
    }

    public boolean isOut(int node1, int node2) {
        return query_pattern.isOut(node1, node2);
    }

    public boolean isRev(int node1, int node2) {
        return query_pattern.isRev(node1, node2);
    }

    public IntArrayList get_node_neighbours(int node) {
        return new IntArrayList(this.map_node_to_neighborhood.get(node));
    }

    public ObjectArraySet<NodesPair> getPairs() {
        return pairs;
    }

    public Int2ObjectOpenHashMap<IntArraySet> getMap_endpoints_to_edges() {
        return map_endpoints_to_edges;
    }

    public Int2ObjectOpenHashMap<NodesPair> getMap_edge_to_endpoints() {
        return map_edge_to_endpoints;
    }

    public Int2ObjectOpenHashMap<IntArraySet> getMap_node_to_neighborhood() {
        return map_node_to_neighborhood;
    }

    public Int2ObjectOpenHashMap<ObjectArraySet<NodesPair>> getMap_pair_to_neighborhood() {
        return map_pair_to_neighborhood;
    }

    public Int2ObjectOpenHashMap<NodesPair> getMap_id_to_pair() {
        return map_id_to_pair;
    }

    public Int2ObjectOpenHashMap<IntArrayList> getMap_node_to_domain() {
        return map_node_to_domain;
    }

    // TO STRING
    @Override
    public String toString() {
        return "QueryStructure{" + "\n" + "query_nodes=" + query_nodes + "\n" + ", node_name_idx=" + map_node_name_to_idx + "\n" + ", query_edges=" + query_edges + "\n" + ", query_pattern=" + query_pattern + "\n" + '}';
    }
}
