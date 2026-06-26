package token;

import java.util.*;

public enum Keyword {

    NULL("null"),
    VOID("void"),
    BOOLEAN("bool"),
    SHORT("short"),
    INTEGER("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    CHAR("char"),
    STR("str"),

    RETURN("return"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    DO("do"),
    BREAK("break"),
    CONTINUE("continue"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    FOR("for"),
    ECHO("echo"),
    INPUT("input"),
    FN("fn"), // function definition

    // modifiers
    CONST("const"),

    // boolean values
    FALSE("false"),
    TRUE("true");

    private final String value;
    private static final Map<String, Keyword> LOOKUP = new HashMap<>();

    public static final List<Keyword> TYPES;
    public static final List<Keyword> MODIFIERS;

    Keyword(String value) {
        this.value = value;
    }

    static {
        for (Keyword keyword : Keyword.values()) {
            LOOKUP.put(keyword.getValue(), keyword);
        }

        TYPES = List.of(Keyword.VOID, Keyword.BOOLEAN, Keyword.INTEGER, Keyword.SHORT, Keyword.LONG, Keyword.FLOAT, Keyword.DOUBLE, Keyword.CHAR, Keyword.STR);
        MODIFIERS = List.of(Keyword.CONST);
    }

    public String getValue() {
        return value;
    }

    public static Keyword find(String value) {
        return LOOKUP.get(value);
    }
}
