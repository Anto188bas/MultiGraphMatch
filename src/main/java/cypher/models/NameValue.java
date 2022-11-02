package cypher.models;


// TODO HAVE TO BE MORE GENERIC (NODE/EDGES)
public class NameValue {
    // ATTRIBUTES
    private String elementName;
    private String elementKey;

    // CONSTRUCTORs
    public NameValue() {
    }

    public NameValue(String elementName, String elementKey) {
        this.elementName = elementName;
        this.elementKey = elementKey;
    }


    // GETTER AND SETTER
    public String getElementName() {
        return elementName;
    }

    public void setElementName(String nodeName) {
        this.elementName = nodeName;
    }

    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String nodeKey) {
        this.elementKey = nodeKey;
    }

    // TO STRING

    @Override
    public String toString() {
        return elementName + "." + elementKey;
    }
}
