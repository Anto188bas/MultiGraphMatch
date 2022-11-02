package condition;

import scala.Function2;

import java.util.ArrayList;
import java.util.HashMap;


public class ArrayDoubleCheck extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<Double, ArrayList<Double>, Boolean>> functions;

    // CONSTRUCTORS
    public ArrayDoubleCheck() {
        init_object();
    }

    // METHODs
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("In", (Double x, ArrayList<Double> y) -> y.contains(x));
    }

    @SuppressWarnings("unchecked")
    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Double) x, (ArrayList<Double>) y);
    }
}
