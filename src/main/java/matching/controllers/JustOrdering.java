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
import ordering.DebugOrdering;
import ordering.EdgeDirection;
import ordering.EdgeOrdering;
import ordering.NodesPair;

import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import target_graph.graph.TargetGraph;
import utility.Utils;


public class JustOrdering extends MatchingBase {
    ObjectArrayList<QueryCondition> simpleConditions;
    public DebugOrdering debugEdgeOrdering;

    public JustOrdering(OutData outData, QueryStructure query, boolean justCount, boolean distinct, long numMaxOccs, TargetGraph targetGraph, TargetBitmatrix target_bitmatrix, ObjectArrayList<QueryCondition> simpleConditions) {
        super(outData, query, justCount, distinct, numMaxOccs, targetGraph, target_bitmatrix);
        this.simpleConditions = simpleConditions;
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
            computeCompatibilityDomains();
        }

        for(NodesPair pair: query.getPairs()) {
            if(pair.getFirst_second().size() == 0) {
                System.out.println("\t\t\tEmpty compatibility domain for pair: " + pair);
                return outData;
            }
        }

        // EDGE ORDERING AND STATE OBJECT CREATION
        computeOrdering();

        //DEBUG INFO
        printDebugInfo();
        return outData;
    }

    protected void computeOrdering() {
        outData.ordering_time = System.currentTimeMillis();
        debugEdgeOrdering = new DebugOrdering(query);
        states.map_state_to_edge = debugEdgeOrdering.getMap_state_to_edge();
        states.map_edge_to_state = debugEdgeOrdering.getMap_edge_to_state();
        states.map_state_to_first_endpoint = debugEdgeOrdering.getMap_state_to_first_endpoint();
        states.map_state_to_second_endpoint = debugEdgeOrdering.getMap_state_to_second_endpoint();
        states.map_state_to_unmatched_node = debugEdgeOrdering.getMap_state_to_unmapped_nodes();
        states.map_edge_to_direction = debugEdgeOrdering.getMap_edge_to_direction();
        outData.ordering_time = (System.currentTimeMillis() - outData.ordering_time) / 1000;
    }

    public void printDebugInfo()  {
        /**
         * LOG
         */
        System.out.println("QUERY NODES");
        query.getQuery_nodes().forEach((id, node) -> {
            System.out.println("ID: " + id + "-> " + node);
        });

        System.out.println("QUERY EDGES");
        query.getQuery_pattern().getOut_edges().forEach((key, list) -> {
            System.out.println(key + "->" + list);
        });

        System.out.println("PAIRS ORDERING");
        System.out.println(debugEdgeOrdering.getPairs_ordering());

        System.out.println("ORDERING DETAILS");
        for (int i = 0; i < states.map_state_to_first_endpoint.length; i++) {
            int edge = states.map_state_to_edge[i];
            int src = states.map_state_to_first_endpoint[i];
            int dst = states.map_state_to_second_endpoint[i];
            int matchedNode = states.map_state_to_unmatched_node[i];
            EdgeDirection direction = states.map_edge_to_direction[i];
            System.out.println("STATE: " + i + "\tSRC: " + src + "\tDST: " + dst + "\tEDGE: " + edge + "\tDIRECTION: " + direction + "\tUN-MATCHED_NODE: " + matchedNode);
        }
    }
}
