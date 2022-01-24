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
    private       IntArrayList            type_directed;
    private       IntArrayList            type_reverse;
    private final String                  direction;
    private int                           codificate_direction;
    private long                          min_deep;
    private long                          max_deep;
    private final HashMap<String, Object> properties;

    public QueryEdge(RelationshipPattern edgePattern, NodesEdgesLabelsMaps label_type_map){
        properties = new HashMap<>();
        min_deep      = 1;
        max_deep      = 1;
        direction     = edgePattern.direction().toString();
        edge_label    = new IntArrayList();
        type_directed = new IntArrayList();
        type_reverse  = new IntArrayList();
        configure_edge_name(edgePattern);
        configure_edge_type(edgePattern, label_type_map);
        configure_path_length(edgePattern);
        configure_edge_properties(edgePattern);
        configure_type_direction();
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

    // CONFIGURE ORIENTED TYPE
    private void configure_type_direction(){
        if (edge_label.size() == 0) return;
        switch (this.direction) {
            case "OUTGOING":
                type_directed = edge_label.clone();
                for (int label: edge_label) type_reverse.add(-1 * label);
                codificate_direction = 1;
                break;
            case "INCOMING":
                for (int label: edge_label) type_directed.add(-1 * label);
                type_reverse  = edge_label.clone();
                codificate_direction = -1;
                break;
            default:
                for (int label: edge_label) {
                    type_directed.add(-1 * label);
                    type_directed.add(label);
                }
                type_reverse = type_directed;
                codificate_direction = 0;
        }
    }

    // GETTER
    public String                  getEdge_name()            {return edge_name;    }
    public IntArrayList            getEdge_label()           {return edge_label;   }
    public String                  getDirection()            {return direction;    }
    public int                     getCodificate_direction() {return codificate_direction;}
    public long                    getMin_deep()             {return min_deep;     }
    public long                    getMax_deep()             {return max_deep;     }
    public HashMap<String, Object> getProperties()           {return properties;   }
    public IntArrayList            getType_directed()        {return type_directed;}
    public IntArrayList            getType_reverse()         {return type_reverse; }

    // EQUIVALENT TO
    public boolean equivalent_to(QueryEdge other_edge) {
        // DEEP CHECK
        if (max_deep != 1 || other_edge.getMax_deep() != 1) return false;
        IntArrayList other_edge_types = other_edge.getEdge_label();
        // TYPE CHECK
        if (!edge_label.equals(other_edge_types))           return false;
        HashMap<String, Object> other_edge_props = other_edge.getProperties();
        // PROPERTIES CHECK
        if (properties.size() != other_edge_props.size())   return false;
        // TODO check me
        for (String key: properties.keySet()){
            if (
                 !other_edge_props.containsKey(key) ||
                 !properties.get(key).equals(other_edge_props.get(key))
            ) return false;
        }
        return true;
    }

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
