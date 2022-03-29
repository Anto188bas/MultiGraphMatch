package algorithms;

public class RewiringGraphEdges {
    Integer U;
    Integer V;
    Integer color;

    public RewiringGraphEdges(Integer U, Integer V, Integer color) {
        this.U = U;
        this.V = V;
        this.color = color;
    }

    @Override
    public String toString() {
        return  "nodeU=" + U +
                ", nodeV=" + V +
                ", color=" + color +
                '}';
    }
}
