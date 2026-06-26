import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Strings {

    private static final Map<Character, Character> ESCAPED_CHARACTERS = new HashMap<>();
    static {
        ESCAPED_CHARACTERS.put('\\', '\\'); // backslash
        ESCAPED_CHARACTERS.put('\"', '\"'); // double quote
        ESCAPED_CHARACTERS.put('\'', '\''); // single quote
        ESCAPED_CHARACTERS.put('{', '{'); // single quote
        ESCAPED_CHARACTERS.put('}', '}'); // single quote
        ESCAPED_CHARACTERS.put('n', '\n'); // newline
        ESCAPED_CHARACTERS.put('t', '\t'); // horizontal tab
        ESCAPED_CHARACTERS.put('0', '\0'); // null character
        ESCAPED_CHARACTERS.put('r', '\r'); // carriage return
    }

    public static boolean isEscapedChar(char ch) {
        return ESCAPED_CHARACTERS.containsKey(ch);
    }

    public static char getEscapedChar(char ch) {
        return ESCAPED_CHARACTERS.get(ch);
    }

    public static boolean reservedChar(char ch) {
        return switch (ch) {
            case '\\', '\"', '\'' -> false;
            default -> true;
        };
    }
}
