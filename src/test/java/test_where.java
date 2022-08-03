import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import matching.controllers.MatchingSimple;
import matching.controllers.MatchingWhere;
import matching.models.OutData;
import reading.FileManager;
import target_graph.edges.EdgeHandler;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class test_where {
    Function<Integer, Boolean> Equals = (x) -> x == 10;
    public boolean comparisonTest(int num, Function<Integer, Boolean> function) {
        return function.apply(num);
    }


    public static void main(String[] args){
        // CONFIGURATION
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // PATH
        String root_dir  = System.getProperty("user.dir");
//        String netw_path = root_dir + "/Networks/Person";
        String netw_path = root_dir + "/Networks/Test";


        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edges_tables_properties = FileManager.files_reading(netw_path + "/edges", ',');

        // NODE ELABORATION
        HashMap<String, GraphMacroNode> macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String> nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
                nodes_tables.clone(),"labels", idx_label, macro_nodes, level_nodeId, nodes_macro
        );
        // FIXME: I added ".clone()" to nodes_tables because it seems that "graph_macro_node_creation" changes the tables. (RG)

        // EDGE ELABORATION
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntOpenHashSet[]>> src_dst_aggregation = new Int2ObjectOpenHashMap<>();
        GraphPaths graphPaths = EdgeHandler.createGraphPaths(edges_tables_properties, idx_label, src_dst_aggregation);


        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(src_dst_aggregation, idx_label, macro_nodes, nodes_macro);

        System.out.println(idx_label.getLabelToIdxNode().keySet());
        System.out.println(idx_label.getLabelToIdxEdge().keySet());

        // QUERY
//        String query_test           = "MATCH (n1:Person)-[r1:college]->(n2:Person), (n3:Person) -[r2:college]-> (n2:Person) WHERE (n1.name <> n2.name AND NOT n1.name IN [\"Antonio\", \"Paolo\"]) OR (n2.name <> \"Franco\" AND n1.age > 18) RETURN n1,n2,n3";
//        String query_test           = "MATCH (n1:Person)-[r1:college]->(n2:Person), (n3:Person) -[r2:college]-> (n2:Person) WHERE (n2.name <> \"Franco\" AND n1.age > 18) RETURN n1,n2,n3";

        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name <> \"Franco\" AND n1.age > 18 AND n3.age >=25) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name <> \"Franco\" AND n1.age > 18) RETURN n0,n1,n2,n3";
//        String query_test           = "MATCH (n1:P)<-[r0:F]-(n0:P), (n0:P) -[r1:F]-> (n2:P), (n0:P) -[r2:F]-> (n3:P) WHERE (n0.name = \"Leonardo\" AND n1.age = 25) RETURN n0,n1,n2,n3";



        WhereConditionExtraction where_managing = new WhereConditionExtraction();
        where_managing.where_condition_extraction(query_test);
        where_managing.normal_form_computing();
        where_managing.buildSetWhereConditions();

        QueryStructure query = new QueryStructure();
        query.parser(query_test, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));


//        System.out.println("NODE 0: " + labels.getIdxToLabelNode().get(0));

//        ArrayList<String> nodes_properties = new ArrayList<>();
//        for(Table node_table: nodes_tables) {
//            for(String column_name: node_table.columnNames()) {
//                if(column_name != "id" && column_name != "labels" && !nodes_properties.contains(column_name)) {
//                    nodes_properties.add(column_name);
//                }
//            }
//        }

        OutData outData = MatchingWhere.matching(
                where_managing,
                true, false, Long.MAX_VALUE, idx_label, target_bitmatrix,
                query, graphPaths, macro_nodes, nodes_macro
        );
    }
}
