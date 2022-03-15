package algorithms;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jgrapht.graph.DefaultEdge;

public class RelationshipEdge extends DefaultEdge{
    private final int label;

    /**
     *
     * Class constructor, Constructs a relationship edge
     *
     * @param label the label of the new edge.
     *
     */
    public RelationshipEdge(IntOpenHashSet label){

        this.label = (int) label.toArray()[0];
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     *
     */
    public int getLabel(){
        return label;
    }

    @Override
    public String toString(){
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }
}