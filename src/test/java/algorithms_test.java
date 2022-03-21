import algorithms.QueryParser;
import org.opencypher.v9_0.ast.Query;
import org.opencypher.v9_0.parser.CypherParser;


public class algorithms_test {
    public static void main(String[] args) throws Exception {

        //String query = "CALL pageRank";
        //String query = "CALL closeness";
        //String query = "CALL katz";
        //String query = "CALL eigenVector";
        //String query = "CALL betweenness";
        //String query = "CALL ClusteringCoefficient";
        //String query = "CALL kSpanningTree.clusterNumber(3)";
        //String query = "CALL labelPropagation";

        String query = "CALL PreferentialAttachmentPrediction.vertex(433, 5)";

        CypherParser parser      = new CypherParser();
        Query query_object         = (Query) parser.parse(query, null);
        System.out.println(query_object.asCanonicalStringVal());


        QueryParser q = new QueryParser(query, args);
        q.parser();

    /*
        //Multithreading algorithm testing
        UtilityGraph utilityGraph = new UtilityGraph(args);

        Runnable runnableColoredShortestPath =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.ColoredShortestPath(254, 431, 2);
                        a.AllColoredShortestPath(254, 431);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableShortestPath =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.DijsktraShortestPath(254,431);
                        a.BellmanFordShortestPath(0,3);
                        a.DijsktraAllShortestPath(0);
                        a.BellmanFordAllShortestPath(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableCentrality =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.EigenVectorCentrality();
                        a.BetweennessCentrality();
                        a.ClosenessCentrality();
                        a.PageRankCentrality();
                        a.KatzCentrality();
                        a.ClusteringCoefficient();
                        a.AverageClusteringCoefficient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableClustering =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.LabelPropagationClustering();
                        a.KSpanningTreeClustering(3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableLinkPrediction =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.PreferentialAttachmentPrediction(0,1);
                        a.CommonNeighborsPrediction(0,1);
                        a.JaccardCoefficientPrediction(0,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };


        Thread threadColoredShortestPath = new Thread(runnableColoredShortestPath);
        Thread threadShortestPath = new Thread(runnableShortestPath);
        Thread threadCentrality = new Thread(runnableCentrality);
        Thread threadClustering = new Thread(runnableClustering);
        Thread threadLinkPrediction = new Thread(runnableLinkPrediction);

        System.out.println("...Testing begin...");
        //threadColoredShortestPath.start();
        //threadShortestPath.start();
        //threadCentrality.start();
        //threadClustering.start();
        //threadLinkPrediction.start();

     */
    }

}
