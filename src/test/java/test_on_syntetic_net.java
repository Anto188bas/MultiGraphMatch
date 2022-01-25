import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import configuration.Configuration;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import domain.AssociationIndex;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import ordering.EdgeOrdering;
import reading.FileManager;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.edges.EdgeHandler;
import target_graph.edges.NewEdgeAggregation;
import target_graph.nodes.GraphMacroNode;
import target_graph.nodes.MacroNodeHandler;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.io.IOException;
import java.util.*;


public class test_on_syntetic_net {
    public static void main(String[] args) throws IOException {
        // CONFIGURATION
        Configuration configuration     = new Configuration(args);
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();

        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(configuration.nodes_main_directory, ',');
        Table[] edges_tables_properties = FileManager.files_reading(configuration.edges_main_directory, ',');

        // NODE ELABORATION
        HashMap<String, GraphMacroNode>          macro_nodes  = new HashMap<>();
        Int2ObjectOpenHashMap<String>            nodes_macro  = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId = new Int2ObjectOpenHashMap<>();
        int max_deep_level = MacroNodeHandler.graph_macro_node_creation(
            nodes_tables,"type", idx_label, macro_nodes, level_nodeId, nodes_macro
        );

        // EDGE ELABORATION
        NewEdgeAggregation graphEdge = new NewEdgeAggregation();
        EdgeHandler.createGraphEdge(edges_tables_properties, idx_label, graphEdge);

        // TARGET BITMATRIX
        TargetBitmatrix target_bitmatrix = new TargetBitmatrix();
        target_bitmatrix.create_bitset(graphEdge, idx_label, macro_nodes, nodes_macro);

        // QUERIES READING
        System.out.println(idx_label.getIdxToLabelEdge());
        List<String> queries = FileManager.query_reading(configuration);
        queries.forEach(query -> {
            QueryStructure query_obj = new QueryStructure();
            query_obj.parser(query, idx_label);

            // MATRIX CONSTRUCTION
            QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
            query_bitmatrix.create_bitset(query_obj, idx_label);


            // COMPATIBILITY COMPUTING
            double inizio_dom = System.currentTimeMillis();
            Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
            AggregationDomain aggregationDomain = new AggregationDomain();
            aggregationDomain.query_target_association(compatibility, target_bitmatrix, query_bitmatrix, query_obj);
            System.out.println("domain computing time: " + (System.currentTimeMillis() - inizio_dom)/1000);


            // LOGGING
            /*
            aggregationDomain.getAggregate_domain().int2ObjectEntrySet().fastForEach(record -> {
                record.getValue().int2IntEntrySet().fastForEach(sub_record -> {
                    System.out.println("src: " + record.getIntKey() + "; dst: " + sub_record.getIntKey() + "; |domain|: " + sub_record.getIntValue());
                });
            });
            */


            // EDGE ORDERING
            double ordering_stime     = System.currentTimeMillis();
            EdgeOrdering edgeOrdering = new EdgeOrdering(query_obj, aggregationDomain.getAggregate_domain());
            StateStructures states    = new StateStructures();
            states.map_state_to_edge  = edgeOrdering.getMap_state_to_edge();
            states.map_edge_to_state  = edgeOrdering.getMap_edge_to_state();
            states.map_state_to_src   = edgeOrdering.getMap_state_to_src();
            states.map_state_to_dst   = edgeOrdering.getMap_state_to_dst();
            System.out.println("ordering computing time: " + (System.currentTimeMillis() - ordering_stime)/1000);

            // LOGGING
            //System.out.println("map_state_to_edge: " + Arrays.toString(states.map_state_to_edge));
            //System.out.println("map_edge_to_state: " + Arrays.toString(states.map_edge_to_state));
            //System.out.println("map_state_to_src: " + Arrays.toString(states.map_state_to_src));
            //System.out.println("map_state_to_dst: " + Arrays.toString(states.map_state_to_dst));


            // SYMMETRY CONDITIONS
            IntArrayList[] symm_cond_nodes = SymmetryCondition.getNodeSymmetryConditions(query_obj);
            IntArrayList[] symm_cond_edges = SymmetryCondition.getEdgeSymmetryConditions(query_obj);

            // LOGGING
            /*
            System.out.println("NODES SYMMETRY CONDITIONS");
            for(int i=0; i<symm_cond_nodes.length; i++)
			    System.out.println("NODE " + i + ": " + symm_cond_nodes[i]);
            System.out.println("EDGES SIMMETRY CONDITIONS:");
		    for(int i=0; i<symm_cond_edges.length; i++)
			    System.out.println("EDGE " + i + ": " + symm_cond_edges[i]);
			 */

            int[][] map_state_to_mapped_nodes = edgeOrdering.getMap_state_to_mapped_nodes();
            for(int i = 0; i < map_state_to_mapped_nodes.length; i++) {
                System.out.println("STATE " + i + "\tMAPPED NODES: " + Arrays.toString(map_state_to_mapped_nodes[i]));
            }
		    // TMP CODE
//		    int first_edge       = states.map_state_to_edge[0];
//		    int q_src            = states.map_state_to_src[0];
//		    int q_dst            = states.map_state_to_dst[0];
//		    AssociationIndex tmp = aggregationDomain.getQuery_target_assoc().get(q_src).get(q_dst);
//            int[] listInitNodes  = tmp.get_complete_table().intColumn("src").asSet().stream().mapToInt(Integer::intValue).toArray();
//
//            MatchingData matchind_data = new MatchingData(query_obj);
//
//            int x = -10;
//            System.out.println(Integer.signum(x));

        });

    }
}
