package target_graph.edges;

public class Edge {
    protected int src;
    protected int dst;
    protected int type;

    // CONSTRUCTORS
    public Edge(int src, int dst) {
        this.src  = src;
        this.dst  = dst;
        this.type = -1;
    }

    public Edge(int src, int dst, int type) {
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

    public int getSrc()  {return src; }
    public int getDst()  {return dst; }
    public int getType() {return type;}

    @Override
    public String toString() {
        return "Edge{" +
                "src=" + src +
                ", dst=" + dst +
                ", type=" + type +
                '}';
    }
}
