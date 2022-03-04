package algorithms;

import java.util.HashMap;

public class VertexLabelMap {
    HashMap<Integer, String> map = new HashMap<>();

    public VertexLabelMap(){}

    Integer getVertexId(String vertexLabel){ return Integer.valueOf(map.get(vertexLabel)); }

    void setVertexLabel(Integer vertexId, String vertexLabel){ map.put(vertexId, vertexLabel); }

    // TODO reading the .csv create the hashmap that contain the couple id / label
}
