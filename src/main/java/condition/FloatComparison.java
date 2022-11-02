package condition;

import scala.Function2;

import java.util.HashMap;
import java.util.Objects;

public class FloatComparison extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<Float, Float, Boolean>> functions;

    // CONSTRUCTOR
    public FloatComparison() {
        init_object();
    }

    // METHODS
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put(">", (Float x, Float y) -> x > y);
        this.functions.put(">=", (Float x, Float y) -> x >= y);
        this.functions.put("=", Objects::equals);
        this.functions.put("<", (Float x, Float y) -> x < y);
        this.functions.put("<=", (Float x, Float y) -> x <= y);
    }

    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Float) x, (Float) y);
    }
}
