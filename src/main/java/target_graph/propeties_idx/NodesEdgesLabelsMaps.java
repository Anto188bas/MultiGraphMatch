package target_graph.propeties_idx;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Arrays;

public class NodesEdgesLabelsMaps {
    // NODE
    private final Int2ObjectOpenHashMap<String> idxToLabelNode;
    private final Object2IntOpenHashMap<String> labelToIdxNode;
    // EDGE
    private final Int2ObjectOpenHashMap<String> idxToLabelEdge;
    private final Object2IntOpenHashMap<String> labelToIdxEdge;

    // CONSTRUCTOR
    public NodesEdgesLabelsMaps(){
        idxToLabelNode = new Int2ObjectOpenHashMap<>();
        labelToIdxNode = new Object2IntOpenHashMap<>();
        idxToLabelEdge = new Int2ObjectOpenHashMap<>();
        labelToIdxEdge = new Object2IntOpenHashMap<>();
    }

    // GETTER
    public Int2ObjectOpenHashMap<String> getIdxToLabelNode() {return idxToLabelNode;}
    public Object2IntOpenHashMap<String> getLabelToIdxNode() {return labelToIdxNode;}
    public Int2ObjectOpenHashMap<String> getIdxToLabelEdge() {return idxToLabelEdge;}
    public Object2IntOpenHashMap<String> getLabelToIdxEdge() {return labelToIdxEdge;}

    // GET IDX RELATED TO A SPECIFIED LABEL
    private int getLabelIdx(String label, Object2IntOpenHashMap<String> labelToIdx){
        return labelToIdx.containsKey(label) ? labelToIdx.getInt(label) : -1;
    }
    // FOR NODE
    public int getLabelIdxNode(String label){return getLabelIdx(label, labelToIdxNode);}
    // FOR EDGE
    public int getLabelIdxEdge(String label){return getLabelIdx(label, labelToIdxEdge);}


    // GET LABEL RELATED TO A SPECIFIED IDX
    private String getLabelName(int idx, Int2ObjectOpenHashMap<String> idxToLabel){
        return idxToLabel.get(idx);
    }
    // FOR NODE
    public String getLabelNameNode(int idx){return getLabelName(idx, idxToLabelNode);}
    // FOR EDGE
    public String getLabelNameEdge(int idx){return getLabelName(idx, idxToLabelEdge);}

    // GET SIZE
    // FOR NODE
    public int n_type_sz(){return this.getIdxToLabelNode().size();}
    // FOR EDGE
    public int e_type_sz(){return this.getIdxToLabelEdge().size();}


    // LABEL ELABORATION METHODS
    private int add_property_if_not_exists(
        String                        label,
        Int2ObjectOpenHashMap<String> idxToLabel,
        Object2IntOpenHashMap<String> labelToIdx,
        int                           offset
    ){
        if(labelToIdx.containsKey(label)) return labelToIdx.getInt(label);
        int label_id = label.equals("none") ? offset - 1 : labelToIdx.size() + offset;
        labelToIdx.put(label, label_id);
        idxToLabel.put(label_id, label);
        return label_id;
    }

    public int[] stringVectorToIntOne(String labels){
        return Arrays.stream(labels.split("[:|~]"))
           .mapToInt(label -> add_property_if_not_exists(label, this.idxToLabelNode, this.labelToIdxNode, 0))
           .toArray();
    }

    public int createEdgeLabelIdx(String label){
        return add_property_if_not_exists(label, this.idxToLabelEdge, this.labelToIdxEdge, 0);
    }

}
