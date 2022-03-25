package algorithms;

import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.function.Supplier;

public class RandomModels {
    private  Supplier<Integer> vSupplier = new Supplier<>() {
        private int id = 0;
        @Override
        public Integer get() {
            return  id++;
        }
    };

    private int m0;
    private int m;
    private int n;

    public RandomModels(int m0, int m, int n) {
        this.m0 = m0;
        this.m = m;
        this.n = n;
        generateGraph();
    }

    private void generateGraph(){
        BarabasiAlbertGraphGenerator<Integer, RelationshipEdge> graph = new BarabasiAlbertGraphGenerator<>(m0,m,n);

        SimpleDirectedGraph<Integer, RelationshipEdge> completeGraph = new SimpleDirectedGraph(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        graph.generateGraph(completeGraph);

        System.out.println(completeGraph.vertexSet());
        System.out.println(completeGraph.edgeSet());
    }
}
