package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import domain.AggregationDomain;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import state_machine.StateStructures;
import target_graph.edges.NewEdgeAggregation;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashMap;

public class NewMatching {
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


    private static int matching_procedure(
            Table              first_compatibility,
            MatchingData       matchingData,
            StateStructures    states,
            GraphPaths         graphPaths,
            QueryStructure     query_obj,
            IntArrayList[]     nodes_symmetry,
            IntArrayList[]     edges_symmetry,
            int numQueryEdges, int numTotalOccs, long numMaxOccs,
            int q_src        , int q_dst,
            boolean justCount, boolean distinct
    ) {
          int si  = 0;
          int psi = -1;
          int sip1;

          for (Row row: first_compatibility) {
              matchingData.setCandidates[0] = NewFindCandidates.find_first_candidates(
                 q_src, q_dst, row.getInt(0), row.getInt(1), states.map_state_to_edge[0],
                 query_obj, graphPaths, matchingData, nodes_symmetry
              );

              while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() -1) {
                  // STATE ZERO
                  matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                  matchingData.solution_nodes[states.map_state_to_src[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                  matchingData.solution_nodes[states.map_state_to_dst[si]] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
                  matchingData.matchedEdges.add(matchingData.solution_edges[si]);
                  matchingData.matchedNodes.add(matchingData.solution_nodes[0]);
                  matchingData.matchedNodes.add(matchingData.solution_nodes[1]);
                  psi = si;
                  si++;

                  matchingData.setCandidates[si] = NewFindCandidates.find_candidates(
                       graphPaths, query_obj, si, nodes_symmetry, edges_symmetry, states, matchingData
                  );
                  matchingData.candidatesIT[si] = -1;

                  while (si > 0) {
                      // BACK TRACKING ON EDGES
                      if(psi >= si) {
                          matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
                          matchingData.solution_edges[si] = -1;
                          // REMOVE THE NODE IF EXIST
                          int selected_candidate = states.map_state_to_mnode[si];
                          if(selected_candidate != -1) {
                              matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
                              matchingData.solution_nodes[selected_candidate]=-1;
                          }
                      }

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
                              matchingData.setCandidates[sip1] = NewFindCandidates.find_candidates(
                                  graphPaths, query_obj, sip1, nodes_symmetry, edges_symmetry, states, matchingData
                              );
                              matchingData.candidatesIT[sip1] = -1;
                              psi = si;
                              si  = sip1;
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
            boolean                         justCount,
            boolean                         distinct,
            long                            numMaxOccs,
            NodesEdgesLabelsMaps            labels_types_idx,
            TargetBitmatrix                 target_bitmatrix,
            QueryStructure                  query_obj,
            GraphPaths                      graphPaths,
            HashMap<String, GraphMacroNode> macro_nodes,
            Int2ObjectOpenHashMap<String>   nodes_macro
    ) {
        // MATCHING DATA
        numTotalOccs = 0;

        // DOMAIN COMPUTING
        // QUERY BITMATRIX COMPUTING
        domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.create_bitset(query_obj, labels_types_idx);
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query_obj.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility);
        domain_time = (System.currentTimeMillis() - domain_time) / 1000;

        // EDGE ORDERING AND STATE OBJECT CREATION
        ordering_stime            = System.currentTimeMillis();

    }
}
