package matching.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.opencypher.v9_0.expressions.In;

public class PathsMatchingData extends MatchingData {

    public IntArrayList[] solutionPaths;
    public ObjectArrayList<IntArrayList>[] setCandidatesPaths;

    public PathsMatchingData(QueryStructure query_obj) {
        super(query_obj);

        int numEdges = query_obj.getQuery_edges().size();

        solutionPaths = new IntArrayList[numEdges];
        setCandidatesPaths = new ObjectArrayList[numEdges];

        for (int i = 0; i < numEdges; i++) {
            solutionPaths[i] = new IntArrayList();
            setCandidatesPaths[i] = new ObjectArrayList<>();
        }
    }

    public String getSolutionPathsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < solutionPaths.length; i++) {
            sb.append(solutionPaths[i].toString());
        }
        return sb.toString();
    }
}
