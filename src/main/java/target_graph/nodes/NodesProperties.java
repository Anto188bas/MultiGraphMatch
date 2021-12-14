package target_graph.nodes;

import tech.tablesaw.api.Table;

public class NodesProperties {
    protected Table nodesProperties;
    //TODO add the indexed columns

    /* CONSTRUCTOR */
    public NodesProperties() {}
    public NodesProperties(Table nodesProperties) {this.nodesProperties = nodesProperties;}

    /* GETTER AND SETTER */
    public Table getNodesProperties()                     {return nodesProperties;}
    public void setNodesProperties(Table nodesProperties) {this.nodesProperties = nodesProperties;}

    //TODO add where methods
}
