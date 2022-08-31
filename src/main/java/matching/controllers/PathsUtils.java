package matching.controllers;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.MatchingData;
import matching.models.PathsMatchingData;
import ordering.EdgeDirection;
import ordering.OrderingUtils;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;

public class PathsUtils {
    /**
     * Method used for paths matching.
     * @return
     */
    public static Int2ObjectOpenHashMap<IntArraySet> getAdiacs(int startTargetNode, int queryEdgeID, QueryStructure query, StateStructures states, GraphPaths graphPaths) {
        Int2ObjectOpenHashMap<IntArraySet> adiacs = new Int2ObjectOpenHashMap<>();  // <node, {edge1, edge2, ...}>

        EdgeDirection direction = states.map_edge_to_direction[queryEdgeID];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeID);

        IntArrayList edgeLabels = queryEdge.getEdge_label();
        // For each state we both know the source and the destination node.
        // So, if the edge is directed, we can only consider out-coming edges.
        // Otherwise, we must consider both in-coming and out-coming edges.

        if(edgeLabels.size() == 0) { // Edge without labels
            if(direction != EdgeDirection.BOTH) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
                adiacs = graphPaths.getAdiacsBySrc(startTargetNode);
            } else { // UNDIRECTED CASE (BOTH)
                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrc(startTargetNode), graphPaths.getAdiacsByDst(startTargetNode));
            }
        } else { // Edge with one or more labels
            if(direction != EdgeDirection.BOTH) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
                adiacs = graphPaths.getAdiacsBySrcAndColors(startTargetNode, edgeLabels);
            } else { // 3. UNDIRECTED CASE (BOTH)
                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrcAndColors(startTargetNode, edgeLabels), graphPaths.getAdiacsByDstAndColors(startTargetNode, edgeLabels));
            }
        }

        return adiacs;
    }


    public static  Int2ObjectOpenHashMap<IntArraySet> getAdiacsByDirection(int targetNode, IntArrayList edgeLabels, GraphPaths graphPaths, EdgeDirection direction) {
        Int2ObjectOpenHashMap<IntArraySet> adiacs;  // <node, {edge1, edge2, ...}>

        if(edgeLabels.size() == 0) { // Edge without labels
            if(direction == EdgeDirection.OUT) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
                adiacs = graphPaths.getAdiacsBySrc(targetNode);
            }  else if(direction == EdgeDirection.IN) { // DIRECTED CASE - EDGE FROM q_dst TO q_src (INBOUND)
                adiacs = graphPaths.getAdiacsByDst(targetNode);
            }
            else { // UNDIRECTED CASE (BOTH)
                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrc(targetNode), graphPaths.getAdiacsByDst(targetNode));
            }
        } else { // Edge with one or more labels
            if(direction == EdgeDirection.OUT) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
                adiacs = graphPaths.getAdiacsBySrcAndColors(targetNode, edgeLabels);
            }  else if(direction == EdgeDirection.IN) { // DIRECTED CASE - EDGE FROM q_dst TO q_src (INBOUND)
                adiacs = graphPaths.getAdiacsByDstAndColors(targetNode, edgeLabels);
            } else { // 3. UNDIRECTED CASE (BOTH)
                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrcAndColors(targetNode, edgeLabels), graphPaths.getAdiacsByDstAndColors(targetNode, edgeLabels));
            }
        }

        return adiacs;
    }

    /**
     * Method used to retrieve candidates for the first state.
     */
    public static ObjectArrayList<IntArrayList> findStartPaths(int startTargetNode, QueryStructure query, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodesSymmetryConditions, StateStructures states) {
        ObjectArrayList<IntArrayList> listCandidates = new ObjectArrayList<>();

        int firstQueryEndpointID = states.map_state_to_first_endpoint[0];
        int secondQueryEndpointID = states.map_state_to_second_endpoint[0];

        int queryEdgeID = states.map_state_to_edge[0];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeID);
        IntArrayList edgeLabels = queryEdge.getEdge_label();

        EdgeDirection direction = states.map_edge_to_direction[queryEdgeID];

        // For the state 0, we first match firstQueryEndpointID. We are sure that secondQueryEndpointID is not matched.
        // Starting from the state 1, we can have several situations:
        // - firstQueryEndpointID matched, secondQueryEndpointID not matched;
        // - firstQueryEndpointID not matched, secondQueryEndpointID matched;
        // - both firstQueryEndpointID and secondQueryEndpointID matched.

        int firstTargetEndpointID = matchingData.solution_nodes[firstQueryEndpointID];
        int secondTargetEndpointID = matchingData.solution_nodes[secondQueryEndpointID]; // Always -1

        int startTargetNodeID = firstTargetEndpointID;
        int endTargetNodeID = secondTargetEndpointID; // Always -1

        Int2ObjectOpenHashMap<IntArraySet> adjacentMap = getAdiacsByDirection(startTargetNodeID, edgeLabels, graphPaths, direction);

        //TODO: replace with cypher
        int minDepth = 1;
        int maxDepth = 10;

        exploreStartPaths(firstQueryEndpointID, secondQueryEndpointID, startTargetNodeID, endTargetNodeID, startTargetNodeID, direction, 0, minDepth, maxDepth, queryEdge, queryEdgeID, matchingData, query, states, nodesSymmetryConditions, listCandidates, adjacentMap, new IntArrayList(), new IntArrayList(), graphPaths, query);

        return listCandidates;
    }

    public static void exploreStartPaths(int startQueryNodeID, int endQueryNodeID, int startTargetNodeID, int endTargetNodeID, int currentTargetNodeID, EdgeDirection directionToConsider, int depth, int minLength, int maxLength, QueryEdge queryEdge, int queryEdgeID, MatchingData matchingData,
                                    QueryStructure queryStructure, StateStructures states, IntArrayList[] nodesSymmetryConditions, ObjectArrayList<IntArrayList> listCandidates,
                                    Int2ObjectOpenHashMap<IntArraySet> adiacs, IntArrayList currentCand, IntArrayList setVisited, GraphPaths graphPaths, QueryStructure query) {
        if(depth >= minLength && depth <= maxLength) {
            if (query.getMap_node_to_domain().get(endQueryNodeID).contains(currentTargetNodeID) &&
                    NewEdgeSelector.nodeCondCheck(endQueryNodeID, currentTargetNodeID, matchingData, nodesSymmetryConditions)
            ) {
                listCandidates.add(currentCand.clone());
            }
        }

        if(depth < maxLength) {
            adiacs.forEach((currentTargetAdjacentNodeID, currentTargetAdjacentEdgeSet) -> {
                if(!setVisited.contains(currentTargetAdjacentNodeID.intValue())) {
                    for(int currentTargetAdjacentEdgeID : currentTargetAdjacentEdgeSet) {
                        setVisited.add(currentTargetAdjacentNodeID.intValue());
                        currentCand.add(currentTargetAdjacentEdgeID);
                        currentCand.add(currentTargetAdjacentNodeID.intValue());

                        Int2ObjectOpenHashMap<IntArraySet> newAdjacentMap = getAdiacsByDirection(currentTargetAdjacentNodeID, queryEdge.getEdge_label(),graphPaths, directionToConsider);
                        exploreStartPaths(startQueryNodeID, endQueryNodeID, startTargetNodeID, endTargetNodeID, currentTargetAdjacentNodeID.intValue(), directionToConsider, depth + 1, minLength, maxLength, queryEdge, queryEdgeID, matchingData, queryStructure, states, nodesSymmetryConditions, listCandidates, newAdjacentMap, currentCand, setVisited, graphPaths, query);

                        currentCand.removeInt(currentCand.size()-1);
                        currentCand.removeInt(currentCand.size()-1);
                        setVisited.removeInt(setVisited.size()-1);
                    }
                }
            });
        }
    }

    public static ObjectArrayList<IntArrayList> findPaths(int si, QueryStructure query, GraphPaths graphPaths, PathsMatchingData matchingData, IntArrayList[] nodesSymmetryConditions, IntArrayList[] edgesSymmetryConditions,  StateStructures states) {
        ObjectArrayList<IntArrayList> listCandidates = new ObjectArrayList<>();

        int firstQueryEndpointID = states.map_state_to_first_endpoint[si];
        int secondQueryEndpointID = states.map_state_to_second_endpoint[si];
        int queryEndpointToMatch = states.map_state_to_unmatched_node[si];

        int queryEdgeID = states.map_state_to_edge[si];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeID);
        IntArrayList edgeLabels = queryEdge.getEdge_label();

        EdgeDirection direction = states.map_edge_to_direction[queryEdgeID];

        int firstTargetEndpointID = matchingData.solution_nodes[firstQueryEndpointID];
        int secondTargetEndpointID = matchingData.solution_nodes[secondQueryEndpointID];

        int startTargetNodeID, endTargetNodeID, startQueryNodeID, endQueryNodeID;
        EdgeDirection directionToConsider;

        if(queryEndpointToMatch == firstQueryEndpointID) { // firstQueryEndpointID un-matched, secondQueryEndpointID matched
            startTargetNodeID = secondTargetEndpointID;
            endTargetNodeID = firstTargetEndpointID;

            startQueryNodeID = secondQueryEndpointID;
            endQueryNodeID = firstQueryEndpointID;

            if(direction == EdgeDirection.IN) {
                directionToConsider = EdgeDirection.OUT;
            } else if(direction == EdgeDirection.OUT) {
                directionToConsider = EdgeDirection.IN;
            } else { // BOTH
                directionToConsider = EdgeDirection.BOTH;
            }
        } else if(queryEndpointToMatch == secondQueryEndpointID) { // firstQueryEndpointID matched, secondQueryEndpointID un-matched
            startTargetNodeID = firstTargetEndpointID;
            endTargetNodeID = secondTargetEndpointID;

            startQueryNodeID = firstQueryEndpointID;
            endQueryNodeID = secondQueryEndpointID;

            directionToConsider = direction;
        } else { // Both endpoints are matched
            // Here we consider the starting node depending on the edge direction.
            // Both endpoints are matched, so we consider the starting endpoint in order to use only out-coming edges (they can be retrieved easily than in-coming edges).
            if(direction == EdgeDirection.IN) {
                startQueryNodeID = secondQueryEndpointID;
                endQueryNodeID = firstQueryEndpointID;

                startTargetNodeID = secondTargetEndpointID;
                endTargetNodeID = firstTargetEndpointID;

                directionToConsider = EdgeDirection.OUT;
            } else if(direction == EdgeDirection.OUT) {
                startQueryNodeID = firstQueryEndpointID;
                endQueryNodeID = secondQueryEndpointID;

                startTargetNodeID = firstTargetEndpointID;
                endTargetNodeID = secondTargetEndpointID;

                directionToConsider = EdgeDirection.OUT;
            } else { // BOTH
                // In this case we have to consider both in-coming and out-coming edges. No matter which node is the starting node.
                // For convention, we consider the firstQueryEndpointID as the starting node.

                startQueryNodeID = firstQueryEndpointID;
                endQueryNodeID = secondQueryEndpointID;

                startTargetNodeID = firstTargetEndpointID;
                endTargetNodeID = secondTargetEndpointID;

                directionToConsider = EdgeDirection.BOTH;
            }
        }

        Int2ObjectOpenHashMap<IntArraySet> adjacentMap = getAdiacsByDirection(startTargetNodeID, edgeLabels, graphPaths, directionToConsider);
        System.out.println("startTargetNodeID: " + startTargetNodeID + "\tendTargetNodeID: " + endTargetNodeID + "\tstartQueryNodeID: " + startQueryNodeID + "\tendQueryNodeID: " + endQueryNodeID + "\tDirection: " + directionToConsider + "\tAdjacentMap: " + adjacentMap);

        //TODO: replace with cypher
        int minDepth = 1;
        int maxDepth = 10;

        explorePaths(startQueryNodeID, endQueryNodeID, startTargetNodeID, endTargetNodeID, startTargetNodeID, directionToConsider, 0, minDepth, maxDepth, queryEdge, queryEdgeID, matchingData, query, states, nodesSymmetryConditions, edgesSymmetryConditions, listCandidates, adjacentMap, new IntArrayList(), new IntArrayList(), graphPaths, query);


        return listCandidates;
    }


    public static boolean pathConditionCheck(int queryEdgeID, IntArrayList path, PathsMatchingData matchingData, StateStructures states, IntArrayList[] edgesSymmetryConditions) {
        boolean check = true;

        IntArrayList condEdge = edgesSymmetryConditions[queryEdgeID];
        for(int i = 0; i < condEdge.size(); i++) {
            IntArrayList refPath = matchingData.solutionPaths[states.map_edge_to_state[condEdge.getInt(i)]];
            if(refPath!=null && (path.size()!=refPath.size() || path.getInt(0)<refPath.getInt(0)))
            {
                check = false;
                break;
            }
        }

        return check;
    }

    public static void explorePaths(int startQueryNodeID, int endQueryNodeID, int startTargetNodeID, int endTargetNodeID, int currentTargetNodeID, EdgeDirection directionToConsider, int depth, int minLength, int maxLength, QueryEdge queryEdge, int queryEdgeID, PathsMatchingData matchingData,
                                    QueryStructure queryStructure, StateStructures states, IntArrayList[] nodesSymmetryConditions, IntArrayList[] edgesSymmetryConditions, ObjectArrayList<IntArrayList> listCandidates,
                                    Int2ObjectOpenHashMap<IntArraySet> adiacs, IntArrayList currentCand, IntArrayList setVisited, GraphPaths graphPaths, QueryStructure query) {
        if(depth >= minLength && depth <= maxLength) {
            if (
                    (
                            (endTargetNodeID == -1) || // un-matched target node
                                    (currentTargetNodeID == endTargetNodeID) && pathConditionCheck(queryEdgeID, currentCand, matchingData, states, edgesSymmetryConditions)
                    )
                            && query.getMap_node_to_domain().get(endQueryNodeID).contains(currentTargetNodeID)
                            && NewEdgeSelector.nodeCondCheck(endQueryNodeID, currentTargetNodeID, matchingData, nodesSymmetryConditions)
            ) {
                listCandidates.add(currentCand.clone());
            }
        }
        if(depth < maxLength) {
            adiacs.forEach((currentTargetAdjacentNodeID, currentTargetAdjacentEdgeSet) -> {
                if(!matchingData.matchedNodes.contains(currentTargetAdjacentNodeID.intValue()) && !setVisited.contains(currentTargetAdjacentNodeID.intValue())) {
                    for(int currentTargetAdjacentEdgeID : currentTargetAdjacentEdgeSet) {
                        if(!matchingData.matchedEdges.contains(currentTargetAdjacentEdgeID)) {
                            setVisited.add(currentTargetAdjacentNodeID.intValue());
                            currentCand.add(currentTargetAdjacentEdgeID);
                            currentCand.add(currentTargetAdjacentNodeID.intValue());

                            Int2ObjectOpenHashMap<IntArraySet> newAdjacentMap = getAdiacsByDirection(currentTargetAdjacentNodeID, queryEdge.getEdge_label(),graphPaths, directionToConsider);
                            explorePaths(startQueryNodeID, endQueryNodeID, startTargetNodeID, endTargetNodeID, currentTargetAdjacentNodeID.intValue(), directionToConsider, depth + 1, minLength, maxLength, queryEdge, queryEdgeID, matchingData, queryStructure, states, nodesSymmetryConditions, edgesSymmetryConditions, listCandidates, newAdjacentMap, currentCand, setVisited, graphPaths, query);

                            currentCand.removeInt(currentCand.size()-1);
                            currentCand.removeInt(currentCand.size()-1);
                            setVisited.removeInt(setVisited.size()-1);
                        }

                    }
                }
            });
        }
    }
}
