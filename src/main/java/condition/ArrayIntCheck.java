package condition;

import scala.Function2;

import java.util.ArrayList;
import java.util.HashMap;


public class ArrayIntCheck extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<Integer, ArrayList<Integer>, Boolean>> functions;

    // CONSTRUCTOR
    public ArrayIntCheck() {
        init_object();
    }

    // METHODs
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("In", (Integer x, ArrayList<Integer> y) -> y.contains(x));
    }

    @SuppressWarnings("unchecked")
    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Integer) x, (ArrayList<Integer>) y);
    }
}
