package ColoredShortestPath;

import com.google.common.graph.ValueGraph;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColorShortestPath {
    final private ValueGraph graph;
    final private int colorNumber;
    private FileWriter writer;
    final private Gson gson = new Gson();
    List<Object> output;

    /**
     *
     * Class constructor, constuct a ColorShortestPath object
     *
     * @param graph the input network
     */
    public ColorShortestPath(ValueGraph graph, int colorNumber) {
        File ShortestPathDir = new File("./OutputTest/ColoredShortestPath");
        if (!ShortestPathDir.exists()){ ShortestPathDir.mkdirs(); }
        this.graph = graph;
        this.colorNumber = colorNumber;
        output = new ArrayList<>();
    }

    /**
     *
     * Invoke the findShortestPath method from the class DijkstraShortestPath, convert to json format the output and save it on the ColoredPath.json file
     *
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @param pathColor the desired path color
     * @throws IOException if the directory "ColoredShortestPath" doesn't exist
     *
     */
    public void ColoredShortestPath(int source, int destination, int pathColor) throws IOException {
        File ColoredSP = new File("./OutputTest/ColoredShortestPath/ColoredPath.json");
        writer = new FileWriter(ColoredSP);
        output.add(source);
        output.add(destination);
        output.add(pathColor);
        output.add(DijkstraColor.findShortestPath(graph, source, destination, pathColor));
        writer.write(gson.toJson(gson.toJson(output)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the findShortestPath method from the class DijkstraShortestPath on all the colors, convert to json format the output and save it on the AllColoredPath.json file
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @throws IOException if the directory "ColoredShortestPath" doesn't exist
     *
     */
    public void AllColoredShortestPath(int source, int destination) throws IOException {
        File ColoredSP = new File("./OutputTest/ColoredShortestPath/AllColoredPath.json");
        writer = new FileWriter(ColoredSP);
        output.add(source);
        output.add(destination);
        for(int i=0;i<colorNumber;i++){
            output.add(i);
            output.add(DijkstraColor.findShortestPath(graph, source, destination, i));
        }
        writer.write(gson.toJson(gson.toJson(output)));
        writer.flush();
        writer.close();
    }

}
