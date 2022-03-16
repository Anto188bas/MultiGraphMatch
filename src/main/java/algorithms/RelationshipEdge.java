package algorithms;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jgrapht.graph.DefaultEdge;

public class RelationshipEdge extends DefaultEdge{
    private final int color;

    /**
     *
     * Class constructor, Constructs a relationship edge
     *
     * @param color the label of the new edge.
     *
     */
    public RelationshipEdge(IntOpenHashSet color){

        this.color = (int) color.toArray()[0];
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     *
     */
    public int getLabel(){
        return color;
    }

    @Override
    public String toString(){
        return "(" + getSource() + " : " + getTarget() + " : " + color + ")";
    }
}