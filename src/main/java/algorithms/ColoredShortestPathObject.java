package algorithms;
import java.util.List;

/**
 *
 * This class create a proper json format for the colored shortest path output
 *
 */
public class ColoredShortestPathObject {
    Integer source;
    Integer destination;
    Integer color;
    List<Integer> path;

    public ColoredShortestPathObject(Integer source, Integer destination, Integer color, List<Integer> path){
        this.source = source;
        this.destination = destination;
        this.color = color;
        this.path = path;
    }

    public ColoredShortestPathObject(Integer color, List<Integer> path){
        this.color = color;
        this.path = path;
    }

    @Override
    public String toString() {
        return "ColoredShortestPath{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", color=" + color +
                ", path=" + path +
                '}';
    }
}
