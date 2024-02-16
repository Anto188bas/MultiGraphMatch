package matching.controllers;

import bitmatrix.models.CompatibilityMap;
import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.MatchingData;
import matching.models.OutData;
import matching.models.PathsMatchingData;
import ordering.NodesPair;

import target_graph.graph.TargetGraph;
import utility.Utils;

import java.awt.desktop.ScreenSleepEvent;


public class MatchingSimple extends MatchingBase {
    ObjectArrayList<QueryCondition> simpleConditions;
    CompatibilityMap compatibilityMap;

    public MatchingSimple(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix, ObjectArrayList<QueryCondition> simpleConditions, CompatibilityMap compatibilityMap) {
        super(outData, query, justCount, distinct, numMaxOccs, targetGraph, target_bitmatrix);
        this.simpleConditions = simpleConditions;
        this.compatibilityMap = compatibilityMap;
    }

    public OutData matching() {
        if (check_nodes_labels()) {
            report();
            return outData;
        }

        // SIMPLE WHERE CONDITIONS
        if (simpleConditions.size() > 0) {
            WhereUtils.assignSimpleConditionsToNodesAndEdges(simpleConditions, query);
            // DOMAINS
            computeFilteredCompatibilityDomains();
        } else {
            // DOMAINS
            if(this.compatibilityMap != null) computeCompatibilityDomains(this.compatibilityMap);
            else computeCompatibilityDomains();
        }

        // EDGE ORDERING AND STATE OBJECT CREATION
        computeOrdering();

        // SYMMETRY CONDITIONS
        computeSymmetryConditions();

        // QUERY INFORMATION
        numQueryEdges = query.getQuery_edges().size();

        // MATCHING DATA
        matchingData = new MatchingData(query);

        // FIRST QUERY NODE
        outData.matching_time = System.currentTimeMillis();

        //DEBUG INFO
//        Utils.printDebugInfo(targetGraph.getGraphPaths(), query, states, edgeOrdering);

        // MATCHING
        matching_procedure();
        report();
        return outData;
    }

    private void matching_procedure() {
        // NEW PART
        IntOpenHashSet edge_types_lens = new IntOpenHashSet();
        this.query.getQuery_edges().forEach((id, edge) -> edge_types_lens.add(edge.getEdge_label().size()));
        FindCandidateParent findCandidate = null;
        if(edge_types_lens.size() == 1) {
            int value = edge_types_lens.stream().findFirst().get();
            findCandidate = switch (value) {
                case 0  -> new FindCandidatesNoTypes();
                case 1  -> new FindCandidateSingleType();
                default -> new FindCandidateMultipleType();
            };
        }
        else findCandidate = new FindCandidateGeneral();

        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
        int q_src = firstPair.getFirstEndpoint();
        int q_dst = firstPair.getSecondEndpoint();

        Int2ObjectOpenHashMap<IntArrayList> first_compatibility = firstPair.getFirst_second();

        for (int f_node : first_compatibility.keySet()) {
            for (int s_node : first_compatibility.get(f_node)) {
                updateCandidatesForStateZero(q_src, q_dst, f_node, s_node, findCandidate);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                    // STATE ZERO
                    startFromStateZero();
                    updateSolutionNodesAndEdgeForStateZero();

                    updateMatchingInfoForStateZero();
                    goAhead();
                    updateCandidatesForStateGraterThanZero(findCandidate);

                    while (si > 0) {
                        // BACK TRACKING ON EDGES
                        if (psi >= si) {
                            removeMatchingInfoForStateGraterThanZero();
                        }

                        // NEXT CANDIDATE
                        matchingData.candidatesIT[si]++;

                        if (shouldBacktrack()) { // BACKTRACKING
                            // outData.num_backtrack ++;
                            backtrack();
                        } else {  // FORWARD TRACKING ON EDGES
                            // SET NODE AND EDGE TO MATCH
                            updateSolutionNodesAndEdgeForStateGreaterThanZero();
                            updateMatchingInfoForStateGreaterThanZero(); // TODO: check the position (it could be after goAhead)

                            if (lastStateReached()) { // INCREASE OCCURRENCES
                                // New occurrence found
                                newOccurrenceFound();
                            } else { // GO AHEAD
                                goAhead();
                                updateCandidatesForStateGraterThanZero(findCandidate);
                            }
                        }
                    }

                    // CLEANING OF STRUCTURES
                    removeMatchingInfoForStateZero();
                }
            }
        }
    }
}
