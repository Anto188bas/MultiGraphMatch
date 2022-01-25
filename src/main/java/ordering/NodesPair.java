package ordering;


import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;

public class NodesPair {
    private final Integer firstEndpoint;
    private final Integer secondEndpoint;

    private final Integer id;

    public NodesPair() {
        this.firstEndpoint = -1;
        this.secondEndpoint = -1;

        this.id = -1;
    }

    public NodesPair(int a, int b) {
        if (a < b) {
            this.firstEndpoint = a;
            this.secondEndpoint = b;
        } else {
            this.firstEndpoint = b;
            this.secondEndpoint = a;
        }

        // Cantor pairing function
        this.id = (int) (0.5 * (this.firstEndpoint + this.secondEndpoint) * (this.firstEndpoint + this.secondEndpoint + 1) + this.secondEndpoint);
    }

    public Integer getFirstEndpoint() {
        return this.firstEndpoint;
    }

    public Integer getSecondEndpoint() {
        return this.secondEndpoint;
    }

    public Integer getId() {
        return id;
    }


    public boolean hasCommonNodes(NodesPair a) {
        return (this.getFirstEndpoint().equals(a.getFirstEndpoint()) ||
                this.getFirstEndpoint().equals(a.getSecondEndpoint()) ||
                this.getSecondEndpoint().equals(a.getFirstEndpoint()) ||
                this.getSecondEndpoint().equals(a.getSecondEndpoint()));
    }

    @Override
    public String toString() {
        return "{" + this.firstEndpoint + ", " + this.secondEndpoint + '}';
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (this.getClass() != o.getClass())
            return false;
        NodesPair q = (NodesPair) o;

        // field comparison
        return (this.getId() == q.getId());
    }


}
