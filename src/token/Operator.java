package token;

import javax.management.OperationsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Operator {

    // Single-character operators
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),

    AMPERSAND("&"),
    PIPE("|"),
    CARET("^"),
    TILDE("~"),
    EXCLAMATION("!"),
    EQUALS("="),
    LANGLE("<"),
    RANGLE(">"),
    QUESTION("?"),

    DOUBLE_PLUS("++"),
    DOUBLE_MINUS("--"),
    DOUBLE_EQUALS("=="),
    PLUS_EQUALS("+="),
    MINUS_EQUALS("-="),
    STAR_EQUALS("*="),
    SLASH_EQUALS("/="),
    NOT_EQUALS("!="),
    LESS_EQUALS("<="),
    GREATER_EQUALS(">="),
    AND("&&"),
    OR("||");

    private final String value;
    private static final Map<String, Operator> LOOKUP = new HashMap<>();

    Operator(String value) {
        this.value = value;
    }

    static {
        for (Operator op : Operator.values()) {
            LOOKUP.put(op.getValue(), op);
        }
    }

    public String getValue() {
        return value;
    }

    public static Operator find(String value) {
        return LOOKUP.get(value);
    }
}