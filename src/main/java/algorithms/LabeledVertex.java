package algorithms;

public class LabeledVertex {
    private final Integer vertexId;
    private String vertexLabel;
    public LabeledVertex(Integer vertexId, String vertexLabel){
        this.vertexId = vertexId;
        this.vertexLabel = vertexLabel;
    }

    public Integer getVertexId() {
        return vertexId;
    }

    public String getVertexLabel() {
        return vertexLabel;
    }

    public void setVertexLabel(String vertexLabel) {
        this.vertexLabel = vertexLabel;
    }

    @Override
    public String toString() {
        return "LabeledVertex{" +
                "vertexId=" + vertexId +
                ", vertexLabel='" + vertexLabel + '\'' +
                '}';
    }
}
