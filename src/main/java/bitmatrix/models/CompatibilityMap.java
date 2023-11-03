package bitmatrix.models;
import  cypher.models.QueryStructure;
import  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import  it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import ordering.NodesPair;
import  target_graph.graph.TargetGraph;
import  java.util.stream.IntStream;


// THIS CLASS HAS BEEN IMPLEMENTED ONLY FOR COMPARISON WITH BIT MATRIX


public class CompatibilityMap {
    private final ObjectArrayList<Pair> compatibility_map;

    public CompatibilityMap(){
        this.compatibility_map = new ObjectArrayList<>();
    }

    public void pair_creation(TargetGraph targetGraph, QueryStructure queryStructure){
        // src-dst --> target compatibilities
        var out_edges = queryStructure.getQuery_pattern().getOut_edges();
        for (int src: out_edges.keySet()){
            var dsts_edges = out_edges.get(src);
            for (int dst: dsts_edges.keySet()){
                IntArrayList edges = dsts_edges.get(dst);
                this.compatibility_map.add(new Pair(src, dst, edges, targetGraph, queryStructure));
            }
        }

        // we are setting the obtained information into NodesPair Object
        while (!compatibility_map.isEmpty()){
            Pair pair  = compatibility_map.pop();
            if (pair.getSrc() > pair.getDst()){
                for (NodesPair nodesPair: queryStructure.getPairs()){
                    if(nodesPair.getFirstEndpoint()  != pair.getDst()) continue;
                    if(nodesPair.getSecondEndpoint() != pair.getSrc()) continue;
                    nodesPair.setCompatibilityDomain(pair.getTdst_tsrcs(), pair.getTsrc_tdsts());
                }
            }
            else {
                for (NodesPair nodesPair: queryStructure.getPairs()){
                    if(nodesPair.getFirstEndpoint()  != pair.getSrc()) continue;
                    if(nodesPair.getSecondEndpoint() != pair.getDst()) continue;
                    nodesPair.setCompatibilityDomain(pair.getTsrc_tdsts(), pair.getTdst_tsrcs());
                }
            }
        }
    }

    private static class Pair{
         private final int src;
         private final int dst;
         IntArrayList edges;
         private final Int2ObjectOpenHashMap<IntArrayList> tsrc_tdsts;
         private final Int2ObjectOpenHashMap<IntArrayList> tdst_tsrcs;

         public Pair(int src, int dst, IntArrayList edges, TargetGraph targetGraph, QueryStructure queryStructure) {
             this.src    = src;
             this.dst    = dst;
             this.edges  = edges;
             tsrc_tdsts  = new Int2ObjectOpenHashMap<>();
             tdst_tsrcs  = new Int2ObjectOpenHashMap<>();
             target_compatibility(targetGraph, queryStructure);
         }

         private void target_compatibility(TargetGraph targetGraph, QueryStructure queryStructure){
             IntArrayList scr_labels = queryStructure.getQuery_nodes().get(this.src).getLabels();
             IntArrayList dst_labels = queryStructure.getQuery_nodes().get(this.dst).getLabels();
             var tsrc_tdsts_colors   = targetGraph.getGraphPaths().getMap_key_to_edge_list();
             for (int tsrc: tsrc_tdsts_colors.keySet()){
                 int [] tsrc_labels  = targetGraph.getNodesLabelsManager().getMapElementIdToLabelSet().get(tsrc);
                 var all_elems_in    = scr_labels.stream().allMatch(intArrayElement ->
                     IntStream.of(tsrc_labels).anyMatch(intArrayListElement -> intArrayListElement == intArrayElement));
                 if(!all_elems_in) continue;
                 var tdsts_colors    = tsrc_tdsts_colors.get(tsrc);
                 for (int tdst: tdsts_colors.keySet()) {
                     int [] tdst_labels = targetGraph.getNodesLabelsManager().getMapElementIdToLabelSet().get(tdst);
                     all_elems_in    = dst_labels.stream().allMatch(intArrayElement ->
                         IntStream.of(tdst_labels).anyMatch(intArrayListElement -> intArrayListElement == intArrayElement));
                     if (!all_elems_in) continue;
                     var colors      = tdsts_colors.get(tdst);
                     for (int color: colors.keySet()){
                         boolean isPresent = false;
                         for (int edge: this.edges){
                             IntArrayList edge_colors = queryStructure.getQuery_edges().get(edge).getEdge_label();
                             if (!edge_colors.contains(color)) continue;
                             if(!tsrc_tdsts.containsKey(tsrc)) tsrc_tdsts.put(tsrc, new IntArrayList());
                             if(!tdst_tsrcs.containsKey(tdst)) tdst_tsrcs.put(tdst, new IntArrayList());
                             tsrc_tdsts.get(tsrc).add(tdst);
                             tdst_tsrcs.get(tdst).add(tsrc);
                             isPresent = true;
                             break;
                         }
                         if(isPresent) break;
                     }
                 }
             }
         }

        public int getSrc() {return src;}
        public int getDst() {return dst;}
        public Int2ObjectOpenHashMap<IntArrayList> getTsrc_tdsts() {return tsrc_tdsts;}
        public Int2ObjectOpenHashMap<IntArrayList> getTdst_tsrcs() {return tdst_tsrcs;}
    }
}
