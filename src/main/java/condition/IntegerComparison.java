package condition;

import scala.Function2;

import java.util.HashMap;
import java.util.Objects;


public class IntegerComparison extends Comparison {
    // ATTRIBUTES
    private HashMap<String, Function2<Integer, Integer, Boolean>> functions;

    // CONSTRUCTORS
    public IntegerComparison() {
        init_object();
    }

    // METHODS
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("GreaterThan", (Integer x, Integer y) -> x > y);
        this.functions.put("GreaterThanOrEqual", (Integer x, Integer y) -> x >= y);
        this.functions.put("Equals", Objects::equals);
        this.functions.put("LessThan", (Integer x, Integer y) -> x < y);
        this.functions.put("LessThanOrEqual", (Integer x, Integer y) -> x <= y);
    }

    public boolean comparison(Object x, Object y, String operator) {
        return functions.get(operator).apply((Integer) x, (Integer) y);
    }
}
