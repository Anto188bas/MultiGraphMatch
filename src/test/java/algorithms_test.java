import algorithms.QueryParser;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class algorithms_test {
    public static void main(String[] args) throws Exception {
        String query = "CALL algorithms.JaccardCoefficientPrediction(433, 5)";
        String query2 = "CALL algorithms.pageRank";
        String query3 = "CALL algorithms.kSpanningTree(10)";
        String query4 = "CALL algorithms.shortestPath(254,543)";
        String query5 = "CALL algorithms.Katz";
        String query6 = "CALL algorithms.coloredShortestPath(223,5454,1)";
        String query7 = "CALL algorithms.coloredShortestPath(254,543)";
        String query8 = "CALL algorithms.wattStrogatzGenerator(10,8, 0.2)";
        String query9 = "CALL algorithms.BarabasiAlbertGenerator(10,10, 200)";
        String query10 = "CALL algorithms.rewireGraph";

        Stopwatch stopwatch = Stopwatch.createStarted();

        QueryParser q = new QueryParser(args);

        q.parser(query);
        q.parser(query2);
        q.parser(query3);
        q.parser(query4);
        q.parser(query5);
        q.parser(query6);
        q.parser(query7);
        q.parser(query8);
        q.parser(query9);
        q.parser(query10);
        stopwatch.stop();
        System.out.println("\u001B[32mParsed "+q.getQueryParsed()+" algorithms package's queries in "+ stopwatch.elapsed(TimeUnit.MILLISECONDS)+" milliseconds\u001B[32m ");
    }
}
