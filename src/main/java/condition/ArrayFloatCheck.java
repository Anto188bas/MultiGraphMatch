package condition;

import scala.Function2;

import java.util.ArrayList;
import java.util.HashMap;

public class ArrayFloatCheck extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<Float, ArrayList<Float>, Boolean>> functions;

    // CONSTRUCTOR
    public ArrayFloatCheck() {
        init_object();
    }

    // METHODs
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("In", (Float x, ArrayList<Float> y) -> y.contains(x));
    }

    @SuppressWarnings("unchecked")
    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Float) x, (ArrayList<Float>) y);
    }
}
