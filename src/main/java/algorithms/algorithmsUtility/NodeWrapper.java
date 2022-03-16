package algorithms.algorithmsUtility;

/**
 *
 * Data structure containing a node, it's total distance from the start and its predecessor.
 *
 * <p>Used by {@link DijkstraColor}.
 *
 */
class NodeWrapper<N extends Comparable<N>>
        implements Comparable<NodeWrapper<N>> {
    private final N node;
    private int totalDistance;
    private NodeWrapper<N> predecessor;

    NodeWrapper(N node, int totalDistance, NodeWrapper<N> predecessor) {
        this.node = node;
        this.totalDistance = totalDistance;
        this.predecessor = predecessor;
    }

    N getNode() {
        return node;
    }

    void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setPredecessor(NodeWrapper<N> predecessor) {
        this.predecessor = predecessor;
    }

    public NodeWrapper<N> getPredecessor() {
        return predecessor;
    }

    @Override
    public int compareTo(NodeWrapper<N> other) {
        int compare = Integer.compare(this.totalDistance, other.totalDistance);
        if (compare == 0) {
            compare = node.compareTo(other.node);
        }
        return compare;
    }

}

