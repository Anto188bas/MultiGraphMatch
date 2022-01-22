package matching;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import domain.AssociationIndex;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import tech.tablesaw.api.Table;

public class FindCandidates {
    public static void find_candidates(
            NewEdgeAggregation target_aggregation,
            QueryStructure     query,
            int                sel_state,
            AggregationDomain  edge_domain,
            IntArrayList[]     nodes_symmetry,
            IntArrayList[]     edges_symmetry,
            StateStructures    states,
            MatchingData       matchingData
    ) {
        QueryEdge queryEdge = query.getQuery_edge(states.map_state_to_edge[sel_state]);
        int q_src  = states.map_state_to_src[sel_state];
        int q_dst  = states.map_state_to_dst[sel_state];
        int t_src  = matchingData.solution_nodes[q_src];
        int t_dst  = matchingData.solution_nodes[q_dst];
        AssociationIndex compatibility = edge_domain.getQuery_target_assoc().get(q_src).get(q_dst);

        if(t_src == -1) {
            // TODO MATCHED t_dst
            //(-1)-[]->(t_dst=q_dst) OR (-1)<-[]-(t_dst=q_dst)
            Table selected_edge_by_src  = compatibility.get_by_src(t_dst);
            Table selected_edges_by_dst = compatibility.get_by_dst(t_dst);
        }
        else if(t_dst == -1) {
            // TODO MATCHED t_src
            //(t_src=q_src)-[]->(-1) OR (t_src=q_src)<-[]-(-1)
            Table selected_edge_by_src  = compatibility.get_by_src(t_src);
            Table selected_edges_by_dst = compatibility.get_by_dst(t_src);
        }
        else {
            // TODO MATCHED BOTH
        }

        /*
        // (target_node=src) --[groups]-- (dst)
        // (q_src)<-[r]-(q_dst) type = negative; (q_src)-[r]->(q_dst) type = positive; both type=+-
        Table     selected_edge_by_src = compatibility.get_by_src(target_node);
        // (src) --[groups]-- (dst=target_node)
        // (q_src)<-[r]-(q_dst) type = positive; (q_src)-[r]->(q_dst) type = positive; both type=+-
        Table     selected_edge_by_dst = compatibility.get_by_dst(target_node);
        */


    }
}
