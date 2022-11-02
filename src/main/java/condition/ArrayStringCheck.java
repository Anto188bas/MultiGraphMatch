package condition;

import scala.Function2;

import java.util.ArrayList;
import java.util.HashMap;


public class ArrayStringCheck extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<String, ArrayList<String>, Boolean>> functions;

    // CONSTRUCTORS
    public ArrayStringCheck() {
        init_object();
    }

    // METHODs
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("In", (String x, ArrayList<String> y) -> y.contains(x));
    }

    @SuppressWarnings("unchecked")
    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((String) x, (ArrayList<String>) y);
    }

}
