package token;

import java.awt.image.LookupOp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Punctuation {

    SEMICOLON(";"),
    COLON(":"),
    DOT("."),
    COMMA(","),
    ARROW("->"),
    LPAREN("("),
    RPAREN(")"),
    LBRACKET("["),
    RBRACKET("]"),
    LBRACE("{"),
    RBRACE("}");

    Punctuation(String value) {
        this.value = value;
    }

    private final String value;
    private static final Map<String, Punctuation> LOOKUP = new HashMap<>();

    static {
        for (Punctuation punctuation : Punctuation.values()) {
            LOOKUP.put(punctuation.getValue(), punctuation);
        }
    }

    public String getValue() {
        return value;
    }

    public static Punctuation find(String value) {
        return LOOKUP.get(value);
    }
}