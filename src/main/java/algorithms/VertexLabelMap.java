package algorithms;
import java.util.HashMap;

public class VertexLabelMap {
    private HashMap<Integer, String> hash = new HashMap<>();

    public VertexLabelMap(){
        //test map
        //inside the main create the map reading by the csv
        hash.put(0,"a");
        hash.put(1,"b");
        hash.put(2,"c");
        hash.put(3,"d");
        hash.put(4,"e");
        hash.put(5,"f");
        hash.put(6,"g");
        hash.put(7,"h");
        hash.put(8,"i");
    }

    public String getVertexLabel(Integer vertexId){ return hash.get(vertexId); }

    public void setVertexLabel(Integer vertexId, String vertexLabel){ hash.put(vertexId, vertexLabel); }

    // TODO reading the .csv create the hashmap that contain the couple id / label
    // using the hashmap could reduce the time and space complexity instead of using a list of vertex
}
