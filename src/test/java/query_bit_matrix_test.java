import bitmatrix.models.QueryBitmatrix;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.Optional;

public class query_bit_matrix_test {
    private static void create_node_colors(NodesEdgesLabelsMaps labels) {
        labels.stringVectorToIntOne("YELLOW");
        labels.stringVectorToIntOne("GREEN" );
        labels.stringVectorToIntOne("PURPLE");
    }

    private static void create_edge_colors(NodesEdgesLabelsMaps types) {
        types.createEdgeLabelIdx("RED"   );
        types.createEdgeLabelIdx("BLUE"  );
        types.createEdgeLabelIdx("ORANGE");
    }

    public static void main(String[] args) {
        NodesEdgesLabelsMaps nodes_edges_labels = new NodesEdgesLabelsMaps();
        create_node_colors(nodes_edges_labels);
        create_edge_colors(nodes_edges_labels);

        String query             = "MATCH p=(n1:GREEN:YELLOW)-[r:RED]->(n2:PURPLE), (n1)<-[r1:BLUE]-(n2) RETURN p";
        //String query           = "MATCH p=(n1:GREEN:YELLOW)-[:RED|ORANGE *2..3]->(n2:PURPLE) RETURN p";
        QueryStructure query_obj = new QueryStructure();
        query_obj.parser(query, nodes_edges_labels, null, null, Optional.empty());
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>> aggregate_edge = query_obj.getQuery_pattern().aggregate_edge();

        System.out.println(nodes_edges_labels.getIdxToLabelEdge());
        System.out.println(nodes_edges_labels.getIdxToLabelNode());
        QueryBitmatrix ww = new QueryBitmatrix();
        ww.create_bitset(query_obj, nodes_edges_labels);
        System.out.println(ww.getTable());
        System.out.println(ww.getBitmatrix());

    }
}
