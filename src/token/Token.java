package token;

public record Token(Enum<?> token, Object value, long line) {
}
