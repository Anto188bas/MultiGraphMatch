import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ordering.EdgeOrdering;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.HashMap;


public class query_reading_for_ordering {
    public static final String query = "MATCH p=(n1)<-[a:blk]-(n2), (n2)-[b:ylw]->(n3), (n3)-[c:blue]->(n4), " +
            "(n2)<-[d:grn]-(n5), (n2)-[e:org]->(n6), (n6)-[f:brw]->(n5) RETURN count(n1)";


    public static void color_matching_configuration(NodesEdgesLabelsMaps labels) {
        labels.createEdgeLabelIdx("blk");
        labels.createEdgeLabelIdx("ylw");
        labels.createEdgeLabelIdx("blue");
        labels.createEdgeLabelIdx("grn");
        labels.createEdgeLabelIdx("org");
        labels.createEdgeLabelIdx("brw");
    }

    public static Int2IntOpenHashMap domain_configuration() {
        Int2IntOpenHashMap domain = new Int2IntOpenHashMap();
        domain.put(0, 100);
        domain.put(1, 50);
        domain.put(2, 20);
        domain.put(3, 30);
        domain.put(4, 80);
        domain.put(5, 45);
        return domain;
    }

    public static QueryStructure getToyQuery() {
        QueryStructure query_structure = new QueryStructure();
        NodesEdgesLabelsMaps node_edge_labels = new NodesEdgesLabelsMaps();
        Int2IntOpenHashMap domain = domain_configuration();

        color_matching_configuration(node_edge_labels);
        query_structure.parser(query, node_edge_labels);

        return query_structure;
    }

    public static void main(String[] args) {
        String query = "MATCH p=(n1)<-[a:blk]-(n2), (n2)-[b:ylw]->(n3), (n3)-[c:blue]->(n4), " +
                "(n2)<-[d:grn]-(n5), (n2)-[e:org]->(n6), (n6)-[f:brw]->(n5) RETURN count(n1)";

        QueryStructure query_structure = new QueryStructure();
        NodesEdgesLabelsMaps node_edge_labels = new NodesEdgesLabelsMaps();
        Int2IntOpenHashMap domains = domain_configuration();
        color_matching_configuration(node_edge_labels);
        query_structure.parser(query, node_edge_labels);

        EdgeOrdering.computeEdgeOrdering(query_structure, domains);

//        System.out.println(query_structure.getQuery_pattern().getIn_edges());
//        System.out.println(query_structure.getQuery_pattern().getOut_edges());
//        System.out.println(query_structure.getQuery_edges());
//        System.out.println(node_edge_labels.getIdxToLabelEdge());
//
    }
}
