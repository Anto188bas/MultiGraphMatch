import algorithms.QueryParser;

public class algorithms_test {
    public static void main(String[] args) throws Exception {
        String query = "CALL algorithms.JaccardCoefficientPrediction(433, 5)";
        String query2 = "CALL algorithms.pageRank";
        String query3 = "CALL algorithms.kSpanningTree(3)";
        String query4 = "CALL algorithms.shortestPath(254,543)";
        String query6 = "CALL algorithms.coloredShortestPath(0,3,1)";
        String query7 = "CALL algorithms.coloredShortestPath(254,543)";
        String query8 = "CALL algorithms.wattStrogatzGenerator(10,8, 0.2)";
        String query9 = "CALL algorithms.BarabasiAlbertGenerator(10,10, 200)";
        String query10 = "CALL algorithms.rewireGraph";
        String query11 = "CALL algorithms.shortestPath(254)";

        QueryParser q = new QueryParser(args);


        q.parser(query);
        q.parser(query2);
        q.parser(query3);
        q.parser(query4);
        q.parser(query6);
        q.parser(query7);
        q.parser(query8);
        q.parser(query9);
        q.parser(query10);
       // q.parser(query11);
    }
}
