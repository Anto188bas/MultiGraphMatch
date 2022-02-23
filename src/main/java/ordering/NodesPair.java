package ordering;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

public class NodesPair {
    private final Integer                       firstEndpoint;
    private final Integer                       secondEndpoint;
    private final Integer                       id;
    private Int2ObjectOpenHashMap<IntArrayList> first_second;
    private Int2ObjectOpenHashMap<IntArrayList> second_first;
    // TODO it will be replaced by new maps created
    private       Table                         compatibility_domain;
    private       IntIndex                      column1_index;
    private       IntIndex                      column2_index;
    private       int                           domain_size;


    public NodesPair() {
        this.firstEndpoint  = -1;
        this.secondEndpoint = -1;
        this.id             = -1;
        this.domain_size    = 0;
    }

    public NodesPair(int a, int b) {
        if (a < b) {
            this.firstEndpoint  = a;
            this.secondEndpoint = b;
        } else {
            this.firstEndpoint  = b;
            this.secondEndpoint = a;
        }

        // Cantor pairing function
        this.id = (int) (0.5 * (this.firstEndpoint + this.secondEndpoint) * (this.firstEndpoint + this.secondEndpoint + 1) + this.secondEndpoint);
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
        return (this.getId().equals(q.getId()));
    }

    public Integer getFirstEndpoint()                            {return this.firstEndpoint;}
    public Integer getSecondEndpoint()                           {return this.secondEndpoint;}
    public Integer getId()                                       {return id;}
    public Table   getCompatibility_domain()                     {return compatibility_domain;}
    public Int2ObjectOpenHashMap<IntArrayList> getFirst_second() {return first_second;}
    public Int2ObjectOpenHashMap<IntArrayList> getSecond_first() {return second_first;}

    public int getDomain_size() {
        return domain_size;
    }

    public void setCompatibility_domain(Table compatibility_domain) {
        // column 1: first, column 2: second
        this.compatibility_domain = compatibility_domain;
        this.column1_index        = new IntIndex(this.compatibility_domain.intColumn("first" ));
        this.column2_index        = new IntIndex(this.compatibility_domain.intColumn("second"));
    }

    // SELECTION BY INDEXING
    public Table getByFirstValue(int first)   {return this.compatibility_domain.where(this.column1_index.get(first));}
    public Table getBySecondValue(int second) {return this.compatibility_domain.where(this.column2_index.get(second));}

    public void setNewCompatibilityDomain(
          Int2ObjectOpenHashMap<IntArrayList> first_second,
          Int2ObjectOpenHashMap<IntArrayList> second_first
    ) {
        this.first_second = first_second;
        this.second_first = second_first;
        this.first_second.int2ObjectEntrySet().fastForEach(record -> {
                this.domain_size += record.getValue().size();
        });
    }

    public IntArrayList getByFirstValueNew(int first)   {return this.first_second.get(first);}
    public IntArrayList getBySecondValueNew(int second) {return this.second_first.get(second);}
}
