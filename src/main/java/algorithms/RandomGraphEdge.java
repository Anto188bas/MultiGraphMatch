package algorithms;

public class RandomGraphEdge {
    private Integer U;
    private Integer V;
    private Integer color;

    public RandomGraphEdge(Integer U, Integer V, Integer color) {
        this.U = U;
        this.V = V;
        this.color = color;
    }

    public Integer getU() {
        return U;
    }

    public Integer getV() {
        return V;
    }

    public Integer getColor() {
        return color;
    }

    public void setU(Integer u) {
        U = u;
    }

    public void setV(Integer v) {
        V = v;
    }

    public void setColor(Integer color) {
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
