import algorithms.QueryParser;

public class algorithms_test {
    public static void main(String[] args) throws Exception {

        //String query = "CALL algorithms.pageRank"; //oppure "CALL PageRank"
        //String query = "CALL algorithms.closeness";
        //String query = "CALL algorithms.katz";
        //String query = "CALL algorithms.eigenVector";
        //String query = "CALL algorithms.betweenness";
        //String query = "CALL algorithms.ClusteringCoefficient";
        //String query = "CALL algorithms.kSpanningTree(3)";
        //String query = "CALL algorithms.labelPropagation";
        //String query = "CALL algorithms.PreferentialAttachmentPrediction(433, 5)";
        //String query = "CALL algorithms.coloredShortestPath(0,3,1)";
        //String query = "CALL algorithms.pageRank";
        String query = "CALL algorithms.kSpanningTree(3)";

        //CypherParser parser = new CypherParser();
        //Query query_object = (Query) parser.parse(query, null);
        //System.out.println(query_object.asCanonicalStringVal());

        QueryParser q = QueryParser.getInstance(query, args);
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
