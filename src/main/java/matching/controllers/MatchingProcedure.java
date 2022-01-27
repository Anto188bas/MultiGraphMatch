package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import com.ibm.icu.impl.Trie2;
import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import domain.AssociationIndex;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import ordering.EdgeOrdering;
import org.opencypher.gremlin.translation.ir.model.SumS;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Arrays;


public class MatchingProcedure {
     public static int     numTotalOccs;
     public static double  domain_time;
     public static double  ordering_stime;
     public static double  symmetry_condition;
     public static double  matching_time;


     public static void report(){
         matching_time = (System.currentTimeMillis() - matching_time) /1000;
         System.out.println("MATCHING REPORT:");
         System.out.println("\t-domain computing time: "   + domain_time );
         System.out.println("\t-ordering computing time: " + ordering_stime);
         System.out.println("\t-symmetry computing time: " + symmetry_condition);
         System.out.println("\t-matching computing time: " + matching_time);
         System.out.println("\t-occurrences: "             + numTotalOccs);
     }

     private static void backtracking(MatchingData matchingData, int si, StateStructures states) {
         // REMOVE THE EDGE
         matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
         matchingData.solution_edges[si] = -1;
         // REMOVE THE NODE IF EXIST
         int selected_candidate = states.map_state_to_mnode[si];
         if(selected_candidate != -1) {
             matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
             matchingData.solution_nodes[selected_candidate]=-1;
         }
     }


     private static int matching_procedure(
         Table first_compatibility,
         MatchingData       matchingData,
         StateStructures    states,
         NewEdgeAggregation target_aggregation,
         QueryStructure     query_obj,
         AggregationDomain  aggregationDomain,
         IntArrayList[]     nodes_symmetry,
         IntArrayList[]     edges_symmetry,
         int numQueryEdges, int numTotalOccs, long numMaxOccs,
         int q_src        , int qs_t,         int qd_t,
         int direction,
         boolean justCount, boolean distinct,
         IntArrayList dir_types
     ) {
         int si  = 0;
         int psi = -1;
         int sip1;

         for (Row row: first_compatibility) {
             matchingData.setCandidates[0] = FindCandidates.find_first_candidate(
                 row, q_src, qs_t, qd_t, dir_types, target_aggregation, direction, matchingData, nodes_symmetry
             );
             while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() -1) {
                 // STATE ZERO
                 matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                 matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                 matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
                 si++;
                 matchingData.setCandidates[si] = FindCandidates.find_candidates(
                         target_aggregation, query_obj, si, aggregationDomain,
                         nodes_symmetry, edges_symmetry, states, matchingData
                 );
                 matchingData.candidatesIT[si] = -1;

                 while (si > 0) {
                     // BACK TRACKING ON EDGES
                     if(psi >= si) backtracking(matchingData, si, states);

                     // NEXT CANDIDATE
                     matchingData.candidatesIT[si]++;
                     boolean backtrack = matchingData.candidatesIT[si] == matchingData.setCandidates[si].size();

                     if(backtrack)
                     {
                         psi = si;
                         si--;
                     }
                     // FORWARD TRACKING ON EDGES
                     else {
                         // SET NODE AND EDGE TO MATCH
                         matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
                         int node_to_match = states.map_state_to_mnode[si];
                         if(node_to_match != -1)
                             matchingData.solution_nodes[node_to_match] =
                                     matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);

                         // INCREASE OCCURRENCES
                         if(si == numQueryEdges-1) {
                             //New occurrence found
                             numTotalOccs++;
                             if(!justCount || distinct) {
                                 // TODO implement me
                             }
                             if(numTotalOccs==numMaxOccs) {
                                 report();
                                 System.exit(0);
                             }
                             psi = si;
                         }
                         // GO AHEAD
                         else {
                             //Update auxiliary info
                             matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                             node_to_match = states.map_state_to_mnode[si];
                             if(node_to_match != -1) {
                                 matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
                             }
                             sip1 = si+1;
                             matchingData.setCandidates[sip1] = FindCandidates.find_candidates(
                                     target_aggregation, query_obj, sip1, aggregationDomain,
                                     nodes_symmetry, edges_symmetry, states, matchingData
                             );
                             matchingData.candidatesIT[sip1] = -1;
                             si++;
                         }
                     }
                 }

                 // CLEANING OF STRUCTURES
                 matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                 matchingData.solution_edges[si] = -1;
                 matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
                 matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
                 matchingData.solution_nodes[0] = -1;
                 matchingData.solution_nodes[1] = -1;
             }
             matchingData.candidatesIT[0] = -1;
         }
         return numTotalOccs;
     }

     public static void matching (
          boolean              justCount,
          boolean              distinct,
          long                 numMaxOccs,
          NodesEdgesLabelsMaps labels_types_idx,
          TargetBitmatrix      target_bitmatrix,
          QueryStructure       query_obj,
          NewEdgeAggregation   target_aggregation
     ) {
         // MATCHING DATA
         numTotalOccs = 0;

         // DOMAIN COMPUTING
         // QUERY BITMATRIX COMPUTING
         domain_time = System.currentTimeMillis();
         QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
         query_bitmatrix.create_bitset(query_obj, labels_types_idx);
         Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
         AggregationDomain aggregationDomain = new AggregationDomain();
         aggregationDomain.query_target_association(compatibility, target_bitmatrix, query_bitmatrix, query_obj);
         domain_time = (System.currentTimeMillis() - domain_time) / 1000;


         // EDGE ORDERING AND STATE OBJECT CREATION
         ordering_stime            = System.currentTimeMillis();
         EdgeOrdering edgeOrdering = new EdgeOrdering(query_obj, aggregationDomain.getAggregate_domain());
         StateStructures states    = new StateStructures();
         states.map_state_to_edge  = edgeOrdering.getMap_state_to_edge();
         states.map_edge_to_state  = edgeOrdering.getMap_edge_to_state();
         states.map_state_to_src   = edgeOrdering.getMap_state_to_src();
         states.map_state_to_dst   = edgeOrdering.getMap_state_to_dst();
         states.map_state_to_mnode = edgeOrdering.getMap_state_to_unmapped_nodes();
         ordering_stime            = (System.currentTimeMillis() - ordering_stime) / 1000;


         // SYMMETRY CONDITION COMPUTING
         symmetry_condition = System.currentTimeMillis();
         IntArrayList[] nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query_obj);
         IntArrayList[] edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query_obj);
         symmetry_condition = (System.currentTimeMillis() - symmetry_condition) / 1000;

         // QUERY INFORMATION
         int numQueryEdges = query_obj.getQuery_edges().size();

         // OTHER CONFIGURATION
         MatchingData matchingData = new MatchingData(query_obj);

         // START MATCHING PHASE
         int si    = 0;
         //int sip1;


         // FIRST QUERY NODES
         matching_time = System.currentTimeMillis();
         int q_src = states.map_state_to_src[0];
         int q_dst = states.map_state_to_dst[0];
         QueryEdge qEdge = query_obj.getQuery_edge(states.map_state_to_edge[si]);
         int direction   = qEdge.getCodificate_direction();

         // DIRECTED
         IntArrayList dir_types = qEdge.getType_directed();
         AssociationIndex first_compatibility = aggregationDomain.getQuery_target_assoc().get(q_src).get(q_dst);
         numTotalOccs = matching_procedure(
              first_compatibility.get_complete_table(), matchingData, states, target_aggregation, query_obj, aggregationDomain,
              nodes_symmetry, edges_symmetry, numQueryEdges, numTotalOccs, numMaxOccs, q_src, 0, 1,
              direction, justCount, distinct, dir_types
         );
         // REVERSE
         dir_types = qEdge.getType_reverse();
         numTotalOccs = matching_procedure(
              first_compatibility.get_complete_tab_rev(), matchingData, states, target_aggregation, query_obj, aggregationDomain,
              nodes_symmetry, edges_symmetry, numQueryEdges, numTotalOccs, numMaxOccs, q_src, 1, 0,
              direction * -1, justCount, distinct, dir_types
         );


         /*
         for (Row row: first_compatibility.get_complete_table()) {
             matchingData.setCandidates[0] = FindCandidates.find_first_candidate(
                 row, q_src, 0, 1, dir_types, target_aggregation, direction, matchingData, nodes_symmetry
             );
             while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                 // STATE ZERO
                 matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                 matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                 matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
                 si++;
                 matchingData.setCandidates[si] = FindCandidates.find_candidates(
                       target_aggregation, query_obj, si, aggregationDomain,
                       nodes_symmetry, edges_symmetry, states, matchingData
                 );

                 while (si > 0) {
                     // FIND NEXT CANDIDATE
                     matchingData.candidatesIT[si]++;
                     boolean backtrack = matchingData.candidatesIT[si] >= matchingData.setCandidates[si].size();

                     // BACK TRACKING ON EDGES
                     if(backtrack) {
                         // REMOVE THE EDGE
                         matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                         matchingData.solution_edges[si] = -1;
                         matchingData.candidatesIT[si]   = -1;
                         // REMOVE THE NODE IF EXIST
                         int selected_candidate = states.map_state_to_mnode[si];
                         if(selected_candidate != -1) {
                             matchingData.matchedNodes.remove(selected_candidate);
                             matchingData.solution_nodes[selected_candidate]=-1;
                         }
                         si--;
                     }
                     // FORWARD TRACKING ON EDGES
                     else {
                         // SET NODE AND EDGE TO MATCH
                         matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
                         int node_to_match = states.map_state_to_mnode[si];
                         if(node_to_match != -1)
                             matchingData.solution_nodes[node_to_match] =
                                 matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);

                         // INCREASE OCCURRENCES
                         if(si == numQueryEdges-1) {
                             //New occurrence found
                             numTotalOccs++;
                             if(!justCount || distinct) {
                                 // TODO implement me
                             }
                             if(numTotalOccs==numMaxOccs) {
                                 report();
                                 System.exit(0);
                             }
                         }
                         // GO AHEAD
                         else {
                             //Update auxiliary info
                             matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                             node_to_match = states.map_state_to_mnode[si];
                             if(node_to_match != -1) {
                                 matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
                             }
                             sip1 = si+1;
                             matchingData.setCandidates[sip1] = FindCandidates.find_candidates(
                                 target_aggregation, query_obj, sip1, aggregationDomain,
                                 nodes_symmetry, edges_symmetry, states, matchingData
                             );
                             matchingData.candidatesIT[sip1] = -1;
                             si++;
                         }
                     }
                 }

                 // CLEANING OF STRUCTURES
                 matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                 matchingData.solution_edges[si] = -1;
                 matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
                 matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
                 matchingData.solution_nodes[0] = -1;
                 matchingData.solution_nodes[1] = -1;
             }
             matchingData.candidatesIT[0] = -1;
         }
         */
         report();
     }
}
