package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import ordering.EdgeOrdering;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;


public class MatchingProcedure {
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
         int numTotalOccs = 0;

         // QUERY BITMATRIX COMPUTING
         QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
         query_bitmatrix.create_bitset(query_obj, labels_types_idx);

         // DOMAIN COMPUTING
         double inizio_dom = System.currentTimeMillis();
         Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
         AggregationDomain aggregationDomain = new AggregationDomain();
         aggregationDomain.query_target_association(compatibility, target_bitmatrix, query_bitmatrix, query_obj);
         System.out.println("domain computing time: " + (System.currentTimeMillis() - inizio_dom)/1000);

         // EDGE ORDERING AND STATE OBJECT CREATION
         double ordering_stime     = System.currentTimeMillis();
         EdgeOrdering edgeOrdering = new EdgeOrdering(query_obj, aggregationDomain.getAggregate_domain());
         StateStructures states    = new StateStructures();
         states.map_state_to_edge  = edgeOrdering.getMap_state_to_edge();
         states.map_edge_to_state  = edgeOrdering.getMap_edge_to_state();
         states.map_state_to_src   = edgeOrdering.getMap_state_to_src();
         states.map_state_to_dst   = edgeOrdering.getMap_state_to_dst();
         // states.map_state_to_mnode TODO complete me
         System.out.println("ordering computing time: " + (System.currentTimeMillis() - ordering_stime)/1000);

         // SYMMETRY CONDITION COMPUTING
         IntArrayList[] nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query_obj);
         IntArrayList[] edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query_obj);

         // QUERY INFORMATION
         int numQueryEdges = query_obj.getQuery_edges().size();

         // OTHER CONFIGURATION
         MatchingData matchingData = new MatchingData(query_obj);

         // START MATCHING PHASE
         //matchingData.setCandidates[0] = TODO implement FindFirstCandidate
         int psi   = -1;
         int si    = 0;
         int sip1;

         while(si != -1){
             // BACKTRACKING ON EDGES
             if(psi>=si) {
                 matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                 matchingData.solution_edges[si] = -1;
                 // FIRST STATE (WE REMOVE BOTH NODES)
                 if(si==0) {
                     matchingData.matchedNodes.remove(matchingData.solution_nodes[0]);
                     matchingData.matchedNodes.remove(matchingData.solution_nodes[1]);
                     matchingData.solution_nodes[0] = -1;
                     matchingData.solution_nodes[1] = -1;
                 }
                 // OTHER STATEs (WE REMOVE ONLY A SINGLE NODE IF POSSIBLE
                 else {
                     int prevNodeState = states.map_state_to_mnode[si-1];
                     int currNodeState = states.map_state_to_mnode[si];
                     if(currNodeState!=prevNodeState) {
                         matchingData.matchedNodes.remove(matchingData.solution_nodes[currNodeState]);
                         matchingData.solution_nodes[currNodeState]=-1;
                     }
                 }
             }
             // FIND NEXT CANDIDATE
             matchingData.candidatesIT[si]++;
             boolean backtrack = false;
             if(matchingData.candidatesIT[si] == matchingData.setCandidates[si].size()){
                 if(si==0){}// TODO evaluate me
                 else backtrack=true;
             }

             // BACKTRACK
             if (backtrack) {
                 psi = si;
                 si--;
             }
             // FORWARDTRACK
             else {
                 // UPDATE MAP
                 matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
                 if(si==0) {
                     matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                     matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 }
                 else {
                     int node_to_match = states.map_state_to_mnode[si];
                     if(matchingData.solution_nodes[node_to_match] == -1)
                        matchingData.solution_nodes[node_to_match] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                 }

                 if(si == numQueryEdges-1) {
                     //New occurrence found
                     numTotalOccs++;
                     if(!justCount || distinct) {
                         // TODO implement me
                     }
                     psi = si;
                     if(numTotalOccs==numMaxOccs)
                        break;
                 }
                 else {
                     //Update auxiliary info
                     matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                     if(si==0) {
                         matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                         matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
                     }
                     else {
                         // TODO check me
                         int prevNodeSate = states.map_state_to_mnode[si-1];
                         int currNodeSate = states.map_state_to_mnode[si];
                         if(currNodeSate != prevNodeSate)
                             matchingData.matchedNodes.add(matchingData.solution_nodes[currNodeSate]);
                     }
                     sip1=si+1;
                     matchingData.setCandidates[sip1] = FindCandidates.find_candidates(
                         target_aggregation, query_obj, sip1, aggregationDomain,
                         nodes_symmetry, edges_symmetry, states, matchingData
                     );
                     matchingData.candidatesIT[si+1]=-1;
                     psi = si;
                     si++;
                 }
             }
         }
     }
}
