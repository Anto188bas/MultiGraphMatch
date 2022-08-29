package matching.controllers;

import cypher.models.QueryEdge;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matching.models.MatchingData;
import ordering.EdgeDirection;
import ordering.NodesPair;
import ordering.OrderingUtils;
import org.javatuples.Triplet;
import org.opencypher.v9_0.expressions.In;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;
import java.util.ArrayList;


public class NewFindCandidates {
    // GLOBAL VARIABLE
    private static final int[] src_vet = new int[] {0};
    private static final int[] dst_vet = new int[] {1};
    private static final int[] src_dst = new int[] {0, 1};
    private static final int[] dst_src = new int[] {1, 0};


    // FIND CANDIDATE FOR SELECTED EDGES (FROM STATE 1 TO STATE N)
    public static IntArrayList find_candidates (
            GraphPaths           graphPaths,
            QueryStructure       query,
            int                  sel_state,
            IntArrayList[]       nodes_symmetry,
            IntArrayList[]       edges_symmetry,
            StateStructures      states,
            MatchingData         matchingData
    ) {
//        // PARAMETERS
        IntArrayList listCandidates    = new IntArrayList();

        int           edge_id           = states.map_state_to_edge[sel_state];
        NodesPair     edge_data         = query.getMap_edge_to_endpoints().get(edge_id);
        QueryEdge     queryEdge         = query.getQuery_edge(edge_id);
        EdgeDirection direction         = states.map_edge_to_direction[edge_id];
        IntArrayList  edge_type         = queryEdge.getEdge_label();

        // QUERY NODES
        int q_src                      = edge_data.getFirstEndpoint();
        int q_dst                      = edge_data.getSecondEndpoint();
        // TARGET NODES
        int t_src                      = matchingData.solution_nodes[q_src];
        int t_dst                      = matchingData.solution_nodes[q_dst];

        ArrayList<Triplet<Integer, Integer, Integer>> edges_submap  = new ArrayList<>();
        int[]         cols;
        // A. t_dst IS MATCHED
        if(t_src == -1) {
            IntArrayList compatible_list   = edge_data.getBySecondValue(t_dst);
            if(compatible_list != null) {
                // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
                if (direction == EdgeDirection.OUT) {          // src to match
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_dst, compatible_list));
                    cols = src_vet;
                }
                // 2. EDGE FROM q_dst TO q_src (INBOUND)
                else if (direction == EdgeDirection.IN) {    // dst to match
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_dst, compatible_list));
                    cols = dst_vet;
                }
                // 3. UNDIRECTED CASE          (BOTH)
                else {                        // 1. src to match and 2. dst to match
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_dst, compatible_list));
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_dst, compatible_list));
                    cols = src_dst;
                }
                // CANDIDATE CONFIGURATION
                if (edge_type.size() == 0)
                    NewEdgeSelector.no_types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_src, cols, graphPaths);
                else
                    NewEdgeSelector.types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_src, cols, graphPaths, queryEdge);
            }
        }

        // B. t_src IS MATCHED
        else if (t_dst == -1){
            IntArrayList compatible_subtable = edge_data.getByFirstValue(t_src);
            if(compatible_subtable != null) {

                // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
                if (direction == EdgeDirection.OUT) {
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_src, compatible_subtable));
                    cols = dst_vet;
                }
                // 2. EDGE FROM q_dst TO q_src (INBOUND)
                else if (direction == EdgeDirection.IN) {
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_src, compatible_subtable));
                    cols = src_vet;
                }
                // 3. UNDIRECTED CASE          (BOTH)
                else {
                    edges_submap.addAll(graphPaths.getBySRCandDSTs(t_src, compatible_subtable));
                    edges_submap.addAll(graphPaths.getByDSTandSRCs(t_src, compatible_subtable));
                    cols = dst_src;
                }
                // CANDIDATE CONFIGURATION
                if (edge_type.size() == 0) {
                    NewEdgeSelector.no_types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_dst, cols, graphPaths);
                }
                else {
                    NewEdgeSelector.types_case(edges_submap, matchingData, nodes_symmetry, listCandidates, q_dst, cols, graphPaths, queryEdge);
                }
            }
        }
        else {
            // 1. EDGE FROM q_src TO q_dst (OUTBOUND)
            if (direction == EdgeDirection.OUT) {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_src, t_dst);

                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            // 2. EDGE FROM q_dst TO q_src (INBOUND)
            else if (direction == EdgeDirection.IN) {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_dst, t_src);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            // 3. UNDIRECTED CASE          (BOTH)
            else {
                Triplet<Integer, Integer, Integer> triplet = graphPaths.getBySRCandDST(t_src, t_dst);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
                triplet = graphPaths.getBySRCandDST(t_dst, t_src);
                if(triplet != null) {
                    edges_submap.add(triplet);
                }
            }
            if (edge_type.size() == 0)
                NewEdgeSelector.no_types_case_matched_nodes(edges_submap, matchingData, edges_symmetry, listCandidates, graphPaths, states, edge_id);
            else
                NewEdgeSelector.type_case_matched_nodes(edges_submap, matchingData, edges_symmetry, listCandidates, graphPaths, states, edge_id, queryEdge);
        }
        return listCandidates;
    }


    //
    public static IntArrayList find_first_candidates(
            int q_src, int q_dst, int t_src, int t_dst, int edge_id,
            QueryStructure query,
            GraphPaths     graphPaths,
            MatchingData   matchingData,
            IntArrayList[] nodes_symmetry,
            StateStructures states
    ){
        IntArrayList listCandidates    = new IntArrayList();
        QueryEdge        queryEdge     = query.getQuery_edge(edge_id);
        EdgeDirection    direction     = states.map_edge_to_direction[edge_id];
        IntArrayList     edge_type     = queryEdge.getEdge_label();
        ArrayList<Triplet<Integer, Integer, Integer>> edges_submap  = new ArrayList<>();
        int q_node = q_src;

        // q_src = t_src AND q_dst = t_dst
        if (direction == EdgeDirection.OUT) {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
        }
        else if (direction == EdgeDirection.IN) {
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        }
        else {
            edges_submap.add(graphPaths.getBySRCandDST(t_src, t_dst));
            edges_submap.add(graphPaths.getBySRCandDST(t_dst, t_src));
        }

        if (edge_type.size() == 0)
            NewEdgeSelector.no_type_case(edges_submap, q_node, graphPaths, matchingData, nodes_symmetry, listCandidates);
        else
            NewEdgeSelector.types_case(edges_submap, q_node, graphPaths, matchingData, nodes_symmetry, listCandidates, queryEdge, t_src, t_dst);
        return listCandidates;
    }

    /**
     * Method used for paths matching.
     * @return
     */
    public static  Int2ObjectOpenHashMap<IntArraySet> getAdiacs(int startTargetNode, int queryEdgeID, QueryStructure query, StateStructures states, GraphPaths graphPaths) {
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

//    public static  Int2ObjectOpenHashMap<IntArraySet> getAdiacs(int startTargetNode, int queryEdgeID, QueryStructure query, StateStructures states, GraphPaths graphPaths) {
//        Int2ObjectOpenHashMap<IntArraySet> adiacs = new Int2ObjectOpenHashMap<>();  // <node, {edge1, edge2, ...}>
//
//        EdgeDirection direction = states.map_edge_to_direction[queryEdgeID];
//        QueryEdge queryEdge = query.getQuery_edge(queryEdgeID);
//
//        IntArrayList edgeLabels = queryEdge.getEdge_label();
//        // For each state we both know the source and the destination node.
//        // So, if the edge is directed, we can only consider out-coming edges.
//        // Otherwise, we must consider both in-coming and out-coming edges.
//
//        if(edgeLabels.size() == 0) { // Edge without labels
//            if(direction != EdgeDirection.BOTH) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
//                adiacs = graphPaths.getAdiacsBySrc(startTargetNode);
//            } else { // UNDIRECTED CASE (BOTH)
//                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrc(startTargetNode), graphPaths.getAdiacsByDst(startTargetNode));
//            }
//        } else { // Edge with one or more labels
//            if(direction != EdgeDirection.BOTH) { // DIRECTED CASE - EDGE FROM q_src TO q_dst (OUTBOUND)
//                adiacs = graphPaths.getAdiacsBySrcAndColors(startTargetNode, edgeLabels);
//            } else { // 3. UNDIRECTED CASE (BOTH)
//                adiacs = OrderingUtils.mergeInt2ObjectOpenHashMap(graphPaths.getAdiacsBySrcAndColors(startTargetNode, edgeLabels), graphPaths.getAdiacsByDstAndColors(startTargetNode, edgeLabels));
//            }
//        }
//
//        return adiacs;
//    }

    public static  Int2ObjectOpenHashMap<IntArraySet> getAdiacsByDirection(int targetNode, IntArrayList edgeLabels, GraphPaths graphPaths, EdgeDirection direction) {
        Int2ObjectOpenHashMap<IntArraySet> adiacs = new Int2ObjectOpenHashMap<>();  // <node, {edge1, edge2, ...}>

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

    public static ObjectArrayList<IntArrayList> findStartPaths(int startTargetNode, int si, QueryStructure query, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodesSymmetryConditions, StateStructures states) {
        ObjectArrayList<IntArrayList> listCandidates = new ObjectArrayList<>();
        int queryEdgeID = states.map_state_to_edge[si];
        int querySourceID = states.map_state_to_first_endpoint[si];

        EdgeDirection direction = states.map_edge_to_direction[queryEdgeID];
        QueryEdge queryEdge = query.getQuery_edge(queryEdgeID);

        IntArrayList edgeLabels = queryEdge.getEdge_label();

        Int2ObjectOpenHashMap<IntArraySet> adiacs = getAdiacs(startTargetNode, queryEdgeID, query, states, graphPaths);

        //TODO: replace with cypher
        int minLenght = 1;
        int maxLenght = 10;
        exploreStartPaths(startTargetNode, startTargetNode, 0, minLenght, maxLenght, queryEdge, queryEdgeID, matchingData, query, states, nodesSymmetryConditions, listCandidates, adiacs, querySourceID, new IntArrayList(), new IntArrayList(), graphPaths, query);

        return listCandidates;
    }

    public static void exploreStartPaths(int startTargetNode, int currentTargetNode, int depth, int minLength, int maxLength, QueryEdge queryEdge, int queryEdgeID, MatchingData matchingData,
        QueryStructure queryStructure, StateStructures states, IntArrayList[] nodesSymmetryConditions, ObjectArrayList<IntArrayList> listCandidates,
        Int2ObjectOpenHashMap<IntArraySet> adiacs, int sourceQueryNode, IntArrayList currentCand, IntArrayList setVisited, GraphPaths graphPaths, QueryStructure query) {

        if(depth >= minLength && depth <= maxLength) {
            // If the currentTargetNode is in the currentQueryNode's domain AND the conditions are satisfied
            if(queryStructure.getMap_node_to_domain().get(sourceQueryNode).contains(currentTargetNode) && NewEdgeSelector.nodeCondCheck(sourceQueryNode, startTargetNode, matchingData, nodesSymmetryConditions)) {
                IntArrayList newCand=new IntArrayList(currentCand);
                newCand.add(startTargetNode);
                listCandidates.add(newCand);
            }
        }

        if(depth < maxLength) {
            adiacs.forEach((currentTargetNodeAdiacID, edgeSet) -> {
                if(!setVisited.contains(currentTargetNodeAdiacID.intValue())) {

                    for(int edgeID: edgeSet) {

                        //TODO: replace with edgeCondCheck
                        if(true) {
                            setVisited.add(currentTargetNodeAdiacID.intValue());
                            currentCand.add(currentTargetNodeAdiacID.intValue());
                            currentCand.add(edgeID);
                            Int2ObjectOpenHashMap<IntArraySet> newAdiacs = getAdiacs(currentTargetNodeAdiacID, queryEdgeID, query, states, graphPaths);

                            exploreStartPaths(startTargetNode, currentTargetNodeAdiacID, depth + 1, minLength, maxLength, queryEdge, queryEdgeID, matchingData, queryStructure, states, nodesSymmetryConditions, listCandidates, newAdiacs, sourceQueryNode, currentCand, setVisited, graphPaths, query);
                            currentCand.removeInt(currentCand.size() - 1);
                            currentCand.removeInt(currentCand.size() - 1);
                            setVisited.removeInt(currentTargetNodeAdiacID);
                        }
                    }

                }
            });
        }
    }

    public static ObjectArrayList<IntArrayList> findPaths(int si, QueryStructure query, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodesSymmetryConditions, IntArrayList[] edgesSymmetryConditions,  StateStructures states) {
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

    //TODO: move to path class
    public static boolean pathConditionCheck(int queryEdgeID, IntArrayList path, MatchingData matchingData, StateStructures states, IntArrayList[] edgesSymmetryConditions) {
        boolean check = true;

        IntArrayList condEdge = edgesSymmetryConditions[queryEdgeID];
        for(int i = 0; i < condEdge.size(); i++) {
            IntArrayList refPath = matchingData.solution_paths[states.map_edge_to_state[condEdge.getInt(i)]];
            if(refPath!=null && (path.size()!=refPath.size() || path.getInt(0)<refPath.getInt(0)))
            {
                check = false;
                break;
            }
        }

        return check;
    }

    public static void explorePaths(int startQueryNodeID, int endQueryNodeID, int startTargetNodeID, int endTargetNodeID, int currentTargetNodeID, EdgeDirection directionToConsider, int depth, int minLength, int maxLength, QueryEdge queryEdge, int queryEdgeID, MatchingData matchingData,
                                         QueryStructure queryStructure, StateStructures states, IntArrayList[] nodesSymmetryConditions, IntArrayList[] edgesSymmetryConditions, ObjectArrayList<IntArrayList> listCandidates,
                                         Int2ObjectOpenHashMap<IntArraySet> adiacs, IntArrayList currentCand, IntArrayList setVisited, GraphPaths graphPaths, QueryStructure query) {
        System.out.println("startQueryNodeID:" + startQueryNodeID + "\tendQueryNodeID: " + endQueryNodeID + "\tstartTargetNodeID: " + startTargetNodeID + "\tendTargetNodeID: " + endTargetNodeID + "\tcurrentTargetNodeID: " + currentTargetNodeID + "\tdirectionToConsider: " + directionToConsider );
        if(depth >= minLength && depth <= maxLength) {
            if (
                    (
                            (endTargetNodeID == -1) || // un-matched target node
                            (currentTargetNodeID == endTargetNodeID) && pathConditionCheck(queryEdgeID, currentCand, matchingData, states, edgesSymmetryConditions)
                    )
                            && query.getMap_node_to_domain().get(endQueryNodeID).contains(currentTargetNodeID) // TODO: check if the domain is correct (source or dest?)
                            && NewEdgeSelector.nodeCondCheck(endQueryNodeID, currentTargetNodeID, matchingData, nodesSymmetryConditions)
            ) {
                listCandidates.add(currentCand.clone());
            }
        }
        if(depth < maxLength) {
            adiacs.forEach((currentTargetAdjacentNodeID, currentTargetAdjacentEdgeSet) -> {
                if(!matchingData.matchedNodes.contains(currentTargetAdjacentNodeID.intValue()) && !setVisited.contains(currentTargetAdjacentNodeID.intValue())) {
                    for(int currentTargetAdjacentEdgeID : currentTargetAdjacentEdgeSet) {
                        // TODO: replace with edgeCondCheck
                        if(!matchingData.matchedEdges.contains(currentTargetAdjacentEdgeID) && true) {
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
