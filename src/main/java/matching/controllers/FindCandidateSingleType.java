package matching.controllers;
import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import ordering.EdgeDirection;
import ordering.NodesPair;
import org.javatuples.Triplet;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import java.util.ArrayList;
import static ordering.EdgeDirection.IN;
import static ordering.EdgeDirection.OUT;

public class FindCandidateSingleType extends FindCandidateParent{
    @Override
    public IntArrayList find_candidates(
        GraphPaths graphPaths, QueryStructure query, int sel_state, IntArrayList[] nodes_symmetry,
        IntArrayList[] edges_symmetry, StateStructures states, MatchingData matchingData
    ) {
        IntArrayList listCandidates = new IntArrayList();

        int edge_id             = states.map_state_to_edge[sel_state];
        NodesPair edge_data     = query.getMap_edge_to_endpoints().get(edge_id);
        QueryEdge queryEdge     = query.getQuery_edge(edge_id);
        EdgeDirection direction = states.map_edge_to_direction[edge_id];
        IntArrayList edge_type  = queryEdge.getEdge_label();

        // QUERY NODES
        int q_src = edge_data.getFirstEndpoint();
        int q_dst = edge_data.getSecondEndpoint();
        // TARGET NODES
        int t_src = matchingData.solution_nodes[q_src];
        int t_dst = matchingData.solution_nodes[q_dst];

        if (t_src == -1) {
            IntArrayList compatible_list = edge_data.getBySecondValue(t_dst);
            if (compatible_list != null) {
                if (direction == OUT)
                    EdgeSelector.types_dst(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_src, t_dst, edge_type.getInt(0));
                else if (direction == IN)
                    EdgeSelector.types_src(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_dst, t_dst, edge_type.getInt(0));
                else {
                    EdgeSelector.types_dst(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_src, t_dst, edge_type.getInt(0));
                    EdgeSelector.types_src(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_dst, t_dst, edge_type.getInt(0));
                }
            }
        }
        else if (t_dst == -1) {
            IntArrayList compatible_list = edge_data.getByFirstValue(t_src);
            if (compatible_list != null) {
                if (direction == OUT)
                    EdgeSelector.types_src(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_dst, t_src, edge_type.getInt(0));
                else if (direction == IN)
                    EdgeSelector.types_dst(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_src, t_src, edge_type.getInt(0));
                else {
                    EdgeSelector.types_src(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_dst, t_src, edge_type.getInt(0));
                    EdgeSelector.types_dst(compatible_list, matchingData, nodes_symmetry, listCandidates, graphPaths, q_src, t_src, edge_type.getInt(0));
                }
            }
        }
        else {
            if (direction == OUT)
                EdgeSelector.types_matched_nodes(matchingData, edges_symmetry, states, graphPaths, listCandidates, t_src, t_dst, edge_id, edge_type);
            else if (direction == IN)
                EdgeSelector.types_matched_nodes(matchingData, edges_symmetry, states, graphPaths, listCandidates, t_dst, t_src, edge_id, edge_type);
            else {
                EdgeSelector.types_matched_nodes(matchingData, edges_symmetry, states, graphPaths, listCandidates, t_src, t_dst, edge_id, edge_type);
                EdgeSelector.types_matched_nodes(matchingData, edges_symmetry, states, graphPaths, listCandidates, t_dst, t_src, edge_id, edge_type);
            }
        }

        return listCandidates;
    }

    @Override
    public IntArrayList find_first_candidates(
        int q_src, int q_dst, int t_src, int t_dst, int edge_id, QueryStructure query, GraphPaths graphPaths,
        MatchingData matchingData, IntArrayList[] nodes_symmetry, StateStructures states
    ) {
        IntArrayList listCandidates = new IntArrayList();
        QueryEdge queryEdge = query.getQuery_edge(edge_id);
        EdgeDirection direction = states.map_edge_to_direction[edge_id];
        IntArrayList edge_type = queryEdge.getEdge_label();
        ArrayList<Triplet<Integer, Integer, Int2ObjectOpenHashMap<IntArrayList>>> edges_submap = new ArrayList<>();
        int q_node = q_src;

        // q_src = t_src AND q_dst = t_dst
        if (direction == OUT) {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
        } else if (direction == IN) {
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        } else {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        }

        EdgeSelector.types_case(edges_submap, q_node, graphPaths, matchingData, nodes_symmetry, listCandidates, queryEdge, t_src, t_dst);
        return listCandidates;
    }
}
