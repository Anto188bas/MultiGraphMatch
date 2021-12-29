package ordering;


public class NodesPair {
    private final Integer firstEndpoint;
    private final Integer secondEndpoint;

    private final Double id;

    public NodesPair(int a, int b) {
        if (a < b) {
            this.firstEndpoint = a;
            this.secondEndpoint = b;
        } else {
            this.firstEndpoint = b;
            this.secondEndpoint = a;
        }

        // Cantor pairing function
        this.id = 0.5d * (this.firstEndpoint + this.secondEndpoint) * (this.firstEndpoint + this.secondEndpoint + 1) + this.secondEndpoint;
    }

    public Integer getFirstEndpoint() {
        return this.firstEndpoint;
    }

    public Integer getSecondEndpoint() {
        return this.secondEndpoint;
    }

    public Double getId() {
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
        System.out.println(this + ", " + q);
        // field comparison
        return Double.compare(this.getId(), q.getId()) == 0;
    }


}
