package matching.controllers;

import bitmatrix.controller.BitmatrixManager;
import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeOrdering;
import simmetry_condition.SymmetryCondition;
import state_machine.StateStructures;
import target_graph.graph.TargetGraph;

import java.util.Arrays;

public abstract class MatchingBase {
    public OutData outData;
    public QueryStructure query;
    public boolean justCount;
    public boolean distinct;
    public long numMaxOccs;

    public TargetBitmatrix target_bitmatrix;
    public TargetGraph targetGraph;

    public EdgeOrdering edgeOrdering;
    public StateStructures states;
    public MatchingData matchingData;

    public IntArrayList[] nodes_symmetry;
    public IntArrayList[] edges_symmetry;

    public int numQueryEdges;

    public int si;
    public int psi;

    public MatchingBase(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix) {
        this.outData = outData;
        this.query = query;
        this.justCount = justCount;
        this.distinct = distinct;
        this.numMaxOccs = numMaxOccs;
        this.target_bitmatrix = target_bitmatrix;
        this.states = new StateStructures();
        this.targetGraph = targetGraph;
    }

    public abstract OutData matching();

    protected boolean check_nodes_labels() {
        for (QueryNode node : query.getQuery_nodes().values()) {
            for (int label : node.getLabels())
                if (label == -1) return true;
        }
        return false;
    }

    protected void computeCompatibilityDomains() {
        outData.domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.createBitset(query, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query.domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), compatibility, targetGraph.getGraphPaths().getMap_node_color_degrees());
        outData.domain_time = (System.currentTimeMillis() - outData.domain_time) / 1000;
    }

    protected void computeFilteredCompatibilityDomains() {
        outData.domain_time = System.currentTimeMillis();
        QueryBitmatrix query_bitmatrix = new QueryBitmatrix();
        query_bitmatrix.createBitset(query, targetGraph.getNodesLabelsManager(), targetGraph.getEdgesLabelsManager());
        Int2ObjectOpenHashMap<IntArrayList> compatibility = BitmatrixManager.bitmatrix_manager(query_bitmatrix, target_bitmatrix);
        query.filtered_domains_elaboration(query_bitmatrix.getTable(), target_bitmatrix.getTable(), target_bitmatrix.getReversedTable(), compatibility, targetGraph.getGraphPaths().getMap_node_color_degrees());
        outData.domain_time = (System.currentTimeMillis() - outData.domain_time) / 1000;
    }

    protected void computeOrdering() {
        outData.ordering_time = System.currentTimeMillis();
        edgeOrdering = new EdgeOrdering(query);
        states.map_state_to_edge = edgeOrdering.getMap_state_to_edge();
        states.map_edge_to_state = edgeOrdering.getMap_edge_to_state();
        states.map_state_to_first_endpoint = edgeOrdering.getMap_state_to_first_endpoint();
        states.map_state_to_second_endpoint = edgeOrdering.getMap_state_to_second_endpoint();
        states.map_state_to_unmatched_node = edgeOrdering.getMap_state_to_unmapped_nodes();
        states.map_edge_to_direction = edgeOrdering.getMap_edge_to_direction();
        outData.ordering_time = (System.currentTimeMillis() - outData.ordering_time) / 1000;
    }

    protected void computeSymmetryConditions() {
        outData.symmetry_time = System.currentTimeMillis();
        nodes_symmetry = SymmetryCondition.getNodeSymmetryConditions(query);
        edges_symmetry = SymmetryCondition.getEdgeSymmetryConditions(query);
        outData.symmetry_time = (System.currentTimeMillis() - outData.symmetry_time) / 1000;
    }

    public void removeMatchingInfoForStateGraterThanZero() {
        matchingData.matchedEdges.remove(matchingData.solution_edges[si]);
        matchingData.solution_edges[si] = -1;
        // REMOVE THE NODE IF EXIST
        int selected_candidate = states.map_state_to_unmatched_node[si];
        if (selected_candidate != -1) {
            matchingData.matchedNodes.remove(matchingData.solution_nodes[selected_candidate]);
            matchingData.solution_nodes[selected_candidate] = -1;
        }
    }

    public void removeMatchingInfoForStateZero() {
        matchingData.matchedEdges.remove(matchingData.solution_edges[0]);
        matchingData.solution_edges[0] = -1;
        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
        matchingData.matchedNodes.remove(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = -1;
        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = -1;
    }

    public void updateSolutionNodesAndEdgeForStateZero() {
        matchingData.solution_edges[0] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_first_endpoint[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
        matchingData.solution_nodes[states.map_state_to_second_endpoint[0]] = matchingData.setCandidates[0].getInt(++matchingData.candidatesIT[0]);
    }

    public void updateMatchingInfoForStateZero() {
        matchingData.matchedEdges.add(matchingData.solution_edges[0]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_first_endpoint[0]]);
        matchingData.matchedNodes.add(matchingData.solution_nodes[states.map_state_to_second_endpoint[0]]);
    }

    public void updateMatchingInfoForStateGreaterThanZero() {
        matchingData.matchedEdges.add(matchingData.solution_edges[si]);
        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1) {
            matchingData.matchedNodes.add(matchingData.solution_nodes[node_to_match]);
        }
    }

    public void updateSolutionNodesAndEdgeForStateGreaterThanZero() {
        matchingData.solution_edges[si] = matchingData.setCandidates[si].getInt(matchingData.candidatesIT[si]);
        int node_to_match = states.map_state_to_unmatched_node[si];
        if (node_to_match != -1)
            matchingData.solution_nodes[node_to_match] = matchingData.setCandidates[si].getInt(++matchingData.candidatesIT[si]);
    }


    public void updateCandidatesForStateZero(int q_src, int q_dst, int f_node, int s_node) {
        matchingData.setCandidates[0] = FindCandidates.find_first_candidates(q_src, q_dst, f_node, s_node, states.map_state_to_edge[0], query, targetGraph.getGraphPaths(), matchingData, nodes_symmetry, states);
        matchingData.candidatesIT[0] = -1;
    }

    public void updateCandidatesForStateGraterThanZero() {
        matchingData.setCandidates[si] = FindCandidates.find_candidates(targetGraph.getGraphPaths(), query, si, nodes_symmetry, edges_symmetry, states, matchingData);
        matchingData.candidatesIT[si] = -1;
    }

    public void goAhead() {
        psi = si;
        si++;
    }

    public void backtrack() {
        psi = si;
        si--;
    }

    public void startFromStateZero() {
        si = 0;
        psi = -1;
    }

    public boolean lastStateReached() {
        return si == numQueryEdges - 1;
    }

    public boolean shouldBacktrack() {
        return (matchingData.candidatesIT[si] == matchingData.setCandidates[si].size());
    }

    public void newOccurrenceFound() {
        if (!justCount || distinct) {
//            outData.occurrences.addOrGet(Arrays.toString(matchingData.solution_edges));
            outData.occurrences.put(Arrays.toString(matchingData.solution_nodes), 1);

            if (outData.occurrences.size() == numMaxOccs) {
                report();
                System.exit(0);
            }
        } else {
            outData.num_occurrences++;

            if (outData.num_occurrences == numMaxOccs) {
                report();
                System.exit(0);
            }
        }
        psi = si;
    }

    public void report() {
        outData.matching_time = (System.currentTimeMillis() - outData.matching_time) / 1000;
//        System.out.println("MATCHING REPORT:");
//        System.out.println("\t-domain computing time: " + outData.domain_time);
//        System.out.println("\t-ordering computing time: " + outData.ordering_time);
//        System.out.println("\t-symmetry computing time: " + outData.symmetry_time);
//        System.out.println("\t-matching computing time: " + outData.matching_time);
//        System.out.println("\t-occurrences: " + outData.num_occurrences);
    }
}
