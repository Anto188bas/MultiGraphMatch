package algorithms;

import com.google.common.graph.*;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.WattsStrogatzGraphGenerator;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * This class contains all the methods for the random networks generation
 *
 */
@SuppressWarnings({"rawtypes", "UnstableApiUsage"})
public class RandomModels {
    private final  Supplier<Integer> vSupplier = new Supplier<>() {
        private int id = 0;
        @Override
        public Integer get() {
            return  id++;
        }
    };

    /**
     *
     * Generate a random network using Barabasi-Albert algorithm
     *
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateBarabasiAlbert(int m0, int m, int n){
        BarabasiAlbertGraphGenerator<Integer, RelationshipEdge> barabasiGenerator = new BarabasiAlbertGraphGenerator<>(m0,m,n);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        barabasiGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Generate a random network using G(n,m) Erdos-Reniy algorithm
     *
     * @param n the number of nodes
     * @param m the number of edges
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateErdosReniyNM(int n, int m ){
        GnmRandomGraphGenerator<Integer, RelationshipEdge> ErdosReniyGenerator = new GnmRandomGraphGenerator<>(n,m, new Random(), false, true);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        ErdosReniyGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     *  Generate a random network using G(n,p) Erdos-Reniy algorithm
     *
     * @param n the number of nodes
     * @param p the edge probability
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateErdosReniyNP(int n, double p){
        GnpRandomGraphGenerator<Integer, RelationshipEdge> ErdosReniyGenerator = new GnpRandomGraphGenerator<>(n,p);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        ErdosReniyGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Generate a random network using Watt-Strogatz algorithm
     *
     * @param n the number of nodes
     * @param k connect each node to its k nearest neighbors in a ring
     * @param p the probability of re-wiring each edge
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateWattStrogatz(int n, int k, double p) {
        WattsStrogatzGraphGenerator<Integer, RelationshipEdge> WattStrogatzGenerator = new WattsStrogatzGraphGenerator<>(n,k,p);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        WattStrogatzGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Rewire the edges of the native network
     *
     * @param initialGraph the source network
     * @return the rewired network as a hashmap of edges
     *
     */
    public HashMap<Integer, RandomGraphEdge> generateRewiring(MutableValueGraph<Integer, Integer> initialGraph) {
        //MutableNetwork<Integer, Integer> newGraph = NetworkBuilder.directed().allowsParallelEdges(true).build();
        HashMap<Integer, RandomGraphEdge> edgesMap = new HashMap<>();
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        int id=0;
        for (var edge : initialGraph.edges()) {
            int U = edge.nodeU();
            int R = random.nextInt(initialGraph.nodes().size());
            if (U == R || R % 2 == 0) R = edge.nodeV();
            //newGraph.addEdge(U, R, id++);
            edgesMap.put(id++,new RandomGraphEdge(U,R, initialGraph.edgeValue(edge).orElse(null)));
        }
        return edgesMap;
    }

    /**
     *
     * Applicate the EdgeSwapping algorithm to the given network
     *
     * @param initialGraph the initial reference network
     * @return the rewired network as a hashmap of edges
     *
     */
    public HashMap<Integer, RandomGraphEdge> generateEdgeSwapping(MutableValueGraph<Integer, Integer> initialGraph){
        HashMap<Integer, RandomGraphEdge> edgesMap = new HashMap<>();
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        int id=0;

        for(var edge:initialGraph.edges())
            edgesMap.put(id++,new RandomGraphEdge(edge.nodeU(), edge.nodeV(),initialGraph.edgeValue(edge.nodeU(), edge.nodeV()).orElse(null)));

        for (int i = 0; i < edgesMap.size(); i++) {
            int index1 = random.nextInt(edgesMap.size());
            int index2 = random.nextInt(edgesMap.size());
            RandomGraphEdge x = edgesMap.get(index1);
            RandomGraphEdge y = edgesMap.get(index2);

            if (!Objects.equals(x.getU(), y.getU()) &&
                !Objects.equals(x.getU(), y.getV()) &&
                !Objects.equals(x.getV(), y.getU()) &&
                !Objects.equals(x.getV(), y.getV()) ){

                //(a,b), (c,d) -> (a,d), (b,c)
                int b = x.getV();
                int c = y.getU();
                int d = y.getV();
                x.setV(d);
                y.setU(b);
                y.setV(c);

                //edgesMap.replace(index1, x, new RandomGraphEdge(x.getU(), y.getV(), x.getColor()));
                //edgesMap.replace(index2, y, new RandomGraphEdge(x.getV(), y.getU(), y.getColor()));
            }
        }
        return edgesMap;
    }
}

