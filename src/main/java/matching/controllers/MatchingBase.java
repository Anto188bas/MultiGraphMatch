package matching.controllers;

import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryNode;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import ordering.EdgeOrdering;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.HashMap;
import java.util.Optional;

public abstract class MatchingBase {
    public OutData outData;
    public QueryStructure query;
    public boolean justCount;
    public boolean distinct;
    public long numMaxOccs;
    public NodesEdgesLabelsMaps labels_types_idx;
    public TargetBitmatrix target_bitmatrix;
    public GraphPaths graphPaths;
    public HashMap<String, GraphMacroNode> macro_nodes;
    public Int2ObjectOpenHashMap<String> nodes_macro;
    public Optional<WhereConditionExtraction> where_managing;

    public EdgeOrdering edgeOrdering;
    public StateStructures states;
    public MatchingData matchingData;

    public IntArrayList[] nodes_symmetry;
    public IntArrayList[] edges_symmetry;

    public int numQueryEdges;

    public int si;
    public int psi;

    public int numTotalOccs;

    public MatchingBase(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs,
                        NodesEdgesLabelsMaps labels_types_idx, TargetBitmatrix target_bitmatrix, GraphPaths graphPaths,
                        HashMap<String, GraphMacroNode> macro_nodes, Int2ObjectOpenHashMap<String> nodes_macro,
                        Optional<WhereConditionExtraction> where_managing) {
        this.outData = outData;
        this.query = query;
        this.justCount = justCount;
        this.distinct = distinct;
        this.numMaxOccs = numMaxOccs;
        this.labels_types_idx = labels_types_idx;
        this.target_bitmatrix = target_bitmatrix;
        this.graphPaths = graphPaths;
        this.macro_nodes = macro_nodes;
        this.nodes_macro = nodes_macro;
        this.where_managing = where_managing;
        this.numTotalOccs = 0;
        this.states = new StateStructures();
    }

    protected boolean check_nodes_labels(){
        for (QueryNode node: query.getQuery_nodes().values()) {
            for(int label: node.getLabels())
                if(label == -1) return true;
        }
        return false;
    }

    public abstract OutData matching ();
    public abstract void updateSolutionNodesAndEdgeForStateZero();
    public abstract void updateMatchingInfoForStateZero();
    public abstract void updateCandidatesForStateZero(int q_src, int q_dst, int f_node, int s_node);
    public abstract void removeMatchingInfoForStateZero();
    public abstract void updateCandidatesForStateGraterThanZero();
    public abstract void removeMatchingInfoForStateGraterThanZero();
    public abstract void updateSolutionNodesAndEdgeForStateGreaterThanZero();
    public abstract void updateMatchingInfoForStateGreaterThanZero();

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

    public  void newOccurrenceFound() {
        numTotalOccs++;
        if (!justCount || distinct) {
            // TODO implement me
        }
        if (numTotalOccs == numMaxOccs) {
            report();
            System.exit(0);
        }
        psi = si;
    }

    public void report() {
        outData.matching_time = (System.currentTimeMillis() - outData.matching_time) / 1000;
        System.out.println("MATCHING REPORT:");
        System.out.println("\t-domain computing time: " + outData.domain_time);
        System.out.println("\t-ordering computing time: " + outData.ordering_time);
        System.out.println("\t-symmetry computing time: " + outData.symmetry_time);
        System.out.println("\t-matching computing time: " + outData.matching_time);
        System.out.println("\t-occurrences: " + outData.num_occurrences);
    }
}
