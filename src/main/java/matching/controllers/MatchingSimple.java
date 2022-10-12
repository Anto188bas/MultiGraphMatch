package matching.controllers;

import bitmatrix.models.TargetBitmatrix;
import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.OutData;
import matching.models.PathsMatchingData;
import ordering.NodesPair;
import target_graph.graph.GraphPaths;
import target_graph.nodes.GraphMacroNode;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import utility.Utils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Optional;

public class MatchingSimple extends MatchingBase {
    ObjectArrayList<QueryCondition> simpleConditions;
    public MatchingSimple(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, NodesEdgesLabelsMaps labels_types_idx, TargetBitmatrix target_bitmatrix, GraphPaths graphPaths, HashMap<String, GraphMacroNode> macro_nodes, Int2ObjectOpenHashMap<String> nodes_macro, ObjectArrayList<QueryCondition> simpleConditions) {
        super(outData, query, justCount, distinct, numMaxOccs, labels_types_idx, target_bitmatrix, graphPaths, macro_nodes, nodes_macro);
        this.simpleConditions = simpleConditions;
    }

    public OutData matching() throws FileNotFoundException {
        if (check_nodes_labels()) {
            report();
            return outData;
        }

        // SIMPLE WHERE CONDITIONS
        if(simpleConditions.size() > 0) {
            WhereUtils.assignSimpleConditionsToNodesAndEdges(simpleConditions, query);

            // DOMAINS
            computeFilteredCompatibilityDomains();
        } else {
            // DOMAINS
            computeCompatibilityDomains();
        }

        // EDGE ORDERING AND STATE OBJECT CREATION
        computeOrdering();

        // SYMMETRY CONDITIONS
        computeSymmetryConditions();

        // QUERY INFORMATION
        numQueryEdges = query.getQuery_edges().size();

        // MATCHING DATA
        matchingData = new PathsMatchingData(query);

        // FIRST QUERY NODE
        outData.matching_time = System.currentTimeMillis();

        //DEBUG INFO
//        Utils.printDebugInfo(graphPaths, query, states, edgeOrdering);

        // MATCHING
        outData.num_occurrences = matching_procedure();
        report();
        return outData;
    }

    private long matching_procedure() {
        NodesPair firstPair = this.query.getMap_edge_to_endpoints().get(states.map_state_to_edge[0]);
        int q_src = firstPair.getFirstEndpoint();
        int q_dst = firstPair.getSecondEndpoint();

        Int2ObjectOpenHashMap<IntArrayList> first_compatibility = firstPair.getFirst_second();

        for (int f_node: first_compatibility.keySet()) {
            for (int s_node: first_compatibility.get(f_node)) {
                updateCandidatesForStateZero(q_src, q_dst, f_node, s_node);

                while (matchingData.candidatesIT[0] < matchingData.setCandidates[0].size() - 1) {
                    // STATE ZERO
                    startFromStateZero();
                    updateSolutionNodesAndEdgeForStateZero();

                    updateMatchingInfoForStateZero();
                    goAhead();
                    updateCandidatesForStateGraterThanZero();

                    while (si > 0) {
                        // BACK TRACKING ON EDGES
                        if (psi >= si) {
                            removeMatchingInfoForStateGraterThanZero();
                        }

                        // NEXT CANDIDATE
                        matchingData.candidatesIT[si]++;

                        if (shouldBacktrack()) { // BACKTRACKING
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
                                updateCandidatesForStateGraterThanZero();
                            }
                        }
                    }

                    // CLEANING OF STRUCTURES
                    removeMatchingInfoForStateZero();
                }
            }
        }
        return numTotalOccs;
    }
}
