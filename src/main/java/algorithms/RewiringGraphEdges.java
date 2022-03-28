package algorithms;


import java.util.Optional;

public class RewiringGraphEdges {
    Integer U;
    Integer V;
    Integer color;

    public RewiringGraphEdges(Integer U, Integer V, Optional<Integer> color){
        this.U = U;
        this.V = V;
        color.ifPresent(integer -> this.color = integer);
    }

    @Override
    public String toString() {
        return  "nodeU=" + U +
                ", nodeV=" + V +
                ", color=" + color +
                '}';
    }
}
