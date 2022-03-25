import algorithms.QueryParser;

public class algorithms_test {
    public static void main(String[] args) throws Exception {
        String query = "CALL algorithms.PreferentialAttachmentPrediction(433, 5)";
        String query2 = "CALL algorithms.pageRank";
        String query3 = "CALL algorithms.kSpanningTree(3)";
        String query4 = "CALL algorithms.shortestPath(254,543)";
        String query6 = "CALL algorithms.coloredShortestPath(0,3,1)";
        String query7 = "CALL algorithms.coloredShortestPath(0,3)";
        QueryParser q = new QueryParser(args);

        q.parser(query);
        q.parser(query2);
        q.parser(query3);
        q.parser(query4);
        q.parser(query6);
        q.parser(query7);
    }
}
