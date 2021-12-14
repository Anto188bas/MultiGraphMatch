package cypher.models;

import cypher.controller.PropertiesUtility;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.opencypher.v9_0.expressions.Expression;
import org.opencypher.v9_0.expressions.LogicalVariable;
import org.opencypher.v9_0.expressions.Range;
import org.opencypher.v9_0.expressions.RelationshipPattern;
import scala.Option;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;

import java.util.HashMap;

public class QueryEdge {
    private String                        edge_name;
    // we could also have that case [r:type1|type2], so the IntArray means type1 or type2
    // if len(edge_label) == 1 normal edge, else or concatenation
    private final IntArrayList            edge_label;
    private final String                  direction;
    private long                          min_deep;
    private long                          max_deep;
    private final HashMap<String, Object> properties;

    public QueryEdge(RelationshipPattern edgePattern, NodesEdgesLabelsMaps label_type_map){
        properties = new HashMap<>();
        min_deep   = 1;
        max_deep   = 1;
        direction  = edgePattern.direction().toString();
        edge_label = new IntArrayList();
        configure_edge_name(edgePattern);
        configure_edge_type(edgePattern, label_type_map);
        configure_path_length(edgePattern);
        configure_edge_properties(edgePattern);
    }

    // CONFIGURE EDGE VARIABLE NAME
    private void configure_edge_name(RelationshipPattern edgePattern){
        Option<LogicalVariable> name = edgePattern.variable();
        if(!name.isDefined()) return;
        edge_name = name.get().name();
    }

    // CONFIGURE EDGE TYPE
    private void configure_edge_type(RelationshipPattern edgePattern, NodesEdgesLabelsMaps label_type_map){
        var types = edgePattern.types().iterator();
        while (types.hasNext())
           edge_label.add(label_type_map.getLabelIdxEdge(types.next().name()));
    }

    // PATH LENGTH CONFIGURATION
    private void configure_path_length(RelationshipPattern edgePattern){
        Option<Option<Range>> edge_length = edgePattern.length();
        if(!edge_length.isDefined()) return;
        Option<Range> edge_range = edge_length.get();
        if(!edge_range.isDefined()) return;
        Range range = edge_range.get();
        var lower   = range.lower();
        var upper   = range.upper();
        if (lower.isDefined()) min_deep = lower.get().value();
        if (upper.isDefined()) max_deep = upper.get().value();
    }

    // CONFIGURE EDGE PROPERTIES
    private void configure_edge_properties(RelationshipPattern edgePattern){
        Option<Expression> edge_properties = edgePattern.properties();
        PropertiesUtility.configure_properties(edge_properties, properties);
    }

    // GETTER
    public String getEdge_name()                   {return edge_name;  }
    public IntArrayList getEdge_label()            {return edge_label; }
    public String getDirection()                   {return direction;  }
    public long getMin_deep()                      {return min_deep;   }
    public long getMax_deep()                      {return max_deep;   }
    public HashMap<String, Object> getProperties() {return properties; }

    // TO STRING

    @Override
    public String toString() {
        return "QueryEdge{"      +
                "edge_name='"    + edge_name   + '\'' + "\n" +
                ", edge_label='" + edge_label  + '\'' + "\n" +
                ", direction='"  + direction   + '\'' + "\n" +
                ", min_deep="    + min_deep    + "\n" +
                ", max_deep="    + max_deep    + "\n" +
                ", properties="  + properties  + "\n" +
                '}';
    }
}
