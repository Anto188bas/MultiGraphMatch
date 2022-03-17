package algorithms;

import com.google.common.graph.ValueGraph;
import java.util.*;

/**
 *
 * Implementation of Dijkstra's algorithm with a {@link TreeSet} and a data structure holding the
 *
 */
@SuppressWarnings("UnstableApiUsage")
public class ColorShortestPath {

    /**
     *
     * Finds the shortest path from {@code source} to {@code target}.
     *
     * @param graph the graph
     * @param source the source node
     * @param target the target node
     * @param <N> the node type
     * @return the shortest path; or {@code null} if no path was found
     *
     */
    public static <N extends Comparable<N>> List<N> findShortestPath(ValueGraph<N, Integer> graph, N source, N target, int edgeColor) {
        Map<N, NodeWrapper<N>> nodeWrappers = new HashMap<>();
        TreeSet<NodeWrapper<N>> queue = new TreeSet<>();
        Set<N> shortestPathFound = new HashSet<>();

        // Add source to queue
        NodeWrapper<N> sourceWrapper = new NodeWrapper<>(source, 0, null);
        nodeWrappers.put(source, sourceWrapper);
        queue.add(sourceWrapper);

        while (!queue.isEmpty()) {
            NodeWrapper<N> nodeWrapper = queue.pollFirst();
            assert nodeWrapper != null;
            N node = nodeWrapper.getNode();
            shortestPathFound.add(node);

            // if the target is reached Build and return the path
            if (node.equals(target)) {
                return buildPath(nodeWrapper);
            }

            // Iterate over all neighbors
            Set<N> neighbors = graph.adjacentNodes(node);
            for (N neighbor : neighbors) {
                // Ignore neighbor if shortest path already found

                //we want to ignore the neighbor if the edge is of a different color
                Optional<Integer> color = graph.edgeValue(node, neighbor);  //color of the edge

                if (color.isPresent()) {
                    if(color.get() != edgeColor)  continue;
                } else continue;


                if (shortestPathFound.contains(neighbor)) {
                    continue;
                }

                // Calculate total distance from start to neighbor via current node
                int distance = graph.edgeValue(node, neighbor).orElseThrow(IllegalStateException::new);
                int totalDistance = nodeWrapper.getTotalDistance() + distance;

                // Neighbor not yet discovered?
                NodeWrapper<N> neighborWrapper = nodeWrappers.get(neighbor);
                if (neighborWrapper == null) {
                    neighborWrapper = new NodeWrapper<>(neighbor, totalDistance, nodeWrapper);
                    nodeWrappers.put(neighbor, neighborWrapper);
                    queue.add(neighborWrapper);
                }

                // Neighbor discovered, but total distance via current node is shorter?
                // --> Update total distance and predecessor
                else if (totalDistance < neighborWrapper.getTotalDistance() && distance == edgeColor) {
                    // The position in the TreeSet won't change automatically;
                    // we have to remove and reinsert the node.
                    // Because TreeSet uses compareTo() to identity a node to remove,
                    // we have to remove it *before* we change the total distance!
                    queue.remove(neighborWrapper);

                    neighborWrapper.setTotalDistance(totalDistance);
                    neighborWrapper.setPredecessor(nodeWrapper);

                    queue.add(neighborWrapper);
                }
            }
        }

        // if all nodes were visited but the target was not found
        return null;
    }

    private static <N extends Comparable<N>> List<N> buildPath(NodeWrapper<N> nodeWrapper) {
        List<N> path = new ArrayList<>();
        while (nodeWrapper != null) {
            path.add(nodeWrapper.getNode());
            nodeWrapper = nodeWrapper.getPredecessor();
        }
        Collections.reverse(path);
        return path;
    }
}
