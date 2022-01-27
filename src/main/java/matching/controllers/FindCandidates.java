package matching.controllers;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import domain.AssociationIndex;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.awt.*;


public class FindCandidates {
    // NODE CHECK FOR BREAKING CONDITION
    public static boolean nodeCondCheck(int q_node, int t_node, MatchingData matchingData, IntArrayList[] nodes_symmetry) {
        boolean condCheck     = true;
        IntArrayList condNode = nodes_symmetry[q_node];
        for(int i=0; i<condNode.size(); i++) {
            int refTargetNode = matchingData.solution_nodes[condNode.getInt(i)];
            if (refTargetNode != -1 && t_node < refTargetNode){
                condCheck = false;
                break;
            }
        }
        return condCheck;
    }

    // EDGE CHECK FOR BREAKING CONDITION
    public static boolean condCheckEdges(int q_edge, int t_edge, MatchingData matchingData, IntArrayList[] edge_symmetry, StateStructures states)
    {
        boolean condCheck=true;
        IntArrayList condEdge = edge_symmetry[q_edge];
        for(int i=0; i<condEdge.size(); i++)
        {
            int refTargetEdge = matchingData.solution_edges[states.map_edge_to_state[condEdge.getInt(i)]];
            if(refTargetEdge!=-1 && t_edge < refTargetEdge) {
                condCheck = false;
                break;
            }
        }
        return condCheck;
    }

    // SET CANDIDATES INTO LIST
    private static void set_candidates(
            IntArrayList       listCandidates,
            NewEdgeAggregation target_aggregation,
            Table directed,    Table reverse,
            MatchingData       matchingData,
            IntArrayList[]     nodes_symmetry,
            IntArrayList       types,
            IntArrayList       types_rev,
            int dir, int col, int col_rev, int node
    ){
        // NO TYPEs
        if (types.size() == 0) {
            // NO TYPEs AND UNDIRECTED CASE
            if (dir == 0) {
                EdgeSelections.no_types_undirected_case(listCandidates, target_aggregation, directed,  col,   node, matchingData, nodes_symmetry);
                EdgeSelections.no_types_undirected_case(listCandidates, target_aggregation, reverse, col_rev, node, matchingData, nodes_symmetry);
            }
            // NO TYPEs AND DIRECTED CASE
            else {
                EdgeSelections.no_types_directed_case(listCandidates, target_aggregation, directed,  col,   node, matchingData, nodes_symmetry, dir);
                EdgeSelections.no_types_directed_case(listCandidates, target_aggregation, reverse, col_rev, node, matchingData, nodes_symmetry, dir * -1);
            }
        }
        // TYPEs
        else {
            EdgeSelections.configured_types_case(listCandidates, target_aggregation, directed,  col,   node, matchingData, nodes_symmetry, types);
            EdgeSelections.configured_types_case(listCandidates, target_aggregation, reverse, col_rev, node, matchingData, nodes_symmetry, types_rev);
        }
    }

    // TODO NOTE: WE ARE NOT CONSIDERING PROPERTIES IN THIS MOMENT. ADD SUCH CHECK AFTER BITMATRIX COMPUTING
    // FIND CANDIDATE FOR SELECTED EDGES
    public static IntArrayList find_candidates (
         NewEdgeAggregation target_aggregation,
         QueryStructure     query,
         int                sel_state,
         AggregationDomain  edge_domain,
         IntArrayList[]     nodes_symmetry,
         IntArrayList[]     edges_symmetry,
         StateStructures    states,
         MatchingData       matchingData
    ) {
        // PARAMETERS
        IntArrayList listCandidates    = new IntArrayList();
        int edge_id                    = states.map_state_to_edge[sel_state];
        QueryEdge queryEdge            = query.getQuery_edge(edge_id);
        int q_src                      = states.map_state_to_src[sel_state];
        int q_dst                      = states.map_state_to_dst[sel_state];
        int t_src                      = matchingData.solution_nodes[q_src];
        int t_dst                      = matchingData.solution_nodes[q_dst];
        int codificated_direction      = queryEdge.getCodificate_direction();
        IntArrayList types             = queryEdge.getType_directed();
        IntArrayList types_reverse     = queryEdge.getType_reverse();
        AssociationIndex compatibility = edge_domain.getQuery_target_assoc().get(q_src).get(q_dst);

        System.out.println(t_src + " -- " + t_dst);

        // UNMATCHED SRC
        if(t_src == -1) {
            // DIRECTED PART: (-1)-[]->(t_dst=q_dst) OR (-1)<-[]-(t_dst=q_dst);
            Table selected_edges_by_dst  = compatibility.get_by_dst(t_dst);
            System.out.println(compatibility.get_by_dst(t_dst));
            // REVERSE  PART: (t'_src=q_dst)<-[]-(-1) OR (t'_src=q_dst)-[]->(-1)
            Table selected_edges_reverse = compatibility.get_rev_by_src(t_dst);
            set_candidates(
               listCandidates, target_aggregation, selected_edges_by_dst, selected_edges_reverse, matchingData,
               nodes_symmetry, types, types_reverse, codificated_direction, 0, 1, q_src
            );
            System.out.println(listCandidates);
        }
        // UNMATCHED DST
        else if(t_dst == -1) {
            // DIRECTED PART: (t_src=q_src)-[]->(-1) OR (t_src=q_src)<-[]-(-1)
            Table selected_edge_by_src   = compatibility.get_by_src(t_src);
            // REVERSE  PART: (-1)<-[]-(t'_dst=q_src) OR (-1)-[]->(t'_dst=q_src)
            Table selected_edges_reverse = compatibility.get_reb_by_dst(t_src);
            set_candidates(
                listCandidates, target_aggregation, selected_edge_by_src, selected_edges_reverse, matchingData,
                nodes_symmetry, types, types_reverse, codificated_direction, 1, 0, q_dst
            );
        }
        // MATCHED BOTH SRC AND DST  TODO CHECK ME POSSIBLE BUG
        else {
            Int2ObjectOpenHashMap<IntArrayList> types_edges = target_aggregation.getSrcDstAssociations(t_src, t_dst);
            if(types_edges != null)
                EdgeSelections.set_edge_candidate_both_nodes_matched(
                    types_edges, types, matchingData, states, listCandidates, edges_symmetry, edge_id, codificated_direction
                );
        }

        return listCandidates;
    }

    public static IntArrayList find_first_candidate(
           Row src_dst,
           int q_src,
           int qs_t,  int qd_t,
           IntArrayList types,
           NewEdgeAggregation target_edges,
           int direction,
           MatchingData matchingData,
           IntArrayList[] nodes_symmetry
    ) {
          IntArrayList listCandidates = new IntArrayList();
          Int2ObjectOpenHashMap<IntArrayList> types_edges = target_edges.getSrcDstAssociations(
             src_dst.getInt("src"), src_dst.getInt("dst")
          );

          // NO TYPES
          if(types.size() == 0) {
              // UNDIRECTED
              if(direction == 0)
                 EdgeSelections.no_types_undirected_case(
                    listCandidates,types_edges, src_dst, qs_t, qd_t, q_src, matchingData, nodes_symmetry
                 );
              else
                  EdgeSelections.no_types_directed_case(
                    listCandidates, types_edges, src_dst, qs_t, qd_t, q_src, matchingData, nodes_symmetry, direction
                  );
          }
          // TYPES
          else {
              EdgeSelections.configured_types_case(
                   listCandidates, types_edges, src_dst, qs_t, qd_t, q_src, matchingData, nodes_symmetry, types
              );
          }

          return listCandidates;
    }
}
