package condition;

import scala.Function2;

import java.util.HashMap;
import java.util.Objects;


public class DoubleComparison extends Comparison {
    // ATTRIBUTE
    private HashMap<String, Function2<Double, Double, Boolean>> functions;

    // CONSTRUCTOR
    public DoubleComparison() {
        init_object();
    }

    // METHODS
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put(">", (Double x, Double y) -> x > y);
        this.functions.put(">=", (Double x, Double y) -> x >= y);
        this.functions.put("=", Objects::equals);
        this.functions.put("<", (Double x, Double y) -> x < y);
        this.functions.put("<=", (Double x, Double y) -> x <= y);
    }

    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Double) x, (Double) y);
    }
}
